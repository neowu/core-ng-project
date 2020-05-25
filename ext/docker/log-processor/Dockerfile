FROM        gradle:jdk14 as build
WORKDIR     /depot
ADD         ./ /depot
RUN         gradle -Penv=prod :ext:log-processor:installDist

FROM        adoptopenjdk/openjdk14:alpine-jre
LABEL       app=log-processor
RUN         addgroup --system app && adduser --system --no-create-home --ingroup app app
USER        app
COPY        --from=build /depot/build/ext/log-processor/install/log-processor /opt/log-processor
ENTRYPOINT  ["/opt/log-processor/bin/log-processor"]
