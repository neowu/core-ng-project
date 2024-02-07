FROM        neowu/jre:21.0.2
LABEL       app=log-exporter
RUN         apk add --no-cache python3 curl
# gcloud iam requires user home, so must not put --no-create-home
RUN         addgroup --gid 1000 --system app && adduser --system --ingroup app --uid 1000 app
RUN         curl -SL https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-cli-459.0.0-linux-x86_64.tar.gz | tar xzf - -C /opt \
                && ln -s /opt/google-cloud-sdk/bin/gsutil /usr/local/bin/gsutil \
                && mkdir -p /var/log/app && chown -R app.app /var/log/app
VOLUME      /var/log/app
USER        1000
COPY        package/dependency     /opt/app
COPY        package/app            /opt/app
CMD         ["/opt/app/bin/log-exporter"]
