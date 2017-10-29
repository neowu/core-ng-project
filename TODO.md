* properties: load property from mounted file, to allow same docker image with different runtime mounted properties for kube 
* cache: 2level short timed local cache, to reduce remote load
* http: wait java9 with http2 client? OkHttp's POST is 5x slower than apache http client 
* search: elasticsearch plan to use java high level client to replace current transport client, wait until complete and migrate to "org.elasticsearch.client:elasticsearch-rest-high-level-client:${elasticVersion}"
* jdk9: split 3rd party product into modules? (e.g. mongo/es/kafka/db)
* redis: support cluster?
* inject: remove method inject support? 

### jdk 9 incompatible list
* flyway: not working
* pmd: not support sourceCompatibility = JavaVersion.VERSION_1_9
* checkstyle: needs to filter out module-info.java
* spotbugs: OBL bug https://github.com/spotbugs/spotbugs/issues/432  
* undertow
```
[Finalizer] ERROR i.undertow - UT005091: Failed to initialize DirectByteBufferDeallocator
java.lang.ClassNotFoundException: sun.misc.Cleaner
	at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:582)
	at java.base/jdk.internal.loader.ClassLoaders$AppClassLoader.loadClass(ClassLoaders.java:185)
	at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:496)
	at java.base/java.lang.Class.forName0(Native Method)
	at java.base/java.lang.Class.forName(Class.java:292)
	at io.undertow.server.DirectByteBufferDeallocator.<clinit>(DirectByteBufferDeallocator.java:23)
	at io.undertow.server.DefaultByteBufferPool.queueIfUnderMax(DefaultByteBufferPool.java:207)
	at io.undertow.server.DefaultByteBufferPool.access$500(DefaultByteBufferPool.java:41)
	at io.undertow.server.DefaultByteBufferPool$ThreadLocalData.finalize(DefaultByteBufferPool.java:300)
	at java.base/java.lang.System$2.invokeFinalize(System.java:2114)
	at java.base/java.lang.ref.Finalizer.runFinalizer(Finalizer.java:102)
	at java.base/java.lang.ref.Finalizer.access$100(Finalizer.java:34)
	at java.base/java.lang.ref.Finalizer$FinalizerThread.run(Finalizer.java:217)
```	
