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

## Docker Swarm e Visualizer

O laboratorio da disciplina cria um cluster Docker-in-Docker com um manager,
dois workers, registry local, API replicada, PostgreSQL e Docker Visualizer.

Documentacao e roteiro completo da apresentacao:

- `docs/APRESENTACAO-CONTAINERS.md`
- `infra/swarm/docker-compose.cluster.yml`
- `infra/swarm/stack.yml`

Inicializacao resumida (Linux ou WSL):

```bash
chmod +x infra/swarm/*.sh
./infra/swarm/init-cluster.sh
./infra/swarm/deploy.sh
```

Escalar a API de duas para cinco replicas:

```bash
DOCKER_HOST=tcp://127.0.0.1:2375 docker service scale harmocrew_api=5
```

Visualizer: `http://localhost:8081`.

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

## Testes de performance

Documentacao e plano JMeter:

- `docs/performance/jmeter-plan.md`
- `docs/performance/report-template.md`
- `docs/performance/harmocrew-performance-test.jmx`

Os testes com JMeter devem comparar o cenario sem NGINX (`docker compose up --build`) com o cenario usando NGINX (`docker compose -f docker-compose.nginx.yml up --build`). Nao registrar metricas sem executar os testes.

## Documentacao final

- `docs/api-spec.md`
- `docs/final-compliance-check.md`
- `docs/performance/performance-results-summary.md`

## Observacao

O logout usa blacklist de token em memoria. Com NGINX e mais de uma instancia da API, essa blacklist nao e compartilhada entre as instancias.
