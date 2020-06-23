FROM        gradle:jdk14 as build
WORKDIR     /depot
ADD         ./ /depot
RUN         gradle -Penv=prod :ext:log-collector:docker

FROM        adoptopenjdk/openjdk14:alpine-jre
LABEL       app=log-collector
RUN         addgroup --system app && adduser --system --no-create-home --ingroup app app
USER        app
COPY        --from=build /depot/build/ext/log-collector/distributions/docker/dependency    /opt/app
COPY        --from=build /depot/build/ext/log-collector/distributions/docker/app           /opt/app
ENTRYPOINT  ["/opt/app/bin/log-collector"]
