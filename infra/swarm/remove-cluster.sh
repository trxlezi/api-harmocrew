#!/bin/sh
set -eu

DOCKER_HOST=tcp://127.0.0.1:2375 docker stack rm harmocrew 2>/dev/null || true

for worker in harmocrew-worker-1 harmocrew-worker-2; do
  docker exec "$worker" docker swarm leave 2>/dev/null || true
done

docker exec harmocrew-manager docker swarm leave --force 2>/dev/null || true
docker compose -f infra/swarm/docker-compose.cluster.yml down
