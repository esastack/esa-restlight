---
tags: ["feature"]
title: "参数解析"
linkTitle: "参数解析"
weight: 50
description: >
    参数解析指从请求中解析出`Controller`参数值的过程（不包含请求实体数据的解析）
---

典型的有

- `@RequestParam`
- `@RequestHeader`
- `@PathVariable`
- `@CookieValue`
- `@MatrixVariable`
- `@QueryBean`
- `RequestContext`
- `HttpRequest`
- `HttpResponse`

## 接口定义

```java
public interface ParamResolver extends Resolver {

    /**
     * Resolves method parameter into an argument value.
     *
     * @param context context
     * @return value resolved
     * @throws Exception ex
     */
    Object resolve(RequestContext context) throws Exception;

}
```

框架会在启动的初始化阶段试图为每一个Controller中的每一个参数都找到一个与之匹配的`ParamResolver`用于请求的参数解析。 

## 框架如何确定`ParamResolver`

### `ParamResolverAdapter`

```java

public interface ParamPredicate {

    /**
     * Whether current {@link ParamResolver} implementation is support given parameter.
     *
     * @param param param
     * @return {@code true} if it supports
     */
    boolean supports(Param param);

}

public interface ParamResolverAdapter extends ParamPredicate, ParamResolver, Ordered {

    @Override
    default int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

}
```

初始化逻辑： 

1. 按照`getOrder()`方法的返回将Spring容器中所有的`ParamResolver`进行排序
2. 按照排序后的顺序依次调用`supports(Param param)`方法， 返回`true`则将其作为该参数的`ParamResolver`，运行时每次请求都将调用`resolve(RequestContext context)`方法进行参数解析，并且运行时不会改变。
3. 未找到则启动报错。

{{< alert title="Note" >}}
- 如果需要解析请求Body内容请参考[请求实体数据解析](../requestentity_resolver)
{{< /alert >}}

细心的人可能会发现该设计可能并不能覆盖到以下场景
- 因为`resolve(RequestContext context)`方法参数中并没有传递`Param`参数， 虽然初始化阶段能根据`supports(Param param)`方法获取参数元数据信息（获取某个注解，获取参数类型等等）判断是否支持，但是如果运行时也需要获取参数的元数据信息（某个注解的值等）的话，此接口则无法满足需求。
- 假如`ParamResolver`实现中需要做序列化操作， 因此期望获取到Spring容器中的序列化器时，则该接口无法支持。

针对此问题，答案是确实无法支持。因为`Restlight`的设计理念是

- `能在初始化阶段解决的问题就在初始化阶段解决`

因此不期望用户以及`Restlight`的开发人员大量的在运行时去频繁获取一些JVM启动后就不会变动的内容(如： 注解的值)，甚至针对某些元数据信息使用`ConcurrentHashMap`进行缓存（看似是为了提高性能的缓存，实际上初始化就固定了的内容反而增加了并发性能的损耗）。

基于以上原因我们提供了另一个`ParamResolver`的实现方式

### `ParamResolverFactory`

```java
public interface ParamResolverFactory extends ParamPredicate, Ordered {

    ParamResolver createResolver(Param param,
                                 StringConverterProvider converters,
                                 List<? extends HttpRequestSerializer> serializers);

    @Override
    default int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
```

与上面的`ParamResolver`类似

初始化逻辑：

1. 按照`getOrder()`方法的返回将所有的`ParamResolverFactory`进行排序
2. 按照排序后的顺序依次调用`supports(Param param)`方法，返回`true`则将其作为该参数的`ParamResolverFactory`，同时调用`createResolver(Param param, StringConverterProvider converters, List<? extends HttpRequestSerializer> serializers)`方法创建出对应的`ParamResolver`
3. 未找到则启动报错。

由于初始化时通过`createResolver(Param param, StringConverterProvider converters, List<? extends HttpRequestSerializer> serializers)`方法传入了`Param`以及序列化器，因此能满足上面的要求。

**两种模式的定位**

- `ParamResolver`： 适用于参数解析器不依赖方法元数据信息以及序列化的场景。例如： 如果参数上使用了@XXX注解则返回某个固定的值。
- `ParamResolverFactory`： 适用于参数解析器依赖方法元数据信息以及序列化的场景。例如： `@RequestParameter(name = "foo")`。

## 自定义参数解析器

将自定义实现的`ParamResolverAdapter`或者`ParamResolverFactory`注入到Spring容器即可。

- `ParamResolverAdapter`案例

场景： 当参数上有`@AppId`注解时， 使用固定的AppId

```java
// 自定义注解
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AppId {
}
```

```java
@Bean
public ParamResolver resolver() {
    return new ParamResolverAdapter() {
        @Override
        public boolean supports(Param param) {
            // 当方法上有此注解时生效
            return param.hasAnnotation(AppId.class);
        }
        
        @Override
        public Object resolve(RequestContext context) {
            return "your appid";
        }
        
    };
}
```
controller使用

```java

@GetMapping("/foo")
public String foo(@AppId String appId) {
    return appId;
}

```

上面的代码自定义实现了依据自定义注解获取固定appId的功能

{{< alert title="Note" >}}
自定义`ParamResolverAdapter`比框架自带的优先级高（如`@RequestHeader`，`@RequestParam`等），如果匹配上了自定义实现，框架默认的功能在当前参数上将不生效。
{{< /alert >}}

- `ParamResolverFactory`

场景： 通过自定义注解获取固定前缀x-custom的Header
```java
// 自定义注解
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomHeader {
    String value();
}
```

```java
@Bean
public ParamResolverFactory resolver() {
    return new ParamResolverFactory() {
        @Override
        public ParamResolver createResolver(Param param,
                                            StringConverterProvider converters,
                                            List<? extends HttpRequestSerializer> serializers) {
            return new Resolver(param);
        }

        @Override
        public boolean supports(Param param) {
            // 当方法上有此注解时生效
            return param.hasAnnotation(CustomHeader.class);
        }
    };
}


/**
 * 实际ParamResolver实现
 */
private static class Resolver implements ParamResolver {

    private final String headerName;

    private Resolver(Param param) {
        CustomHeader anno = param.getAnnotation(CustomHeader.class);
        if (anno.value().length() == 0) {
            throw new IllegalArgumentException("Name of header must not be empty.");
        }
        // 初始化时组装好需要的参数
        this.headerName = "x-custom" + anno.value();
    }

    @Override
    public Object resolve(RequestContext context) {
        // 运行时直接获取Header
        return context.request().headers().get(headerName);
    }

}

```
controller使用

```java

@GetMapping("/foo")
public String foo(@CustomHeader("foo") String foo) {
    return foo;
}

```

{{< alert title="Note" >}}
自定义`ParamResolverFactory`比框架自带的优先级高（如`@RequestHeader`，`@RequestParam`等）， 如果匹配上了自定义实现，框架默认的功能在当前参数上将不生效。
{{< /alert >}}
