## Change log
### 6.9.2 (10/3/2018 - 10/8/2018)
* httpClient: tweak request/response to make it more low level style, 
* api: refined query param bean behavior, empty value will be treated as null, refer to core.framework.impl.web.bean.QueryParamMapper 
* management: added /_sys/vm, /_sys/thread, /_sys/heap to provide more detailed diagnostic info (similar result of jcmd) 

### 6.9.1 (10/3/2018)
* httpClient: fix bug on shouldRetry

### 6.9.0 (9/24/2018 - 10/2/2018)    !!! only support java 11 !!!
* jdk: update to java 11 
* log: internal tweaking, make logParam be aware of max param length, consume less memory with large trace log
* db: removed test initDB().runScript(), since in real life we always prefer to use repository(), as refactor/entity typing/validation reason, script can still be achieved by calling database directly 
* util: !!! renamed Strings.isEmpty to Strings.isBlank to match jdk 11
* validation: !!! renamed @NotEmpty to @NotBlank to match jdk 11 naming convention 
* httpClient: replaced apache httpClient with JDK builtin impl 

### 6.8.5 (9/20/2018 - 9/24/2018)   
* db: log params for batch operations, disable ssl for mysql
* log: adjust logger name abbr, only leave last 2 tokens for long package
* log: reduce trace log, only log thread/date once at beginning, replace timestamp with duration in nanos
* log-processor: removed id in action/trace, redundant with "_id"

### 6.8.4 (9/18/2018 - 9/20/2018)  !!! action log format changed, please use latest log-processor, docker: neowu/log-processor:6.8.4 !!! 
* http: not setting Date response header as it's not necessary
* util: delete some methods of Files, not too useful in cloud env 
* api: for api ts definition change LocalDateTime/LocalDate to string type
* log: renamed old refId to correlationId, and now refId is for directly reference action, bulk message handler now logs all clients/correlationIds/refIds 

### 6.8.3 (9/13/2018 - 9/18/2018)
* http: removed request.pathParam/request.queryParam, for type safety, we use api interface/impl, so only keep low level interface to keep flexibility 
* http: path param now only supports string/integer/long/enum
* kafka: bulk message handler logs all message in trace for troubleshooting 
* api: webservice client generates more efficient code for dynamic path pattern

### 6.8.2 (9/10/2018 - 9/13/2018)
* http: change framework bad request error code to "INVALID_HTTP_REQUEST" 
* db: changed db().defaultIsolationLevel() to db().isolationLevel(), and if specified, isolation level will be set on connection creation, rather than before every transaction started (to avoid unnecessary db operation)
* api: webservice client logs the calling method, tweak http client logging

### 6.8.1 (9/6/2018 - 9/10/2018)
* http: make ip access check as built in logic, to deny before routing check (which may return not found or method not allowed)
* api: added api().bean(beanClass) for raw request/response/ws bean
* util: removed Exceptions.error(), use new Error(Strings.format()) instead, which is more straightforward and code analyzer friendly (easier to check whether hide root cause) 

### 6.8.0 (9/5/2018 - 9/6/2018)  !!! api cleanup, remove replaceable method  !!!
* session: removed session.remove(key), just use session.set(key, null)
* redis: log returned value
* validator: log bean if failed to validate, to help troubleshooting
* http: removed module config, route(), replaced with http().route(method,path,controller)
* http: mask json response, beanBody/httpClient

### 6.7.1 (8/31/2018 - 9/5/2018)
* http: set tcp keep alive timeout to 620s, to adapt to both AWS ELB and gcloud LB
* http: move graceful shutdown logic into IO thread, and not count as action, before reading request 
* http: process health-check in IO thread        
* log-processor: fix index template should use strict_date_optional_time as date format
* ws: draft websocket impl
* api: tweak api client retry, shorten keep alive timeout, and retry on socketException (for connection reset)

### 6.7.0 (8/23/2018 - 8/31/2018)   !!! search API break changes !!!
* db: skip query.fetch() if limit is 0
* search: update to 6.4.0, 
    !!! removed @Index(index, type), replaced with @Index(name) as multiple types per index is deprecated and will be removed in ES 7.0
    switched to java high level client, which uses HTTP:9200
    removed DeleteByQuery, as no actual usage for now, can be substituted by ForEach if needed
* cache: support evict and put multiple keys, change keys to Collection<String>, since in real case, keys usually are constructed from List<String> or Set<String>
* kafka: removed /_sys kafka admin support, for actual cases, it's still better use kafka provided scripts

### 6.6.7 (8/20/2018 - 8/23/2018)
* log-processor: change index pattern to name-yyyy.MM.dd, to make clean index job works with metricbeats
* utils: deleted core.framework.util.Charsets, in favor of java.nio.charset.StandardCharsets (as IDE prompts)

### 6.6.6 (8/18/2018 - 8/20/2018)
* kafka: tweak producer, added request rate/size metrics, enable compression

### 6.6.5 (8/15/2018 - 8/17/2018)   !!! minor API name changes, compile should fix !!! 
* log: make log-processor leverage built-in kafak support, to index action as well
* redis,mongo,elasticserch: fix forEach elapsed time tracking 
* redis: replace setnx with set command

### 6.6.4 (8/9/2018 - 8/15/2018)
* kafka: update to 2.0.0
* redis: check values must not be empty in encoding
* log: remove track debug log to make trace log concise 

### 6.6.3 (8/9/2018 - 8/9/2018)
* redis: support List

### 6.6.2 (8/8/2018 - 8/8/2018)
* db: support batchExecute

### 6.6.1 (8/5/2018 - 8/7/2018)
* search: support update by script 
* http: update undertow to 2.0.11

### 6.6.0 (7/31/2018 - 8/5/2018)   !!! check break changes !!!
* db: !!! replaced repository.update with repository.partialUpdate(), repository.update will update all fields and field can be NULL 
* search: update to 6.3.2
* util: removed Lists.newArrayList(...values)/Sets.newHashSet(...values)/Maps.newHashMap(k,v) in favor of JDK 10 builtin List/Set/Map.of/copyOf
* html: !!! changed Message.get(key) to return String rather than Optional<String>, and throw error if key not existed, to make it more strict

### 6.5.2 (7/26/2018 - 7/30/2018)
* kafka: update to 1.1.1
* db: fix SQLParams may throw exception which cause action log failed to send

### 6.5.1 (7/7/2018 - 7/12/2018)
* template: support data url in html template
* validation: if bean has default value, it requires to put @NotNull (this may break, review all request/response, and run unit test to verify)
* web: query param mapper ignore not existed keys, in order to support default value in query bean  

### 6.5.0 (7/3/2018 - 7/4/2018)
due to removed support List<T> as request/response type
* web: request.bean must pass class type 
* validation: remove support to validate top level List<T> 
* validation: converted core.framework.impl.validate.ValidationException as internal exception, to treat as internal error, in application level, use BadRequestException instead
* webservice: validate response bean on both controller and webserviceClient 
* json: removed Optional<T> support and jackson dependency, as only use case of Optional<T> is responseType
* test: renamed methods in core.framework.test.Assertions, to avoid conflicting with assertj 

### 6.4.2 (6/28/2018 - 7/3/2018)
* site: !!! updated csp design, allow to specify entire csp to keep maximum flexibility, this is due to difficulty of external sdk integration, e.g. facebook, ga  
        use site().security().csp(csp) or sys.webSecurity.csp to configure, site().security() will enable rest headers without csp          
* sys: validate keys in sys.properties
* api: removed support List<T> as request/response due to security concerns

### 6.4.1 (6/26/2018 - 6/27/2018)
* api: added api().httpClient() to configure http client for api client, and potentially support local experiment code to call website/interface directly 
* api: retry on NoHttpResponseException with non-idempotent methods, trade off for availability/complexity, best result we can get with keep-alive + graceful shutdown + retry   

### 6.4.0 (6/24/2018 - 6/25/2018)
* session: !!! redis session key changed from "sessionId:{id}" to "sessionId:{sha256(id)}" for security reason, so the redis log won't show clear text session id value, 
        update lib will lose all existing user session, please deploy on scheduled time
* httpClient: added retry support, refer to core.framework.impl.http.RetryHandler for details
        make API client to retry (in kube env, persistent connection can be stale during deployment)

### 6.3.7 (6/21/2018 - 6/24/2018)
* api: make RemoteServiceException exposes https status
* site: make default error handler fits most of cases to reduce the need of creating custom error handler 
* make http server / scheduler / kafka listener / executor shutdown gracefully 
* httpClient: added DefaultServiceUnavailableRetryStrategy, enable evictIdleConnections 

### 6.3.6 (6/14/2018 - 6/21/2018)
* api: assert same service interface must not have duplicate method name
* search: update to 6.3.0, add analysis-common plugin for stemmer support during test

### 6.3.5 (6/13/2018 - 6/14/208)
* executor: support to submit void task as syntax sugar
* api: tweak enum generation to use JAVA_ENUM_NAME: "PROPERTY_VALUE" pattern

### 6.3.4 (6/4/2018 - 6/13/2018)
* db: added db().batchSize() to configure batch size on batchInsert and batchDelete
* db: enable rewriteBatchedStatements=true for MySQL
* api: retire old /_sys/api, promote v2
* test: replace EnumConversionValidator/EnvResourceValidator with assertJ extension, added validator assertions, refer to core.framework.test.Assertions

### 6.3.3 (5/28/2018 - 6/4/2018)
* api: make error message more friendly when service response returns value type
* api: add validation to prevent from using same simple class name for service interface and request/response bean, in order to improve maintainability and simplify API typescript definition generation  
* api: added /_sys/api/v2 for new version of api definition exposing (client js will read json and generate client code directly)
* db: fix sql params log should log @DBEnumValue value instead of enum.name()

### 6.3.2 (5/22/2018 - 5/28/2018)
* site: finalize csp design, make img-src supports data:, use sys.webSecurity.trustedSources to configure 
* http: update undertow to 2.0.9
* search: support auto complete

### 6.3.1 (5/16/2018 - 5/22/2018)
* site: update site.enableWebSecurity(String... trustedSources) to use CSP to replace x-frame-options since it's deprecated 
* executor: tweak use case when task submit another task to executor, to support async long polling or retry use cases 

### 6.3.0 (5/9/2018 - 5/15/2018)
* http: update undertow to 2.0.7
* api: make error message more friendly when service method param misses @PathParam
* config: make Config class stateful, use override style to configure during test
* search: !!! moved search to core-ng-search/core-ng-search-test modules, use config(SearchConfig.class)/config(InitSearchConfig.class) to configure, refer to log-processor gradle config for dependency config 
* mongo: !!! moved mongo to core-ng-mongo/core-ng-mong-test modules, use config(MongoConfig.class) to configure
* db: Query added fetchOne()

### 6.2.2 (5/3/2018)
* scheduler: log error when trigger returned invalid next execution time

### 6.2.0 (4/25/2018 - 5/1/2018)        !!! only support Java 10+
* thread: removed core.framework.util.Threads.availableProcessors, since java 10 supports cpu limits well in docker/kube
* db: update connection max idle timeout to 1 hour, to fit most scenario (e.g. IDC with firewall)  
* scheduler: removed secondly trigger, replaced with custom trigger to make it more flexible
* search: update es to 6.2.4 
* executor: !!! removed built-in Executor binding, please use executor().add(); in config to keep same behavior, 
            allow executor().add(name, poolSize) to create multiple pools

### 6.1.5 (4/19/2018)
* bug: fix site().publishAPI(cidrs) not setting cidr correctly

### 6.1.4 (4/16/2018 - 4/17/2018)
* kafka: added POST /_sys/kafka/topic/:topic/message/:messageId, to allow publish message thru internal management API
* bug: fix array param format 
* bug: fix multiple kafka management controller conflict  

### 6.1.2/6.1.3 (4/9/2018 - 4/16/2018)
* site: added publishAPI() / sys.site.publishAPI.allowCIDR to allow access /sys/_api from trusted network 
* kafka: update to 1.1.0, add management controller method to increase partition/delete records 
* search: update es to 6.2.3
* db: add query.project()

### 6.1.1 (4/4/2018 - 4/9/2018)
* action: simplify actionId naming scheme, since actionId doesn't need to be used in path anymore
          examples: action=api:patch:/ajax/product/:id, action=http:get:/, action=topic:some-topic, action=job:some-job  
* api: typescript definition generates string enum

### 6.1.0 (3/7/2018 - 4/4/2018)
* kafka: update to 1.0.1, add config to register publisher without topic (instead of passing null)
* http: update undertow to 2.0.3
* log: log masking redesign, only customer data from form param/json body require masking
       mask sessionId value in cookies log
       !!! removed CustomMessageFilter, replace with masked field to simplify usage 

### 6.0.0 (3/4/2018 - 3/6/2018)     !!! only support Java 9+
* jdk: drop java 8 support
* log: removed write action/trace to file, updated sys.properties log key to sys.log.appender
        sys.log.appender=console => write action/trace to console
        sys.log.appender=kafkaURI => forward log to kafka
       (in cloud env, console logging is preferred no matter it's docker or systemd/journald, or use log forwarding) 
* http: update undertow to 2.0.1

### 5.3.8 (2/28/2018 - 3/4/2018)    !!! 5.3.X is last version to support Java 8
* http: limit max requestURL length to 1000
* search: update es to 6.2.2

### 5.3.7 (2/24/2018 - 2/27/2018)
* http: not handling /health-check as action anymore, means /health-check will not be part of action log, to reduce noisy in kube env
* httpClient: removed HTTPRequest static method shortcut, use new HTTPRequest(method, uri) instead, to enforce consistent api style 
* api: tweak api generation

### 5.3.6 (2/14/2018 - 2/20/2018)
* redis: support increaseBy
* api: tweak api generation to fit client impl, refer to https://github.com/neowu/frontend-demo-project/blob/master/website-frontend-ts/src/service/user.ts as example

### 5.3.5 (2/12/2018 - 2/14/2018)
* inject: bind(object) will inject object, to make it easier to register bean with both manual wired and autowired dependencies
* properties: removed support of loading properties from file path, for kube we will using env/jvm argument overriding
* httpClient: add basic auth support
* search: update es to 6.2.1

### 5.3.4 (2/5/2018 - 2/11/2018)
* http: add ContentType.IMAGE_PNG constant, (e.g. used by captcha controller)
* api: change returned content type to javascript, make it easier to view by browser
* test: removed core.framework.test.EnvWebValidator, since all static file/content will be handled in frontend project, copy the impl to your own project if you still need
* http: set max entity size to 10M, to prevent from large post body
* http: support text/xml as body
* http: change ip whitelist to cidr to support subnet

### 5.3.3 (2/1/2018 - 2/4/2018)
* api: support configure api client timeout and slow operation threshold (default is 30s and 15s)
* log: add redis read/write entries tracking, index read/write entries as null if not set  
* search: update es to 6.1.3
* api: add "_sys/api" to return typescript definition, used by frontend

### 5.3.2 (1/29/2018 - 2/1/2018)
* html: added "autofocus", "allowfullscreen", "hidden" & "async" to boolean attributes
* api: remove openapi impl, not useful in actual development life cycle, we plan to impl tool to generate typescript ajax client from interface directly 
* api: temporary removed: update API client with 30s timeout and 15s slow operation threshold (will support configure in next version)

### 5.3.1 (1/28/2018 - 1/29/2018)
* search: update es to 6.1.2
* html: added "required", "sortable" to boolean attributes

### 5.3.0 (1/22/2018 - 1/23/2018)
* http: added gzip support, added cache param for static content 
* api: update API client with 30s timeout and 15s slow operation threshold

### 5.2.10 (1/15/2018 - 1/17/2018)
* http: support ip whitelist(due to gcloud public LB does not support ingress IP restriction), update undertow to 1.4.22
* db: change mysql jdbc driver to com.mysql.cj.jdbc.Driver (works with mysql:mysql-connector-java:6.0.6), old one is deprecated, https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-api-changes.html
* test: update junit to 5.0.3 and tweak dependency

### 5.2.9 (12/26/2017 - 1/10/2018)
* search: update es to 6.1.1
* http: support patch method, for partial update webservice, e.g. update status for one entity
