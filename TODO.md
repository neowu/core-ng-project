### ideas

* template, use ByteBuffer[] for performance?

* impl own json bind by referring https://github.com/json-iterator/java and https://github.com/ngs-doo/dsl-json with annotation processor?

* framework error (queue listener, background task error, custom scheduler trigger) forward to kafka?
* use adminClient to check kafka ready? or retry sending message?

* kafka: is static membership (group.instance.id) useful within stateful set?

* db: update "on duplicated key" values() syntax,
  > The use of VALUES() to refer to the new row and columns is deprecated beginning with MySQL 8.0.20, and is subject to removal in a future version of MySQL.

* support sse with POST?

* track if undertow improved leak with virtual threads
  https://github.com/undertow-io/undertow/blob/main/core/src/main/java/io/undertow/server/DefaultByteBufferPool.java

* track okHTTP fixed pin issue with virtual threads (looks like needs to wait until 5.0.0 released)
  https://github.com/square/okhttp/issues/8284
* switch httpclient to jdk built in after jdk 24?, then send goaway frame in ShutdownHandler
  https://bugs.openjdk.org/browse/JDK-8335181

* log diagram, fix d3 tooltip (generate separated json, and make d3 tooltip show other non-HTML info)

* redis using ping/pong to validate connection? for zero downtime upgrading e.g. with gcloud memory store
* migrate to dragonflydb and support RESP3 (cluster / MOVED handling) ?
* migrate to opensearch ?
* log exporter, save in parquet format, or reimplement in rust?
