* kafka: combine json reader/validator into handler, since one topic can only have one message class and there is no rabbitMQ anymore
* properties: load property from mounted file, to allow same docker image with different runtime mounted properties for kube 
