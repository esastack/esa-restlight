---
sort: 4
---

# `ExceptionResolver`

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

