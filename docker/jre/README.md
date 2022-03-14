this is slim jre image targeted for core-ng web application w/ cloud env

analyze dependencies by

```bash
jdeps --class-path './*' -recursive -summary --multi-release 17 *.jar
```

included following modules

```
java.base@17
java.compiler@17
java.datatransfer@17
java.desktop@17
java.instrument@17
java.logging@17
java.management@17
java.management.rmi@17
java.naming@17
java.prefs@17
java.rmi@17
java.security.jgss@17
java.security.sasl@17
java.sql@17
java.transaction.xa@17
java.xml@17
jdk.attach@17
jdk.charsets@17
jdk.crypto.cryptoki@17
jdk.crypto.ec@17
jdk.internal.jvmstat@17
jdk.jdi@17
jdk.jdwp.agent@17
jdk.jfr@17
jdk.localedata@17
jdk.management@17
jdk.management.jfr@17
jdk.naming.dns@17           // for "mongodb+srv://" connection protocol
jdk.naming.rmi@17           // for kafka jmx connector, used by monitor 
jdk.net@17
jdk.unsupported@17
```
