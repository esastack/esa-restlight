---
tags: ["feature"]
title: "Filter"
linkTitle: "Filter"
weight: 20
---

## 基本使用

```java
@Bean
public Filter addHeaderFilter() {
    return new Filter() {
        @Override
        public CompletableFuture<Void> doFilter(AsyncRequest request, AsyncResponse response, FilterChain chain) {
            request.setHeader("foo", "bar");
            return chain.doFilter(request, response);
        }
    };
}
```

上面的例子将会给所有到来的请求都加上一个固定的Header。

## 异步`Filter`

```java
@Bean
public Filter filter() {
    return new Filter() {
        @Override
        public CompletableFuture<Void> doFilter(AsyncRequest request, AsyncResponse response, FilterChain chain) {
            return CompletableFuture.runAsync(() -> {
                // do something...
            }).thenCompose(r -> {
                // invoke next filter
                return chain.doFilter(request, response);
            });
        }
    };
}
```

上面的例子演示了在`doFilter(xxx)`中进行异步操作，并在该操作完成后回调`FilterChain`继续执行后续操作。

{{< alert title="Tip" >}}
上面演示的异步只是开了一个新的线程， 实际场景中可使用`Netty`等实现更优雅的异步方式。
{{< /alert >}}

## 终止`Filter`的执行

当不期望执行后续的`Filter`时可返回一个`CompletableFuture.completedFuture(null)`实例。

```java
@Override
public CompletableFuture<Void> doFilter(AsyncRequest request, AsyncResponse response, FilterChain chain) {
    return CompletableFuture.completedFuture(null);
}
```

{{< alert title="Warning" color="warning" >}}
- `doFilter(xxx)`请勿返回`null`
- 所有方法都将会在IO线程上调用，尽量不要阻塞， 否则将对性能会有较大的影响。
{{< /alert >}}
