# Restlight

![Build](https://github.com/esastack/esa-restlight/workflows/Build/badge.svg?branch=main)
[![codecov](https://codecov.io/gh/esastack/esa-restlight/branch/main/graph/badge.svg?token=CCQBCBQJP6)](https://codecov.io/gh/esastack/esa-restlight)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.esastack/restlight-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.esastack/restlight-parent/)
[![GitHub license](https://img.shields.io/github/license/esastack/esa-restlight)](https://github.com/esastack/esa-restlight/blob/main/LICENSE)

Restlight is a lightweight and rest-oriented web framework.

## Features

- HTTP1.1/HTTP2/H2C/HTTPS support
- SpringMVC and JAX-RS annotations support
- High performance: 2 to 4 times Spring Web
- Fully asynchronous: Based on `CompletableFuture`
- Threading Model: Connector thread, IO thread, Biz thread
- Thread Scheduling: Schedule requests to IO thread pool, Biz thread pool, or any custom thread pool
- Self-Protection: Connection creation limit, CPU load overload protection
- Spring Boot Actuator support
- Extension: Use SPI(enhanced) to extend Restlight
- Layered architecture: Use Restlight in Spring Boot, Spring, and embedded environment without compatibility issues
- more...

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

## Performance

### Test cases

- We built an echo server by ESA Restlight and used a http client to do the requests for RPS testing with different bytes of payload(16B, 128B, 512B, 1KB, 4KB, 10KB) and different Threading-Models(IO, BIZ).
- Also we used `spring-boot-starter-web`(2.3.2.RELEASE) to build a server which is same with above for RPS testing.

### Hardware Used

We used the following software for the testing:

- wrk4.1.0

- |        | OS                       | CPU  | Mem(G) |
  | ------ | ------------------------ | ---- | ------ |
  | server | centos:6.9-1.2.5(docker) | 4    | 8      |
  | client | centos:7.6-1.3.0(docker) | 16   | 3      |
  

### JVM Options

```
-server -Xms3072m -Xmx3072m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m -XX:+UseConcMarkSweepGC -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 -XX:+PrintTenuringDistribution -XX:+PrintGCDateStamps -XX:+PrintGCDetails -Xloggc:logs/gc-${appName}-%t.log -XX:NumberOfGCLogFiles=20 -XX:GCLogFileSize=480M -XX:+UseGCLogFileRotation -XX:HeapDumpPath=.
```

### Server Options

| Framework  | Options                                                      |
| ---------- | ------------------------------------------------------------ |
| Restlight  | restlight.server.io-threads=8<br/>restlight.server.core-biz-threads=16<br/>restlight.server.max-biz-threads=16<br/>restlight.server.blocking-queue-length=512 |
| Spring Web | server.tomcat.threads.max=32<br/>server.tomcat.accept-count=128 |



### RPS

|                | 16B       | 128B      | 512B      | 1KB       | 4KB      | 10KB     |
| -------------- | --------- | --------- | --------- | --------- | -------- | -------- |
| Restlight(IO)  | 129457.26 | 125344.89 | 125206.74 | 116963.24 | 85749.45 | 49034.57 |
| Restlight(BIZ) | 101385.44 | 98786.62  | 97622.33  | 96504.81  | 68235.2  | 46460.79 |
| Spring Web     | 35648.27  | 38294.94  | 37940.3   | 37497.58  | 32098.65 | 22074.94 |

#### See more details in [Reference Doc](https://github.com/esastack/esa-restlight/wiki)
