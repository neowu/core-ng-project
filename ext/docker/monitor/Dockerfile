FROM        adoptopenjdk/openjdk14:alpine as build
WORKDIR     /depot
ADD         ./ /depot
RUN         ./gradlew -Penv=prod :ext:monitor:installDist

FROM        adoptopenjdk/openjdk14:alpine-jre
LABEL       app=monitor
RUN         addgroup --system app && adduser --system --no-create-home --ingroup app app
USER        app
COPY        --from=build /depot/build/ext/monitor/install/monitor /opt/monitor
ENTRYPOINT  ["/opt/monitor/bin/monitor"]
