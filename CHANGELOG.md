## Change log

### 3.3.8 (8/11/2015 - 8/12/2015)
* http server: interceptor ignore built-in controllers
* mongo: use LinkedHashMap to keep same order as in mongo
* log: draft of log forwarding

### 3.3.7 (8/11/2015)
* web: fix webservice controller inspection (getAnnotation/methodInfo)
* mongo: find supports orderby/skip/limit

### 3.3.6 (8/7/2015 - 8/10/2015)
* log: push action log to rabbitmq, index by ES/Kibana
* log: renamed action log requestId to id, and use refId for reference
* webService: WebContext can retrive request() to support web service
* rabbitmq: publish message with appId = -Dcore.appName
* webservice client: pass -Dcore.appName via "client" header

### 3.3.5 (8/6/2015)
* internal: expose web template manager for cms widget impl
* internal: renamed -Dcore.web to -Dcore.webPath, added -Dcore.appName, prepare for log aggregating

### 3.3.4 (8/5/2015)
* make default values to fit AWS medium/large instance, simplify env properties
* rabbitmq: make default user/password to rabbitmq/rabbitmq
* redis: make default pool size to (5,50)
* db: make default pool size to (5,50)

### 3.3.3 (8/4/2015)
* cache/session: update default redis pool size, (min=8,max=32) optimized to AWS medium/large instances.
* redis: print pool size info for slow query

### 3.3.2 (7/31/2015)
* updated c3p0 to 0.9.5.1 (all client must update c3p0 lib to use this version of core-ng)

  make c3p0 not use thread pool for checkin, we don't do test on checkin, this improves performance under high load

  dataSource.setForceSynchronousCheckins(true);

* updated mysql driver to 5.1.36 for db-migration

### 3.3.1 (7/30/2015)
* website: message support, site().message().messageProvider() is for custom message service

### 3.3.0 (7/29/2015)
* mongo: refactory, support fongo, minor API changes

### 3.2.9 (7/28/2015)
* queue: composite queue publisher
* queue: MessagePublisher.publish with routingKey, only support for RabbitMQ

### 3.2.8 (7/22/2015 － 7/23/2015)
* template: validate model class
* add TemplateEngine for general purpose
* module: renamed bind(supplier) to bindSupplier()
* template: "include" support
* queue: renamed MessagePublisher.reply to publish
* template: custom function support, prepare for #msg, #js, #css
* template: load from string

### 3.2.7 (7/21/2015 - 7/22/2015)
* db repository, added selectAll()
* template engine first draft impl, removed thymeleaf
* website static content

### 3.2.6 (7/17/2015 - 7/20/2015)
* refactory rabbitmq support
* update elasticsearch to 1.7

### 3.2.5 (7/16/2015)
* standardized validation exception and make validator throw validationException, refactory validator

### 3.2.4 (7/15/2015)
* fix: http response body validation pass empty list 
* databaseImpl, track available/total connections when getting conn from pool
* httpClient, allow server ssl cert change during renegotiation 

### 3.2.3 (7/14/2015)
* tune http client log info
* enhanced Files, create temp file, logging 

### 3.2.2 (7/13/2015)
* fix: api() client to pass "Accept: application/json" 
* replace URL/URLPath encoding with apache common codec
* tuned async task begin/end log message

### 3.2.1 (7/11/2015 - 7/13/2015)
* measure startup time
* measure time on test db schema creation

### 3.2.0 (7/9/2015)
* update gradle to 2.5
* update build gradle to publish to s3 directly

### 3.1.9 (7/8/2015)
* fix: api webservice client encode path param
* renamed StandardAppModule to SystemModule, and added jdbc pool properties

### 3.1.8 (6/30/2015)
* fix: requestURL(), parse x-forwarded-port to get requested port  
  
### 3.1.7.1 (6/30/2015)
* fix: test hsql db map BigDecimal to DECIMAL(10,2)

### 3.1.7 (6/26/2015 - 6/29/2015)
* refactory config structure to simplify test context
* refactory class validation into general one with spec
* WebContext to pass from interceptor

### 3.1.6 (6/25/2015)
* updated web customErrorHandler, moved it to http() and in top level of Module
* add requiredProperty() method in module

### 3.1.5 (6/24/2015)
* async task support, inject AsyncExecutor
* webservice support List as request/response, for channel-advisor and also for us 

### 3.1.4 (6/22/2015 - 6/23/2015)
* removed module.loadYML(), use YAML.load(ClasspathResources.text()) instead, for db init data, use YAML loadlist then call repository.batchInsert
* improve web service interface validator, better error message for bean param/path param missing @PathParam 
* Cache management controllers (list names/clear key) 

### 3.1.3 (6/22/2015)
* move repository() into db() and register db entity class to view. 

### 3.1.2 (6/22/2015)
* fix: webservice interface validator should allow String as @PathParam

### 3.1.1 (6/18/2015 - 6/22/2015)
* db added timeout for checkout conn and query, default is 60s
* update ElasticSearch API according to ES 1.6.0
* bind with supplier to support for expensive or external dependent object, test context can override by not calling supplier.get()
* mark trace in ActionLogContext, write trace log for all subsequent actions (apiCall/messages)
* create Application.configure()/start(), to make core-ng be used as script for testing/scripting 
* trace external calling time and count, e.g. db, redis

### 3.1.0 (6/15/2015)
* update trace log path

### 3.0.9 (6/15/2015)
* cache value type validator
* refactory cache impl
* Cache error handle, fault tolerant 

### 3.0.8
* database batchInsert/Delete
* httpClient supports download bytes
* webservice client

### 3.0.7
* support test initDB(), runscript and createSchema

### 3.0.6 (6/6/2015 - 6/8/2015)
* update rabbitmq/db config

### 3.0.5 (6/5/2015)
* refactory validator
* refactory module/config(testConfig)/builder/impl structure
* refactory session provider, local session cleanup
* added rabbitmq subscriber and listener

### 3.0.4 (6/4/2015)
* refactory module config, session changes to site().session()
* added core-ng-api for interface module
* updated row mapper, use code generation, updated Row interface

### 3.0.3 (6/3/2015)
* added core-ng-test support

### 3.0.2 (6/3/2015)
* YML supports type

### 3.0.1 (6/2/2015)
* support to add cache without name
* cache management controller
* add repository support