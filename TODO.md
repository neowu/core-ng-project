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
* type literal: support better cast for generic, JSON.fromJSON() / bind?
* change gradle to kotlin?
* impl own json bind by referring https://github.com/json-iterator/java and https://github.com/ngs-doo/dsl-json with annotation processor?
* rethink module structure to fit java module export requirement
* run diagnostic when heap usage is high, send trace, log().alert() ?

* refine web body bean, revisit cache/validation impl 
* /_sys/, kafka controller, should be call message handler directly?
* revisit trace log truncation, better/more elegant handling?
* ControllerInspector: better way to deal with JDK12, add "--add-opens java.base/jdk.internal.reflect=ALL-UNNAMED" ?
* message publisher how to verify?

### jdk 11 issues
* spotbugs: OBL bug https://github.com/spotbugs/spotbugs/issues/432  
* elasticsearch libs has too many duplicated namespaces with module-info.java enabled
* class in interface module generates Validator make interface depends on core-ng module (java module)
* JDK TLSv1.3 issues: cause jdk httpclient run into infinite loop on concurrent condition  
  https://bugs.openjdk.java.net/browse/JDK-8213202  (fixed in openjdk 12)
  or set system env jdk.tls.acknowledgeCloseNotify=true  
* JDK HTTPClient issue: https://bugs.openjdk.java.net/browse/JDK-8211437

### kube deployment + http client keep alive issue, reference
https://github.com/kubernetes/contrib/issues/1123
https://trac.nginx.org/nginx/ticket/1022
