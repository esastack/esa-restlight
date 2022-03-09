---
tags: ["feature"]
title: "异常处理"
linkTitle: "异常处理"
weight: 40
---

## Spring MVC异常处理

[Restlight for Spring MVC](../../springmvc_support/)支持使用Spring MVC中的`@ExceptionHandler`, `@ControllerAdvice`等注解，并且对此能力进行了增强
参考[ExceptionHandler支持](../../springmvc_support/exception_handler)

{{< alert title="Note" >}}
仅在项目引入了`restlight-springmvc-provider`依赖的情况下使用（默认引入）
{{< /alert >}}

## `ExceptionResolver`

eg.

处理`RuntimeException`

```java
@Component
public class GlobalExceptionResolver implements ExceptionResolver<RuntimeException> {

    @Override
    public CompletionStage<Void> handleException(RequestContext context, RuntimeException e) {
        // handle exception here
        return CompletableFuture.completedFuture(null);
    }

}
```

{{< alert title="Tip" >}}
处理不同异常类型实现不同的`ExceptionResolver<T extends Throwable>`
{{< /alert >}}
