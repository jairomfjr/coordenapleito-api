# Vapt Vupt — Refatoração analítica, Kubernetes e operação

Documento de referência para **infraestrutura**, **ETL** e **desenvolvimento**: o que foi implementado na refatoração CQRS do dashboard Vapt Vupt e o que precisa estar configurado no Kubernetes (e equivalentes Docker Compose) para o fluxo funcionar de ponta a ponta.

Documentos relacionados:

- [Contrato ETL (API, cargas, estados)](vapt-vupt-etl-contrato.md)
- [Exemplo Secret K8s](k8s/coordenapleito-vapt-vupt-integracao-secret.example.yaml)

---

## 1. Visão geral da arquitetura

```
┌─────────────────┐     grava staging      ┌──────────────────────────────────────┐
│  ETL externo    │ ─────────────────────► │ public                               │
│  (fora do repo) │   vapt_vupt_atendimento│  vapt_vupt_atendimento_cidadao       │
└────────┬────────┘   vapt_vupt_atendimento_cidadao                                │
         │                                                                        │
         │ opcional: POST /integracao/vapt-vupt/cargas                            │
         ▼                                                                        │
┌─────────────────┐     consolida          ┌──────────────────────────────────────┐
│  coordenapleito-api  │ ─────────────────────► │ integracao.vapt_vupt_carga           │
│  (schedulers +  │                        │ analitico.vapt_vupt_consolidacao     │
│   serviços)     │                        │ analitico.vapt_vupt_dashboard_*      │
└────────┬────────┘                        └──────────────────────────────────────┘
         │
         │ GET /projetos/vapt-vupt/dashboard-resumo
         ▼
┌─────────────────┐
│  Frontend       │  /dashboard-projetos (aba Vapt Vupt)
│  (Next.js)      │
└─────────────────┘
```

| Camada | Schema / local | Responsabilidade |
|--------|----------------|------------------|
| Staging | `public` | Dados operacionais ingeridos pelo ETL e pela aplicação |
| Integração | `integracao` | Controle de cargas (`vapt_vupt_carga`, logs) |
| Analítico | `analitico` | KPIs, rankings e evolução pré-calculados para o dashboard |
| ETL | Externo | Extract/transform e upsert no staging |
| Coordenapleito | API | Consolidação, leitura CQRS, schedulers, APIs admin |

O ETL **não** calcula KPIs do dashboard. O Coordenapleito consolida a partir do staging e persiste no analítico.

---

## 2. O que foi implementado na refatoração

### 2.1 Banco de dados (Flyway)

Scripts em `src/main/resources/db/migration/`:

| Versão | Arquivo | Conteúdo |
|--------|---------|----------|
| V8 | `V8__vapt_vupt_integracao_schema.sql` | Schema `integracao`, `vapt_vupt_carga`, `vapt_vupt_carga_log` |
| V9 | `V9__vapt_vupt_analitico_schema.sql` | Schema `analitico`, tabelas de KPI/ranking/evolução, view `v_vapt_vupt_dashboard_atual` |
| V10 | `V10__shedlock.sql` | Tabela `shedlock` (lock dos schedulers em cluster) |
| V11 | `V11__vapt_vupt_fix_mensagem_erro_text.sql` | Ajuste `mensagem_erro` varchar→TEXT e recriação da view |
| V12 | `V12__vapt_vupt_dashboard_kpi_data_insercao.sql` | Coluna `data_insercao` em `analitico.vapt_vupt_dashboard_kpi` |

Perfis **stage** e **production**: `spring.jpa.hibernate.ddl-auto=none` — schema gerido só pelo Flyway (evita conflito Hibernate × views).

### 2.2 Backend — consolidação e leitura

| Componente | Função |
|------------|--------|
| `ConsolidarDashboardVaptVuptService` | Agrega staging (`VaptVuptDashboardQueryDao`), grava `analitico`, invalida cache |
| `ObterDashboardVaptVuptResumoService` | Leitura CQRS: analítico publicado; fallback on-read se não houver consolidação |
| `DashboardVaptVuptLeituraDao` | Monta `VaptVuptDashboardResumoModel` a partir de `analitico.*` |
| `VaptVuptAnaliticoPersistenceDao` | Persiste métricas na consolidação |
| `VaptVuptIntegracaoController` | APIs `/integracao/vapt-vupt/*` (cargas, consolidar, backfill, admin) |
| `VaptVuptAtendimentoController` | `GET /projetos/vapt-vupt/dashboard-resumo` → envelope com `resumo` + `meta` |

Resposta do dashboard (`VaptVuptDashboardResponseModel`):

- **`resumo`**: mesmos KPIs/gráficos de antes.
- **`meta`**: `fonte` (`CONSOLIDADO` | `ON_READ`), `consolidacaoCodigo`, `publicadoEm`, `versaoRegra`.

`VaptVuptDashboardQueryDao` está **@Deprecated** — uso restrito à consolidação (escrita).

### 2.3 Schedulers

| Scheduler | Cron padrão | Fuso | Ação |
|-----------|-------------|------|------|
| `VaptVuptConsolidacaoScheduler` | `0 */2 * * * *` | `America/Fortaleza` | Processa cargas em `AGUARDANDO_CONSOLIDACAO` (fila) |
| `VaptVuptConsolidacaoDiariaScheduler` | `0 0 5 * * *` | `America/Fortaleza` | `consolidarFull()` — mesma regra do backfill / botão Reconsolidar |

Ambos usam **ShedLock** (tabela `shedlock`) para uma única réplica executar por vez.

Propriedades (`application.properties`):

```properties
app.vapt-vupt.consolidacao.enabled=true
app.vapt-vupt.consolidacao.scheduler.cron=0 */2 * * * *
app.vapt-vupt.consolidacao.daily.enabled=true
app.vapt-vupt.consolidacao.daily.cron=0 0 5 * * *
```

### 2.4 Segurança

| Endpoint | Autenticação |
|----------|----------------|
| `POST /integracao/vapt-vupt/cargas` | Header `X-Integracao-Token` (`permitAll` + validação de token) |
| `POST /integracao/vapt-vupt/cargas/{codigo}/consolidar` | Idem |
| Demais `/integracao/vapt-vupt/*` | JWT — perfis **ANALISTA** ou **ADMINISTRADOR** |
| `GET /projetos/vapt-vupt/dashboard-resumo` | JWT (usuários do dashboard) |

### 2.5 Frontend

| Item | Detalhe |
|------|---------|
| Página | `/dashboard-projetos` — aba **Vapt Vupt** |
| Serviço | `vaptVuptAtendimentosService.listarDashboardResumo()` → envelope `resumo` + `meta` |
| Banner | `VaptVuptDashboardMetaBanner` — verde (consolidado) / amarelo (on-read) |
| Admin | `vaptVuptIntegracaoService.backfillFull()` — botão Reconsolidar / Gerar consolidação (Analista, Administrador) |

### 2.6 Testes e observabilidade

- `VaptVuptConsolidacaoIntegrationTest` (Testcontainers; requer Docker).
- Métricas Micrometer: `vapt_vupt.consolidacao.carga`, `vapt_vupt.consolidacao.full`.
- MDC em consolidação: `vaptVupt.cargaCodigo`, `vaptVupt.consolidacaoEscopo`, etc.

---

## 3. Fluxos operacionais

### 3.1 Cenário recomendado com ETL noturno (ex.: 20h) sem webhook

1. **20h** — ETL atualiza `vapt_vupt_atendimento` e `vapt_vupt_atendimento_cidadao` no **mesmo PostgreSQL** da API.
2. **05h** — `VaptVuptConsolidacaoDiariaScheduler` executa `consolidarFull()`.
3. Usuários abrem o dashboard — `meta.fonte = CONSOLIDADO`.

Não é obrigatório `POST /cargas` neste cenário.

### 3.2 Cenário com integração por API (atualização mais rápida)

1. ETL termina ingestão no staging.
2. `POST /coordenapleito-api/integracao/vapt-vupt/cargas` com `X-Integracao-Token`.
3. Em até ~2 min, `VaptVuptConsolidacaoScheduler` consolida a carga (ou `POST .../consolidar` imediato).
4. Dashboard atualizado com cache invalidado.

### 3.3 Manual (suporte / primeira carga)

- Frontend: botão na aba Vapt Vupt.
- API: `POST /integracao/vapt-vupt/consolidacoes/backfill-full` (JWT admin).

---

## 4. Kubernetes — o que a infraestrutura precisa configurar

Não há manifests completos de Deployment no repositório; abaixo está o **contrato** que o cluster deve atender.

### 4.1 Deployment `coordenapleito-api` (obrigatório)

| Variável de ambiente | Obrigatório | Descrição |
|----------------------|-------------|-----------|
| `SPRING_PROFILES_ACTIVE` | Sim | `production` ou `stage` |
| `SPRING_DATASOURCE_URL` | Sim | JDBC PostgreSQL (mesmo DB do ETL para staging) |
| `SPRING_DATASOURCE_USERNAME` | Sim | Preferir Secret |
| `SPRING_DATASOURCE_PASSWORD` | Sim | Preferir Secret |
| `APP_CORS_ALLOWED_ORIGINS` | Sim (prod) | URL do frontend (ex.: `https://coordenapleito.sps.ce.gov.br`) |

**Réplicas:** ≥1 pod **Ready 24/7**. Schedulers rodam **dentro** da JVM da API; se não houver pod às 05:00 (Fortaleza), a consolidação diária não executa.

**Flyway:** na subida da API, aplica V8–V12. Validar nos logs: migrações aplicadas ou *Schema is up to date*.

**ShedLock:** usa o mesmo PostgreSQL; não exige Redis nem CronJob K8s separado para consolidação.

### 4.2 Secret — token ETL (opcional, recomendado se ETL chamar API)

| Recurso | Conteúdo |
|---------|----------|
| Secret `coordenapleito-vapt-vupt-integracao` | `webhook-token` (gerar: `openssl rand -hex 32`) |
| Env na API | `APP_INTEGRACAO_VAPT_VUPT_WEBHOOK_TOKEN` ← `secretKeyRef` |

Exemplo: [k8s/coordenapleito-vapt-vupt-integracao-secret.example.yaml](k8s/coordenapleito-vapt-vupt-integracao-secret.example.yaml)

Equivalente Docker Compose (já no repo):

- `docker-compose.yml` / `docker-compose.stage.yml` — `APP_INTEGRACAO_VAPT_VUPT_WEBHOOK_TOKEN`

Se o token estiver **vazio**, endpoints com `X-Integracao-Token` falham; schedulers internos e backfill JWT continuam funcionando.

### 4.3 Ingress / rede

- Rota pública ou interna para `{host}/coordenapleito-api/*`.
- ETL (se em outro namespace): conectividade até o Ingress/Service da API para `POST /integracao/vapt-vupt/cargas`.

### 4.4 ETL (CronJob ou externo ao cluster)

| Item | Responsável |
|------|-------------|
| Agendamento (ex. cron 20h) | Time ETL / infra |
| Upsert em `vapt_vupt_*` no **mesmo** PostgreSQL da API | ETL |
| (Opcional) `POST /integracao/vapt-vupt/cargas` + header `X-Integracao-Token` | ETL |
| (Opcional) `POST .../cargas/{codigo}/consolidar` | ETL |

O Secret do token deve ser o **mesmo** na API e no job do ETL (Secret compartilhado ou cópia sincronizada).

### 4.5 O que **não** criar no Kubernetes

| Não necessário | Motivo |
|----------------|--------|
| CronJob K8s para consolidação Vapt Vupt | Schedulers embutidos na API |
| Job de migração Flyway separado | Flyway na subida da API |
| Redis para lock | ShedLock em PostgreSQL |
| Banco dedicado só para `analitico` | Schemas no mesmo PostgreSQL |

### 4.6 Overrides opcionais (ConfigMap / env)

Spring relaxed binding — exemplos:

| Objetivo | Propriedade / env |
|----------|-------------------|
| Desligar toda consolidação | `APP_VAPT_VUPT_CONSOLIDACAO_ENABLED=false` |
| Desligar só job 05h | `APP_VAPT_VUPT_CONSOLIDACAO_DAILY_ENABLED=false` |
| Mudar horário FULL | `APP_VAPT_VUPT_CONSOLIDACAO_DAILY_CRON=0 0 6 * * *` |
| Mudar fila 2 min | `APP_VAPT_VUPT_CONSOLIDACAO_SCHEDULER_CRON=...` |
| Desligar Flyway Vapt | `APP_VAPT_VUPT_FLYWAY_ENABLED=false` (não recomendado em prod) |

---

## 5. Schedulers — referência rápida

### Fila a cada 2 minutos (`VaptVuptConsolidacaoScheduler`)

- **Não** varre o staging sozinho.
- Lista `integracao.vapt_vupt_carga` com status `AGUARDANDO_CONSOLIDACAO`.
- Para cada registro: consolida (escopo `CARGA`), publica analítico, invalida cache.
- Origem típica da fila: `POST /cargas` do ETL.

### Diário 05:00 (`VaptVuptConsolidacaoDiariaScheduler`)

- Chama `consolidarFull()` (escopo `FULL`).
- Lê **todo** o staging atual e publica nova consolidação.
- Independente de `POST /cargas` — adequado ao ETL que só grava tabelas à noite.

---

## 6. APIs principais

| Método | Path | Auth | Uso |
|--------|------|------|-----|
| `GET` | `/projetos/vapt-vupt/dashboard-resumo` | JWT | Dashboard (frontend) |
| `POST` | `/integracao/vapt-vupt/cargas` | `X-Integracao-Token` | ETL pós-ingestão |
| `POST` | `/integracao/vapt-vupt/cargas/{codigo}/consolidar` | Token | Consolidação imediata |
| `POST` | `/integracao/vapt-vupt/consolidacoes/backfill-full` | JWT admin | FULL manual / scheduler lógico do botão |
| `POST` | `/integracao/vapt-vupt/cargas/{codigo}/reconsolidar` | JWT admin | Reprocessar uma carga |
| `GET` | `/integracao/vapt-vupt/cargas/{codigo}` | JWT admin | Status da carga |

Context path da API: `/coordenapleito-api` (prefixar nas URLs externas).

---

## 7. Checklist de validação (infra + operação)

### Subida da API

- [ ] `SPRING_PROFILES_ACTIVE` correto
- [ ] Conexão PostgreSQL OK
- [ ] Flyway: versão ≥ 11 em `flyway_schema_history`
- [ ] Log: `Started CoordenapleitoApplication`
- [ ] Tabelas `integracao.*`, `analitico.*`, `shedlock` existem

### Schedulers

- [ ] Pelo menos 1 pod da API ligado às 05:00 Fortaleza
- [ ] Após 05:00, log: `Consolidação diária Vapt Vupt concluída`
- [ ] (Se ETL usa carga) após `POST /cargas`, em ~2 min: `Scheduler Vapt Vupt: N carga(s) consolidada(s)`

### Dashboard

- [ ] `GET /projetos/vapt-vupt/dashboard-resumo` retorna `meta.fonte: CONSOLIDADO` após consolidação
- [ ] Frontend `/dashboard-projetos` — faixa verde com data de publicação

### ETL (se aplicável)

- [ ] Mesmo banco que a API
- [ ] Token alinhado API ↔ ETL
- [ ] `POST /cargas` retorna `201` e status `AGUARDANDO_CONSOLIDACAO`

---

## 8. Troubleshooting

| Sintoma | Causa provável | Ação |
|---------|----------------|------|
| Dashboard amarelo (ON_READ) | Sem consolidação `PUBLICADO` | Aguardar 05h, backfill admin ou Reconsolidar |
| ETL não consolida após ingestão | Sem `POST /cargas` e sem job 05h ainda | Registrar carga ou aguardar FULL diário |
| Token inválido no ETL | Secret divergente ou vazio na API | Alinhar `APP_INTEGRACAO_VAPT_VUPT_WEBHOOK_TOKEN` |
| Flyway / Hibernate × view | `ddl-auto=update` em prod | Usar perfil stage/production (`ddl-auto=none`) |
| Scheduler não roda às 05h | API desligada / scale to zero | Garantir réplicas 24/7 |
| Dados antigos após consolidar | Cache | Consolidação já invalida cache; forçar novo GET no frontend |

---

## 9. Referência de arquivos no repositório

| Área | Caminho |
|------|---------|
| Migrações | `src/main/resources/db/migration/V8–V12` |
| Config | `application.properties`, `application-stage.properties`, `application-production.properties` |
| Schedulers | `infrastructure/scheduling/VaptVuptConsolidacao*.java` |
| Integração API | `api/controller/integracao/VaptVuptIntegracaoController.java` |
| Dashboard API | `api/controller/VaptVuptAtendimentoController.java` |
| Contrato ETL | [vapt-vupt-etl-contrato.md](vapt-vupt-etl-contrato.md) |
| Secret K8s exemplo | [k8s/coordenapleito-vapt-vupt-integracao-secret.example.yaml](k8s/coordenapleito-vapt-vupt-integracao-secret.example.yaml) |
| Frontend dashboard | `coordenapleito-frontend/src/app/dashboard-projetos/` |
| Frontend integração | `coordenapleito-frontend/src/services/vapt-vupt-integracao.ts` |

---

*Última atualização: refatoração CQRS Vapt Vupt com consolidação diária às 05:00 (America/Fortaleza) e integração frontend com `resumo` + `meta`.*
