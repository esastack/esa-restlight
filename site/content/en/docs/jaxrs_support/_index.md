---
tags: ["JAX-RS"]
title: "JAX-RS3.0 支持"
linkTitle: "JAX-RS3.0 支持"
weight: 700
description: >
    `Restlight`支持了JAX-RS的使用习惯，你可以按照[JAX-RS](https://github.com/jakartaee/rest)的方式使用Restlight
---

>> 需要说明地是，`Restlight`全面支持了`JAX-RS3.0`标准，包括注解和接口定义。相比于`JAX-RS2.0`标准，3.0版本中的包名发生了改变（由`javax.ws.rs`改为`jakarta.ws.rs`），如果你从
>> `JAX-RS2.0`升级而来，请知晓该变化。

引入依赖

```xml
<dependency>
    <groupId>io.esastack</groupId>
    <artifactId>restlight-starter</artifactId>
    <version>${restlight.version}</version>
</dependency>
<dependency>
	<groupId>io.esastack</groupId>
	<artifactId>restlight-jaxrs-provider</artifactId>
	<version>${restlight.version}</version>
</dependency>
<dependency>
	<groupId>io.esastack</groupId>
	<artifactId>commons</artifactId>
	// ${commons.version} >= 0.2.2
	<version>${commons.version}</version>
</dependency>
```

