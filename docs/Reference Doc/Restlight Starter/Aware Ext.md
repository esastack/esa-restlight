---
sort: 1400
---

# `Aware`扩展

在`Spring`场景，`Restlight`支持通过`xxxAware`接口获取一些内部对象。

其中包含

- `RestlightBizExecutorAware`: 获取业务线程池
- `RestlightIoExecutorAware`: 获取IO线程池
- `RestlightServerAware`: 获取`RestlightServer`
- `RestlightDeployContextAware`: 获取`DeployContext`

eg.

获取业务线程池

```java
@Controller
public class HelloController implements RestlightBizExecutorAware {

    private Executor bizExecutor;

    @Override
    public void setRestlightBizExecutor(Executor bizExecutor) {
        this.bizExecutor = bizExecutor;
    }


    @GetMapping("/foo")
    public CompletableFuture<String> foo() {
        return CompletableFuture.supplyAsync(() -> "Hello Restlight!", bizExecutor);
    }

}
```

