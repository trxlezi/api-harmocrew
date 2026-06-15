# Trabalho backend

## Maven + PostgreSQL

Subir somente o banco:

```powershell
docker compose up -d postgres
```

Rodar a API localmente:

```powershell
.\mvnw spring-boot:run
```

URLs:

- API: `http://localhost:8080`
- Health: `http://localhost:8080/health`
- Swagger: `http://localhost:8080/swagger-ui.html`

## Docker Compose

Subir PostgreSQL e API:

```powershell
docker compose up --build
```

Parar:

```powershell
docker compose down
```

Logs:

```powershell
docker compose logs -f api
docker compose logs -f postgres
```

## NGINX

Subir PostgreSQL, duas instancias da API e NGINX:

```powershell
docker compose -f docker-compose.nginx.yml up --build
```

URLs via NGINX:

- API: `http://localhost:8080`
- Health: `http://localhost:8080/health`
- Swagger: `http://localhost:8080/swagger-ui.html`

Parar:

```powershell
docker compose -f docker-compose.nginx.yml down
```

## Testes

```powershell
.\mvnw clean test
```

## Observacao

O logout usa blacklist de token em memoria. Com NGINX e mais de uma instancia da API, essa blacklist nao e compartilhada entre as instancias.
