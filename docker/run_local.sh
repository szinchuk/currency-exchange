#!/bin/bash

docker-compose -p local -f docker-compose-local.yml pull
docker-compose -p local -f docker-compose-local.yml down --remove-orphans --rmi local --volumes
docker-compose -p local -f docker-compose-local.yml up --build --remove-orphans --abort-on-container-exit
