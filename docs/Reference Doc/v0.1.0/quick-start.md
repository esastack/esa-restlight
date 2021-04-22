Create a Spring Boot application and add dependency

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
