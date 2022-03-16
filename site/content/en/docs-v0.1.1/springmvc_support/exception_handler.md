---
tags: ["SpringMVC"]
title: "ExceptionHandler支持"
linkTitle: "ExceptionHandler支持"
weight: 20
---

`Restlight`支持业务自定义异常处理逻辑。对于Controller方法中抛出的异常，处理逻辑和顺序如下（**注意：一个异常只会被处理一次**）：

1.尝试在Controller内部查找异常处理方法来处理当前异常，未找到进入2
2.尝试寻找全局异常处理方法来处理当前异常，未找到则返回错误信息

## Controller级异常处理

局部异常处理方法只能处理当前Controller中抛出的异常，示例如下：

```java
@RestController
@RequestMapping("/exception")
public class LocalExceptionResolver {

    @RequestMapping("/willBeHandled")
    public void willBeHandled() {
        throw new IllegalArgumentException("IllegalArgumentException...");
    }

    @RequestMapping("/willNotBeHandled")
    public void willNotBeHandled() {
        throw new RuntimeException("RuntimeException...");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleLocalException(IllegalArgumentException ex) {
        return "HandleLocalException [" + ex.getMessage() + "]";
    }
}
```

`handleLocalException(XXX)`方法会处理当前类中所有的`IllegalArgumentException`异常。

## `@ControllerAdvice`全局异常处理
通过`@ControllerAdvice`或`@RestControllerAdvice`来标识全局异常处理类，并可通过相应的属性设置该全局异常处理类生效的范围。使用示例如下：

```java
@RestControllerAdvice(basePackages = {"esa.restlight.samples.starter.exceptionhandler", "esa.restlight.samples.starter.controller"})
public class GlobalExceptionResolver {

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception ex) {
        return Result.ok();
    }

    static class Result {
        private int status;
        private String msg;

        static Result ok() {
            Result r = new Result();
            r.status = 200;
            return r;
        }
        
        // getter and setter
    }

}
```

如上所示， `handleException(XXX)`方法会处理`esa.restlight.samples.starter.exceptionhandler`和`esa.restlight.samples.starter.controller`包中抛出的所有`Exception`。

## 高级使用

为了让用户更方便的使用异常处理， 类似的我们支持像Controller上一样的去使用一些注解的功能如 `@RequestParam`, `@RequestHeader`， `@CookieValue`等方便的进行参数绑定而不是自己手动的去Request里面取并做一次参数转换的操作。

eg:

```java
@ControllerAdvice
public class GlobalExceptionResolver {

    @ExceptionHandler(Exception.class)
    public String handleException(@RequestParam("foo") String foo, Exception ex) {
        // do something here...
        return "HandleGlobalException [" + ex.getMessage() + "]";
    }

}
```

{{< alert title="Warning" color="warning" >}}
由于流的特性，因此在Body中的数据如果已经被读取过后便不能进行再一次的读取。 因此这里无法满足类似`@RequestBody`的功能
{{< /alert >}}

## 异步异常处理器

异常处理器可以类似Controller中异步处理一样完成异步的处理
`Restlight`异步支持
- CompletableFuture
- ListenableFuture(Guava)
- Future(io.netty.util.concurrent.Future)

使用CompletableFuture异步方式：

```java
@ExceptionHanlder(Exception.class)
public CompletableFuture<String> handleExcption(Exception ex) {
    return CompletableFuture.supplyAsync(() -> {
        // ...
        return "Hello Restlight!";
    });
}
```
`ListenableFuture(Guava)`与`Future(io.netty.util.concurrent.Future)`同理。
