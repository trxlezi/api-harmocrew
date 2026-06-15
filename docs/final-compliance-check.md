# Auditoria Final de Conformidade - HarmoCrew API

## Resumo executivo

Status geral: pronto para revisao final.

O projeto atende aos principais requisitos tecnicos da disciplina: API REST com Spring Boot, banco relacional PostgreSQL, JPA/Hibernate, mais de 4 entidades, relacionamentos Um-para-Muitos e Muitos-para-Muitos, CRUDs pela API, autenticacao JWT, senha com BCrypt, Docker, NGINX como balanceador, testes JMeter e testes funcionais documentados.

O documento formal de especificacao do dominio, entidades e API Web foi criado em `docs/api-spec.md` e inclui os integrantes do grupo com seus prontuarios.

## Estado Git

- Branch atual: `main`
- Alinhamento com remoto: `0 ahead / 0 behind` em relacao a `origin/main`
- Arquivos locais nao commitados no momento da auditoria:
  - `README.md`
  - `docs/functional-tests.http`
  - `docs/performance/`
  - `docs/final-compliance-check.md`
  - `docs/api-spec.md`
- Arquivos removidos ou ignorados antes do commit:
  - `jmeter.log`: log local do JMeter
  - `entrega/`: copia local de prints e dashboards HTML

## Resultado dos testes

Comando executado:

```powershell
.\mvnw clean test
```

Resultado:

- Build: sucesso
- Testes: 10
- Falhas: 0
- Erros: 0
- Skipped: 0

Warnings observados:

- Lombok/Java `Unsafe` com aviso de metodo depreciado
- Mockito/ByteBuddy com aviso de agent dinamico
- SpringDoc informa que `/v3/api-docs` e Swagger estao habilitados

Esses warnings nao quebraram a compilacao nem os testes.

## Checklist tecnico

- API REST: atendido, controllers Spring WebMVC em `/api`, `/auth` e `/health`
- Banco relacional: atendido, PostgreSQL em dev/Docker
- ORM: atendido, Spring Data JPA/Hibernate
- Entidades: atendido, 9 entidades JPA
- Relacionamento Um-para-Muitos: atendido, por exemplo `MusicalProject -> Task`
- Relacionamento Muitos-para-Muitos: atendido, `MusicalProject <-> Artist`
- CRUDs: atendido, ha CRUD completo para varias entidades
- JWT: atendido, register/login/logout e filtro Bearer
- Senha criptografada: atendido, BCryptPasswordEncoder
- NGINX: atendido, `docker-compose.nginx.yml` com `api-1`, `api-2` e `nginx`
- JMeter: atendido, plano `.jmx`, `.jtl`, dashboards HTML e resumo comparativo
- Testes funcionais: atendido, `docs/functional-tests.http`
- Documentacao: atendido, com `docs/api-spec.md`, Swagger/OpenAPI e documentos auxiliares

## Estrutura tecnica

- Java configurado no Maven: 21
- Java usado no ambiente de teste local: 25.0.3 LTS
- Spring Boot: 4.0.6
- Build: Maven Wrapper
- Dependencias principais:
  - `spring-boot-starter-webmvc`
  - `spring-boot-starter-data-jpa`
  - `spring-boot-starter-security`
  - `spring-boot-starter-validation`
  - `springdoc-openapi-starter-webmvc-ui`
  - `postgresql`
  - `h2` para testes
  - `lombok`
- Banco normal: PostgreSQL em `jdbc:postgresql://localhost:5432/harmocrew`
- Banco Docker: PostgreSQL no host `postgres`, database/user/password `harmocrew`
- Banco de teste: H2 em memoria com profile `test`
- DDL: `spring.jpa.hibernate.ddl-auto=update` em dev e `create-drop` em teste
- Swagger:
  - `/swagger-ui.html`
  - `/swagger-ui/index.html`
  - `/v3/api-docs`
- OpenAPI Bearer: configurado em `OpenApiConfig`

## Entidades e relacionamentos

| Entidade | Tabela | Campos principais | Repository | Endpoints relacionados |
| --- | --- | --- | --- | --- |
| `User` | `users` | id, name, email, password, role, createdAt | `UserRepository` | `/auth/register`, `/auth/login`, `/auth/logout` |
| `Artist` | `artists` | id, stageName, bio, mainSpecialty, instruments, musicalStyles, availability, city | `ArtistRepository` | `/api/artists` |
| `MusicalProject` | `musical_projects` | id, title, description, musicalStyle, status, needs, startDate | `MusicalProjectRepository` | `/api/projects` |
| `Task` | `tasks` | id, title, description, status, priority, dueDate, responsibleName | `TaskRepository` | `/api/tasks`, `/api/projects/{projectId}/tasks` |
| `Application` | `applications` | id, message, specialty, availability, status, createdAt | `ApplicationRepository` | `/api/applications`, `/api/projects/{projectId}/applications` |
| `Rehearsal` | `rehearsals` | id, title, date, time, location, notes, status | `RehearsalRepository` | `/api/rehearsals`, `/api/projects/{projectId}/rehearsals` |
| `WeeklyGoal` | `weekly_goals` | id, title, description, weekLabel, dueDate, status | `WeeklyGoalRepository` | `/api/weekly-goals`, `/api/projects/{projectId}/weekly-goals` |
| `DecisionRecord` | `decision_records` | id, title, description, impact, decidedAt, status | `DecisionRecordRepository` | `/api/decisions`, `/api/projects/{projectId}/decisions` |
| `CollaborationMessage` | `collaboration_messages` | id, content, sentAt, type | `CollaborationMessageRepository` | `/api/messages`, `/api/projects/{projectId}/messages` |

Relacionamentos JPA confirmados:

- `User` 1 --- 1 `Artist`: `@OneToOne`
- `MusicalProject` 1 --- N `Task`: `@OneToMany` / `@ManyToOne`
- `MusicalProject` N --- N `Artist`: `@ManyToMany` com tabela `musical_project_artists`
- `Artist` 1 --- N `Application`: `@OneToMany` / `@ManyToOne`
- `MusicalProject` 1 --- N `Application`: `@OneToMany` / `@ManyToOne`
- Relacionamentos adicionais: projetos com rehearsals, messages, decisions e weekly goals

## CRUDs pela API

| Entidade | Endpoints encontrados | CRUD completo? | Atende como entidade 1 ou 2 com CRUD completo? | Atende como entidade 3 ou 4 com operacao parcial? | Observacoes |
| --- | --- | --- | --- | --- | --- |
| `Artist` | `GET /api/artists`, `GET /api/artists/{id}`, `POST /api/artists`, `PUT /api/artists/{id}`, `DELETE /api/artists/{id}` | Sim | Sim | Sim | Usa DTOs `ArtistRequest`/`ArtistResponse`; integrado via service/repository |
| `MusicalProject` | `GET /api/projects`, `GET /api/projects/{id}`, `GET /api/projects/summaries`, `POST /api/projects`, `PUT /api/projects/{id}`, `POST /api/projects/{projectId}/artists/{artistId}`, `DELETE /api/projects/{id}` | Sim | Sim | Sim | Inclui associacao N:N com Artist |
| `Task` | `GET /api/tasks`, `GET /api/tasks/{id}`, `GET /api/projects/{projectId}/tasks`, `POST /api/projects/{projectId}/tasks`, `PUT /api/tasks/{id}`, `PATCH /api/tasks/{id}/status`, `DELETE /api/tasks/{id}` | Sim | Sim | Sim | CRUD completo e status parcial |
| `Application` | `GET /api/applications`, `GET /api/applications/{id}`, `GET /api/projects/{projectId}/applications`, `POST /api/projects/{projectId}/applications`, `PATCH /api/applications/{id}/status`, `DELETE /api/applications/{id}` | Parcial | Nao | Sim | Nao ha `PUT` completo, mas ha create/read/delete/status |
| `Rehearsal` | `GET /api/rehearsals`, `GET /api/projects/{projectId}/rehearsals`, `POST`, `PUT`, `PATCH status`, `DELETE` | Sim | Sim | Sim | CRUD completo |
| `WeeklyGoal` | `GET /api/weekly-goals`, `GET /api/projects/{projectId}/weekly-goals`, `POST`, `PUT`, `PATCH status`, `DELETE` | Sim | Sim | Sim | CRUD completo |
| `DecisionRecord` | `GET /api/decisions`, `GET /api/projects/{projectId}/decisions`, `POST`, `PUT`, `PATCH status`, `DELETE` | Sim | Sim | Sim | CRUD completo |
| `CollaborationMessage` | `GET /api/messages`, `GET /api/projects/{projectId}/messages`, `POST`, `DELETE` | Parcial | Nao | Sim | Operacoes CRUD parciais |
| `User` | `/auth/register`, `/auth/login`, `/auth/logout` | Parcial | Nao | Sim | Criacao e leitura indireta via auth; sem CRUD administrativo |

Conclusao de CRUD:

- Pelo menos 2 entidades tem CRUD completo: sim (`Artist`, `MusicalProject`, alem de `Task`, `Rehearsal`, `WeeklyGoal`, `DecisionRecord`)
- Pelo menos 4 entidades tem alguma operacao CRUD pela API: sim
- Status HTTP observados no codigo:
  - `POST`: `201 Created` em criacoes
  - `DELETE`: `204 No Content`
  - `GET`/`PUT`/`PATCH`: `200 OK` por padrao Spring

## Autenticacao JWT

Evidencias:

- `/auth/register`: existe e cria `User`; tambem pode criar `Artist` inicial
- `/auth/login`: existe e retorna token
- `/auth/logout`: existe e invalida token em blacklist em memoria
- Senha: `PasswordEncoder` com `BCryptPasswordEncoder`
- Rotas protegidas: `SecurityConfig` exige autenticacao para rotas fora de health, Swagger e login/register
- Bearer: `JwtAuthenticationFilter` le header `Authorization: Bearer ...`
- Swagger/OpenAPI: `OpenApiConfig` define esquema `bearerAuth`
- `AuthResponse`: nao possui campo `password`
- Limitacao de logout: documentada no README e docs Docker/NGINX

Teste HTTP real via NGINX:

- Register: token retornado, `userId` retornado, sem campo `password`
- Login: token retornado
- Endpoint protegido sem token: `401`
- Endpoint protegido com token: `200`
- Logout: `204`

## Banco e ORM

- PostgreSQL configurado para execucao normal
- H2 configurado apenas para testes
- Entidades anotadas com `@Entity` e `@Table`
- Repositories estendem `JpaRepository`
- Hibernate cria/atualiza schema via `ddl-auto`
- Migrations: nao existem Flyway/Liquibase; nao obrigatorio pelo enunciado, mas seria melhoria
- Docker Compose liga API ao PostgreSQL com `jdbc:postgresql://postgres:5432/harmocrew`

## Docker e NGINX

Arquivos verificados:

- `Dockerfile`
- `docker-compose.yml`
- `docker-compose.nginx.yml`
- `nginx/nginx.conf`

Validacoes executadas:

- `docker compose config`: sucesso
- `docker compose -f docker-compose.nginx.yml config`: sucesso
- `docker compose -f docker-compose.nginx.yml up --build -d`: sucesso
- `docker compose -f docker-compose.nginx.yml ps`: `api-1`, `api-2`, `postgres healthy` e `nginx` ativos
- `/health` via NGINX: validado antes e no fluxo de auth

NGINX:

- Porta exposta: `localhost:8080 -> nginx:80`
- Upstream:
  - `api-1:8080`
  - `api-2:8080`
- Algoritmo: `least_conn`

## Testes de performance JMeter

Arquivos verificados:

- `docs/performance/harmocrew-performance-test.jmx`
- `docs/performance/results-sem-nginx.jtl`
- `docs/performance/results-com-nginx.jtl`
- `docs/performance/report-sem-nginx/index.html`
- `docs/performance/report-com-nginx/index.html`
- `docs/performance/performance-results-summary.md`
- `docs/performance/report-template.md`
- `docs/performance/jmeter-plan.md`

Resumo real dos resultados atuais:

| Cenario | Requisicoes | Erros | Taxa de erro | Media | Mediana | P90 | P95 | P99 | Throughput |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Sem NGINX | 1000 | 0 | 0% | 3.27 ms | 3 ms | 5 ms | 6 ms | 10 ms | 34.06 req/s |
| Com NGINX | 1000 | 0 | 0% | 52.57 ms | 19 ms | 72 ms | 115 ms | 511 ms | 34.72 req/s |

O relatorio explica o erro antigo do cenario sem NGINX e o reteste limpo. Os resultados sao adequados para anexar ao PDF academico, com a observacao de que foram medidos localmente e em endpoints publicos simples.

## Testes funcionais documentados

Arquivo:

- `docs/functional-tests.http`

Cobertura:

- `GET /health`
- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/logout`
- Uso de `Authorization: Bearer {{token}}`
- CRUD documentado de `Artist`
- CRUD documentado de `MusicalProject`
- Operacoes de `Task`
- Operacoes de `Application`

Esse arquivo pode ser usado com REST Client, Thunder Client ou ferramenta similar. Ainda faltam prints externos/anexos mostrando a execucao dessas chamadas para reforcar a entrega.

## Documentacao da API

- README: atualizado com comandos essenciais
- Swagger/OpenAPI: disponivel e com Bearer JWT
- Documentacao de Docker/NGINX: `docs/docker-nginx.md`
- Documentacao de performance: `docs/performance/*`
- Testes funcionais: `docs/functional-tests.http`
- Documento formal de dominio/API: `docs/api-spec.md`

## Tabela CR1 a CR11

| Codigo do criterio | Descricao resumida | Status | Evidencia no projeto | Observacoes | Acao recomendada |
| --- | --- | --- | --- | --- | --- |
| CR1 | Documento de especificacao do dominio, entidades e API Web | Atendido | `docs/api-spec.md` | Documento inclui dominio, entidades, relacionamentos, endpoints e integrantes | Manter o arquivo junto da entrega |
| CR2 | CRUD da entidade 1 pela API Web integrada ao banco com ORM | Atendido | `ArtistController`, `ArtistService`, `ArtistRepository` | CRUD completo com JPA | Usar `Artist` como entidade 1 |
| CR3 | CRUD da entidade 2 pela API Web integrada ao banco com ORM | Atendido | `MusicalProjectController`, `MusicalProjectService`, `MusicalProjectRepository` | CRUD completo com JPA | Usar `MusicalProject` como entidade 2 |
| CR4 | Alguma operacao CRUD com entidade 3 pela API Web integrada ao banco com ORM | Atendido | `TaskController`, `TaskService`, `TaskRepository` | CRUD completo, mais status | Usar `Task` como entidade 3 |
| CR5 | Alguma operacao CRUD com entidade 4 pela API Web integrada ao banco com ORM | Atendido | `ApplicationController`, `ApplicationService`, `ApplicationRepository` | Operacoes create/read/delete/status | Usar `Application` como entidade 4 |
| CR6 | Implementacao de 1 relacionamento Um-para-Muitos | Atendido | `MusicalProject` `@OneToMany` com `Task`; `Task` `@ManyToOne` | Mapeado com JPA/Hibernate | Mostrar trecho no PDF |
| CR7 | Implementacao de 1 relacionamento Muitos-para-Muitos | Atendido | `MusicalProject` `@ManyToMany` com `Artist`, tabela `musical_project_artists` | Mapeado com `@JoinTable` | Mostrar trecho no PDF |
| CR8 | Autenticacao funcional com register, login e logout | Atendido | `AuthController`, `AuthService`, `JwtAuthenticationFilter`, teste HTTP real | BCrypt, JWT e Bearer funcionando | Anexar print do fluxo |
| CR9 | Configuracao e funcionamento do balanceamento com NGINX | Atendido | `docker-compose.nginx.yml`, `nginx/nginx.conf`, `docker compose ps` | `api-1`, `api-2`, `nginx`, `postgres healthy` | Anexar print do `ps` e `/health` |
| CR10 | Relatorio de testes de performance com JMeter | Atendido | `docs/performance/performance-results-summary.md`, `.jmx`, `.jtl`, dashboards HTML | Comparacao com e sem NGINX com metricas reais | Anexar dashboards e resumo |
| CR11 | Testes funcionais documentados com Thunder Client, Postman ou equivalente | Atendido | `docs/functional-tests.http` | Cobre auth, Bearer, health, CRUDs e operacoes principais | Tirar prints da execucao |

## Pendencias criticas

- Conferir os arquivos no `git status` antes do commit.

## Pendencias recomendadas

- Adicionar Flyway ou Liquibase futuramente para versionar schema do banco.
- Adicionar prints dos testes funcionais executados no Thunder Client, REST Client ou Postman.
- Acrescentar testes automatizados para todos os controllers secundarios, se houver tempo.
- Evitar usar Java 25 no ambiente final se o projeto declara Java 21; Java 21 e o alvo correto do projeto.

## Evidencias para anexar na entrega

- Swagger aberto em `http://localhost:8080/swagger-ui/index.html`
- `docker compose -f docker-compose.nginx.yml ps` mostrando `api-1`, `api-2`, `postgres healthy` e `nginx`
- `curl` ou `Invoke-WebRequest` para `http://localhost:8080/health`
- Dashboard JMeter sem NGINX: `docs/performance/report-sem-nginx/index.html`
- Dashboard JMeter com NGINX: `docs/performance/report-com-nginx/index.html`
- Resumo JMeter: `docs/performance/performance-results-summary.md`
- Testes funcionais: `docs/functional-tests.http`
- Print do fluxo register/login/logout
- Print do endpoint protegido sem token retornando `401`
- README com comandos principais

## Proximo passo recomendado

Antes do commit:

1. Conferir os arquivos no `git status`.
2. Tirar prints finais para o PDF.

Depois disso, o projeto fica pronto para commit e para montar o PDF/relatorio academico.
