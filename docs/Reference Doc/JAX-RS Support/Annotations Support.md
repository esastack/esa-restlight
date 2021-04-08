---
sort: 1
---

# JAX-RS 注解支持

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
- `@QueryParam`
- `@PathParam`
- `@HeaderParam`
- `@MatrixParam`
- `@CookieParam`
- `@FormParam`
- `@BeanParam`
- `@DefaultValue`

```note
现阶段`Restlight`仅实现了`JAX-RS`的注解， 其余功能暂未实现（`Provider`, `Request`, `Response`）等暂不支持
```


## 注解使用

`@QueryParam`, `@PathParam`,`@HeaderParam`,`@MatrixParam`, `@MatrixVariable`,`@FormParam`

参数绑不支持`javax.ws.rs.ext.ParamConverterProvier`扩展

## 个别注解说明

### `@QueryParam`

除普通用法外， 当未指定`value()`或者`name`且参数对象为`Map<String, List<String>`类型时， 将整个ParameterMap（即AsyncRequest.getParameterMap）作为参数的值。

eg.

```java
public void foo(@QueryParam Map<String, List<String> params) {
    //...
}
```

```note
除url中的参数之外同时支持Post中Content-Type为application/x-www-form-urlencoded的form表单参数
```

### `@CookieParam`

普通`String`类型

```java
public void foo(@CookieParam String c) {
    //...
}
```

`Cookie`对象(io.netty.handler.codec.http.cookie.Cookie)

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

```note
不支持`JAX-RS`中的`jax.ws.rs.core.Cookie`， 仅支持`io.netty.handler.codec.http.cookie.Cookie`
```

### `@HeaderParam`

除获取单个header之外， 可以如果参数类型为`io.netty.handler.codec.http.HttpHeaders`则以所有的Header作为参数的值

eg:

```java
public void foo(@HeaderParam HttpHeaders headers) {
    //...
}
```
