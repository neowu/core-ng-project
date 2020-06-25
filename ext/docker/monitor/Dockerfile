FROM        gradle:jdk14 as build
WORKDIR     /depot
ADD         ./ /depot
RUN         gradle -Penv=prod :ext:monitor:docker

FROM        adoptopenjdk/openjdk14:alpine-jre
LABEL       app=monitor
RUN         addgroup --system app && adduser --system --no-create-home --ingroup app app
USER        app
COPY        --from=build /depot/build/ext/monitor/docker/package/dependency    /opt/app
COPY        --from=build /depot/build/ext/monitor/docker/package/app           /opt/app
ENTRYPOINT  ["/opt/app/bin/monitor"]
