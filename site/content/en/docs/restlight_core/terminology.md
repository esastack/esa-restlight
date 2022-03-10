---
tags: ["Core"]
title: "术语"
linkTitle: "术语"
weight: 1
---

## `RequestContext`
 表示一个请求处理过程中的上下文信息，包含：`HttpRequest`、`HttpResponse`、`Attributes`等

```java
public interface RequestContext {

    /**
     * Obtains current {@code request}.
     *
     * @return request
     */
    HttpRequest request();

    /**
     * Obtains current {@code response}.
     *
     * @return response
     */
    HttpResponse response();

    /**
     * Obtains {@link Attributes} corresponding with current {@link RequestContext}.
     *
     * @return attributes
     */
    Attributes attrs();

}
```

{{< alert title="Tip" >}}
- `RequestContext`所属包名为`io.esastack.restlight.server.context`
- `HttpRequest`所属包名为`io.esastack.restlight.server.core`
- `HttpResponse`所属包名为`io.esastack.restlight.server.core`
- `Attributes`所属包名为`esa.commons.collection`
{{< /alert >}}

## `Mapping`

 表示一个请求匹配的条件， 用于确定一个请求是否能够路由到某个目标对对象。

eg.

`curl -X GET http://localhost:8080/hello`

```java
Mapping.get("/hello");
```

`curl -X POST http://localhost:8080/foo?a=1`

```java
Mapping.post("/foo")
    .hasParam("a", "1");
```

`curl -X GET -H "a:1" -H "Content-Type:application/json" -H "Accept:application/json" http://localhost:8080/foo?a=1`

```java
Mapping.get("/foo")
        .hasParam("a", "1")
        .noneParam("b", "1")
        .hasHeader("a", "1")
        .noneHeader("b", "1")
        .consumes(MediaType.APPLICATION_JSON)
        .produces(MediaType.APPLICATION_JSON);
```

## `Route`

`Route`中包含了一个`Mapping`用于路由匹配， 一个请求都将期望路由到具体的一个`Route`， 如果找不到任何一个`Route`则响应一个`404`， 同时一个`Route`还负责请求本身的业务处理。

eg.

```java
@Bean
public ManagementConfigure configure() {
    Mapping mapping = Mapping.get("/foo");
    Route route = Route.route(mapping)
        .handle((ctx) -> {
            ctx.response().entity("Hello Restlight!");
        })
        .onError(((ctx, error) -> {
            // error occurred
         }))
        .onComplete(((ctx, t) -> {
            // request completed
        }));
}
```
