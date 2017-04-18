#!/usr/bin/env bash
set -eu
set -o pipefail

heroku apps:destroy --app play-authenticate --confirm play-authenticate
heroku apps:create play-authenticate
heroku addons:create heroku-postgresql:hobby-dev --app play-authenticate
