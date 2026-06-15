# Relatorio de teste de performance

## Objetivo

Comparar a API HarmoCrew sem balanceamento e com balanceamento via NGINX.

## Ambiente

- Data:
- Maquina:
- Sistema operacional:
- CPU:
- Memoria:
- Docker:
- Java:
- JMeter:
- Branch/commit testado:

## Configuracao do teste

- Usuarios virtuais:
- Ramp-up:
- Loops:
- Duracao aproximada:
- Endpoints testados:

## Cenario A: sem balanceamento

Comando usado:

```powershell
docker compose up --build
```

URL base:

```text
http://localhost:8080
```

Observacoes:

- 

## Cenario B: com NGINX

Comando usado:

```powershell
docker compose -f docker-compose.nginx.yml up --build
```

URL base:

```text
http://localhost:8080
```

Observacoes:

- 

## Tabela de resultados

| Cenario | Samples | Erros (%) | Media (ms) | Mediana (ms) | P90 (ms) | P95 (ms) | P99 (ms) | Min (ms) | Max (ms) | Throughput |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Sem NGINX | 1000 | 0 | 3.27 | 3 | 5 | 6 | 10 | 1 | 31 | 34.06 req/s |
| Com NGINX | 1000 | 0 | 52.57 | 19 | 72 | 115 | 511 | 2 | 4074 | 34.72 req/s |

Dados reais detalhados: `docs/performance/performance-results-summary.md`.

## Comparacao

- Tempo medio:
- Percentis:
- Throughput:
- Erros:
- Estabilidade:

## Conclusao

Preencher apos executar os testes.

## Prints

Adicionar prints do JMeter ou dashboard HTML:

- Sumario sem NGINX:
- Sumario com NGINX:
- Grafico sem NGINX:
- Grafico com NGINX:
