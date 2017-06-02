#!/usr/bin/env bash
docker rm -f local-redis
docker run --name local-redis -p 6379:6379 --rm redis