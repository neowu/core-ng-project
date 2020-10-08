# replace with gradle image once gradle:jdk15 is available
FROM        adoptopenjdk/openjdk15:alpine as build
WORKDIR     /depot
ADD         ./ /depot
RUN         ./gradlew -Penv=prod :ext:monitor:docker

FROM        adoptopenjdk/openjdk15:alpine-jre
LABEL       app=monitor
RUN         addgroup --system app && adduser --system --no-create-home --ingroup app app
USER        app
COPY        --from=build /depot/build/ext/monitor/docker/package/dependency    /opt/app
COPY        --from=build /depot/build/ext/monitor/docker/package/app           /opt/app
ENTRYPOINT  ["/opt/app/bin/monitor"]
