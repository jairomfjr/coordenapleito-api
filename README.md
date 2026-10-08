# Coordenapleito API

Backend do sistema **Coordenapleito**, responsável por autenticação, regras de negócio e persistência dos dados de equipamentos, usuários, atividades e módulos sociais.

## O que é o projeto

A API centraliza as operações do Coordenapleito e expõe endpoints REST consumidos pelo frontend.  
Também inclui rotinas de geocodificação para atualização de latitude/longitude de equipamentos com validações para município e estado.

## Tecnologias empregadas

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA (Hibernate)
- Spring Security + JWT
- PostgreSQL
- Flyway
- Lombok
- ModelMapper
- SpringDoc OpenAPI (Swagger)

## Pré-requisitos

- Java 21
- Maven 3.9+
- PostgreSQL

### Busca na listagem de cidadãos (nome / e-mail)

A API usa a função `unaccent` do PostgreSQL para ignorar diferenças de acentuação e maiúsculas entre o termo digitado e o valor gravado (ex.: “ANDRÉ”, “andre”, “André”).  
É necessário habilitar a extensão **uma vez** no banco:

```sql
CREATE EXTENSION IF NOT EXISTS unaccent;
```

Script de referência: `sql/enable-unaccent-postgresql.sql`. Sem essa extensão, a listagem com filtro por nome ou e-mail pode falhar em tempo de execução.

## Execução local

```bash
mvn spring-boot:run
```

API disponível em:

- `http://localhost:8080/coordenapleito-api`

## Perfis úteis

- `atualizar-coordenadas`: executa rotina em lote de atualização de coordenadas.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=atualizar-coordenadas
```

## Documentação da API

Com a aplicação em execução:

- Swagger UI: `http://localhost:8080/coordenapleito-api/swagger-ui/index.html`
