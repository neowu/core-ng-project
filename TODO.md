* properties: load property from mounted file, to allow same docker image with different runtime mounted properties for kube 
* cache: 2level short timed local cache, to reduce remote load
* http: wait java9 with http2 client? OkHttp's POST is 5x slower than apache http client 
* search: elasticsearch plan to use java high level client to replace current transport client, wait until complete and migrate to "org.elasticsearch.client:elasticsearch-rest-high-level-client:${elasticVersion}"
* jdk9: restructure packages, split 3rd party product into modules (e.g. mongo/es/kafka/db)
* junit 5
