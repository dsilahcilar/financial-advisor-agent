#!/usr/bin/env bash

export AGENT_APPLICATION=$(dirname "$0")/..
export SPRING_PROFILES_ACTIVE=shell,starwars,docker-desktop

$(dirname "$0")/support/agent.sh
