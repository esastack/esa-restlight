---
sort: 1
---

# Quick Start

引入依赖

```xml
<dependency>
	<groupId>io.esastack</groupId>
	<artifactId>restlight-spring</artifactId>
	<version>${restlight.version}</version>
</dependency>

<dependency>
	<groupId>io.esastack</groupId>
	<artifactId>restlight-springmvc-provider</artifactId>
	<version>${restlight.version}</version>
</dependency>
```

编写`Controller`并注入`Spring`

```java
// Spring容器扫描并注入
@Controller
@RequestMapping("/hello")
public class HelloController {

    @GetMapping(value = "/restlight")
    public String restlight() {
        return "Hello Restlight!";
    }
}
```

使用`Restlight4Spring`启动Server

```java
ApplicationContext context = ...
Restlight4Spring.forServer(context)
        .daemon(false)
        .server()
        .start();
```

启动并访问： http://localhost:8080/hello 即可看到输出：

```properties
Hello Restlight!
```

```tip
同理其他`Route`, `HandlerInterceptor`,`Filter`等也可直接注入到`Spring`并作为参数启动`Restlight4Spring`配置启动。
```
