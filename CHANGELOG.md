## Change log

### 3.2.0 (7/9/2015)
* update gradle to 2.5
* update build gradle to publish to s3 directly

### 3.1.9 (7/8/2015)
* fix api webservice client encode path param
* renamed StandardAppModule to SystemModule, and added jdbc pool properties

### 3.1.8 (6/30/2015)
* fix requestURL(), parse x-forwarded-port to get requested port  
  
### 3.1.7.1 (6/30/2015)
* fix test hsql db map BigDecimal to DECIMAL(10,2)

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
* fix webservice interface validator should allow String as @PathParam

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