#!/bin/sh
set -e

MONGO_HOST=${MONGO_HOST:-mongo}
MONGO_PORT=${MONGO_PORT:-27017}

echo "Waiting for MongoDB at ${MONGO_HOST}:${MONGO_PORT}..."
retry=0
until nc -z "$MONGO_HOST" "$MONGO_PORT"; do
  retry=$((retry+1))
  if [ $retry -ge 60 ]; then
    echo "Timed out waiting for MongoDB after $retry attempts"
    exit 1
  fi
  sleep 1
done

echo "MongoDB is available - starting application"
exec java -jar /app/app.jar --spring.profiles.active=prod

