FROM        neowu/jre:24.0.1
LABEL       app=monitor
RUN         addgroup --gid 1000 --system app && adduser --system --no-create-home --ingroup app --uid 1000 app
USER        1000
COPY        package/dependency     /opt/app
COPY        package/app            /opt/app
CMD         ["/opt/app/bin/monitor"]
