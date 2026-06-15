# Docker e NGINX

## Cenario 1: execucao local sem API em Docker

Use Docker apenas para o PostgreSQL:

```powershell
docker compose up -d postgres
```

Rode a API com Maven:

```powershell
.\mvnw spring-boot:run
```

URLs de teste:

- `http://localhost:8080/health`
- `http://localhost:8080/swagger-ui.html`

Parar o banco:

```powershell
docker compose down
```

## Cenario 2: execucao com Docker Compose

Sobe PostgreSQL e API no Docker:

```powershell
docker compose up --build
```

URLs de teste:

- `http://localhost:8080/health`
- `http://localhost:8080/swagger-ui.html`

Logs:

```powershell
docker compose logs -f api
docker compose logs -f postgres
```

Parar:

```powershell
docker compose down
```

## Cenario 3: execucao com NGINX balanceando

Sobe PostgreSQL, duas instancias da API e NGINX:

```powershell
docker compose -f docker-compose.nginx.yml up --build
```

O NGINX escuta em `localhost:8080` e encaminha para:

- `api-1:8080`
- `api-2:8080`

URLs de teste:

- `http://localhost:8080/health`
- `http://localhost:8080/swagger-ui.html`

Logs:

```powershell
docker compose -f docker-compose.nginx.yml logs -f nginx
docker compose -f docker-compose.nginx.yml logs -f api-1
docker compose -f docker-compose.nginx.yml logs -f api-2
docker compose -f docker-compose.nginx.yml logs -f postgres
```

Parar:

```powershell
docker compose -f docker-compose.nginx.yml down
```

## Problemas comuns

- `docker` nao reconhecido: Docker Desktop nao esta instalado, nao esta aberto ou nao esta no PATH.
- Porta `5432` ocupada: existe outro PostgreSQL local rodando.
- Porta `8080` ocupada: pare a API local antes de subir Compose ou NGINX.
- API nao conecta no banco local: confirme que `docker compose up -d postgres` foi executado e que o healthcheck do PostgreSQL esta saudavel.
- API nao conecta no banco dentro do Docker: confirme se `SPRING_DATASOURCE_URL` usa `jdbc:postgresql://postgres:5432/harmocrew`.
- Logout em ambiente balanceado: a blacklist de tokens fica em memoria e nao e compartilhada entre `api-1` e `api-2`.
