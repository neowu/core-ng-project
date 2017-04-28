FROM        openjdk:jre-alpine
MAINTAINER  neo
EXPOSE      8080
ADD         file/log-processor.tar    /opt
ENTRYPOINT  ["/bin/sh", "-c", "/opt/log-processor/bin/log-processor"]
