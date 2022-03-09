---
tags: ["Server"]
title: "请求处理"
linkTitle: "请求处理"
weight: 2
---

## 业务处理

```java
Mapping mapping = Mapping.get("/foo");
Route route = Route.route(mapping)
        .handle((ctx) -> {
            // biz logic here
            ctx.response().entity("Hello Restlight!");
        });
```

## 异常处理

```java
Mapping mapping = Mapping.get("/foo");
Route route = Route.route(mapping)
        .handle((ctx) -> {
            // biz logic here
            ctx.response().entity("Hello Restlight!");
        })
        .onError(((ctx, error) -> {
            // error occurred
        }));
```

## Complete事件

```java
Mapping mapping = Mapping.get("/foo");
Route route = Route.route(mapping)
        .handle((ctx) -> {
            // biz logic here
            ctx.response().entity("Hello Restlight!");
        })
        .onComplete(((ctx, t) -> {
            // request completed
        }));
```

## 异步

`Route`请求处理生命周期均支持基于`Completablefuture`的异步使用
```java
Route route = route(get("/foo"))
        .handleAsync((ctx) ->
                CompletableFuture.runAsync(() -> {
                    // biz logic
                }))
        .onErrorAsync((ctx, throwable) ->
                CompletableFuture.runAsync(() -> {
                    // error
                }))
        .onCompleteAsync((ctx, t) ->
                CompletableFuture.runAsync(() -> {
                    // complete
                }));
```
