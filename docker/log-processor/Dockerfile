FROM        adoptopenjdk/openjdk13:alpine as build
WORKDIR     /depot
ADD         ./ /depot
RUN         ./gradlew -Penv=prod :log-processor:installDist

FROM        adoptopenjdk/openjdk13:alpine-jre
LABEL       app=log-processor
RUN         addgroup --system app && adduser --system --no-create-home --ingroup app app
USER        app
COPY        --from=build /depot/build/log-processor/install/log-processor /opt/log-processor
ENTRYPOINT  ["/opt/log-processor/bin/log-processor"]
