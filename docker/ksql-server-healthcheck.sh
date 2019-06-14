#!/usr/bin/env bash

if [[ $(curl -s -o /dev/null -w %{http_code} http://localhost:8088/info) = 200 ]]; then
  echo "Woohoo! KSQL Server is up!"
  exit 0
else 
  echo -e $(date) "\tKSQL Server HTTP state: " $(curl -s -o /dev/null -w %{http_code} http://localhost:8088/info) " (waiting for 200)"
  exit 1
fi
