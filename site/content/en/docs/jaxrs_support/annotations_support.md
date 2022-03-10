---
tags: ["JAX-RS"]
title: "JAX-RS 注解支持"
linkTitle: "JAX-RS 注解支持"
weight: 10
---

## 支持的注解

- `@Path`
- `@GET`
- `@POST`
- `@PUT`
- `@DELETE`
- `@HEAD`
- `@PATCH`
- `@OPTIONS`
- `@Consumes`
- `@Produces`
- `@Context`
- `@QueryParam`
- `@PathParam`
- `@HeaderParam`
- `@MatrixParam`
- `@CookieParam`
- `@FormParam`
- `@BeanParam`
- `@DefaultValue`

## 个别注解说明

### `@QueryParam`

除普通用法外， 当未指定`value()`或者`name`且参数对象为`Map<String, List<String>`类型时， 将整个ParameterMap（即HttpRequest.paramsMap()）作为参数的值。

eg.

```java
public void foo(@QueryParam Map<String, List<String> params) {
    //...
}
```
{{< alert title="Note">}}
除url中的参数之外同时支持Post中Content-Type为application/x-www-form-urlencoded的form表单参数
{{< /alert >}}

### `@CookieParam`

普通`String`类型

```java
public void foo(@CookieParam String c) {
    //...
}
```

`Cookie`对象(io.esastack.commons.net.http.Cookie)

```java
public void foo(@CookieParam Cookie c) {
    //...
}
```

获取所有的`Cookie`

```java
public void foo(@CookieParam Set<Cookie> cookies) {
    //...
}
```

{{< alert title="Note">}}
不支持`JAX-RS`中的`jakarta.ws.rs.core.Cookie`， 仅支持`io.esastack.commons.net.http.Cookie`
{{< /alert >}}
