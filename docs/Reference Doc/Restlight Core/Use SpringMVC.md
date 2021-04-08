---
sort: 2
---

# 配合 Restlight for SpringMVC使用

```xml
<dependency>
	<groupId>io.esastack</groupId>
	<artifactId>restlight-core</artifactId>
	<version>${restlight.version}</version>
</dependency>
<dependency>
	<groupId>io.esastack</groupId>
	<artifactId>restlight-springmvc-provider</artifactId>
	<version>${restlight.version}</version>
</dependency>
```

编写`Controller`

```java
@RequestMapping("/hello")
public class HelloController {

    @GetMapping(value = "/restlight")
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
