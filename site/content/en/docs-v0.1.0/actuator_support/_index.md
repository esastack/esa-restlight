---
tags: ["Actuator"]
title: "Spring Boot Actuator支持"
linkTitle: "Spring Boot Actuator支持"
weight: 500
description: >
    Restlight适配了[Spring Boot Actuator](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-project/spring-boot-actuator)， 提供详细的健康检查以及监控等接口
---
# Quick Start

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
{{< alert title="Note" >}}
Actuator的版本请配合对应的Spring Boot版本引入
{{< /alert >}}

访问 `Get` `localhost:8080/actuator/info`返回`{}`

{{< alert title="Tip" >}}
Spring Boot2.0之后的Atctuator默认只开启了`info`和`health`两个接口， 可以使用`management.endpoints.web.exposure.include=info,health,foo`开启。
{{< /alert >}}
