# Casa Cidadão — consolidação analítica

Domínio **independente** do Vapt Vupt. Utiliza apenas o catálogo `servico_vapt_vupt` como referência de serviços.

## Fluxo operacional

1. Cadastro de cidadão via API `/cidadaos` (mesma entidade que a tela Cidadãos).
2. Registro de atendimentos em `/projetos/casa-cidadao/atendimentos` (cidadão + serviço + `dataAtendimento`).
3. Unicidade: `(cidadao_id, servico_vapt_vupt_id, data_atendimento)` — o mesmo serviço em dias diferentes é permitido.

## Consolidação

- Tabela operacional: `casa_cidadao_atendimento`
- Schema analítico: `analitico.casa_cidadao_*` (migrations V15)
- Job: `CasaCidadaoConsolidacaoDiariaScheduler` (cron padrão 06:00 Fortaleza)
- Reconsolidação manual: `POST /projetos/casa-cidadao/consolidacoes/reconsolidar` (Analista/Administrador)

## Leitura do dashboard

- `GET /projetos/casa-cidadao/dashboard-resumo` — sem filtros: lê última consolidação `PUBLICADO`
- Com `?ano=&mes=`: agregação on-read na operacional (período filtrado)

## Frontend

- Menu Projetos → Casa Cidadão (`/projetos/casa-cidadao`) — listagem de atendimentos com registro em modal (cidadão → serviço + data)
- Migração **V16** — tabela Envers `casa_cidadao_atendimento_aud` (obrigatória em stage/prod com `ddl-auto=none`)
- Dashboard Projetos → aba Casa do Cidadão
