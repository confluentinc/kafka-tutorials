#!/usr/bin/env bash

set -e -u -o pipefail

# Create a Semaphore pipeline for test purposes
# Useful for limiting the set of individual jobs executed by the "Run the tests" block
#
# dependencies
# - yq
# - jq
#
# usage
# ./tools/create-test-pipeline.sh ksql "$(aws sts get-caller-identity | jq -r .Account).dkr.ecr.us-west-2.amazonaws.com" us-west-2 > .semaphore/semaphore.yml

filter_job_regex=$1
docker_registry=$2
docker_registry_region=$3

yq r "$(dirname $0)/../.semaphore/semaphore.yml" -j \
    | jq -r '.blocks[] |= select(.name=="Run the tests")' \
    | jq -r --arg expr $filter_job_regex '(.blocks[0].task.jobs) |= [.[] | select(.commands[0] | test($expr))]' \
    | yq w - fail_fast.stop.when true \
    | yq w - 'blocks[0].task.prologue[+]' "aws ecr get-login-password --region $docker_registry_region | docker login --username AWS --password-stdin $docker_registry" \
    | yq w - 'blocks[0].task.secrets[+]' aws_credentials \
    | yq d - promotions \
    | yq r --prettyPrint - 
