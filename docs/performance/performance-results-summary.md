# Resumo dos Testes de Performance - HarmoCrew API

## Objetivo

Comparar a execucao da API sem balanceamento e com balanceamento via NGINX.

## Ambiente

- Sistema operacional: Microsoft Windows 10 Pro
- Java: 25.0.3 LTS
- Docker: 29.5.3
- Docker Compose: v5.1.4
- JMeter: 5.6.3
- Spring Boot: 4.0.6
- Banco: PostgreSQL 16 Alpine em Docker
- Cenarios executados:
  - Sem NGINX: PostgreSQL + 1 instancia da API
  - Com NGINX: PostgreSQL + 2 instancias da API + NGINX

## Diagnostico do erro inicial

O arquivo antigo `results-sem-nginx.jtl`, arquivado em `docs/performance/backup-20260615-154942/`, registrava 100 erros em 1000 amostras, ou 10%.

As falhas ocorreram nos samplers `GET /health` e `GET /v3/api-docs`, com:

- `responseCode`: `Non HTTP response code: org.apache.http.NoHttpResponseException`
- `responseMessage`: `Non HTTP response message: localhost:8080 failed to respond`
- `failureMessage`: assertion esperava codigo `200`, mas recebeu erro de conexao sem resposta HTTP

A causa provavel e que o JMeter iniciou enquanto o servico em `localhost:8080` ainda estava indisponivel, reiniciando ou trocando entre cenarios. O reteste limpo foi feito apos derrubar os ambientes, subir apenas um cenario por vez e validar `/health` e `/v3/api-docs` antes da carga. No reteste, o erro nao se repetiu.

## Cenario A - Sem NGINX

- Comando usado para subir ambiente:

```powershell
docker compose up --build -d
```

- Comando JMeter usado:

```powershell
jmeter -n -t docs/performance/harmocrew-performance-test.jmx -l docs/performance/results-sem-nginx.jtl -e -o docs/performance/report-sem-nginx
```

- Endpoints testados:
  - `GET /health`
  - `GET /v3/api-docs`

- Total de requisicoes: 1000
- Sucessos: 1000
- Erros: 0
- Taxa de erro: 0%
- Tempo medio: 3.27 ms
- Mediana: 3 ms
- Minimo: 1 ms
- Maximo: 31 ms
- P90: 5 ms
- P95: 6 ms
- P99: 10 ms
- Throughput aproximado: 34.06 req/s
- Duracao aproximada: 29.36 s

Metricas por endpoint:

| Endpoint | Requisicoes | Erros | Media (ms) | Mediana (ms) | P90 (ms) | P95 (ms) | Max (ms) |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| `GET /health` | 500 | 0 | 2.60 | 2 | 4 | 4 | 31 |
| `GET /v3/api-docs` | 500 | 0 | 3.94 | 4 | 6 | 8 | 14 |

Observacoes:

- O cenario sem NGINX respondeu sem erros apos validacao previa do ambiente.
- Este cenario tem menos camadas de rede, pois o cliente acessa a API diretamente pela porta `8080`.

## Cenario B - Com NGINX

- Comando usado para subir ambiente:

```powershell
docker compose -f docker-compose.nginx.yml up --build -d
```

- Comando JMeter usado:

```powershell
jmeter -n -t docs/performance/harmocrew-performance-test.jmx -l docs/performance/results-com-nginx.jtl -e -o docs/performance/report-com-nginx
```

- Endpoints testados:
  - `GET /health`
  - `GET /v3/api-docs`

- Total de requisicoes: 1000
- Sucessos: 1000
- Erros: 0
- Taxa de erro: 0%
- Tempo medio: 52.57 ms
- Mediana: 19 ms
- Minimo: 2 ms
- Maximo: 4074 ms
- P90: 72 ms
- P95: 115 ms
- P99: 511 ms
- Throughput aproximado: 34.72 req/s
- Duracao aproximada: 28.80 s

Metricas por endpoint:

| Endpoint | Requisicoes | Erros | Media (ms) | Mediana (ms) | P90 (ms) | P95 (ms) | Max (ms) |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| `GET /health` | 500 | 0 | 43.23 | 15 | 58 | 105 | 2286 |
| `GET /v3/api-docs` | 500 | 0 | 61.92 | 25 | 78 | 117 | 4074 |

Observacoes:

- O cenario com NGINX tambem respondeu sem erros.
- A media e os percentis ficaram maiores que no cenario direto, provavelmente pelo custo adicional do proxy e por picos de resposta durante a execucao.
- O throughput ficou semelhante ao cenario sem NGINX.

## Comparacao

| Cenario | Requisicoes | Erros | Taxa de erro | Media (ms) | Mediana (ms) | P90 (ms) | P95 (ms) | P99 (ms) | Max (ms) | Throughput |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Sem NGINX | 1000 | 0 | 0% | 3.27 | 3 | 5 | 6 | 10 | 31 | 34.06 req/s |
| Com NGINX | 1000 | 0 | 0% | 52.57 | 19 | 72 | 115 | 511 | 4074 | 34.72 req/s |

Na carga usada, os dois cenarios ficaram estaveis em relacao a erros. O cenario sem NGINX teve menor latencia, enquanto o cenario com NGINX manteve throughput aproximado semelhante e validou o balanceamento entre duas instancias.

O papel do NGINX neste teste foi atuar como proxy reverso e balanceador, recebendo as requisicoes em `localhost:8080` e distribuindo para `api-1:8080` e `api-2:8080` dentro da rede Docker.

## Conclusao

O cenario sem NGINX apresentou menor tempo de resposta. O cenario com NGINX apresentou maior latencia, mas tambem finalizou com 0% de erro e throughput semelhante, mostrando que o balanceamento funcionou para a carga testada.

Os resultados atuais estao prontos para serem usados no relatorio academico, desde que acompanhados da observacao de que a comparacao foi feita em ambiente local e com endpoints publicos simples.

## Evidencias geradas

- `docs/performance/results-sem-nginx.jtl`
- `docs/performance/report-sem-nginx/index.html`
- `docs/performance/results-com-nginx.jtl`
- `docs/performance/report-com-nginx/index.html`
- Backup dos resultados antigos: `docs/performance/backup-20260615-154942/`

Prints recomendados:

- Dashboard HTML sem NGINX: pagina inicial e tabela de estatisticas.
- Dashboard HTML com NGINX: pagina inicial e tabela de estatisticas.
- `docker compose ps` do cenario sem NGINX, se quiser evidenciar os containers.
- `docker compose -f docker-compose.nginx.yml ps` do cenario com NGINX.
