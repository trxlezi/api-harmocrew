# Especificacao da API Web - HarmoCrew API

## 1. Dominio da aplicacao

HarmoCrew e uma API REST para colaboracao musical, descoberta de artistas e talentos, gerenciamento de projetos musicais, tarefas, candidaturas, ensaios, metas, decisoes e mensagens de colaboracao.

## 2. Objetivo da API

A API permite:

- autenticacao de usuarios;
- cadastro e consulta de artistas;
- gestao de projetos musicais;
- associacao de artistas a projetos;
- controle de tarefas;
- candidaturas de artistas a projetos;
- registro de ensaios;
- metas semanais;
- decisoes;
- mensagens de colaboracao.

## 3. Integrantes do grupo

| Nome | Prontuario |
| --- | --- |
| Rafael Yukio Shiraishi | BP3052591 |
| Gabriel Trolezi Caetano | BP3051862 |
| Kaian Muniz de Souza | BP3051901 |
| Joao Vitor Santos | BP3051552 |

## 4. Tecnologias utilizadas

- Java 21
- Maven
- Spring Boot 4.0.6
- Spring WebMVC
- Spring Data JPA
- Hibernate
- Spring Security
- JWT
- BCrypt
- PostgreSQL
- H2 em testes
- Docker
- NGINX
- Swagger/OpenAPI
- JMeter

## 5. Entidades do dominio

### User

Objetivo: representar o usuario autenticavel da API.

Campos principais:

- id
- name
- email
- password
- role
- createdAt

Relacionamentos:

- User 1:1 Artist

### Artist

Objetivo: representar um artista ou talento musical cadastrado.

Campos principais:

- id
- stageName
- bio
- mainSpecialty
- instruments
- musicalStyles
- availability
- city

Relacionamentos:

- Artist 1:1 User
- Artist N:N MusicalProject
- Artist 1:N Application

### MusicalProject

Objetivo: representar um projeto musical colaborativo.

Campos principais:

- id
- title
- description
- musicalStyle
- status
- needs
- startDate

Relacionamentos:

- MusicalProject N:N Artist
- MusicalProject 1:N Task
- MusicalProject 1:N Application
- MusicalProject 1:N Rehearsal
- MusicalProject 1:N CollaborationMessage
- MusicalProject 1:N DecisionRecord
- MusicalProject 1:N WeeklyGoal

### Task

Objetivo: representar uma tarefa de um projeto musical.

Campos principais:

- id
- title
- description
- status
- priority
- dueDate
- responsibleName

Relacionamentos:

- Task N:1 MusicalProject

### Application

Objetivo: representar uma candidatura de artista para participar de um projeto.

Campos principais:

- id
- message
- specialty
- availability
- status
- createdAt

Relacionamentos:

- Application N:1 Artist
- Application N:1 MusicalProject

### Rehearsal

Objetivo: representar um ensaio vinculado a um projeto.

Campos principais:

- id
- title
- date
- time
- location
- notes
- status
- participantArtistIds

Relacionamentos:

- Rehearsal N:1 MusicalProject

### WeeklyGoal

Objetivo: representar uma meta semanal vinculada a um projeto e a um artista responsavel.

Campos principais:

- id
- title
- description
- weekLabel
- dueDate
- status

Relacionamentos:

- WeeklyGoal N:1 MusicalProject
- WeeklyGoal N:1 Artist

### DecisionRecord

Objetivo: representar decisoes registradas dentro de um projeto musical.

Campos principais:

- id
- title
- description
- impact
- decidedAt
- status

Relacionamentos:

- DecisionRecord N:1 MusicalProject
- DecisionRecord N:1 Artist

### CollaborationMessage

Objetivo: representar mensagens de colaboracao dentro de um projeto.

Campos principais:

- id
- content
- sentAt
- type

Relacionamentos:

- CollaborationMessage N:1 MusicalProject
- CollaborationMessage N:1 Artist

## 6. Relacionamentos

Relacionamentos reais encontrados no mapeamento JPA:

- User 1:1 Artist, com `@OneToOne`
- MusicalProject 1:N Task, com `@OneToMany` e `@ManyToOne`
- MusicalProject N:N Artist, com `@ManyToMany` e tabela `musical_project_artists`
- Artist 1:N Application, com `@OneToMany` e `@ManyToOne`
- MusicalProject 1:N Application, com `@OneToMany` e `@ManyToOne`
- MusicalProject 1:N Rehearsal
- MusicalProject 1:N CollaborationMessage
- MusicalProject 1:N DecisionRecord
- MusicalProject 1:N WeeklyGoal
- Artist 1:N WeeklyGoal
- Artist 1:N DecisionRecord
- Artist 1:N CollaborationMessage

Relacionamento usado para CR6:

- MusicalProject 1:N Task

Relacionamento usado para CR7:

- MusicalProject N:N Artist

## 7. Entidades escolhidas para avaliacao dos CRUDs

CRUD completo:

- Artist
- MusicalProject

Tambem possuem CRUD completo:

- Task
- Rehearsal
- WeeklyGoal
- DecisionRecord

Operacoes adicionais ou parciais:

- Application
- CollaborationMessage
- User, via autenticacao

## 8. Autenticacao e seguranca

Endpoints de autenticacao:

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/logout`

Tambem existem os aliases:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`

A senha do usuario e criptografada com BCrypt. O login gera um token JWT. As rotas protegidas exigem o header:

```http
Authorization: Bearer <token>
```

Rotas publicas:

- `GET /health`
- `POST /auth/register`
- `POST /auth/login`
- `POST /api/auth/register`
- `POST /api/auth/login`
- Swagger/OpenAPI

As demais rotas exigem autenticacao. O logout usa blacklist de token em memoria.

## 9. Tabela de modelagem da API REST

| Verbo HTTP | Path | Body de Requisicao | Body de Retorno | Status HTTP de sucesso | Status HTTP de erro |
| --- | --- | --- | --- | --- | --- |
| GET | `/health` | Nao possui | HealthResponse | 200 | Nao informado |
| POST | `/auth/register` | RegisterRequest | AuthResponse | 201 | 400, 409 |
| POST | `/auth/login` | LoginRequest | AuthResponse | 200 | 400, 401 |
| POST | `/auth/logout` | Nao possui | Nao possui | 204 | 400, 401 |
| GET | `/api/artists` | Nao possui | List ArtistResponse | 200 | 401 |
| GET | `/api/artists/{id}` | Nao possui | ArtistResponse | 200 | 401, 404 |
| POST | `/api/artists` | ArtistRequest | ArtistResponse | 201 | 400, 401 |
| PUT | `/api/artists/{id}` | ArtistRequest | ArtistResponse | 200 | 400, 401, 404 |
| DELETE | `/api/artists/{id}` | Nao possui | Nao possui | 204 | 401, 404 |
| GET | `/api/projects` | Nao possui | List MusicalProjectResponse | 200 | 401 |
| GET | `/api/projects/summaries` | Nao possui | List MusicalProjectSummaryResponse | 200 | 401 |
| GET | `/api/projects/{id}` | Nao possui | MusicalProjectResponse | 200 | 401, 404 |
| POST | `/api/projects` | MusicalProjectRequest | MusicalProjectResponse | 201 | 400, 401 |
| PUT | `/api/projects/{id}` | MusicalProjectRequest | MusicalProjectResponse | 200 | 400, 401, 404 |
| POST | `/api/projects/{projectId}/artists/{artistId}` | Nao possui | MusicalProjectResponse | 200 | 401, 404 |
| DELETE | `/api/projects/{id}` | Nao possui | Nao possui | 204 | 401, 404 |
| GET | `/api/tasks` | Query opcional `projectId` | List TaskResponse | 200 | 401 |
| GET | `/api/tasks/{id}` | Nao possui | TaskResponse | 200 | 401, 404 |
| GET | `/api/projects/{projectId}/tasks` | Nao possui | List TaskResponse | 200 | 401 |
| POST | `/api/projects/{projectId}/tasks` | TaskRequest | TaskResponse | 201 | 400, 401, 404 |
| PUT | `/api/tasks/{id}` | TaskRequest | TaskResponse | 200 | 400, 401, 404 |
| PATCH | `/api/tasks/{id}/status` | TaskStatusUpdateRequest | TaskResponse | 200 | 400, 401, 404 |
| DELETE | `/api/tasks/{id}` | Nao possui | Nao possui | 204 | 401, 404 |
| GET | `/api/applications` | Query opcional `projectId` ou `artistId` | List ApplicationResponse | 200 | 401 |
| GET | `/api/applications/{id}` | Nao possui | ApplicationResponse | 200 | 401, 404 |
| GET | `/api/projects/{projectId}/applications` | Nao possui | List ApplicationResponse | 200 | 401 |
| POST | `/api/projects/{projectId}/applications` | ApplicationRequest | ApplicationResponse | 201 | 400, 401, 404 |
| PATCH | `/api/applications/{id}/status` | ApplicationStatusUpdateRequest | ApplicationResponse | 200 | 400, 401, 404 |
| DELETE | `/api/applications/{id}` | Nao possui | Nao possui | 204 | 401, 404 |
| GET | `/api/rehearsals` | Query opcional `projectId` | List RehearsalResponse | 200 | 401 |
| GET | `/api/projects/{projectId}/rehearsals` | Nao possui | List RehearsalResponse | 200 | 401 |
| POST | `/api/projects/{projectId}/rehearsals` | RehearsalRequest | RehearsalResponse | 201 | 400, 401, 404 |
| PUT | `/api/rehearsals/{id}` | RehearsalRequest | RehearsalResponse | 200 | 400, 401, 404 |
| PATCH | `/api/rehearsals/{id}/status` | RehearsalStatusUpdateRequest | RehearsalResponse | 200 | 400, 401, 404 |
| DELETE | `/api/rehearsals/{id}` | Nao possui | Nao possui | 204 | 401, 404 |
| GET | `/api/weekly-goals` | Query opcional `projectId` ou `ownerArtistId` | List WeeklyGoalResponse | 200 | 401 |
| GET | `/api/projects/{projectId}/weekly-goals` | Nao possui | List WeeklyGoalResponse | 200 | 401 |
| POST | `/api/projects/{projectId}/weekly-goals` | WeeklyGoalRequest | WeeklyGoalResponse | 201 | 400, 401, 404 |
| PUT | `/api/weekly-goals/{id}` | WeeklyGoalRequest | WeeklyGoalResponse | 200 | 400, 401, 404 |
| PATCH | `/api/weekly-goals/{id}/status` | WeeklyGoalStatusUpdateRequest | WeeklyGoalResponse | 200 | 400, 401, 404 |
| DELETE | `/api/weekly-goals/{id}` | Nao possui | Nao possui | 204 | 401, 404 |
| GET | `/api/decisions` | Query opcional `projectId` | List DecisionRecordResponse | 200 | 401 |
| GET | `/api/projects/{projectId}/decisions` | Nao possui | List DecisionRecordResponse | 200 | 401 |
| POST | `/api/projects/{projectId}/decisions` | DecisionRecordRequest | DecisionRecordResponse | 201 | 400, 401, 404 |
| PUT | `/api/decisions/{id}` | DecisionRecordRequest | DecisionRecordResponse | 200 | 400, 401, 404 |
| PATCH | `/api/decisions/{id}/status` | DecisionStatusUpdateRequest | DecisionRecordResponse | 200 | 400, 401, 404 |
| DELETE | `/api/decisions/{id}` | Nao possui | Nao possui | 204 | 401, 404 |
| GET | `/api/messages` | Query opcional `projectId` | List CollaborationMessageResponse | 200 | 401 |
| GET | `/api/projects/{projectId}/messages` | Nao possui | List CollaborationMessageResponse | 200 | 401 |
| POST | `/api/projects/{projectId}/messages` | CollaborationMessageRequest | CollaborationMessageResponse | 201 | 400, 401, 404 |
| DELETE | `/api/messages/{id}` | Nao possui | Nao possui | 204 | 401, 404 |

## 10. Documentacao Swagger/OpenAPI

URLs locais:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/swagger-ui/index.html`
- `http://localhost:8080/v3/api-docs`

A documentacao Swagger/OpenAPI mostra os endpoints reais da aplicacao e pode ser usada junto com a tabela REST deste documento.

## 11. Banco de dados

A execucao normal usa PostgreSQL. A configuracao padrao usa:

- URL: `jdbc:postgresql://localhost:5432/harmocrew`
- usuario: `harmocrew`
- senha: `harmocrew`

Nos testes automatizados, o projeto usa H2 em memoria:

- URL: `jdbc:h2:mem:harmocrew_test`
- modo de compatibilidade: PostgreSQL

As entidades usam JPA/Hibernate. A geracao de tabelas usa `spring.jpa.hibernate.ddl-auto=update` na configuracao principal e `create-drop` nos testes.

O banco pode ser iniciado com Docker Compose:

```powershell
docker compose up --build
```

## 12. Execucao e testes

Rodar testes automatizados:

```powershell
.\mvnw clean test
```

Rodar com Docker Compose:

```powershell
docker compose up --build
```

Rodar com NGINX:

```powershell
docker compose -f docker-compose.nginx.yml up --build
```

Health check:

```http
GET /health
```

URL local:

```text
http://localhost:8080/health
```

## 13. Evidencias relacionadas

Arquivos do repositorio que comprovam os criterios:

- `README.md`
- `docs/functional-tests.http`
- `docs/performance/performance-results-summary.md`
- `docs/final-compliance-check.md`
- `docker-compose.yml`
- `docker-compose.nginx.yml`
- `nginx/nginx.conf`
- `docs/performance/harmocrew-performance-test.jmx`
- `src/main/java/br/edu/ifsp/harmocrew_api/entities`
- `src/main/java/br/edu/ifsp/harmocrew_api/controllers`
- `src/main/java/br/edu/ifsp/harmocrew_api/services`
- `src/main/java/br/edu/ifsp/harmocrew_api/repositories`
