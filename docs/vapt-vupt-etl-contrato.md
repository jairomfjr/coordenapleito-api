# Contrato ETL externo — Vapt Vupt → Coordenapleito

O ETL de ingestão roda **fora** do monorepo Coordenapleito. Este documento define o contrato entre o job externo, o staging (`public`) e a API de integração (`integracao` / `analitico`).

> **Kubernetes, schedulers, refatoração completa e checklist de infra:** ver [vapt-vupt-kubernetes-e-refatoracao.md](vapt-vupt-kubernetes-e-refatoracao.md).

## Visão geral

```
ETL externo
  → staging: vapt_vupt_atendimento, vapt_vupt_atendimento_cidadao
  → POST /integracao/vapt-vupt/cargas (registro pós-ingestão)
  → (opcional) POST /integracao/vapt-vupt/cargas/{codigo}/consolidar
Coordenapleito
  → scheduler ou webhook → ConsolidarDashboardVaptVuptService
  → schema analitico (kpi, ranking, evolucao_mensal)
  → GET /vapt-vupt-atendimentos/dashboard-resumo (leitura CQRS)
```

## Máquina de estados da carga (`integracao.vapt_vupt_carga`)

| Status | Responsável | Descrição |
|--------|-------------|-----------|
| `EM_EXECUCAO` | ETL | Carga em andamento no job externo |
| `SUCESSO_INGESTAO` | ETL | Ingestão concluída (opcional; pode ir direto ao próximo) |
| `AGUARDANDO_CONSOLIDACAO` | ETL / API | Pronto para o Coordenapleito consolidar |
| `CONSOLIDANDO` | Coordenapleito | Consolidação em execução |
| `CONSOLIDADO` | Coordenapleito | Métricas publicadas no schema `analitico` |
| `FALHA_INGESTAO` | ETL | Erro na ingestão |
| `FALHA_CONSOLIDACAO` | Coordenapleito | Erro na consolidação |
| `CANCELADA` | ETL / Admin | Carga cancelada |

### Transição obrigatória pós-ingestão

Após commit do staging, o ETL deve registrar a carga no Coordenapleito com status **`AGUARDANDO_CONSOLIDACAO`** (via API abaixo). O Coordenapleito **não** inicia consolidação durante a ingestão.

## API — registro de carga (ETL)

**`POST /coordenapleito-api/integracao/vapt-vupt/cargas`**

- Autenticação: header `X-Integracao-Token` = valor de `app.integracao.vapt-vupt.webhook-token`
- Content-Type: `application/json`

### Corpo (`VaptVuptCargaRegistroInput`)

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| `tipoCarga` | `INCREMENTAL` \| `COMPLETA` \| `REPROCESSAMENTO` | não (default `INCREMENTAL`) | Tipo da carga |
| `periodoAno` | integer | não | Ano da janela afetada |
| `periodoMes` | integer | não | Mês da janela (1–12) |
| `dataInicio` | ISO-8601 offset | não | Início da janela de dados |
| `dataFim` | ISO-8601 offset | não | Fim da janela |
| `quantidadeAtendimentos` | long | **sim** | Linhas de atendimento inseridas/atualizadas |
| `quantidadeCidadaos` | long | **sim** | Linhas de cidadão vinculadas |
| `quantidadeLinhasLidas` | long | não | Total lido na origem |
| `quantidadeLinhasRejeitadas` | long | não | Rejeitadas na validação |
| `watermarkAnterior` | ISO-8601 offset | não | Checkpoint anterior |
| `watermarkNovo` | ISO-8601 offset | não | Novo checkpoint |
| `origemSistema` | string | não (default `VAPT_VUPT_ETL`) | Identificador do job |

### Resposta (`201 Created`)

`VaptVuptCargaModel` com `codigo` (UUID), `status` = `AGUARDANDO_CONSOLIDACAO`, contadores e timestamps.

### Exemplo

```http
POST /coordenapleito-api/integracao/vapt-vupt/cargas
X-Integracao-Token: <token-configurado>
Content-Type: application/json

{
  "tipoCarga": "INCREMENTAL",
  "periodoAno": 2026,
  "periodoMes": 5,
  "quantidadeAtendimentos": 1520,
  "quantidadeCidadaos": 1480,
  "watermarkNovo": "2026-05-17T23:59:59-03:00"
}
```

## API — webhook de consolidação (opcional)

**`POST /coordenapleito-api/integracao/vapt-vupt/cargas/{codigo}/consolidar`**

- Mesmo header `X-Integracao-Token`
- Dispara consolidação imediata para a carga (além do scheduler interno, cron padrão a cada 2 minutos)
- Resposta: `VaptVuptConsolidacaoModel` com `status` = `PUBLICADO` em caso de sucesso

Se o token não estiver configurado no servidor, os endpoints de integração retornam erro de negócio.

## Disparo automático (Coordenapleito)

### Cargas pendentes (a cada 2 minutos)

- Scheduler: `VaptVuptConsolidacaoScheduler` com ShedLock
- Propriedade: `app.vapt-vupt.consolidacao.enabled=true`
- Cron: `app.vapt-vupt.consolidacao.scheduler.cron` (padrão `0 */2 * * * *`)

Processa todas as cargas em `AGUARDANDO_CONSOLIDACAO` na ordem de `data_cadastro` (disparadas pelo ETL via `POST /cargas` ou webhook `/consolidar`).

### Consolidação diária FULL (05:00 Fortaleza)

- Scheduler: `VaptVuptConsolidacaoDiariaScheduler` com ShedLock
- Propriedades:
  - `app.vapt-vupt.consolidacao.daily.enabled=true` (padrão ligado)
  - `app.vapt-vupt.consolidacao.daily.cron` (padrão `0 0 5 * * *`)
  - `app.vapt-vupt.consolidacao.daily.zone` (padrão `America/Fortaleza`)

Executa a mesma regra do `POST /integracao/vapt-vupt/consolidacoes/backfill-full` (botão **Reconsolidar** no dashboard): lê todo o staging (`vapt_vupt_atendimento`, `vapt_vupt_atendimento_cidadao`, etc.) e publica nova consolidação em `analitico`.

**Cenário típico:** ETL atualiza as tabelas às **20h**; às **05h** o Coordenapleito reconsolida sem depender de `POST /cargas`. O registro de carga pelo ETL continua opcional (atualização mais rápida via scheduler de 2 min).

## APIs administrativas (JWT)

Requerem perfil `ANALISTA` ou `ADMINISTRADOR`:

| Método | Path | Descrição |
|--------|------|-----------|
| `GET` | `/integracao/vapt-vupt/cargas/{codigo}` | Status da carga |
| `GET` | `/integracao/vapt-vupt/cargas/{codigo}/consolidacoes` | Histórico de consolidações |
| `GET` | `/integracao/vapt-vupt/consolidacoes/{codigo}` | Detalhe de uma consolidação |
| `POST` | `/integracao/vapt-vupt/cargas/{codigo}/reconsolidar` | Reprocessa carga |
| `POST` | `/integracao/vapt-vupt/consolidacoes/backfill-full` | Consolidação FULL inicial |

## Responsabilidades

| Camada | ETL externo | Coordenapleito |
|--------|-------------|-----------|
| Extract/transform da origem | sim | não |
| Upsert staging | sim | não |
| KPIs, rankings, evolução mensal | não | sim (consolidação) |
| Labels legíveis de serviço no dashboard | não | sim (`VaptVuptServicoLabelResolver` na escrita) |
| Leitura do dashboard | não | sim (`DashboardVaptVuptLeituraDao`) |

## Idempotência e concorrência

- Uma carga só consolida se estiver em `AGUARDANDO_CONSOLIDACAO` (lock pessimista por `codigo`).
- Reconsolidação cria nova consolidação e marca a anterior como `SUBSTITUIDO`.
- Versão das regras: `app.vapt-vupt.consolidacao.versao-regra` (gravada em `analitico.vapt_vupt_consolidacao.versao_regra`).

## Migrações de banco

Scripts Flyway em `src/main/resources/db/migration/`:

- `V8__vapt_vupt_integracao_schema.sql` — `integracao.vapt_vupt_carga`, `vapt_vupt_carga_log`
- `V9__vapt_vupt_analitico_schema.sql` — tabelas analíticas e view de leitura
- `V10__shedlock.sql` — lock do scheduler
- `V11__vapt_vupt_fix_mensagem_erro_text.sql` — corrige `mensagem_erro` varchar→TEXT (bases criadas antes pelo Hibernate)
- `V12__vapt_vupt_dashboard_kpi_data_insercao.sql` — coluna `data_insercao` em `analitico.vapt_vupt_dashboard_kpi`

Ativar com `app.vapt-vupt.flyway.enabled=true` (padrão). Perfis `stage`/`production` usam `spring.jpa.hibernate.ddl-auto=none` (schema só via Flyway).

## Kubernetes — token de integração

Propriedade Spring: `app.integracao.vapt-vupt.webhook-token`  
Variável de ambiente equivalente: **`APP_INTEGRACAO_VAPT_VUPT_WEBHOOK_TOKEN`**

### Secret (exemplo)

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: coordenapleito-vapt-vupt-integracao
  namespace: <seu-namespace>
type: Opaque
stringData:
  webhook-token: "364c98e3938417f45287985b126ca0c947c1163ce34842a0248dff645c9dedcf"
```

### Deployment da API (trecho)

```yaml
env:
  - name: APP_INTEGRACAO_VAPT_VUPT_WEBHOOK_TOKEN
    valueFrom:
      secretKeyRef:
        name: coordenapleito-vapt-vupt-integracao
        key: webhook-token
```

O job ETL deve usar o **mesmo** valor no header `X-Integracao-Token` (via Secret compartilhado ou cópia sincronizada).

Arquivo de referência: `docs/k8s/coordenapleito-vapt-vupt-integracao-secret.example.yaml`
