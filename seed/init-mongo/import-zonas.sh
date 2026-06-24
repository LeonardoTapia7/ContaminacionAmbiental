#!/bin/bash
set -e
echo "=== MongoDB init script: importing zonas.json ==="
# The official mongo image executes scripts in /docker-entrypoint-initdb.d only
# when the database is initialized for the first time (i.e. when the data volume is empty).
# mongoimport binary is available in the image, so we can call it directly.

if [ -f /docker-entrypoint-initdb.d/zonas.json ]; then
  echo "Found zonas.json, importing into contaminacion.zonas"
  mongoimport --db contaminacion --collection zonas --drop --jsonArray --file /docker-entrypoint-initdb.d/zonas.json || true
  echo "Import finished"
else
  echo "zonas.json not found in /docker-entrypoint-initdb.d"
fi

