#!/bin/sh
set -e

echo "Delete app"
curl -X DELETE https://api.heroku.com/apps/play-authenticate \
-H "Accept: application/vnd.heroku+json; version=3" \
-H "Authorization: Bearer $HEROKU_API_KEY"

echo "Create app"
curl -X POST https://api.heroku.com/apps \
-H "Accept: application/vnd.heroku+json; version=3" \
-H "Authorization: Bearer $HEROKU_API_KEY" \
-H "Content-Type: application/json" \
-d '{
    "name": "play-authenticate"
}'

echo "Attach postgres addon"
curl -n -X POST https://api.heroku.com/addon-attachments \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $HEROKU_API_KEY" \
-H "Accept: application/vnd.heroku+json; version=3" \
-d '{
  "addon": "heroku-postgresql:dev",
  "app": "play-authenticate",
  "name": "DATABASE"
}'
