FROM        eclipse-temurin:17-alpine as build
RUN         jlink --add-modules java.base,java.compiler,java.desktop,java.instrument,java.logging,java.security.jgss,java.security.sasl,java.sql,java.xml \
                  --add-modules jdk.charsets,jdk.crypto.cryptoki,jdk.dynalink,jdk.jdi,jdk.localedata,jdk.management.jfr,jdk.net,jdk.unsupported \
                  --output /opt/jre --strip-java-debug-attributes --no-man-pages --no-header-files --compress=2

FROM        alpine:latest
ENV         JAVA_HOME=/opt/jre PATH=/opt/jre/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
COPY        --from=build /opt/jre /opt/jre
