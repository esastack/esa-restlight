---
sort: 8
---

# Restlight Server

`esa.restlight.server.Restlite`为`Restlight`架构中的`Restlight Server`模块的入口类， 在`ESA HttpServer`  基础上丰富了更多的功能

- 引入业务线程池
- 基于`CompletableFuture`的响应式编程支持
- 线程调度
- `Filter`
- 请求路由（根据url, method, header等条件将请求路由到对应的Handler）

{% include list.liquid all=true %}

