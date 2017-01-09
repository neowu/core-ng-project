FROM        openjdk:jre
MAINTAINER  neo
ENV         KAFKA_VERSION=0.10.1.1 SCALA_VERSION=2.11
ADD         http://www.us.apache.org/dist/kafka/${KAFKA_VERSION}/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz /tmp/
RUN         mkdir -p /opt \
                && tar xfz /tmp/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz -C /opt \
                && rm /tmp/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz \
                && ln -s /opt/kafka_${SCALA_VERSION}-${KAFKA_VERSION} /opt/kafka
ADD         conf/server.properties /opt/kafka/config/
EXPOSE      9092
ENTRYPOINT  ["/opt/kafka/bin/kafka-server-start.sh", "/opt/kafka/config/server.properties"]