### ideas

* template, use ByteBuffer[] for performance?
* mongo: update entity decoder to use switch(fieldName) to replace if statement? (optimization)
* framework error (queue listener, background task error, custom scheduler trigger) forward to kafka?
* change gradle to kotlin? (it's worse if not use plugins block)
  > refer to https://docs.gradle.org/current/userguide/kotlin_dsl.html#sec:kotlin_using_standard_api)
* impl own json bind by referring https://github.com/json-iterator/java and https://github.com/ngs-doo/dsl-json with annotation processor?

* redis using ping/pong to validate connection? for zero downtime upgrading e.g. with gcloud memory store
* websocket, replace AbstractReceiveListener, so it can include IO/error handling within action lifecycle?

* use adminClient to check kafka ready? or retry sending message?
* mongo: monitor thru mongo command, db.stats()?
