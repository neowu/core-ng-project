FROM        eclipse-temurin:17.0.3_7-jdk-alpine as build
RUN         jlink --add-modules java.base,java.compiler,java.desktop,java.instrument,java.logging,java.management.rmi,java.naming,java.security.jgss,java.security.sasl,java.sql,java.xml \
                  --add-modules jdk.charsets,jdk.crypto.cryptoki,jdk.jdi,jdk.localedata,jdk.management.jfr,jdk.naming.dns,jdk.naming.rmi,jdk.net,jdk.unsupported \
                  --output /opt/jre --strip-java-debug-attributes --no-man-pages --no-header-files --compress=2

FROM        alpine:latest
# install /lib/ld-linux-x86-64.so.2 for native lib, e.g. kafka/snappy
RUN         apk add --no-cache gcompat
ENV         JAVA_HOME=/opt/jre PATH=/opt/jre/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
COPY        --from=build /opt/jre /opt/jre
