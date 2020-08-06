### ideas
* template, use ByteBuffer[] for performance?
* mongo: collect mongo stats thru JMX ConnectionPoolStatisticsMBean?
* mongo: update entity decoder to use switch(fieldName) to replace if statement? (optimization)
* framework error (queue listener, background task error, custom scheduler trigger) forward to kafka?

* change gradle to kotlin?
* impl own json bind by referring https://github.com/json-iterator/java and https://github.com/ngs-doo/dsl-json with annotation processor?
* rethink module structure to fit java module export requirement

* /_sys/, kafka controller, should call message handler directly?
* revisit trace log truncation, better/more elegant handling?
* write unit test support to check break compatibility of API/DB?
* redis: support zset / resp3 ? 

### jdk 14 issues
* spotbugs: OBL bug https://github.com/spotbugs/spotbugs/issues/432  
* elasticsearch libs has too many duplicated namespaces with module-info.java enabled
* class in interface module generates Validator make interface depends on core-ng module (java module)
