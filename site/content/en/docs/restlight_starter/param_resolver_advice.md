---
tags: ["feature"]
title: "ParamResolverAdvice"
linkTitle: "ParamResolverAdvice"
weight: 60
---

`ParamResolverAdvice`允许用户在`ParamResolver`参数解析器解析参数的前后添加业务逻辑以及修改解析后的参数。

## 接口定义

```java
public interface ParamResolverAdvice {

    /**
     * This method is called around {@link ParamResolver#resolve(RequestContext)}.
     *
     * @param context context
     * @return resolved arg value
     * @throws Exception exception
     */
    Object aroundResolve(ParamResolverContext context) throws Exception;

}
```

## 自定义`ParamResolverAdvice`

与`ParamResolver`相同，`ParamResolverAdvice`自定应时同样需要实现`ParamResolverAdviceAdapter`或`ParamResolverAdviceFactory`接口

### 方式1：实现 `ParamResolverAdviceAdapter`

接口定义

```java
public interface ParamResolverAdviceAdapter extends ParamPredicate, ParamResolverAdvice, Ordered {

    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
```

### 方式2：实现`ParamResolverAdviceFactory`

接口定义

```java
public interface ParamResolverAdviceFactory extends ParamPredicate, Ordered {
    /**
     * Creates an instance of {@link ParamResolverAdvice} for given handler method.
     *
     * @param param    method
     * @param resolver httpParamResolver associated with this parameter
     * @return advice
     */
    ParamResolverAdvice createResolverAdvice(Param param, ParamResolver resolver);
}
```

`ParamResolverAdviceAdapter`接口以及`ParamResolverAdviceFactory`接口与`ParamResolver`中的`ParamResolverAdapter`接口以及`ParamResolverFactory`接口的使用方式相同，这里不过多赘述。

{{< alert title="Note" >}}
`ParamResolverAdvice`与`ParamResolver`生命周期是相同的, 即应用初始化的时候便会决定每个参数的`ParamResolverAdvice`
{{< /alert >}}
