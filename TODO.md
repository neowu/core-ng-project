### ideas
* redis: support cluster, https://github.com/RedisLabs/redis-cluster-proxy / https://github.com/twitter/twemproxy
* inject: remove method inject support? 
* template, use ByteBuffer[] for performance?
* mongo: collect mongo stats thru JMX ConnectionPoolStatisticsMBean?
* mongo: update entity decoder to use switch(fieldName) to replace if statement? (optimization)
* cache: hit rate report? cluster support? envoy? or use managed memorystore on large scale?
* framework error (queue listener, background task error, customer scheduler trigger) forward to kafka?

* change gradle to kotlin?
* impl own json bind by referring https://github.com/json-iterator/java and https://github.com/ngs-doo/dsl-json with annotation processor?
* rethink module structure to fit java module export requirement

* refine web body bean, revisit cache/validation impl 
* /_sys/, kafka controller, should be calling message handler directly?
* revisit trace log truncation, better/more elegant handling?
* monitoring, es only need to put one host to get all nodes info of cluster, update monitor config

### jdk 12 issues
* spotbugs: OBL bug https://github.com/spotbugs/spotbugs/issues/432  
* elasticsearch libs has too many duplicated namespaces with module-info.java enabled
* class in interface module generates Validator make interface depends on core-ng module (java module)
* JDK HTTPClient issue: https://bugs.openjdk.java.net/browse/JDK-8211437

### kube deployment + http client keep alive issue, reference
https://github.com/kubernetes/contrib/issues/1123
https://trac.nginx.org/nginx/ticket/1022
