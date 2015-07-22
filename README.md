## CORE-NG
core-ng is a webapp framework. it's designed to support our own projects, not as generic web framework

It's still working in progress, so all API is subjected to change. keep in mind

## TODO
* template: "include" support
* website: i18n
* website: cms components
* push action log to rabbitmq directly, index thru ES/Kibana
* resilient retry and throttling
* Cache pre warm, static or provide structure
* webservice client retry on network issue
* nested db transaction?
* validate ES index object
* provide ws interface to send queue message for dev and prod troubleshoot?
* template: "map" support?
* review/refactory/unit-test all packages
* website static content security check, (in prod, this is not part of java, but nginx)
* template security check, escaping and etc

## Change log
please check [CHANGELOG.md](CHANGELOG.md)

