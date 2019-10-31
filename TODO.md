### ideas
* cache: advanced feature: local/remote 2 level, async get on expiration, pre warm? ben-manes/caffeine
* redis: support cluster? or just use https://github.com/twitter/twemproxy
* inject: remove method inject support? 
* template, use ByteBuffer[] for performance?
* mongo: collect mongo stats thru JMX ConnectionPoolStatisticsMBean?
* mongo: update entity decoder to use switch(fieldName) to replace if statement? (optimization)
* cache: hit rate report? cluster support? envoy?
* framework error (queue listener, background task error, customer scheduler trigger) forward to kafka?
* streaming, data pipeline? (kafka stream/beam/google dataflow) 

* ws: provide way to handler json command with validation?
* change gradle to kotlin?
* impl own json bind by referring https://github.com/json-iterator/java and https://github.com/ngs-doo/dsl-json with annotation processor?
* rethink module structure to fit java module export requirement
* run diagnostic when heap usage is high, send trace, log().alert() ?

* refine web body bean, revisit cache/validation impl 
* /_sys/, kafka controller, should be calling message handler directly?
* revisit trace log truncation, better/more elegant handling?
* support LocalTime in db?
* db/redis diagnostic controller/stats?
* ws: on connect not allow set session? different sessionImpl?

### jdk 12 issues
* spotbugs: OBL bug https://github.com/spotbugs/spotbugs/issues/432  
* elasticsearch libs has too many duplicated namespaces with module-info.java enabled
* class in interface module generates Validator make interface depends on core-ng module (java module)
* JDK HTTPClient issue: https://bugs.openjdk.java.net/browse/JDK-8211437

### kube deployment + http client keep alive issue, reference
https://github.com/kubernetes/contrib/issues/1123
https://trac.nginx.org/nginx/ticket/1022
