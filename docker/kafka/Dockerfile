FROM        neowu/kafka:2.8.0
ENV         KAFKA_CLUSTER_ID=""
ADD         conf/kraft/server.properties /opt/kafka/config/kraft/
ENTRYPOINT  ["/bin/bash", "-c", "/opt/kafka/bin/kafka-storage.sh format -t ${KAFKA_CLUSTER_ID} -c /opt/kafka/config/kraft/server.properties; /opt/kafka/bin/kafka-server-start.sh /opt/kafka/config/kraft/server.properties ${KAFKA_ARGS}"]
