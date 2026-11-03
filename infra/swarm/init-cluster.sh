#!/bin/sh
set -eu

compose_file="infra/swarm/docker-compose.cluster.yml"

docker compose -f "$compose_file" up -d

for node in harmocrew-manager harmocrew-worker-1 harmocrew-worker-2; do
  attempts=0
  until docker exec "$node" docker info >/dev/null 2>&1; do
    attempts=$((attempts + 1))
    if [ "$attempts" -ge 30 ]; then
      echo "Docker daemon de $node nao ficou pronto a tempo." >&2
      exit 1
    fi
    sleep 1
  done
done

manager_ip="$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' harmocrew-manager)"

if ! docker exec harmocrew-manager docker info --format '{{.Swarm.LocalNodeState}}' | grep -q active; then
  docker exec harmocrew-manager docker swarm init --advertise-addr "$manager_ip"
fi

worker_token="$(docker exec harmocrew-manager docker swarm join-token -q worker)"

for worker in harmocrew-worker-1 harmocrew-worker-2; do
  if ! docker exec "$worker" docker info --format '{{.Swarm.LocalNodeState}}' | grep -q active; then
    docker exec "$worker" docker swarm join --token "$worker_token" "$manager_ip:2377"
  fi
done

docker exec harmocrew-manager docker node ls
