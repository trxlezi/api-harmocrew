# HarmoCrew API

Backend REST do HarmoCrew em Spring Boot.

## Requisitos

- Java 21 ou superior
- Docker e Docker Compose
- Maven Wrapper incluido no repositorio

## Subir PostgreSQL

```powershell
docker compose up -d
```

O banco de desenvolvimento sobe em `localhost:5432` com:

- database: `harmocrew`
- usuario: `harmocrew`
- senha: `harmocrew`

## Rodar a API

```powershell
.\mvnw spring-boot:run
```

Por padrao a API roda em `http://localhost:8080`.

As configuracoes principais podem ser sobrescritas por variaveis de ambiente:

- `SERVER_PORT`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`

## Health check

```powershell
Invoke-RestMethod http://localhost:8080/health
```

Resposta esperada:

```json
{
  "status": "UP",
  "application": "harmocrew-api",
  "timestamp": "2026-06-10T18:00:00Z"
}
```

## Swagger/OpenAPI

Se a aplicacao estiver rodando, acesse:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Testes

```powershell
.\mvnw clean test
```

Os testes usam perfil `test` com H2 em memoria, sem depender do PostgreSQL local.

## Escopo atual

Este primeiro passo prepara apenas a base do backend:

- estrutura inicial de pacotes;
- endpoint `GET /health`;
- configuracao de PostgreSQL para desenvolvimento;
- Docker Compose com PostgreSQL;
- Swagger/OpenAPI;
- teste automatizado do health check.

Ainda nao foram implementados CRUDs, JWT, entidades de dominio ou integracao com frontend.
