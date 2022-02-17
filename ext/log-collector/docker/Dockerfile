FROM        neowu/jre:17.0.2
LABEL       app=log-collector
RUN         addgroup --system app && adduser --system --no-create-home --ingroup app app
USER        app
COPY        package/dependency     /opt/app
COPY        package/app            /opt/app
CMD         ["/opt/app/bin/log-collector"]
