FROM        openjdk
MAINTAINER  neo
EXPOSE      8080
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8080/health-check || exit 1
ENV         JAVA_OPTS="-XX:+UseG1GC -Xmx256m -Xss256k -Djava.awt.headless=true"
ADD         file/demo-site.tar    /opt
ENTRYPOINT  ["/bin/bash", "-c", "/opt/demo-site/bin/demo-site"]
