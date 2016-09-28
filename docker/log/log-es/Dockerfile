FROM        elasticsearch
MAINTAINER  neo
RUN         set -x \
                && /usr/share/elasticsearch/bin/plugin install royrusso/elasticsearch-HQ
COPY        conf/elasticsearch.yml /etc/elasticsearch/elasticsearch.yml
