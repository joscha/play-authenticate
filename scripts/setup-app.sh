#!/bin/sh

curl -X POST https://api.heroku.com/apps \
-H "Accept: application/vnd.heroku+json; version=3" \
-H "Authorization: Bearer $HEROKU_API_KEY" \
-H "Content-Type: application/json" \
-d "{\"name\":\"play-authenticate\"}"

curl -X DELETE https://api.heroku.com/apps/play-authenticate \
-H "Accept: application/vnd.heroku+json; version=3" \
-H "Authorization: Bearer $HEROKU_API_KEY"
