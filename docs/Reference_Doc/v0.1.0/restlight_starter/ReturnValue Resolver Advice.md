---
sort: 8
---

# `ReturnValueResolverAdvice`

`ReturnValueResolverAdvice`允许用户在`ReturnValueResolver`参数解析器解析参数的前后添加业务逻辑以及修改解析后的参数。

## 接口定义

```java
public interface ReturnValueResolverAdvice {
    /**
     * 使用此方法的返回值作为ReturnValueResolver.resolve()的参数调用
     */
    Object beforeResolve(Object returnValue, AsyncRequest request, AsyncResponse response);
}
```

## 自定义`ReturnValueResolverAdvice`

与`ArgumentResovler`相同， `ReturnValueResolverAdvice`自定应时同样需要使用`ReturnValueResolverAdviceAdapter`接口以及`ReturnValueResolverAdviceFactory`接口

### 方式1：自定义`ReturnValueResolverAdviceAdapter`

```java
public interface ReturnValueResolverAdviceAdapter
        extends ReturnValueResolverPredicate, ReturnValueResolverAdvice, Ordered {

    @Override
    default Object beforeResolve(Object returnValue, AsyncRequest request, AsyncResponse response) {
        return returnValue;
    }

    @Override
    default int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
```

### 方式2：自定义`ReturnValueResolverAdviceFactory`

```java
public interface ReturnValueResolverAdviceFactory extends ReturnValueResolverPredicate, Ordered {
    /**
     * 生成ReturnValueResolverAdvice
     */
    ReturnValueResolverAdvice createResolverAdvice(InvocableMethod method, ReturnValueResolver resolver);

    @Override
    default int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
```

`ReturnValueResolverAdviceAdapter`接口以及`ReturnValueResolverAdviceFactory`接口与`ReturnValueResolver`中的`ReturnValueResolverAdapter`接口以及`ReturnValueResolverFactory`接口的使用方式相同， 这里不过多赘述。

```note
`ReturnValueResolverAdvice`与`ReturnValueResolver`生命周期是相同的, 即应用初始化的时候便会决定每个参数的`ReturnValueResolverAdvice`
```