---
sort: 100
---

# Quick Start

依赖

```xml
<dependency>
	<groupId>io.esastack</groupId>
	<artifactId>restlight-server</artifactId>
	<version>${restlight.version}</version>
</dependency>
```

一行代码启动一个Http Server

```java
Restlite.forServer()
        .daemon(false)
        .deployments()
        .addRoute(route(get("/hello"))
                .handle((request, response) ->
                        response.sendResult("Hello Restlight!".getBytes(StandardCharsets.UTF_8))))
        .server()
        .start();
```

运行并访问： http://localhost:8080/hello  即可看到输出： 

```
Hello Restlight!
```
