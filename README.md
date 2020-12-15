# Restlight

![Build](https://github.com/esastack/esa-restlight/workflows/Build/badge.svg?branch=main)
[![codecov](https://codecov.io/gh/esastack/esa-restlight/branch/main/graph/badge.svg?token=CCQBCBQJP6)](https://codecov.io/gh/esastack/esa-restlight)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.esastack/restlight/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.esastack/restlight/)
[![GitHub license](https://img.shields.io/github/license/esastack/esa-restlight)](https://github.com/esastack/esa-restlight/blob/main/LICENSE)

Restlight is a lightweight and rest-oriented web framework.

## Quick Start

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

#### See more details in [Reference Doc](https://github.com/esastack/esa-restlight/wiki)
