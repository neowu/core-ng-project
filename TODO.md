### ideas

* kafka: is static membership (group.instance.id) useful within stateful set?
  > static membership can avoid rebalance when pod restart

* switch httpclient to jdk built in after jdk 24?, then send goaway frame in ShutdownHandler
  https://bugs.openjdk.org/browse/JDK-8335181

* redis using ping/pong to validate connection? for zero downtime upgrading e.g. with gcloud memory store
* support hsql postgres dialect, to test postgres specific sql?

* with jspecify / spotbugs NPE checking, rethink interface design, avoid null if possible?
