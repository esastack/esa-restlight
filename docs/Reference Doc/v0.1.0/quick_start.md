# Quick Start

Create a Spring Boot application and add dependency

> **Note：`netty` 4.1.52.Final and `tcnative` 2.0.34.Final are directly dependent on.**

> **Note: Please make sure the version of `tcnative` matches the version of `netty`.**


```xml
<dependency>
    <groupId>io.esastack</groupId>
    <artifactId>restlight-starter</artifactId>
    <version>${mvn.version}</version>
</dependency>
```

Write your Controller

```java
@RestController
@SpringBootApplication
public class RestlightDemoApplication {

    @GetMapping("/hello")
    public String hello() {
        return "Hello Restlight!";
    }

    public static void main(String[] args) {
        SpringApplication.run(RestlightDemoApplication.class, args);
    }
}
```

Run your application and then you would see something like

```
Started Restlight server in 1265 millis on 0.0.0.0:8080
```

curl http://localhost:8080/hello 
