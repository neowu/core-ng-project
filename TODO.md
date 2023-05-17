### ideas

* template, use ByteBuffer[] for performance?

* change gradle to kotlin? (it's worse if not use plugins block)
  > refer to https://docs.gradle.org/current/userguide/kotlin_dsl.html#sec:kotlin_using_standard_api)
* impl own json bind by referring https://github.com/json-iterator/java and https://github.com/ngs-doo/dsl-json with annotation processor?

* framework error (queue listener, background task error, custom scheduler trigger) forward to kafka?
* redis using ping/pong to validate connection? for zero downtime upgrading e.g. with gcloud memory store
* use adminClient to check kafka ready? or retry sending message?

* kafka: is static membership (group.instance.id) useful within stateful set?

* log: use es data stream + ILM to rotate index? or time series data stream (TSDS) useful (only for metrics data)
  > not able to close index, only delete, and can simplify log processor and ES interface (no need to support "index" param in all requests)
  > to use TSDS, convert statMessage into pure metrics, and make error/info into action?
  > https://www.elastic.co/guide/en/elasticsearch//reference/current/tsds.html

* db: update "on duplicated key" values() syntax,
  > The use of VALUES() to refer to the new row and columns is deprecated beginning with MySQL 8.0.20, and is subject to removal in a future version of MySQL.
