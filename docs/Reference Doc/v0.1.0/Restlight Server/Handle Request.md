---
sort: 3
---

# 请求处理

## 业务处理

```java
Mapping mapping = Mapping.get("/foo");
Route route = Route.route(mapping)
        .handle((request, response) -> {
            // biz logic here
            response.sendResult("Hello Restlight!".getBytes(StandardCharsets.UTF_8));
        });
```

## 异常处理

```java
Mapping mapping = Mapping.get("/foo");
Route route = Route.route(mapping)
        .handle((request, response) -> {
            // biz logic here
            response.sendResult("Hello Restlight!".getBytes(StandardCharsets.UTF_8));
        })
        .onError(((request, response, error) -> {
            // error occurred
        }));
```

## Complete事件

```java
Mapping mapping = Mapping.get("/foo");
Route route = Route.route(mapping)
        .handle((request, response) -> {
            // biz logic here
            response.sendResult("Hello Restlight!".getBytes(StandardCharsets.UTF_8));
        })
        .onComplete(((request, response, t) -> {
            // request completed
        }));
```

## 异步

`Route`请求处理生命周期均支持基于`Completablefuture`的异步使用
```java
Route route = route(get("/foo"))
        .handleAsync((request, response) ->
                CompletableFuture.runAsync(() -> {
                    // biz logic
                }))
        .onErrorAsync((request, response, throwable) ->
                CompletableFuture.runAsync(() -> {
                    // error
                }))
        .onCompleteAsync((request, response, t) ->
                CompletableFuture.runAsync(() -> {
                    // complete
                }));
```