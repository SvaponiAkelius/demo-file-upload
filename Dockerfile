FROM adoptopenjdk/openjdk11-openj9:alpine-slim

# install utils
RUN apk add --no-cache \
    bash \
    lsof \
    curl \
    iputils \
    nmap \
    tcpdump \
    jq

# expose server port
EXPOSE 8080

# local variables (visible only while building the image)
ARG USER=app
ARG GROUP=app

# create application user and group
RUN addgroup -S $GROUP
RUN adduser -S $USER -G $GROUP

# set current dir to $USER home
WORKDIR /home/$USER

# define aliases
COPY Dockerfile.bashrc .bashrc

# copy application artifact
COPY target/*.jar server.jar

# change user (remember: this does not change the current dir, only WORKDIR does)
USER $USER

# notes:
# - server.jar is visible from the current dir (because of WORKDIR /home/$USER)
# - JAVA_OPTIONS may contain multiple args, do not wrap in ""
ENTRYPOINT java $JAVA_OPTIONS -jar -Dserver.port=8080 server.jar

# use this for debug (image stays alive without starting the server)
# ENTRYPOINT ["/bin/bash", "-c", "while true; do sleep 2; done"]
