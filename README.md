## CORE-NG
core-ng is a webapp framework. it's designed to support our own projects, not as generic web framework

It's still working in progress, so all API is subjected to change. keep in mind

## TODO
* mongo: support count()
* async: think about how to support batch with fixed concurrency, or Batch object and chain next easier
* template, use ByteBuffer[] for performance tuning?
* redis: investigate redis hiccup, like 200ms for one operation under load
* web: get/form post, validate bean class and code generation for param serialization?
* real time monitor to ES?
* provide ws interface to send queue message for dev and prod troubleshoot?
* general retry and throttling?
* webservice: client retry on network issue?
* website static content security check, (in server env, this is handled by nginx directly)
* validator: annotation for website, like @Pattern or @SafeString?
* cm: config management, dynamic update properties?
* cache: advanced feature: local/remote 2 level, async get on expiration, pre warm?
* db: batch insert/update auto convert to small batch like 3000?
* template security check, escaping and etc
* db: support mysql insert on duplicated key (benchmark speed vs select + insert or update)?

## Change log
please check [CHANGELOG.md](CHANGELOG.md)

