#!/bin/sh
set -eu

export DOCKER_HOST="tcp://127.0.0.1:2375"

docker build -t registry:5000/harmocrew-api:latest .
docker push registry:5000/harmocrew-api:latest
docker stack deploy --with-registry-auth -c infra/swarm/stack.yml harmocrew
docker stack services harmocrew
