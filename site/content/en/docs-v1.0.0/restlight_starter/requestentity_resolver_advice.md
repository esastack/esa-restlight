---
tags: ["feature"]
title: "RequestResolverAdvice"
linkTitle: "RequestResolverAdvice"
weight: 66
description: >
    `RequestEntityResolverAdvice`允许用户在`RequestEntityResolver`返回值处理的前后添加业务逻辑。
---

## 接口定义

```java
public interface RequestEntityResolverAdvice {

    /**
     * This method will be called around
     * {@link RequestEntityResolver#readFrom(RequestEntity, RequestContext)}.
     *
     * @param context context
     * @return object   resolved value
     * @throws Exception exception
     */
    Object aroundRead(RequestEntityResolverContext context) throws Exception;

}
```

## 自定义`RequestEntityResolverAdvice`

与`RequestEntityResolver`相同，`RequestEntityResolverAdvice`自定应时同样需要实现`RequestEntityResolverAdviceAdapter`或`RequestEntityResolverAdviceFactory`接口

### 方式1 实现`RequestEntityResolverAdviceAdapter`

接口定义

```java
public interface RequestEntityResolverAdviceAdapter extends RequestEntityResolverAdvice,
        ParamPredicate, Ordered {

    @Override
    default boolean supports(Param param) {
        return true;
    }

    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}


```

### 方式2：实现`RequestEntityResolverAdviceFactory`

接口定义

```java
public interface RequestEntityResolverAdviceFactory extends ParamPredicate, Ordered {
    /**
     * 生成RequestEntityResolverAdvice
     */
    RequestEntityResolverAdvice createResolverAdvice(Param param);

    @Override
    default int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
```

`RequestEntityResolverAdviceAdapter`接口以及`RequestEntityResolverAdviceFactory`接口与`RequestEntityResolver`中的`RequestEntityResolverAdapter`接口以及`RequestEntityResolverFactory`接口的使用方式相同，这里不过多赘述。
