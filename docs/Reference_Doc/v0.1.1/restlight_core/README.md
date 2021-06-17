---
sort: 9
---

# Restlight Core

`esa.restlight.core.Restlight`为`Restlight`架构中的`Restlight Core`模块的入口类， 在`Restlight Server` 基础上丰富了更多的功能

- `Controller`
- `ControllerAdvice`
- `HandlerInterceptor`: 拦截器
- `ExceptionHandler`: 全局异常处理器
- `BeanValidation`: 参数校验
- `ArgumentResolver`: 参数解析扩展
- `ArgumentResolverAdvice`: 参数解析扩展
- `ReturnValueResolver`: 返回值解析扩展
- `ReturnValueResolverAdvice`: 返回值解析扩展
- `RequestSerializer`: 请求序列化器（通常负责反序列化Body内容）
- `ResposneSerializer`: 响应序列化器（通常负责序列化响应对象到Body）
- 内置`Jackson`, `Fastjson`, `Gson`, `ProtoBuf`序列化支持
- …

`Restlight Core`在`Restlight Server`中`Route`的业务处理部分做了封装， 完成拦截器，参数绑定，反序列化，返回值解析，序列化等一系列功能。

`Restlight Core`为核心实现， 实际使用时需配合`Restlight SpringMVC`以及`Restlight JAX-RS`实现。

```tip
`Restlight Core`拥有`Restlight Server`的所有特性， 具体功能特性请参考`Restlight Server`部分
```

由于`Restlight Core`为标准实现， 需要配合`Restlight SpringMVC`或者`Restlight JAX-RS`一起使用

{% include list.liquid all=true %}
