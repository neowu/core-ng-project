## CORE-NG
core-ng is a webapp framework. it's designed to support our own projects, not as generic web framework

It's still working in progress, so all API is subjected to change. keep in mind

## TODO
* rename AbstractApplication to Application
* real time monitor to ES?
* provide ws interface to send queue message for dev and prod troubleshoot?
* general retry and throttling?
* webservice: client retry on network issue?
* template: "map" support?
* review/refactory/unit-test all packages (final/encapsulation/etc)
* website static content security check, (in server env, this is handled by nginx directly)
* template security check, escaping and etc
* db pool: actively check/close connection before using from transactionManager?
* validator: annotation for website, like @Pattern or @SafeString?
* webservice: @Version to let client pass thru header for action log?
* cm: config management, dynamic update properties?
* cache: advanced feature: local/remote 2 level, async get on expiration, pre warm?

## Change log
please check [CHANGELOG.md](CHANGELOG.md)

