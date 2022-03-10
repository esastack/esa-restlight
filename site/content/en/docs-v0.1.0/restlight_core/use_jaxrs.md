---
tags: ["JAX-RS"]
title: "配合 JAX-RS标准"
linkTitle: "配合 JAX-RS标准"
weight: 10
description: >
    基于`Restlight Core`为兼容`JAX-RS`注解使用习惯的扩展实现
---

eg.

引入依赖

```xml
<dependency>
	<groupId>io.esastack</groupId>
	<artifactId>restlight-core</artifactId>
	<version>${restlight.version}</version>
</dependency>
<dependency>
	<groupId>io.esastack</groupId>
	<artifactId>restlight-jaxrs-provider</artifactId>
	<version>${restlight.version}</version>
</dependency>
```

编写`Controller`

```java
@Path("/hello")
public class HelloController {

    @Path("/restlight")
    @GET
    @Produces(MediaType.TEXT_PLAIN_VALUE)
    public String restlight() {
        return "Hello Restlight!";
    }
}
```

使用`Restlight`启动Server

```java
Restlight.forServer()
        .daemon(false)
        .deployments()
        .addController(HelloController.class)
        .server()
        .start();
```

启动并访问： http://localhost:8080/hello  即可看到输出： 

```properties
Hello Restlight!
```
