#!/bin/sh
set -e
set -x

gem install heroku -V
heroku apps:destroy --app play-authenticate --confirm play-authenticate
heroku apps:create play-authenticate
heroku addons:create heroku-postgresql:hobby-dev --app play-authenticate
