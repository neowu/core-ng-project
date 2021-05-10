FROM        gradle:jdk16 as build
WORKDIR     /depot
ADD         ./ /depot
RUN         gradle -Penv=prod :ext:log-processor:docker

FROM        adoptopenjdk/openjdk16:alpine-jre
LABEL       app=log-processor
# install /lib/ld-linux-x86-64.so.2 for native lib, e.g. kafka/snappy
RUN         apk add --no-cache gcompat
RUN         addgroup --system app && adduser --system --no-create-home --ingroup app app
USER        app
COPY        --from=build /depot/build/ext/log-processor/docker/package/dependency    /opt/app
COPY        --from=build /depot/build/ext/log-processor/docker/package/app           /opt/app
ENTRYPOINT  ["/opt/app/bin/log-processor"]
