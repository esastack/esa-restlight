---
sort: 1
---

# Restlight 异步保证

- **`Restlight`保证在框架层面对所有的`CompletableFuture`处理都不会主动的切换执行的线程**。



这个保证使得用户无需担心不知道自己的代码将会在什么线程上执行。

```tip
上面的保证意味着如果用字自己切换了执行的线程（通常异步可能都会做这个动作）， 那么这个节点以后的所有代码都将在切换后的线程上执行， 而不是在`Restlight`的业务线程池。
```

**例**

用户在`HandlerInterceptor`中写下了如下代码

```java
private final Executor customExecutor = ...;
            
@Override
public CompletableFuture<Boolean> preHandle0(AsyncRequest request,
                                             AsyncResponse response,
                                             Object handler) {
    // 线程切换到 customExecutor并回调Restlight
    return CompletableFuture.supplyAsync(() -> {
        // ....
        return true;
    }, customExecutor);
}
```

其中当执行`HandlerInterceptor.preHandle(xxx)`时用户使用了自定义的线程池作为异步实现，并在完成操作后回调`Restlight`， 后续所有`Controller`, `ExceptionHandler`等操作都将在`customExecutor`的线程上执行（除非用户主动切换）



下面的`Controller`将会在`customExecutor`的线程上被调用， 而不是业务线程池

```java
@GetMapping(value = "/test")
public String foo() {
    // biz logic
    return "Hello Restlight!";
}
```

如果需要回到业务线程池执行则需要用户自行通过`CompletableFuture`进行操作

```java
@GetMapping(value = "/test")
public CompletableFuture<String> foo() {
    // 回到业务线程池执行Controller逻辑
    return CompletableFuture.supplyAsync(() -> {
        // biz logic
        return  "Hello Restlight!";
    }, bizExecutor);
}
```