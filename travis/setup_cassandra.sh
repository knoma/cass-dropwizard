#!/bin/bash
function cassandra_ready() {
  count=0
  while ! cqlsh -e "describe cluster;" 2>&1; do
    echo "waiting for cassandra"
    if [ $count -gt 30 ]; then
      exit
    fi
    ((count += 1))
    sleep 1
  done
  echo "cassandra is ready"
}

cassandra_ready
pwd
cqlsh -f src/main/resources/cql/db.cql
