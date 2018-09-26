### ideas
* cache: advanced feature: local/remote 2 level, async get on expiration, pre warm? ben-manes/caffeine
* redis: support cluster? or just use https://github.com/twitter/twemproxy
* inject: remove method inject support? 
* template, use ByteBuffer[] for performance?
* mongo: collect mongo stats thru JMX ConnectionPoolStatisticsMBean?
* mongo: update entity decoder to use switch(fieldName) to replace if statement? (optimization)
* cache: hit rate report?
* framework error (queue listener, background task error, customer scheduler trigger) forward to kafka?
* streaming, data pipeline? (kafka stream/beam/google dataflow) 

* http: switch to java 11 http client, not set userAgent for api client (in httperrorhandler, check userAgent==null to determine ajax or rest) 
* ws: provide way to handler json command with validation?
* kafka: unique groupId to listen all messages for ws
* type literal: support better cast for generic, JSON.fromJSON() / bind?

### jdk 9/10 incompatible list
* gradle: gradle doesn't support java module as first class yet, https://guides.gradle.org/building-java-9-modules/
* spotbugs: OBL bug https://github.com/spotbugs/spotbugs/issues/432  
* elasticsearch libs has too many duplicated namespaces with module-info.java enabled
* class in interface module generates Validator make interface depends on core-ng module (java module)
