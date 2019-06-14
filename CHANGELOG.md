## Change log
### 6.12.8 (6/12/2019 - )
* log-collector: stripe on parsing app.allowedOrigins config 
* log: add sessionHash in ActionLogContext to improve data analytic

### 6.12.7 (5/23/2019 - 6/6/2019)
* search: update es to 7.1.1
* classpath/properties: check if there are multiple resources with same name in different jars (within same classapth)
* kafka: update to 2.2.1

### 6.12.6 (5/20/2019 - 5/21/2019)
* http: update okHTTP to 3.14.2
* search: (bug) ElasticSearchMigration throws exception on failure

### 6.12.5 (5/7/2019 - 5/13/2019)
* log-processor: support sys.log.appender to configure whether index log-processor action logs, by default it's empty (to configure by env in kube)
        console -> output to console
        elasticsearch -> index directly to elasticsearch
* search: update es to 7.0.1        
* cache: throw exception if loader returns null when called cache.get(key, loader)
* site: not set csp by default, in case application may generate it dynamically, (and even with static setting, it's set by sys.properties explicitly anyway)

### 6.12.4 (4/17/2019 - 4/25/2019)
* mongo: fix: unit test to support multiple test mongo servers on different ports
* search: fix: elasticsearch local test conflicts with mongo java server
* json: explicitly defined ZonedDateTime/LocalDateTime format, to make it more strict to comply with ES and js, (put 3 digits for nano fraction)

### 6.12.3 (4/9/2019 - 4/17/2019)
* http: update okHTTP to 3.14.1
* search: update es to 7.0.0, change index.flush to index.refresh (those 2 are different in ES)
* sys: sys.properties supports allowCIDRs in 2 formats, 1) cidr,cidr 2) name1: cidr, cidr; name2: cidr

### 6.12.2 (4/2/2019 - 4/9/2019)
* http: update undertow to 2.0.20
* search: update es to 6.7.1
* test: added assertEnumClass(class1).hasAllConstantsOf(class2)
* http: update http().bean() to accept only one class (for simplification), support register enum
* http: revert okHTTP to 3.12.2 due to https://github.com/square/okhttp/issues/4875
* log: tweak trace log, still attempt to show warning if reached max trace length (to limit overall trace within 900k)

### 6.12.1 (4/1/2019)
* kafka: fix kafka producer metrics can be NaN

### 6.12.0 (3/18/2019 - 4/1/2019)
* log: added log-collector to collect event from JS
* api: RemoteServiceException uses original error message 
    e.g. website->svc1->svc2, website will be able to display error message from svc2
* kafka: update to 2.2.0
* http: update okHTTP to 3.14.0
* search: update es to 6.7.0   
    !!! ES is forcing one index can only have one type (and naming it as "_doc"), you may need to rebuild index when upgrading to latest, refer to https://github.com/elastic/elasticsearch/pull/38270

### 6.11.2 (3/6/2019 - 3/15/2019)
* json: introduced Bean class to (de)serialize json with strict class type and bean validation, keep JSON loose to adapt any classes
* redis: support hash increaseBy
* cache: validate value from cache to prevent stale data

### 6.11.1 (2/8/2019 - 2/27/2019)   !!! this version has behavior and config API changes
* db: not allowing db entity with default value, due to it may cause undesired behavior on partialUpdate, ("new entity() -> change fields -> updatePartial(entity)" will update those fields with default value since they are not null)
* cache: stop supporting value and List<T> (considered as bad practice, better wrap with class for maintenance)        
         removed to register Cache<T> with name in CacheConfig, as it's generally for value type
* validator: simplify validator, use field name as part of error key (actual use cases use JS to validate anyway, if there is actual need, may add @Label to customize the error key)
* http: body bean must be registered via http().bean(), this only applies to beans used by raw controllers directly 
        e.g.
        http().route(POST, "/ajax", bind(AJAXController.class)::ajax);
        http().bean(RequestBean.class, ResponseBean.class);  
* search: update ES to 6.6.1
* db: close connection if statement is already closed due to previous error        

### 6.10.10 (1/16/2019 - 1/31/2019)
* mongo: support enum as map key to be consistent with other subsystem
* json: JSON.fromJSON/toJSON added validation for app beans
* search: update ES to 6.6.0

### 6.10.9 (1/7/2019 - 1/16/2019)
* cookies: support SameSite attribute, and make SessionId cookie SameSite to prevent CSRF
* bean: support enum as Map key (mongo document is not supported yet) 

### 6.10.8 (1/2/2019 - 1/4/2019)
* executor: use "TASK_REJECTED" error code when rejecting task during shutdown
* session: always try to save session even if on exception flow, in case of invalidate session or pass generated session id
