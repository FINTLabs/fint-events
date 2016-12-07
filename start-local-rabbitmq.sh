#!/usr/bin/env bash
docker run --name local-rabbitmq -p 15672:15672 -p 5672:5672 --rm rabbitmq:3-management