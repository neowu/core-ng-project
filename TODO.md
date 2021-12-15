### ideas
* template, use ByteBuffer[] for performance?
* mongo: collect mongo stats thru JMX ConnectionPoolStatisticsMBean?
* mongo: update entity decoder to use switch(fieldName) to replace if statement? (optimization)
* framework error (queue listener, background task error, custom scheduler trigger) forward to kafka?
* change gradle to kotlin?
* impl own json bind by referring https://github.com/json-iterator/java and https://github.com/ngs-doo/dsl-json with annotation processor?

* /_sys/, kafka controller, should call message handler directly?
* revisit trace log truncation, better/more elegant handling?

* elasticsearch libs has too many duplicated namespaces with module-info.java enabled

* redis using ping/pong to validate connection? for zero downtime upgrading e.g. with gcloud memory store
* websocket, replace AbstractReceiveListener, so it can include IO/error handling within action lifecycle?

* provide general support/framework for backend-test/regression-test/load-test?

* monitor: alert on kafka message api_changed? always publish api, not only for frontend?
* action visualization to show service flow/dependency diagram?

* use adminClient to check kafka ready? or retry sending message?
* retry on message handler? like when mysql is in maintenance 
