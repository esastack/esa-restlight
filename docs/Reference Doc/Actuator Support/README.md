---
sort: 500
---

# Spring Boot Actuator支持

Restlight适配了[Spring Boot Actuator](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-project/spring-boot-actuator)， 提供详细的健康检查以及监控等接口

```note
仅支持`Spring Boot2X`版本
```

## Quick Start

引入Maven依赖：

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-actuator</artifactId>
	<version>2.1.1.RELEASE</version>
</dependency>
<dependency>
	<groupId>io.esastack</groupId>
	<artifactId>restlight-starter-actuator</artifactId>
	<version>${restlight.version}</version>
</dependency>
```

```note
Actuator的版本请配合对应的Spring Boot版本引入
```

访问 `Get` `localhost:8080/actuator/info`返回`{}`

```tip
Spring Boot2.0之后的Atctuator默认只开启了`info`和`health`两个接口， 可以使用`management.endpoints.web.exposure.include=info,health,foo`开启。
```

{% include list.liquid all=true %}


