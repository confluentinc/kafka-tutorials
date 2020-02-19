#!/bin/bash

function readiness_probe {
  /usr/bin/nc -z -w 2 0.0.0.0 8081
}

echo "Waiting for the SR to become available ..."

until readiness_probe
do
  sleep 5
done

echo "SR port available"
