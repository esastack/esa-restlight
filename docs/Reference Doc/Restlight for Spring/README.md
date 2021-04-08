---
sort: 1000
---

# Restlight for Spring

`esa.restlight.spring.Restlight4Spring`为`Restlight`架构中的`Restlight for Srping`模块的入口类， 在`Restlight Core` 基础上增强了自动配置功能

- `Route`自动配置
- `Filter`自动配置
- `Controller`自动配置
- `ControllerAdvice`自动配置
- `HandlerInterceptor`自动配置
- `ExceptionHandler`自动配置
- `ArgumentResolver`自动配置
- `ArgumentResolverAdvice`自动配置
- `ReturnValueResolver`自动配置
- `ReturnValueResolverAdvice`自动配置
- `RequestSerializer`自动配置
- `ResposneSerializer`自动配置
- `Validator`自动配置
- ...

```tip
**自动配置**是指`Restlight4Spring`会从`ApplicationContext`中获取对应类型的`bean`并配置到`Restlight`。
```

{% include list.liquid all=true %}
