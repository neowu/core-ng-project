FROM        gradle:jdk15 as build
WORKDIR     /depot
ADD         ./ /depot
RUN         gradle -Penv=prod :ext:log-processor:docker

FROM        adoptopenjdk/openjdk15:alpine-jre
LABEL       app=log-processor
RUN         addgroup --system app && adduser --system --no-create-home --ingroup app app
USER        app
COPY        --from=build /depot/build/ext/log-processor/docker/package/dependency    /opt/app
COPY        --from=build /depot/build/ext/log-processor/docker/package/app           /opt/app
ENTRYPOINT  ["/opt/app/bin/log-processor"]
