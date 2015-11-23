## CORE-NG
core-ng is a webapp framework. it's designed to support our own projects, not as generic web framework

It's still working in progress, so all API is subjected to change. keep in mind

## TODO
* create benchmark: refactory and performance tuning
* create ASCII to handle ASCII chars/string
* String.split by char

* template, use ByteBuffer[] for performance tuning?
* async: think about how to support batch with fixed concurrency, or Batch object and chain next easier
* redis: investigate redis hiccup, like 200ms for one operation under load
* web: get/form post, validate bean class and code generation for param serialization?
* real time monitor to ES?
* provide ws interface to send queue message for dev and prod troubleshoot?
* general retry and throttling?
* webservice: client retry on network issue?
* website static content security check, (in server env, this is handled by nginx directly)
* db pool: actively check/close connection before using from transactionManager?
* validator: annotation for website, like @Pattern or @SafeString?
* cm: config management, dynamic update properties?
* cache: advanced feature: local/remote 2 level, async get on expiration, pre warm?
* db: batch insert/update auto convert to small batch like 3000?
* template security check, escaping and etc

## Change log
please check [CHANGELOG.md](CHANGELOG.md)

