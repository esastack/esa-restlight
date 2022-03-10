---
tags: ["feature"]
title: "请求实体数据解析"
linkTitle: "请求实体数据解析"
weight: 65
description: >
    请求实体数据解析指将请求body的内容反序列化并解析出`Controller`参数值的过程。
---

典型的有

- `@RequestBody`

## 接口定义

```java
public interface RequestEntityResolver extends Resolver {

    /**
     * Deserialize the given {@code entity} to result.
     *
     * @param entity  entity
     * @param context context
     * @return resolved value, which must not be null.
     * @throws Exception any exception
     */
    HandledValue<Object> readFrom(RequestEntity entity, RequestContext context) throws Exception;

}

```

框架会在启动的初始化阶段试图为每一个Controller中的每一个参数都找到一个与之匹配的`RequestEntityResolver`用于请求的参数解析。

## 框架如何确定`RequestEntityResolver`

{{< alert title="Note" color="warning" >}}
框架在**初始化阶段**会预先通过`ParamPredicate#supports(Param param)`来确定一组可以用来处理给定方法参数的`RequestEntityResolver`
并进行排序，在**运行过程中**仍可在执行`RequestEntityResolver#readFrom(RequestEntity entity, RequestContext context)`
过程中再次进行判断，如果当前`RequestEntityResolver`不支持处理当前请求的实体内容解析，则可以直接返回`HandledValue.failed()`，框架将继续尝试下一个优先级的
`RequestEntityResolver`，直至遍历完所有跟当前方法绑定的`RequestEntityResolver`。

> 需要说明地是：1.大部分场景下在初始化阶段绑定到`Param`上的`RequestEntityResolver`在运行时均不需要再次进行判断，
> 可以直接进行处理（这也是框架默认的行为），只有在特殊的场景下，请求执行过程中参数的改变将影响到请求实体内容解析逻辑的时候才需要在
> 实际处理过程中进行二次匹配，比如`JAX-RS`标准中定义的行为。2.下文所展示的均为初始化绑定的使用方式。
{{< /alert >}}


### `RequestEntityResolverAdapter`

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

public interface RequestEntityResolverAdapter extends RequestEntityResolver, ParamPredicate, Ordered {

    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
```

初始化逻辑：

1. 按照`getOrder()`方法的返回将Spring容器中所有的`RequestEntityResolver`进行排序
2. 按照排序后的顺序依次调用`supports(Param param)`方法， 返回`true`则将其作为该参数的`RequestEntityResolver`，运行时每次请求都将按优先级顺序调用`readFrom(RequestEntity entity, RequestContext context)`方法进行参数解析，直至处理成功。
3. 未找到则启动报错。

{{< alert title="Note" >}}
- 如果需要解析其他`Controller`参数请求参考[参数解析](../param_resolver)
{{< /alert >}}

细心的人可能会发现该设计可能并不能覆盖到以下场景
- 因为`readFrom(RequestEntity entity, RequestContext context)`方法参数中并没有传递`Param`参数， 虽然初始化阶段能根据`supports(Param param)`方法获取参数元数据信息（获取某个注解，获取参数类型等等）判断是否支持，但是如果运行时也需要获取参数的元数据信息（某个注解的值等）的话，此接口则无法满足需求。
- 假如`RequestEntityResolver`实现中需要做序列化操作， 因此期望获取到Spring容器中的序列化器时，则该接口无法支持。

针对此问题，答案是确实无法支持。因为`Restlight`的设计理念是

- `能在初始化阶段解决的问题就在初始化阶段解决`

因此不期望用户以及`Restlight`的开发人员大量的在运行时去频繁获取一些JVM启动后就不会变动的内容(如： 注解的值)，甚至针对某些元数据信息使用`ConcurrentHashMap`进行缓存（看似是为了提高性能的缓存，实际上初始化就固定了的内容反而增加了并发性能的损耗）。

基于以上原因我们提供了另一个`RequestEntityResolver`的实现方式

### `ParamResolverFactory`

```java
public interface RequestEntityResolverFactory extends ParamPredicate, Ordered {

    RequestEntityResolver createResolver(Param param,
                                         List<? extends HttpRequestSerializer> serializers);

    @Override
    default int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
```

与上面的`RequestEntityResolver`类似

初始化逻辑：

1. 按照`getOrder()`方法的返回将所有的`RequestEntityResolverFactory`进行排序
2. 按照排序后的顺序依次调用`supports(Param param)`方法，返回`true`则将其作为该参数的`RequestEntityResolverFactory`，同时调用`createResolver(Param param, List<? extends HttpRequestSerializer> serializers)`方法创建出对应的`RequestEntityResolver`
3. 未找到则启动报错。

由于初始化时通过`createResolver(Param param, List<? extends HttpRequestSerializer> serializers)`方法传入了`Param`以及序列化器，因此能满足上面的要求。

**两种模式的定位**

- `RequestEntityResolver`： 适用于参数解析器不依赖方法元数据信息以及序列化的场景。例如： 如果参数上使用了@XXX注解则返回某个固定的值。
- `RequestEntityResolverFactory`： 适用于参数解析器依赖方法元数据信息以及序列化的场景。

## 自定义参数解析器

将自定义实现的`RequestEntityResolverAdapter`或者`RequestEntityResolverFactory`注入到Spring容器即可。

- `RequestEntityResolverAdapter`案例

场景： 当参数上有`@Pojo`注解时，使用固定的pojo

```java
// 自定义注解
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Pojo {
}
```

```java
private static final Pojo pojo = new Pojo();

@Bean
public RequestEntityResolver resolver() {

    return new RequestEntityResolverAdapter() {
        @Override
        public HandledValue<Object> readFrom(RequestEntity entity, RequestContext context) throws Exception {
            return HandledValue.succeed(pojo);
        }

        @Override
        public boolean supports(Param param) {
            return param.hasAnnotation(Pojo.class);
        }
    };
}
```
controller使用

```java

@GetMapping("/foo")
public Pojo foo(@Pojo Pojo pojo) {
    return pojo;
}

```

上面的代码自定义实现了依据自定义注解获取固定Pojo的功能

{{< alert title="Note" >}}
自定义`RequestEntityResolverAdapter`比框架自带的优先级高（如`@RequestBody`），如果匹配上了自定义实现，框架默认的功能在当前参数上将不生效。
{{< /alert >}}

- `RequestEntityResolverFactory`

场景： 通过自定义注解解析请求实体数据并将将解析后的POJO的name设置为固定值。
```java
// 自定义注解
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomPojo {
    String value();
}
```

```java
@Bean
public RequestEntityResolverFactory resolver() {
    return new RequestEntityResolverFactory() {
        @Override
        public RequestEntityResolver createResolver(Param param,
                                                    List<? extends HttpRequestSerializer> serializers) {
            return new Resolver(param);
        }

        @Override
        public boolean supports(Param param) {
            // 当方法上有此注解时生效
            return param.hasAnnotation(CustomPojo.class);
        }
    };
}

/**
 * 实际RequestEntityResolver实现
 */
private static class Resolver implements RequestEntityResolver {

    private final HttpRequestSerializer serializer;
    private final String fixedName;

    private Resolver(HttpRequestSerializer serializer, Param param) {
        CustomPojo anno = param.getAnnotation(CustomPojo.class);
        if (anno.value().length() == 0) {
            throw new IllegalArgumentException("Name of header must not be empty.");
        }
        // 初始化时组装好需要的参数
        this.fixedName = anno.value();
        this.serializer = serializer;
    }

    @Override
    public HandledValue<Object> readFrom(RequestEntity entity, RequestContext context) throws Exception {
        HandledValue<Object> handled = serializer.deserialize(entity);
        if (handled.isSuccess()) {
            Pojo pojo = (Pojo) handled.value();
            pojo.setName(fixedName);
        }
        return handled;
    }

}

```
controller使用

```java

@GetMapping("/foo")
public Pojo foo(@CustomPojo("foo") Pojo foo) {
    return foo;
}

```

{{< alert title="Note" >}}
自定义`RequestEntityResolverFactory`比框架自带的优先级高（如`@RequestBody`），如果匹配上了自定义实现，框架默认的功能在当前参数上将不生效。
{{< /alert >}}
