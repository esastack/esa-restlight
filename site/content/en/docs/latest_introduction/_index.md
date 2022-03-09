---
tags: ["notification"]
title: "新版说明"
linkTitle: "新版说明"
weight: 90
description: >
    `Restlight`最新发布了V1.0.0版本，全面支持了JAX-RS3.0标准并在整体架构设计上和处理流程上做了部分优化，提供了更多丰富的功能和API。
---

## Notification
`Restlight`1.0.0为不兼容的版本升级，如果你从其他版本升级到该版本可能会需要修改一定量的业务源代码，具体包括：
- 包名修改：之前`esa.restlight`开头的包将统一以`io.esastack.restlight`开头
- 修改自定义`ArgumentResolver`相关的实现，包括：`ArgumentResolver`、`ArgumentResolverAdapter`、`ArgumentResolverFactory`
、`ArgumentResolverAdvice`、`ArgumentResolverAdviceAdapter`、`ArgumentResolverAdviceFactory`等，最新用法请参考[参数解析](../restlight_starter/param_resolver)和[ParamResolverAdvice](../restlight_starter/param_resolver_advice)
- 修改自定义`ReturnValueResolver`相关的实现，包括：`ReturnValueResolver`、`ReturnValueResolverAdapter`、`ReturnValueResolverFactory`、`ReturnValueResolverAdvice`、`ReturnValueResolverAdviceAdapter`、`ReturnValueResolverAdviceFactory`等，最新用法请参考[返回值解析](../restlight_starter/responseentity_resolver)和[ResponseEntityResolverAdvice](../restlight_starter/responseentity_resolver_advice)
- 修改自定义拦截器，包括：`HandlerInterceptor`、`Interceptor`、`InterceptorFactory`、`MappingInterceptor`、`RouteInterceptor`等， 最新用法请参考[拦截器](../restlight_starter/interceptor)
- 修改自定义`Filter`，最新用法请参考：[Filter](../restlight_starter/filter)
- 其它使用`AsyncRequest`、`AsyncResponse`的地方，具体请参考[术语](../restlight_server/terminology)

### Q&A
##### Q：为何不能兼容之前的版本？
- 兼容JAX-RS3.0的需要：`Restlight`原有的请求处理流程和API设计无法满足JAX-RS3.0标准的需要，因此必须在框架核心逻辑上进行重新的设计和考量，而这势必导致接口无法完全向下兼容。
- 之前的设计已阻碍框架的进一步演进，比如：用户需要在自定义的异常处理器或过滤器中返回指定的entity这一功能就无法实现，因为响应entity的序列化时机和接口API设计上都存在局限性，以至于在不进行重新设计的情况下永远无法实现该功能。
- 接口易用性和可理解性的需要。

##### Q：升级成本高，可以不升级吗？
- 当然可以，之前版本已经比较稳定，如果出现重大bug我们会在原有版本上及时修复，保证0.x版本在生产环境稳定运行。

## Feature
- [全面支持JAX-RS3.0](https://github.com/esastack/esa-restlight/projects/2)
- [支持将Restlight打包成Native Image](https://github.com/esastack/esa-restlight/projects/1)
- [支持接口上的注解](https://github.com/esastack/esa-restlight/issues/36)
- 新增`RouteFilter`，在路由匹配完成之后执行
- 新增`StringConverter`相关接口，支持在参数解析阶段将`String`转成自定义`POJO`
- 支持动态添加/卸载路由
- `HttpRequest`、`HttpResponse`、参数解析、返回值处理等接口提供了更多方法，详见具体章节

## Fix
- [修复http2特殊场景下内存泄露的bug](https://github.com/esastack/esa-restlight/issues/75)
- [修复`MockMVC`场景下`Filter`不生效的bug](https://github.com/esastack/esa-restlight/issues/85)
- [修复当开启`AccessLog`时`MockMVC`无法正常启动的bug](https://github.com/esastack/esa-restlight/issues/87)

## Optimization
- [屏蔽对外暴露netty接口](https://github.com/esastack/esa-restlight/issues/12)
- [将`AsyncRequest`和`AsyncResponse`整合成RequestContext](https://github.com/esastack/esa-restlight/issues/32)
- [`HttpBodySerializer`接口优化](https://github.com/esastack/esa-restlight/issues/33)
- [`Serializer`命名优化](https://github.com/esastack/esa-restlight/issues/34)
- [当异常被成功处理时不打印异常栈](https://github.com/esastack/esa-restlight/issues/83)
- 使用`CompletionStage`替换接口定义中的`CompletableFuture`

