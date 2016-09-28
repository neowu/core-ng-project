FROM        openjdk
MAINTAINER  neo
EXPOSE      8080
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8080/health-check || exit 1
ENV         JAVA_OPTS="-XX:+UseG1GC -Xms256m -Xmx2048m -Xss256k -Djava.awt.headless=true"
ADD         file/log-processor.tar    /opt
ENTRYPOINT  ["/bin/bash", "-c", "/opt/log-processor/bin/log-processor"]
