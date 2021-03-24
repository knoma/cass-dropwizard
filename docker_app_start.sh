#!/bin/sh
###################################################################################################################################
# This script is used by the docker container to start the application inside the container. This will not start the application. #
###################################################################################################################################

env

java ${APP_JVM_OPTIONS} \
    -jar ${APP_JAR} server config.yml
