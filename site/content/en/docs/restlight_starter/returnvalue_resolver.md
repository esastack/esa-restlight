---
tags: ["feature"]
title: "返回值解析"
linkTitle: "返回值解析"
weight: 70
description: >
    返回值解析指将`Controller`返回值写入到`AsyncResponse`中的过程（包含序列化）
---

## 返回值解析逻辑

Restlight默认支持的返回值解析方式包括

- `@ResponseBody`
- `@ResponseStatus`
- 普通类型(String, byte[], int 等)

与参数解析类似， 每个功能都对应了一个返回值解析器的实现。

## **接口定义**

```java
public interface ReturnValueResolver {

    /**
     * 解析出对应返回值为byte[]
     */
    byte[] resolve(Object returnValue,
                   AsyncRequest request,
                   AsyncReponse response) throws Exception;

}
```

框架会在启动的初始化阶段试图为每一个Controller中的每一个参数都找到一个与之匹配的`ReturnValueResolver`用于响应的返回值解析。

## 框架如何确定`ReturnValueResolver`

### `ReturnValueResolverAdapter`

```java

public interface ReturnValueResolverPredicate {

    /**
     * 判断当前ReturnValueResolver是否支持给定InvocableMethod解析
     * 每一个Controller都对应一个InvocableMethod实例， 
     * 可以通过InvocableMethod获取注解, 返回值类型等各类反射相关的元数据信息
     */
    boolean supports(InvocableMethod invocableMethod);

}

public interface ReturnValueResolverAdapter extends ReturnValueResolverPredicate, ReturnValueResolver, Ordered {

    @Override
    default int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

}
```

初始化逻辑： 

1. 按照`getOrder()`方法的返回将Srping容器中所有的`ReturnValueResolverAdapter`进行排序
2. 按照排序后的顺序依次调用`supports(InvocableMethod invocableMethod)`方法， 返回`true`则将其作为该参数的`ReturnValueResolver`， 运行时每次请求都将调用`resolve(Object returnValue, AsyncRequest request, AsyncReponse response)`方法进行参数解析， 并且运行时不会改变。
3. 未找到则启动报错。

细心的人可能会发现该设计可能并不能覆盖到以下场景

- 因为`resolve(Object returnValue, AsyncRequest request, AsyncReponse response)`方法参数中并没有传递`InvocableMethod参数`， 虽然初始化阶段能根据`supports(InvocableMethod invocableMethod)`方法获取Controller方法元数据信息（获取某个注解， 获取参数类型等等）判断是否支持， 但是如果运行时也需要获取Controller方法的元数据信息（某个注解的值等）的话，此接口则无法满足需求。
- 假如`ReturnValueResolverAdapter`实现中需要做序列化操作， 因此期望获取到Spring容器中的序列化器时，则该接口无法支持（例如`@ResponseBody`的场景）。

针对以上问题， 答案是确实无法支持。因为`Restlight`的设计理念是

- `能在初始化阶段解决的问题就在初始化阶段解决`

因此不期望用户以及`Restlight`的开发人员大量的在运行时去频繁获取一些JVM启动后就不会变动的内容(如： 注解的值)， 甚至针对某些元数据信息使用`ConcurrentHashMap`进行缓存（看似是为了提高性能的缓存， 实际上初始化就固定了的内容反而增加了并发性能的损耗）。

基于以上原因我们提供了另一个`ReturnValueResolver`的实现方式

### `ReturnValueResolverFactory`

```java
public interface ReturnValueResolverFactory extends ReturnValueResolverPredicate, Ordered {

    ReturnValueResolver createResolver(InvocableMethod invocableMethod,
                                    List<? extends HttpResponseSerializer> serializers);

    @Override
    default int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
```

与上面的`ReturnValueResolver`类似

初始化逻辑：

1. 按照`getOrder()`方法的返回将所有的`ReturnValueResolverFactory`进行排序
2. 按照排序后的顺序依次调用`supports(InvocableMethod invocableMethod)`方法， 返回`true`则将其作为该参数的`ReturnValueResolverFactory`， 同时调用`createResolver(InvocableMethod invocableMethod, List<? extends HttpResponseSerializer> serializers)`方法创建出对应的`ReturnValueResolver`
3. 未找到则启动报错。

由于初始化时通过`createResolver(InvocableMethod invocableMethod, List<? extends HttpResponseSerializer> serializers)`方法传入了`InvocableMethod`以及序列化器， 因此能满足上面的要求。

**两种模式的定位**

- `ReturnValueResolver`： 适用于解析器不依赖方法元数据信息以及序列化的场景。例如： 如果Controller方法上上使用了@XXX注解则返回某个固定的值。
- `ReturnValueResolverFactory`： 适用于解析器依赖方法元数据信息以及序列化的场景。例如： @ResponseBody， @ResponseStatus(reason = "error")。

## 自定义返回值解析器

将自定义实现的`ReturnValueResolverAdapter`或者`ReturnValueResolverFactory`注入到Spring容器即可。

- `ReturnValueResolverAdapter`案例

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
public ReturnValueResolverAdapter resolver() {

    private static byte[] APP_ID = "your appid".getBytes(StandardCharsets.UTF_8);

    return new ReturnValueResolverAdapter() {
        @Override
        public boolean supports(InvocableMethod invoicableMethod) {
            // 当方法上有此注解时生效
            return invoicableMethod.hasMethodAnnotation(AppId.class);
        }
 
        @Override
        public byte[] resolve(Object returnValue,
                   AsyncRequest request,
                   AsyncReponse response) {
            return APP_ID;
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
自定义`ReturnValueResolverAdapter`比框架自带的优先级高（如`@ResponseBody`）， 如果匹配上了自定义实现，框架默认的功能在当前方法上上将不生效。
{{< /alert >}}


- `ReturnValueResolverFactory`

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
    public ReturnValueResolverFactory resolver() {
        return new ReturnValueResolverFactory() {
            @Override
            public boolean supports(InvocableMethod invocableMethod) {
                // 当方法上有此注解时生效
                return String.class.equals(invocableMethod.getMethod().getReturnType()) && parameter.hasMethodAnnotation(CustomHeader.class);
            }
            
            public ReturnValueResolver createResolver(InvocableMethod invocableMethod,
                                                      List<? extends HttpResponseSerializer> serializers) {
                return new Resovler(invocableMethod);
            }
            
        };
    }

    /**
     * 实际ArgumentResolver实现
     */
    private static class Resolver implements ReturnValueResolver {

        private final String suffix;

        private Resolver(InvocableMethod invocableMethod) {
            // 获取前缀
            Suffix anno = invocableMethod.getMethodAnnotation(Suffix.class);
            this.suffix = anno.value();
        }

        @Override
        public byte[] resolve(Object returnValue,
                   AsyncRequest request,
                   AsyncReponse response) {
            // 拼接
            return suffix + String.valueOf(returnValue);
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
自定义`ReturnValueResolverFactory`比框架自带的优先级高（如`@ResponseBody`）， 如果匹配上了自定义实现，框架默认的功能在当前方法上上将不生效。
{{< /alert >}}
