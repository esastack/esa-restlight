---
tags: ["feature"]
title: "RouteFilter"
linkTitle: "RouteFilter"
weight: 25
description: >
    `RouteFilter`在路由匹配成功后执行。
---

## 基本使用

```java
@Bean
public RouteFilter addHeaderFilter() {
    return new RouteFilter() {
        @Override
        public CompletionStage<Void> routed(HandlerMapping mapping, RouteContext context, RouteFilterChain next) {
            String handleName;
            if ((handleName = mapping.methodInfo().handlerMethod().method().getName()).equals("get")) {
                context.request().headers().set("handlerName", handleName);
            }
            return next.doNext(mapping, context);
        }
    };
}
```

上面的例子将会在路由匹配完成后，对所有`Handler`方法名称为`get`的方法的请求都加上一个固定的Header。

{{< alert title="Note" >}}
自定义`RouteFilterFactory`将其注入Spring容器或者配置成可以被SPI加载的方式也可以实现`RouteFilter`注入。
{{< /alert >}}

## 异步`RouteFilter`

```java
@Bean
public RouteFilter routeFilter() {
    return new RouteFilter() {
        @Override
        public CompletionStage<Void> routed(HandlerMapping mapping, RouteContext context, RouteFilterChain next) {
            return CompletableFuture.runAsync(() -> {
                // do something...
            }).thenCompose(r -> {
                // invoke next filter
                return next.doNext(mapping, context);
            });
        }
    };
}
```

上面的例子演示了在`routed(xxx, xxx)`中进行异步操作，并在该操作完成后回调`RouteFilterChain`继续执行后续操作。

{{< alert title="Tip" >}}
上面演示的异步只是开了一个新的线程， 实际场景中可使用`BIZ Scheduler`等实现更优雅的异步方式。
{{< /alert >}}

## 终止`RouteFilter`的执行

当不期望执行后续的`RouteFilter`时可返回一个`CompletableFuture.completedFuture(null)`实例。


```java
@Override
public CompletionStage<Void> routed(HandlerMapping mapping, RouteContext context, RouteFilterChain next) {
    return CompletableFuture.completedFuture(null);
}
```

{{< alert title="Warning" color="warning" >}}
- `routed(xxx)`请勿返回`null`
{{< /alert >}}
