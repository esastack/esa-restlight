---
sort: 100
---
# Architecture

## 设计原则

- 高兼容性
- 极致性能
- 全链路异步
- 易用
- 可扩展

## 功能架构
**分层架构设计**

![Architecture](../../img/Architecture.png)

架构图中`ESA HttpServer`, `Restlight Server`, `Restlight Core`, `Restlight for Spring`, `Restlight Starter`几个模块均可作为一个独立的模块使用， 满足不同场景下的需求。

### ESA HttpServer

基于`Netty` 实现的一个简易的[HttpServer](https://github.com/esastack/esa-httpserver)， 支持Http1.1/Http2以及Https等

### Restlight Server

在`ESA HttpServer`基础之上封装了

- 引入业务线程池
- `Filter`
- 请求路由（根据url, method, header等条件将请求路由到对应的Handler）
- 基于`CompletableFuture`的响应式编程支持
- 线程调度
- ...

#### 对应启动入口类`esa.restlight.server.Restlite`

```tip
适合各类框架，中间件等基础组建中启动或期望使用代码显示启动HttpServer的场景
```

### Restlight Core

在`Restlight Server`之上， 扩展支持了`Controller`方式（在`Controller`类中通过诸如`@RequestMappng`等注解的方式构造请求处理逻辑）完成业务逻辑以及诸多常用功能

- `HandlerInterceptor`: 拦截器
- `ExceptionHandler`: 全局异常处理器
- `BeanValidation`: 参数校验
- `ArgumentResolver`: 参数解析扩展
- `ReturnValueResolver`: 返回值解析扩展
- `RequestSerializer`: 请求序列化器（通常负责反序列化Body内容）
- `ResposneSerializer`: 响应序列化器（通常负责序列化响应对象到Body）
- 内置`Jackson`, `Fastjson`, `Gson`, `ProtoBuf`序列化支持
- ...

#### 对应启动入口类`esa.restlight.core.Restlight`

```tip
适合各类框架，中间件等基础组建中启动或期望使用代码显示启动HttpServer的场景
```

### Restlight SpringMVC & Restlight JAX-RS

- `Restlight SpringMVC`对`SpringMVC`中的注解使用习惯的`Restlight Core`的扩展实现（`@RequestMapping`, `@RequestParam`等）。

- `Restlight JAX-RS`对`JAX-RS`中的注解使用习惯的`Restlight Core`的扩展实现（`@Path`, `@GET`, `@QueryParam`等）。

### Restlight for Spring

在`Restlight Core`基础上支持在`Spring`场景下通过`ApplicationContext`容器自动配置各种内容（`RestlightOptions`, 从容器中自动配置`Filter`, `Controller`, `ControllerAdvice`等）

```tip
适合`Spring Boot`场景
```

### Restlight Starter

在`Restlight for Spring`基础上支持在`Spring Boot`场景的自动配置

```tip
适合`Spring Boot`场景
 ```

### Restlight Actuator

在`Restlight Starter`基础上支持在`Spring Boot Actuator`原生各种`Endpoint`s支持以及`Restlight`独有的`Endpoint`s。

```tip
适合`Spring Boot Actuator`场景
```

