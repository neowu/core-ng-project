## Change Log In Wonder

### 1.1.0 (12/13/2021)

* corresponds to upstream version **7.8.0**.
* es: update to 7.14.0
* log-processor: kibana 7.14 added duration human precise formatter, updated all time fields of index pattern
  > must update kibana/es to 7.14 to use this version of log-processor
* api: always publish /_sys/api, for internal api change monitoring
* api: added /_sys/api/message to publish message definition, for future message change monitoring
* error: refactored ErrorResponse and AJAXErrorResponse, !!! changed ErrorResponse json field from "error_code" to "errorCode"
  > !!! for consistency, it breaks contract, it's transparent if both client/server upgrade to same framework version, ErrorResponse only be used when client is from coreng
  > if client uses old version of framework, RemoteServiceException will not have errorCode
  > ErrorResponse is renamed to InternalErrorResponse, AJAXErrorResponse is renamed to ErrorResponse
* api: changed system property "sys.publishAPI.allowCIDR" to "sys.api.allowCIDR", !!! update sys.properties if this is used

### 1.0.5 (12/13/2021)

* corresponds to upstream version **7.7.5**.
* mongo: updated driver to 4.3.0
* action: added ActionLogContext.remainingProcessTime() for max time left for current action, to control external calling timeout or future get with timeout
* http: update undertow to 2.2.9

### 1.0.4 (12/13/2021)

* corresponds to upstream version **7.7.4**.
* site: added Session.timeout(Duration), to allow application set different timeout
  > e.g. use cases are like longer mobile app session expiration time, or "remember me" feature
* http: disabled okHTTP builtin retryOnConnectionFailure, use RetryInterceptor to log all connection failures explicitly, more failure case handling

### 1.0.3 (12/13/2021)

* corresponds to upstream version **7.7.3**.
* log: fixed error when third-party lib calls slf4f logger with empty var args (Azure SDK)
* executor: updated way to print canceled tasks during shutdown (with JDK16, it cannot access private field of internal JDK classes)
* log-processor: add first version of action flow diagram, pass actionId to visualize entire action flow with all related _id and correlation_id
  > e.g. https://localhost:8443/diagram/action?actionId=7A356AA3B1A5C6740794

### 1.0.2 (12/13/2021)

* corresponds to upstream version **7.7.2**.
* monitor: fixed overflowed vmRSS value, use long instead of int
* api: added "app" in APIDefinitionResponse
* monitor: api config json schema changed !!!
  > changed from map to List<ServiceURL>, to simplify config. it requires the latest framework, refers to above
    ```json
        {
          "api": {
            "services": ["https://website", "https://backoffice"]
          } 
        }
    ```

### 1.0.1 (12/13/2021)

* corresponds to upstream version **7.7.1**.
* log-processor/kibana: added http server/client dashboard and visualizations (http / dns / conn / reties / delays)
* http: added "action.stats.http_delay" to track time between http request start to action start (time spent on HTTPIOHandler)
  added action.stats.request_body_length/action.stat.response_body_length to track
* httpClient: track request/response body length thru perf_stats.http.write_entries/read_entries
* db: fixed insertIgnore reports write_entries wrong, mysql 8.0 return SUCCESS_NO_INFO (-2) if insert succeeds

### 1.0.0 (12/13/2021)

* corresponds to upstream version **7.7.0**.
* api: replaced /_sys/api, to expose more structured api info
  > one purpose is to create api monitoring, to alert if api breaks backward compatibility
  > !!! for ts client code generator, refer to https://github.com/neowu/frontend-demo-project/blob/master/website-frontend/webpack/api.js
* redis: support pop with multiple items
  > !!! only be supported since redis 6.2, use latest redis docker image if you use this feature
  > pop without count still uses old protocol, so it's optional to upgrade redis
* monitor: support to monitor api changes, alert config json schema changed !!!
  > refer to ext/monitor/src/test/resources/monitor.json, ext/monitor/src/test/resources/alert.json for example config
  > !!! alert config -> channels changed, to support one channel with multiple matchers, e.g.
  ```json 
    "notifications": [
        {"channel": "backendWarnChannel", "matcher": {"severity": "WARN", "indices": ["trace", "stat"]}},
        {"channel": "backendErrorChannel", "matcher": {"severity": "ERROR", "indices": ["trace", "stat"]}},
        {"channel": "frontendWarnChannel", "matcher": {"severity": "WARN", "indices": ["event"]}},
        {"channel": "frontendWarnChannel", "matcher": {"severity": "WARN", "errorCodes": ["API_CHANGED"], "indices": ["stat"]}},
        {"channel": "frontendErrorChannel", "matcher": {"severity": "ERROR", "indices": ["event"]}},
        {"channel": "frontendErrorChannel", "matcher": {"severity": "ERROR", "errorCodes": ["API_CHANGED"], "indices": ["stat"]}},
        {"channel": "additionalErrorCodeChannel", "matcher": {"apps": ["product-service"], "errorCodes": ["PRODUCT_ERROR"]}}
    ]
  ```
* log-processor: updated kibana objects to be compatible with kibana 7.12.0, rebuild objects with kibana object builder
  > refer to core-ng-demo-project/kibana-generator

* es: update to 7.13.0, updated ElasticSearch.putIndexTemplate impl to use new PutComposableIndexTemplateRequest
  > !!! refer to https://www.elastic.co/guide/en/elasticsearch/reference/current/index-templates.html
  > must update index template format to match new API, refer to ext/log-processor/src/main/resources/index/action-index-template.json as example