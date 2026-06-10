# Checkup BRADWBK - HarmoCrew API

Baseado no PDF `[BRADWBK] Especificacoes Projeto Back-end - 2026.pdf`.

## Requisitos atendidos no codigo

| Criterio | Status | Evidencia no projeto |
| --- | --- | --- |
| Dominio da API | Atendido | HarmoCrew: colaboracao musical, artistas, projetos, tarefas, candidaturas, ensaios, mensagens, decisoes e metas. |
| Pelo menos 4 entidades | Atendido | `User`, `Artist`, `MusicalProject`, `Task`, `Application`, `Rehearsal`, `CollaborationMessage`, `DecisionRecord`, `WeeklyGoal`. |
| Relacionamento um-para-muitos | Atendido | `MusicalProject` -> `Task`, `Application`, `Rehearsal`, `DecisionRecord`, `WeeklyGoal`. |
| Relacionamento muitos-para-muitos | Atendido | `MusicalProject` <-> `Artist`. |
| CRUD completo entidade 1 | Atendido | `ArtistController`: `GET`, `GET/{id}`, `POST`, `PUT`, `DELETE`. |
| CRUD completo entidade 2 | Atendido | `MusicalProjectController`: `GET`, `GET/{id}`, `POST`, `PUT`, `DELETE`. |
| Operacao CRUD entidade 3 | Atendido | `TaskController`: listar, criar, atualizar, atualizar status, excluir. |
| Operacao CRUD entidade 4 | Atendido | `ApplicationController`: listar, criar, atualizar status, excluir. |
| Banco relacional + ORM | Atendido | Spring Data JPA/Hibernate com PostgreSQL em desenvolvimento e H2 em testes. |
| Senha criptografada | Atendido | `BCryptPasswordEncoder` em `SecurityConfig`; cadastro usa hash BCrypt. |
| JWT | Atendido | `JwtService` gera e valida tokens HMAC-SHA256; `JwtAuthenticationFilter` protege rotas. |
| `/auth/register` | Atendido | `POST /auth/register` e alias `POST /api/auth/register`. |
| `/auth/login` | Atendido | `POST /auth/login` e alias `POST /api/auth/login`, retorna token JWT. |
| `/auth/logout` | Atendido | `POST /auth/logout` e alias `POST /api/auth/logout`, invalida token em blacklist em memoria. |
| Swagger/OpenAPI | Atendido | `springdoc-openapi`, Swagger UI em `/swagger-ui.html`. |
| NGINX balanceador | Atendido como configuracao | `nginx/nginx.conf` e `docker-compose.nginx.yml` com `api-1`, `api-2` e `nginx`. |

## Pendencias de entrega que nao sao codigo

| Item do PDF | Status | O que falta fazer |
| --- | --- | --- |
| Documento de especificacao da API em PDF ou link | Pendente de documento final | Exportar Swagger/OpenAPI ou montar PDF com dominio, integrantes, entidades, relacionamentos e tabela de rotas. |
| Relatorio comparativo JMeter com e sem balanceamento | Pendente de execucao | Rodar testes contra `http://localhost:8080` sem balanceamento e contra NGINX com `docker compose -f docker-compose.nginx.yml up --build`; gerar graficos/prints e PDF. |
| Evidencia de testes funcionais | Pendente de evidencias | Testar rotas no Postman/Thunder Client/Insomnia/JMeter e anexar prints ou exportacao da colecao. |
| Repositorio Git | Pendente no workspace atual | A raiz analisada nao esta em um repositorio Git. Inicializar/publicar se for requisito da entrega. |
| Video de apresentacao | Pendente externo | Gravar demonstracao do backend e dos testes ate 15 minutos. |

## Como subir com balanceamento

```powershell
docker compose -f docker-compose.nginx.yml up --build
```

Depois acesse:

- API via NGINX: `http://localhost:8080`
- Health check: `http://localhost:8080/health`
- Swagger: `http://localhost:8080/swagger-ui.html`

## Fluxo minimo para teste funcional

1. Criar conta em `POST /auth/register`.
2. Fazer login em `POST /auth/login`.
3. Copiar o token retornado.
4. Chamar rotas protegidas com header `Authorization: Bearer <token>`.
5. Encerrar sessao em `POST /auth/logout` com o mesmo header.
