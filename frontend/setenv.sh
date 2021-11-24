#!/bin/sh

# This file is for setting system properties in Tomcat so that the tiramisu frontend can connect to the database.
# It should be placed in the Tomcat /bin folder before starting server

export JAVA_OPTS="$JAVA_OPTS -DRDS_HOSTNAME="
export JAVA_OPTS="$JAVA_OPTS -DRDS_DB_NAME="
export JAVA_OPTS="$JAVA_OPTS -DRDS_USERNAME="
export JAVA_OPTS="$JAVA_OPTS -DRDS_PASSWORD="
export JAVA_OPTS="$JAVA_OPTS -DRDS_PORT="
export JAVA_OPTS="$JAVA_OPTS -DdriverClassName=org.postgresql.Driver"
export JAVA_OPTS="$JAVA_OPTS -DvalidationQuery=$VALIDATION_QUERY"
export JAVA_OPTS="$JAVA_OPTS -DmaxActive=30"
export JAVA_OPTS="$JAVA_OPTS -DmaxIdle=10"
export JAVA_OPTS="$JAVA_OPTS -DminIdle=5"
export JAVA_OPTS="$JAVA_OPTS -DinitialSize=5"
export JAVA_OPTS="$JAVA_OPTS -DmaxWait=5000"
export JAVA_OPTS="$JAVA_OPTS -DtestWhileIdle=true"
export JAVA_OPTS="$JAVA_OPTS -DremoveAbandoned=true"
export JAVA_OPTS="$JAVA_OPTS -DlogAbandoned=true"
export JAVA_OPTS="$JAVA_OPTS -DOBA_API_KEY=TEST"
export JAVA_OPTS="$JAVA_OPTS -DOBA_URL=http://localhost:8080/oba/api/where/"
export JAVA_OPTS="$JAVA_OPTS -DPREDICT_URL="
export JAVA_OPTS="$JAVA_OPTS -DROUTE_PATH="
export JAVA_OPTS="$JAVA_OPTS -DINOUT_PATH="
