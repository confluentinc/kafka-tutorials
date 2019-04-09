#!/usr/bin/env bash

set -o errexit
set -o pipefail
set -o nounset
set -o xtrace

cd "$(dirname "$0")/.."

gradle shadowJar
docker build -t io.confluent.developer/kstreams-filter:0.0.1 .
