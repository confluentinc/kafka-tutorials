#!/usr/bin/env bash

set -o errexit
set -o pipefail
set -o nounset
set -o xtrace

bundle exec jekyll clean
JEKYLL_ENV=production bundle exec jekyll build

aws s3 sync _site s3://mdrogalis-64b49dab-1657-4914-a0d0-339eafd2328d
