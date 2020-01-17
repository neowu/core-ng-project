FROM        adoptopenjdk/openjdk13:alpine as build
WORKDIR     /depot
ADD         ./ /depot
RUN         ./gradlew -Penv=prod :ext:log-collector:installDist

FROM        adoptopenjdk/openjdk13:alpine-jre
LABEL       app=log-collector
RUN         addgroup --system app && adduser --system --no-create-home --ingroup app app
USER        app
COPY        --from=build /depot/build/ext/log-collector/install/log-collector /opt/log-collector
ENTRYPOINT  ["/opt/log-collector/bin/log-collector"]
