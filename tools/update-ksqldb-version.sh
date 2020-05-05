#!/usr/bin/env bash

set -e -u -o pipefail

# Update ksqlDB images in docker-compose.yml files
#
# dependencies
# - awscli
#
# usage
# ./tools/update-ksqldb-version.sh 0.9.0-rc287 "$(aws sts get-caller-identity | jq -r .Account).dkr.ecr.us-west-2.amazonaws.com"
# ./tools/update-ksqldb-version.sh 0.8.1

version=$1
registry=${2:-}
repo_prefix=

if [[ -n "$registry" ]]; then
    repo_prefix="$registry\/confluentinc\/"
else
    repo_prefix="confluentinc\/"
fi

cd "$(dirname $0)/.."

# both the xargs and sed incanations may need to be changed for non-macOS contexts
find _includes/tutorials/**/ksql -name docker-compose.yml \
    | xargs -I {} sed -i '' -E "s/image:.*confluentinc\/(ksqldb.+):.+/image: $repo_prefix\1:$version/g" {}
