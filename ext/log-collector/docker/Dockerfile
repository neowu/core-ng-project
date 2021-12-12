FROM        adoptopenjdk/openjdk16:alpine-jre
LABEL       app=log-collector
# install /lib/ld-linux-x86-64.so.2 for native lib, e.g. kafka/snappy
RUN         apk add --no-cache gcompat
RUN         addgroup --system app && adduser --system --no-create-home --ingroup app app
USER        app
COPY        package/dependency     /opt/app
COPY        package/app            /opt/app
CMD         ["/opt/app/bin/log-collector"]
