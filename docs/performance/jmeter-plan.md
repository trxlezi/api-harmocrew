# Plano de testes com JMeter

## Objetivo

Comparar o comportamento da API HarmoCrew em dois cenarios:

- Cenario A: API sem balanceamento, usando `docker-compose.yml`.
- Cenario B: API com balanceamento via NGINX, usando `docker-compose.nginx.yml`.

Este documento define como executar os testes e quais metricas coletar. Ele nao contem resultados.

## Pre-requisitos

- Docker Desktop instalado e rodando.
- Java instalado.
- Apache JMeter instalado.
- Projeto atualizado localmente.
- Portas `5432` e `8080` livres antes de subir os containers.

## Cenario A: sem balanceamento

Neste cenario, o Docker Compose sobe PostgreSQL e uma instancia da API.

Comandos:

```powershell
docker compose down
docker compose up --build
```

URL base:

```text
http://localhost:8080
```

Validacao antes do JMeter:

```powershell
Invoke-WebRequest http://localhost:8080/health
```

## Cenario B: com NGINX

Neste cenario, o Docker Compose sobe PostgreSQL, duas instancias da API e NGINX.

Comandos:

```powershell
docker compose -f docker-compose.nginx.yml down
docker compose -f docker-compose.nginx.yml up --build
```

URL base via NGINX:

```text
http://localhost:8080
```

Validacao antes do JMeter:

```powershell
Invoke-WebRequest http://localhost:8080/health
```

## Endpoints testados

Endpoints publicos sugeridos para a primeira comparacao:

- `GET /health`
- `GET /v3/api-docs`

Esses endpoints nao exigem token e evitam dependencia de dados cadastrados.

Endpoints autenticados podem ser adicionados em uma etapa posterior, usando `POST /auth/register`, `POST /auth/login` e header `Authorization: Bearer <token>`.

## Carga sugerida

Use a mesma configuracao nos dois cenarios.

Sugestao inicial:

- Usuarios virtuais: `50`
- Ramp-up: `30` segundos
- Loops: `10`

Sugestao intermediaria:

- Usuarios virtuais: `100`
- Ramp-up: `60` segundos
- Loops: `10`

Sugestao forte:

- Usuarios virtuais: `200`
- Ramp-up: `120` segundos
- Loops: `10`

Evite aumentar a carga antes de confirmar que o ambiente local esta estavel.

## Metricas a coletar

Coletar no JMeter:

- Samples
- Average response time
- Median response time
- 90th percentile
- 95th percentile
- 99th percentile
- Min
- Max
- Throughput
- Error percentage
- Received KB/sec
- Sent KB/sec

Coletar tambem:

- Uso de CPU e memoria pelo Docker Desktop.
- Logs da API.
- Logs do NGINX no cenario B.

## Como executar no JMeter

Arquivo sugerido:

```text
docs/performance/harmocrew-performance-test.jmx
```

No modo grafico:

1. Abrir o Apache JMeter.
2. Abrir o arquivo `.jmx`.
3. Conferir as variaveis `host`, `port`, `threads`, `rampUp` e `loops`.
4. Executar o teste.
5. Exportar os resultados.

No modo linha de comando:

```powershell
jmeter -n -t docs/performance/harmocrew-performance-test.jmx -l docs/performance/results-sem-nginx.jtl -e -o docs/performance/report-sem-nginx
```

Para o cenario com NGINX:

```powershell
jmeter -n -t docs/performance/harmocrew-performance-test.jmx -l docs/performance/results-com-nginx.jtl -e -o docs/performance/report-com-nginx
```

## Como exportar resultados

No JMeter:

- Salvar o arquivo `.jtl` de cada execucao.
- Gerar dashboard HTML com `-e -o`.
- Tirar prints dos principais graficos/tabelas.

Nomes sugeridos:

- `results-sem-nginx.jtl`
- `results-com-nginx.jtl`
- `report-sem-nginx/`
- `report-com-nginx/`

Esses arquivos podem ser grandes; antes de commitar, avaliar se devem ficar fora do Git.

## Como comparar os cenarios

Comparar:

- Tempo medio de resposta.
- Percentis 90/95/99.
- Throughput.
- Percentual de erro.
- Estabilidade dos tempos ao longo do teste.
- Logs de erro.

A comparacao deve usar a mesma carga nos dois cenarios. Nao misturar resultados de cargas diferentes na mesma tabela.
