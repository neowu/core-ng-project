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
* kafka: unique groupId to listen all messages for ws
* type literal: support better cast for generic, JSON.fromJSON() / bind?
* change gradle to kotlin?
* impl own json bind by referring https://github.com/json-iterator/java and https://github.com/ngs-doo/dsl-json with annotation processor?
* rethink module structure to fit java module export requirement
* run diagnostic when heap usage is high, send trace, log().alert() ?
* JSON.from/to add validation?
* specify app_name by ENV to allow deploy multiple same service as different name

### jdk 9/10/11 issues
* spotbugs: OBL bug https://github.com/spotbugs/spotbugs/issues/432  
* elasticsearch libs has too many duplicated namespaces with module-info.java enabled
* class in interface module generates Validator make interface depends on core-ng module (java module)
* undertow http/2.0 has bug under jdk 11, causing http client keep reading from channel/frame (impact JDK 11 http client / OKHTTP works fine (probably because it cleanup connection pool after keep alive timeout))
* undertow has bugs with h2c protocol, from test, not all the ExchangeCompletionListener will be executed which make ShutdownHandler not working properly
  and cause GOAWAY frame / EOF read issue with small UndertowOptions.NO_REQUEST_TIMEOUT (e.g. 10ms)
* jdk httpclient doesn't work with http response without content-length, now we have to let Response.empty return 200
  https://bugs.openjdk.java.net/browse/JDK-8211437  

### kube deployment + http client keep alive issue, reference
https://github.com/kubernetes/contrib/issues/1123
https://trac.nginx.org/nginx/ticket/1022
