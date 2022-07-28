---
tags: ["JAX-RS"]
title: "JAX-RS 接口支持"
linkTitle: "JAX-RS 接口支持"
weight: 1000
description: >
    `Restlight支持JAX-RS3.0中定义的所有适用于服务端的标准接口`
---

## 支持的接口
- `Application`
- `Configuration`
- `Providers`
- `MessageBodyReader`
- `MessageBodyReader`
- `MessageBodyWriter`
- `ContextResolver`
- `ExceptionMapper`
- `Feature`
- `DynamicFeature`
- `ContainerRequestFilter`
- `ContainerResponseFilter`
- `ReaderInterceptor`
- `WriterInterceptor`
- `ParamConverterProvider`
- `@Priority`
- `@ConstrainedTo`
- `@PreMatching`
- `@NameBinding`
- ...

{{< alert title="Note">}}
上述接口的语义、功能与`JAX-RS`标准中定义的相同。
{{< /alert >}}

## 使用方式
使用时将实现上述接口的实现类直接注入`Spring`容器并加上指定注解（如果是`Controller`方法需要加上`@Path`，其他的扩展加上`@Provider`）即可生效。

eg.

##### 1.自定义`Application`，`@ApplicationPath("/abc")`为可选项

```java
@ApplicationPath("/abc")
@Component
public class ApplicationImpl extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Collections.singleton(HelloWorldResource.class);
    }

}
```

##### 2.自定义`Provider`

```java
@Component
@Provider
public class RequestFilterDemo implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext context) {
        context.getHeaders().add("name", "value");
    }
    
}
```

##### 3.自定义`Controller`

```java
@Component
@Path("/simple")
public class HelloWorldController {

    @GET
    public String index() {
        return "Hello JAX-RS3.0";
    }

}

```

{{< alert title="Note">}}
如果在非Spring环境下使用JAX-RS，则需要在应用初始化时将`JAX-RS`接口的实现类添加到启动上下文，示例如下：
```java
Restlight.forServer()
        .daemon(false)
        .deployments()
        // 添加JAX-RS接口实现类
        .addExtensions(xxxx)
        .server()
        .start();
    }
```
{{< /alert >}}

