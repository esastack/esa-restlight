---
tags: ["feature"]
title: "返回值解析"
linkTitle: "返回值解析"
weight: 70
description: >
    返回值解析指将`Controller`返回值序列化并写入到`HttpResponse`中的过程。
---

## 返回值解析逻辑

Restlight默认支持的返回值解析方式包括

- `@ResponseBody`
- `@ResponseStatus`
- 普通类型(String, byte[], int 等)

与参数解析类似， 每个功能都对应了一个返回值解析器的实现。

## **接口定义**

```java
public interface ResponseEntityResolver {

    /**
     * 解析出对应返回值并通过channel写回
     */
    HandledValue<Void> writeTo(ResponseEntity entity,
                               ResponseEntityChannel channel,
                               RequestContext context) throws Exception;

}
```

{{< alert title="Tip" color="warning" >}}
> 考虑到直接实现`ResponseEntityResolver`接口实现的复杂性，在实际使用时建议直接继承`AbstractResponseEntityResolver`类并重写
> 其中的模版方法`serialize(ResponseEntity entity, List<MediaType> mediaTypes, RequestContext context)`将返回值序列化成byte[]，
> 其余的操作由`AbstractResponseEntityResolver`自动完成，下文示例均采用该种方式。
{{< /alert >}}

框架会在启动的初始化阶段试图为每一个Controller中的每一个参数都找到一组与之匹配的`ResponseEntityResolver`用于响应的返回值解析。

## 框架如何确定`ResponseEntityResolver`

{{< alert title="Note" color="warning" >}}
框架在**初始化阶段**会预先通过`HandlerPredicate#supports(HandlerMethod method)`来确定一组可以用来处理给定方法返回值的`ResponseEntityResolver`
并进行排序，在**运行过程中**仍可在执行`ResponseEntityResolver#writeTo(ResponseEntity entity, ResponseEntityChannel channel, RequestContext context)`
过程中再次进行判断，如果当前`ResponseEntityResolver`不支持处理当前请求的返回值，则可以直接返回`HandledValue.failed()`，框架将继续尝试下一个优先级的
`ResponseEntityResolver`，直至遍历完所有跟当前方法绑定的`ResponseEntityResolver`。

> 需要说明地是：1.大部分场景下在初始化阶段绑定到`HandlerMethod`上的`ResponseEntityResolver`在运行时均不需要再次进行判断，
> 可以直接进行处理（这也是框架默认的行为），只有在特殊的场景下，请求执行过程中参数的改变将影响到返回值解析逻辑的时候才需要在
> 实际处理过程中进行二次匹配，比如`JAX-RS`标准中定义的行为。2.下文所展示的均为初始化绑定的使用方式。
{{< /alert >}}

### `ResponseEntityResolverAdapter`

```java

public interface HandlerPredicate {

    /**
     * 判断当前ResponseEntityResolver是否支持给定HandlerMethod解析
     * 每一个Controller都对应一个HandlerMethod实例， 
     * 可以通过HandlerMethod获取注解, 返回值类型等各类反射相关的元数据信息
     */
    boolean supports(HandlerMethod method);

}

public interface ResponseEntityResolverAdapter extends ResponseEntityResolver, HandlerPredicate, Ordered {

    @Override
    default boolean supports(HandlerMethod method) {
        return true;
    }

    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
```

初始化逻辑：

1. 按照`getOrder()`方法的返回将Spring容器中所有的`ResponseEntityResolverAdapter`进行排序
2. 按照排序后的顺序依次调用`supports(HandlerMethod method)`方法， 返回`true`则将其作为该参数的`ResponseEntityResolver`，运行时每次请求都将按顺序调用`writeTo(ResponseEntity entity, ResponseEntityChannel channel, RequestContext context)`方法进行返回值处理，直至处理成功。
3. 未找到则启动报错。

细心的人可能会发现该设计可能并不能覆盖到以下场景

- 因为`writeTo(ResponseEntity entity, ResponseEntityChannel channel, RequestContext context)`方法参数中并没有传递`HandlerMethod`参数，虽然初始化阶段能根据`HandlerMethod method`方法获取Controller方法元数据信息（获取某个注解，获取参数类型等等）判断是否支持，但是如果运行时也需要获取Controller方法的元数据信息（某个注解的值等）的话，此接口则无法满足需求。
- 假如`ResponseEntityResolverAdapter`实现中需要做序列化操作，因此期望获取到Spring容器中的序列化器时，则该接口无法支持。

针对以上问题， 答案是确实无法支持。因为`Restlight`的设计理念是

- `能在初始化阶段解决的问题就在初始化阶段解决`

因此不期望用户以及`Restlight`的开发人员大量的在运行时去频繁获取一些JVM启动后就不会变动的内容(如： 注解的值)， 甚至针对某些元数据信息使用`ConcurrentHashMap`进行缓存（看似是为了提高性能的缓存， 实际上初始化就固定了的内容反而增加了并发性能的损耗）。

基于以上原因我们提供了另一个`ResponseEntityResolver`的实现方式

### `ResponseEntityResolverFactory`

```java
public interface ResponseEntityResolverFactory extends HandlerPredicate, Ordered {

    ResponseEntityResolver createResolver(HandlerMethod method,
                                          List<? extends HttpResponseSerializer> serializers);

    @Override
    default int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
```

与上面的`ResponseEntityResolver`类似

初始化逻辑：

1. 按照`getOrder()`方法的返回将所有的`ResponseEntityResolverFactory`进行排序
2. 按照排序后的顺序依次调用`supports(HandlerMethod method)`方法，返回`true`则将其作为该参数的`ResponseEntityResolverFactory`，同时调用`createResolver(HandlerMethod method, List<? extends HttpResponseSerializer> serializers)`方法创建出对应的`ResponseEntityResolver`
3. 未找到则启动报错。

由于初始化时通过`createResolver(HandlerMethod method, List<? extends HttpResponseSerializer> serializers)`方法传入了`HandlerMethod`以及序列化器，因此能满足上面的要求。

**两种模式的定位**

- `ResponseEntityResolver`： 适用于解析器不依赖方法元数据信息以及序列化的场景。例如： 如果Controller方法上上使用了@XXX注解则返回某个固定的值。
- `ResponseEntityResolverFactory`： 适用于解析器依赖方法元数据信息以及序列化的场景。例如： @ResponseBody， @ResponseStatus(reason = "error")。

## 自定义返回值解析器

将自定义实现的`ResponseEntityResolverAdapter`或者`ResponseEntityResolverFactory`注入到Spring容器即可。

- `ResponseEntityResolverAdapter`案例

场景： 当Controller方法上有`@AppId`注解时， 返回固定的AppId

```java
// 自定义注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AppId {
}
```

```java
@Bean
public ResponseEntityResolver resolver() {

    private static byte[] APP_ID = "your appid".getBytes(StandardCharsets.UTF_8);

    return new AbstractResponseEntityResolver() {

        @Override
        protected byte[] serialize(ResponseEntity entity,
                                   List<MediaType> mediaTypes,
                                   RequestContext context) {
            return APP_ID;
        }
        
        @Override
        public boolean supports(HandlerMethod method) {
            if (method == null) {
                return false;
            }
        
            // 当方法上有此注解时生效
            return method.hasMethodAnnotation(AppId.class);
        }
    };
}
```
controller使用

```java

@GetMapping("/foo")
@AppId
public String foo() {
    return "";
}

```

上面的代码自定义实现了依据自定义注解获取固定appId的功能

{{< alert title="Note" >}}
自定义`ResponseEntityResolverAdapter`比框架自带的优先级高（如`@ResponseBody`），如果匹配上了自定义实现，框架默认的功能在当前方法上上将不生效。
{{< /alert >}}


- `ResponseEntityResolverFactory`

场景： 通过自定义注解对所有String类型的返回值加上一个指定前缀。
```java
// 自定义注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Suffix {
    String value();
}
```

```java
    @Bean
    public ResponseEntityResolverFactory resolver() {
        return new ResponseEntityResolverFactory() {
    
            @Override
            public ResponseEntityResolver createResolver(HandlerMethod method,
                                                         List<? extends HttpResponseSerializer> serializers) {
                return new Resovler(method);
            }
            
            @Override
            public boolean supports(HandlerMethod method) {
                if (method == null) {
                    return false;
                }
                return String.class.equals(method.method().getReturnType()) 
                        && method.hasMethodAnnotation(CustomHeader.class, false);
            }
        };
    }

    /**
     * 实际ResponseEntityResolver实现
     */
    private static class Resolver extends AbstractResponseEntityResolver {

        private final String suffix;

        private Resolver(HandlerMethod method) {
            // 获取前缀
            Suffix anno = method.getMethodAnnotation(Suffix.class, false);
            this.suffix = anno.value();
        }

        @Override
        protected byte[] serialize(ResponseEntity entity,
                                   List<MediaType> mediaTypes,
                                   RequestContext context) {
            // 拼接
            return (suffix + entity.response().entity()).getBytes(StandardCharsets.UTF_8);
        }

    }
```
controller使用

```java

@GetMapping("/foo")
@Suffix
public String foo() {
    return "foo";
}

```

{{< alert title="Note" >}}
自定义`ResponseEntityResolverFactory`比框架自带的优先级高（如`@ResponseBody`），如果匹配上了自定义实现，框架默认的功能在当前方法上上将不生效。
{{< /alert >}}
