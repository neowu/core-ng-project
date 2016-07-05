## TODO
* template, use ByteBuffer[] for performance tuning?
* web: get/form post, validate bean class and code generation for param serialization?
* general retry and throttling?
* webservice: client retry on network issue?
* website static content security check, (in server env, this is handled by nginx directly)
* validator: annotation for website, like @HTMLSafeString?
* cm: config management, dynamic update properties?
* cache: advanced feature: local/remote 2 level, async get on expiration, pre warm?
* template security check, escaping and etc
* http: make http session https only?
* think about /test//b, and /:path(\*)
* framework error, queue listener, background task error notification?
* extend i18n message to validator/format/called by code directly
* faster synchronous rpc

## Change log
### 4.6.7 (7/5/2016)
* db: support LocalDate type as column

### 4.6.6 (7/1/2016)
* bind: use mock executor in integration test

### 4.6.5 (6/29/2016)
* mongo: support readPreference in all operation

### 4.6.4 (6/28/2016)
* search: support search type in ES

### 4.6.3 (6/24/2016)
* hash: added SHA1 SHA256 support

### 4.6.2 (6/22/2016 - 6/23/2016)
* elasticsearch: remove groovy test support, actually script query is never useful, not plan to use anymore
* mock: mockRedis supports all operations
* elasticsearch: support foreach for reindex support

### 4.6.1 (6/22/2016)
* httpclient: ContentType.parse supports to ignore illegal charset
* session: support to configure cookie domain

### 4.6.0 (6/20/2016 - 6/21/2016)
* mongo: refine mongo decoding logic and error message
* http: fix webContext should init at beginning, to make it available in 404 error handler

### 4.5.9 (6/10/2016)
* mongo: support connect to multiple mongo with name
* mongo: support MapReduce/Aggregate with readPreference
* httpclient: wrap UncheckedIOException as HTTPClientException

### 4.5.8 (6/8/2016)
* queue: update queue handling action to be queue/{queue}/{message_type}, to make it easier to manage and analyze

### 4.5.7 (6/7/2016)
* template: support classpath template (for include)

### 4.5.6 (6/3/2016)
* session: remove key

### 4.5.5 (5/25/2016)
* httpclient: throw HTTPClientException when status code is not supported

### 4.5.4 (5/24/2016)
* template: fix template engine with c:src in String template

### 4.5.3 (5/23/2016)
* validate: support Optional<T>
* validate: removed @ValueNotNull/@ValueNotEmpty/@ValuePattern, use @NotNull/@NotEmpty/@Pattern to simplify

### 4.5.2 (5/20/2016)
* mongo: count() supports null filter
* background-task: fix task can be broken if exception occurred

### 4.5.1 (5/18/2016)
* error: fix errorCode.severity handling by remoteServiceException
* mongo: support mapReduce

### 4.5.0 (5/9/2016)
* web: support return all values for file upload and form post
* web: support PUT for file uploading

### 4.4.9 (4/27/2016)
* http: fixed pass errorCode.severity

### 4.4.8 (4/20/2016)
* http: add Map<String, String) queryParams();

### 4.4.7 (4/15/2016 - 4/18/2016)
* template: remove c:msg, use m:text instead
* exception: removed @Warning, use programmable severity (to simplify webservice exception handling and error report)

### 4.4.6 (4/15/2016)
* template: support m:text and m:{attr} for message body and attribute

### 4.4.5 (4/15/2016)
* route: fix dynamic path should not match trailing slash
* error: refactory errorCode support, make webservice client translate

### 4.4.4 (4/14/2016)
* httpclient: put error code to httpclientexception
* rabbitmq: fixed queue listener to report error code on ack
* thread: try to shutdown thread pool gracefully

### 4.4.3 (4/13/2016)
* template: hide templateManager, not needed by application
* mongo: log Bson filter/projection/sort in json string format

### 4.4.2 (4/12/2016)
* json: support Optional as field, queue/ws interface supported too

### 4.4.1 (4/8/2016)
* http: update httpclient request to accept ContentType
* es: update to 2.3.1
* httpclient: throw HTTPClientException for invalid url

### 4.4.0 (4/7/2016)
* log: put 1M limit of trace log to forward to remote
* log: add message filter support

### 4.3.9 (4/1/2016)
* mongo: support eval for db-migration
* mongo: set connection max idle time to 30mins

### 4.3.8 (3/31/2016)
* hmac: changed input from String to bytes
* es: update to 2.3.0

### 4.3.7 (3/30/2016)
* validate: add @ValuePattern

### 4.3.6 (3/30/2016)
* http: request supports body() method

### 4.3.5 (3/28/2016)
* http: added 422 status code

### 4.3.4 (3/25/2016)
* log: actionLogContext.put checks duplication to avoid huge trace log

### 4.3.3 (3/23/2016 - 3/25/2016)
* es: update es to 2.2.1
* web: for file response, close exchange when end

### 4.3.2 (3/18/2016)
* mongo: update default timeout to 15s
* rabbitmq: publisher supports message with priority

### 4.3.1 (3/17/2016)
* http: added status code 410
* build: update gradle 2.12, flyway 4.0

### 4.3.0 (3/15/2016)
* mongo: support @MongoEnumValue
* db: renamed @EnumValue to @DBEnumValue

### 4.2.9 (3/11/2016)
* template: remove LanguageProvider as first step for i18n message support refactory

### 4.2.8-b1,b2,GA (3/9/2016 - 3/10/2016)
* stopwatch: start to use nanoTime
* log: actionlog start to trace cpuTime
* mongo: register enum codec from entity, for filter
* http: support statusCode 429
* rabbitmq: update to 3.6.1

### 4.2.7-b1,b2,b3,b4 (3/3/2016 - 3/8/2016)
* rabbitmq: make rabbitmq listener use dedicated thread pool
* executor: remove Batch<T> support
* scheduler: use dedicated thread pool
* search: remove client(), add analyze/index meta support
* mongo: added Mongo interface for unit test and management
* validation: support @Pattern
* mongo: disable cursorFinalizer, framework always close cursor
* thread: limit executor to processor*4 thread, to collect data for next phase design

### 4.2.6 (3/3/2016)
* config: update template and message config API
* web: support convert query param as value type

### 4.2.5 (3/2/2016)
* template: refine i18n language support

### 4.2.4 (2/29/2016 - 3/1/2016)
* mongo: removed old Mongo interface, refined mongo impl

### 4.2.3 (2/29/2016)
* mongo: add timeout to query, support MongoCollection<T>

### 4.2.2 (2/25/2016)
* mongo: updated to 3.2.2, added fongo support back
* search: exposed ElasticSearch as interface
* queue: tweak queue listener to result in less thread when rabbitmq is busy, track slow acknowledge

### 4.2.1 (2/24/2016)
* batch: allow specify maxConcurrentHandlers for batch
* async: provide thread name for all thread pool
* redis: update default redis timeout to 5s

### 4.2.0 (2/24/2016)
* validation: update validation exception message
* json: add from/to enum value to JSON, to support search query build with enum

### 4.1.9 (2/23/2016)
* util: ASCII supports char
* log: improve log error handling

### 4.1.8 (2/22/2016)
* fix: allow logger to log null message, e.g. errorMessage of NullPointerException

### 4.1.7 (2/17/2016 - 2/18/2016)
* rabbitmq: unified single/batch message polling

### 4.1.6 (2/17/2016)
* util: moved InputStreams to util, open to use
* log: make max trace log 3000, and only append warn/error after
* search: added SearchResponse<T> to convert hits to object on framework level

### 4.1.5 (2/16/2016)
* tuning: internal tuning, move low level optimization class to impl package, leave core.framework.util simple ones
* httpclient: use byte[] as body, remove ByteBuf
* log: use 500k as max trace log to forward to log processor, removed max trace event size limit, truncate after 5000 and only add warning events

### 4.1.4 (2/12/2016 - 2/15/2016)
* cache: make cache process byte[] directly to redis
* log: limit trace log 200k max per line, rise max log events per action to 10,000
* json: add afterburner module for object binding performance

### 4.1.3 (2/11/2016)
* db: lower default db timeout to 15s
* queue: updated rabbitMQ api and config api
* json: start convert json to bytes directly, to lower memory footprint with queue/ES/cache

### 4.1.2 (2/10/2016)
* web: removed web/not-found, web/method-not-allowed action assign, since we use error_code now
* background: moved pool-cleanup, collect-stat job to background thread, not included in action
* web: renamed all internal /management/ path to /\_sys/

### 4.1.1 (2/9/2016)
* monitor: initial monitoring draft, forward monitor metrics via logforwarder

### 4.1.0 (2/4/2016)
* template: invalid url attr will write src="", container will write empty if content is null
* elasticsearch: support 2.2.0, load groovy plugin in test context

### 4.0.9 (2/3/2016)
* redis: loose slow_redis warning threshold and timeout, on busy server due to CPU context switch, it's relative easy to hit it

### 4.0.8 (2/2/2016)
* web: requestURL now contains QueryString, (requestURL is url without decoding)

### 4.0.7 (1/28/2016)
* log: renamed all slow query error_code and naming
* pool: add error code POOL_TIME_OUT

### 4.0.6 (1/27/2016)
* template: warning if url fragment gets null url
* log: forward log one by one to simplify, batch happens on log-processor

### 4.0.5 (1/22/2016)
* log: make 3rd party log level to info, (e.g. ES log sampler error by INFO level in separated thread)
* elasticsearch: set ping timeout, support dynamic index name (for alias or time serial index name)

### 4.0.4 (1/21/2016)
* fix: typo in @NotEmpty/@ValueNotEmpty
* ws: support enum in path param

### 4.0.3 (1/19/2016)
* cache: update getAll to return Map<String, T>

### 4.0.2 (1/18/2016)
* web: removed URIBuilder, added Encodings.encodeURIComponent and decodeURIComponent
* web: use URI query param encoding/decoding to set/get cookie (refered as URLEncoding in other place, e.g. jquery)

### 4.0.1 (1/13/2016)
* schedule: support weekly and monthly trigger

### 4.0.0 (1/8/2016 - 1/11/2016)
* bytebuf: improve for skip/available as it will be wrapped by buffered stream or S3 client
* web: mark path not found error code to PATH_NOT_FOUND (to ignore 3rd party scan)
* batch: new async support, use Executor

### 3.10.1 (1/7/2016)
* mongo: support LocalDateTime in filter

### 3.10.0 (1/7/2016)
* httpclient: fix NPE with HttpEntity is null on 204

### 3.9.10 (1/5/2016)
* elasticsearch: ignore cluster name for transport client
