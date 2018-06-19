#!/bin/bash

. .env

HOST=${DOCKER_PGHOST:-$HOST}
PGPASSWORD=$PASSWORDADMIN psql -h $HOST -p $PORT -U $ADMINUSER -d $DATABASE -f sql/testdata.sql
