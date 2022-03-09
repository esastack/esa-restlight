---
tags: ["feature"]
title: "返回值解析"
linkTitle: "返回值解析"
weight: 70
description: >
    返回值解析指将`Controller`返回值写入到`HttpResponse`中的过程（包含序列化）
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
     * Writes the given {@code value} to {@link HttpResponse}.
     *
     * @param entity  entity
     * @param channel the channel to write resolved {@code entity}
     * @param context context
     * @return resolved value, which must not be {@code null}.
     * @throws Exception any exception
     */
    HandledValue<Void> writeTo(ResponseEntity entity,
                               ResponseEntityChannel channel,
                               RequestContext context) throws Exception;

}
```

## 框架如何确定`ResponseEntityResolver`
**运行时匹配：**

1. 按照`getOrder()`方法的返回将Spring容器中所有的`ResponseEntityResolver`进行排序（初始化时已经完成并且不会动态改变）
2. 按照排序后的顺序依次调用`writeTo(ResponseEntity, ResponseEntityChannel, RequestContext)`方法， 返回`HandledValue.isSuccess()`则表示响应body成功序列化并写回，否则继续调用下一个`ResponseEntityResolver`的`writeTo`方法。
3. 未找到则运行时报错。

细心的人可能会发现该设计可能并不能覆盖到以下场景

- 假如`ResponseEntityResolver`实现中需要做序列化操作， 因此期望获取到Spring容器中的序列化器时，则该接口无法支持（例如`@ResponseBody`的场景）。

针对以上问题， 答案是确实无法支持。因为`Restlight`的设计理念是

- `能在初始化阶段解决的问题就在初始化阶段解决`

基于以上原因我们提供了另一个`ResponseEntityResolverFactory`的实现方式

### `ResponseEntityResolverFactory`

```java
public interface ResponseEntityResolverFactory extends Ordered {

    /**
     * Creates an instance of {@link ResponseEntityResolverAdvice} for given handler method.
     *
     * @param serializers all the {@link HttpResponseSerializer}s in the context
     * @return resolver
     */
    ResponseEntityResolver createResolver(List<? extends HttpResponseSerializer> serializers);

    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
```

由于初始化时通过`createResolver(List<? extends HttpResponseSerializer> serializers)`方法传入了序列化器， 因此能满足上面的要求。

**两种模式的定位**

- `ResponseEntityResolver`： 适用于解析器不依赖方法元数据信息以及序列化的场景。例如： 如果Controller方法上上使用了@XXX注解则返回某个固定的值。
- `ResponseEntityResolverFactory`： 适用于解析器依赖方法元数据信息以及序列化的场景。例如： @ResponseBody， @ResponseStatus(reason = "error")。

## 自定义返回值解析器

将自定义实现的`ResponseEntityResolver`或者`ResponseEntityResolverFactory`注入到Spring容器即可。

- `ResponseEntityResolver`案例

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

    return new ResponseEntityResolver() {
        @Override
        public HandledValue<Void> writeTo(ResponseEntity entity, ResponseEntityChannel channel,
            RequestContext context) throws Exception {
                if (entity.handler().isPresent()
                        && entity.handler().get().hasMethodAnnotation(APPID.class, false)) {
                    channel.writeThenEnd(APP_ID);
                    return HandledValue.succeed(null);
                } else {
                    return HandledValue.failed();
                }
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
自定义`ResponseEntityResolver`比框架自带的优先级高（如`@ResponseBody`）， 如果匹配上了自定义实现，框架默认的功能在当前方法上上将不生效。
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
            public ResponseEntityResolver createResolver(List<? extends HttpResponseSerializer> serializers) {
                return new Resolver();
            }
        };
    }

    /**
     * 实际ArgumentResolver实现
     */
    private static class Resolver implements ResponseEntityResolver {

        @Override
        public HandledValue<Void> writeTo(ResponseEntity entity, ResponseEntityChannel channel,
                                          RequestContext context) throws Exception {
            Optional<HandlerMethod> method = entity.handler();
            if (!method.isPresent() || !String.class.equals(method.get().method().getReturnType())) {
                return HandledValue.failed();
            }
            if (!method.get().hasMethodAnnotation(CustomHeader.class, false)) {
                return HandledValue.failed();
            }
            Suffix anno = method.get().getMethodAnnotation(Suffix.class);
            channel.writeThenEnd((anno.value() + String.valueOf(entity.response().entity())).getBytes(StandardCharsets.UTF_8));
            return HandledValue.succeed(null);
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
自定义`ResponseEntityResolverFactory`比框架自带的优先级高（如`@ResponseBody`）， 如果匹配上了自定义实现，框架默认的功能在当前方法上上将不生效。
{{< /alert >}}
