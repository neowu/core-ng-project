FROM        openjdk:jre-alpine
MAINTAINER  neo
ARG         KAFKA_VERSION=1.0.0
ARG         SCALA_VERSION=2.12
ENV         KAFKA_ARGS=
RUN         apk add --no-cache curl bash \
                && mkdir -p /opt \
                && curl -SL http://www.us.apache.org/dist/kafka/${KAFKA_VERSION}/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz | tar xzf - -C /opt \
                && ln -s /opt/kafka_${SCALA_VERSION}-${KAFKA_VERSION} /opt/kafka
ADD         conf/server.properties /opt/kafka/config/
EXPOSE      9092
VOLUME      /data
ENTRYPOINT  ["/bin/sh", "-c", "/opt/kafka/bin/kafka-server-start.sh /opt/kafka/config/server.properties ${KAFKA_ARGS}"]
