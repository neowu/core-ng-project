### ideas
* template, use ByteBuffer[] for performance?
* mongo: collect mongo stats thru JMX ConnectionPoolStatisticsMBean?
* mongo: update entity decoder to use switch(fieldName) to replace if statement? (optimization)
* framework error (queue listener, background task error, custom scheduler trigger) forward to kafka?

* change gradle to kotlin?
* impl own json bind by referring https://github.com/json-iterator/java and https://github.com/ngs-doo/dsl-json with annotation processor?

* /_sys/, kafka controller, should call message handler directly?
* revisit trace log truncation, better/more elegant handling?
* monitor to collect api/db definition overtime and compare to check backaward compability?
* way to expose service structure/dependency diagram?

* elasticsearch libs has too many duplicated namespaces with module-info.java enabled
* class in interface module generates Validator make interface depends on core-ng module (java module)

* redis using ping/pong to validate connection? for zero downtime upgrading e.g. with gcloud memory store
