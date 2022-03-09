---
tags: ["Core"]
title: "Restlight Core"
linkTitle: "Restlight Core"
weight: 900
description: >
    `io.esastack.restlight.core.Restlight`为`Restlight`架构中的`Restlight Core`模块的入口类， 在[`ESA HttpServer`](https://github.com/esastack/esa-httpserver) 基础上丰富了更多的功能
---

- 引入业务线程池
- 基于`CompletionStage`的响应式编程支持
- 线程调度
- `Filter`
- 请求路由（根据url, method, header等条件将请求路由到对应的Handler）
- `RouteFilter`  
- `Controller`
- `ControllerAdvice`
- `HandlerInterceptor`: 拦截器
- `ExceptionHandler`: 全局异常处理器
- `BeanValidation`: 参数校验
- `ParamResolver`: 参数解析扩展
- `ParamResolverAdvice`: 参数解析扩展
- `ResponseEntityResolver`: 返回值解析扩展
- `ResponseEntityResolverAdvice`: 返回值解析扩展
- `RequestSerializer`: 请求序列化器（通常负责反序列化Body内容）
- `ResposneSerializer`: 响应序列化器（通常负责序列化响应对象到Body）
- 内置`Jackson`, `Fastjson`, `Gson`, `ProtoBuf`序列化支持
- …

`Restlight Core`为核心实现， 实际使用时需配合`Restlight SpringMVC`以及`Restlight JAX-RS`实现。
