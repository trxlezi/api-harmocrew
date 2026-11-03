# HarmoCrew: Docker, Compose e Swarm

## 1. Definicao do projeto escolhido

O HarmoCrew e uma plataforma academica de colaboracao musical. Artistas podem
criar perfis, publicar projetos, descobrir oportunidades, candidatar-se e trocar
mensagens. Para esta disciplina, o recorte demonstrado e o backend REST em Java
21 com Spring Boot e PostgreSQL. O aplicativo Flutter do repositorio `harmoCrew`
atua como cliente da API e nao precisa fazer parte do cluster de servidores.

## 2. Servicos da demonstracao

| Servico | Imagem | Papel |
| --- | --- | --- |
| API HarmoCrew | `registry:5000/harmocrew-api:latest` | Regras de negocio e API REST |
| PostgreSQL | `postgres:16-alpine` | Persistencia relacional |
| Visualizer | `dockersamples/visualizer:stable` | Representacao grafica do cluster |
| Registry | `registry:2` | Distribuicao local da imagem da API aos nos |
| Docker DinD | `docker:29-dind` | Um manager e dois workers isolados |

O Dockerfile da API usa multi-stage build: a primeira etapa compila com JDK 21
e Maven Wrapper; a segunda leva apenas o JRE e o JAR para a imagem final.

## 3. Infraestrutura como codigo

- `docker-compose.yml`: API e PostgreSQL para desenvolvimento local.
- `infra/swarm/docker-compose.cluster.yml`: cria o laboratorio com tres Docker
  Engines em containers e um registry local.
- `infra/swarm/stack.yml`: descreve os servicos implantados no Swarm, rede
  overlay, volumes, healthcheck, replicas e politicas de atualizacao.

## 4. Roteiro da demonstracao

Execute a partir da raiz de `api-harmocrew` em Linux ou WSL, com acesso ao
socket Docker. O laboratorio usa containers privilegiados por se tratar de uma
demonstracao local de Docker-in-Docker.

```bash
chmod +x infra/swarm/*.sh
./infra/swarm/init-cluster.sh
./infra/swarm/deploy.sh
```

Mostre os tres nos e os servicos:

```bash
DOCKER_HOST=tcp://127.0.0.1:2375 docker node ls
DOCKER_HOST=tcp://127.0.0.1:2375 docker stack services harmocrew
```

Abra:

- API/Swagger: <http://localhost:8080/swagger-ui.html>
- Health check: <http://localhost:8080/health>
- Docker Visualizer: <http://localhost:8081>

## 5. Variacao do numero de replicas

A stack inicia com duas replicas da API. Aumente para cinco e acompanhe a
distribuicao no Visualizer:

```bash
DOCKER_HOST=tcp://127.0.0.1:2375 docker service scale harmocrew_api=5
DOCKER_HOST=tcp://127.0.0.1:2375 docker service ps harmocrew_api
```

Depois reduza para uma replica e observe o Swarm remover as tarefas excedentes:

```bash
DOCKER_HOST=tcp://127.0.0.1:2375 docker service scale harmocrew_api=1
DOCKER_HOST=tcp://127.0.0.1:2375 docker service ps harmocrew_api
```

Explique que o routing mesh publica a porta 8080 do servico e balanceia as
requisicoes entre as replicas, independentemente do no em que cada tarefa roda.

## 6. Encerramento e limpeza

```bash
./infra/swarm/remove-cluster.sh
```

Para tambem apagar os volumes do laboratorio e os dados persistidos, use
explicitamente `docker compose -f infra/swarm/docker-compose.cluster.yml down -v`.
Esse comando nao faz parte do script porque remove dados.

## 7. Pontos para explicar ao professor

1. Imagens tornam o ambiente reproduzivel e o multi-stage reduz a imagem final.
2. Compose declara o laboratorio; Stack declara a aplicacao distribuida.
3. O manager mantem o estado desejado e os workers executam as tarefas.
4. A rede overlay conecta containers que estao em Docker Engines diferentes.
5. `replicas` representa o estado desejado; o Swarm reconcilia falhas e escala.
6. O Visualizer permite enxergar em qual no cada replica foi agendada.
7. PostgreSQL fica no manager nesta demonstracao para preservar a associacao do
   volume. Em producao, seria necessario armazenamento distribuido ou banco
   gerenciado, alem de secrets externos e alta disponibilidade do banco.
