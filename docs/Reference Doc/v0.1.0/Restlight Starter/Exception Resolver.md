---
sort: 4
---

# 异常处理

## Spring MVC异常处理

[Restlight for Spring MVC](../SpringMVC%20Support/)支持使用Spring MVC中的`@ExceptionHandler`, `@ControllerAdvice`等注解，并且对此能力进行了增强
参考[ExceptionHandler支持](../SpringMVC%20Support/ExceptionHandler.html)

```note
仅在项目引入了`restlight-springmvc-provider`依赖的情况下使用（默认引入）
```

## `ExceptionResolver`

eg.

处理`RuntimeException`

```java
@Component
public class GlobalExceptionResolver implements ExceptionResolver<RuntimeException> {
    
    @Override
    public CompletableFuture<Void> handleException(AsyncRequest request,
                                                   AsyncResponse response,
                                                   RuntimeException e) {
        // handle exception here
        return CompletableFuture.completedFuture(null);
    }
}
```

```tip
处理不同异常类型实现不同的`ExceptionResolver<T extends Throwable>`
```