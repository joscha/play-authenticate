#!/bin/sh
set -e
set -x

heroku apps:destroy --app play-authenticate --confirm play-authenticate
heroku apps:create play-authenticate
heroku addons:create heroku-postgresql:hobby-dev --app play-authenticate
