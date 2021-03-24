FROM adoptopenjdk/openjdk11

ARG DEPLOY_HOME="/opt/app"

ENV DEPLOY_HOME=$DEPLOY_HOME
ENV APP_JVM_OPTIONS="-Xms1g -Xmx1g -XX:+DisableExplicitGC"
ENV APP_JAR=cass-dropwizard-rest.jar


# Make deployment directory
RUN mkdir -p -m 775 $DEPLOY_HOME

#Copy jar
COPY build/libs/cass-dropwizard-rest.jar $DEPLOY_HOME/cass-dropwizard-rest.jar


#Copy config yaml
COPY config.yml $DEPLOY_HOME/config.yml

#Copy startup script
COPY docker_app_start.sh $DEPLOY_HOME/docker_app_start.sh

#Make startup script executable
RUN chmod +x $DEPLOY_HOME/docker_app_start.sh

EXPOSE 9000 9001

WORKDIR /opt/app

ENTRYPOINT ["sh","-x","docker_app_start.sh"]
