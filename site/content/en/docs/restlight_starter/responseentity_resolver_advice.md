---
tags: ["feature"]
title: "ResponseResolverAdvice"
linkTitle: "ResponseResolverAdvice"
weight: 80
description: >
    `ResponseEntityResolverAdvice`允许用户在`ResponseEntityResolver`返回值处理的前后添加业务逻辑。
---

## 接口定义

```java
public interface ResponseEntityResolverAdvice {

    /**
     * This method will be called around
     * {@link ResponseEntityResolver#writeTo(ResponseEntity, ResponseEntityChannel, RequestContext)}.
     *
     * @param context context
     * @throws Exception exception
     */
    void aroundWrite(ResponseEntityResolverContext context) throws Exception;

}
```

## 自定义`ResponseEntityResolverAdvice`

与`ResponseEntityResolver`相同， `ResponseEntityResolverAdvice`自定应时同样需要实现`ResponseEntityResolverAdviceAdapter`或`ResponseEntityResolverAdviceFactory`接口

### 方式1 实现`ResponseEntityResolverAdviceAdapter`

接口定义

```java
public interface ResponseEntityResolverAdviceAdapter
        extends ResponseEntityPredicate, ResponseEntityResolverAdvice, Ordered {

    @Override
    default void aroundWrite(ResponseEntityResolverContext context) {
        context.proceed();
    }

    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
```

### 方式2：实现`ResponseEntityResolverAdviceFactory`

接口定义

```java
public interface ResponseEntityResolverAdviceFactory extends ResponseEntityPredicate, Ordered {
    /**
     * 生成RResponseEntityResolverAdvice
     */
    ResponseEntityResolverAdvice createResolverAdvice(ResponseEntity entity);

    @Override
    default int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
```

`ResponseEntityResolverAdviceAdapter`接口以及`ResponseEntityResolverAdviceFactory`接口与`ResponseEntityResolver`中的`ResponseEntityResolverAdapter`接口以及`ResponseEntityResolverFactory`接口的使用方式相同， 这里不过多赘述。
