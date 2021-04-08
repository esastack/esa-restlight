---
sort: 200
---

# 术语

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
Mapping mapping = Mapping.get("/foo");
Route route = Route.route(mapping)
        .handle((request, response) -> {
            response.sendResult("Hello Restlight!".getBytes(StandardCharsets.UTF_8));
        })
        .onError(((request, response, error) -> {
            // error occurred
        }))
        .onComplete(((request, response, t) -> {
            // request completed
        }));
```