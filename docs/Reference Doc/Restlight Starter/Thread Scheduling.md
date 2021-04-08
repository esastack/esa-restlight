---
sort: 100
---

# Thread-Scheduling

![threadingmodel.png](../../img/ThreadingModel.png)

线程调度允许用户根据需要随意制定`Controller`在`IO`线程上执行还是在`Biz`线程上执行还是在自定义线程上运行。

## `Restlight`线程调度

### 使用`@Scheduled`注解进行线程调度

eg.

在`IO`线程上执行

```java
@Scheduled(Schedulers.IO)
@GetMapping("/foo")
public String io() {
    // ....
    return "";
}
```

在`BIz`线程上执行

```java
// 在业务线程池中执行
@Scheduled(Schedulers.BIZ)
@GetMapping("/bar")
public String biz() {
    // ....
    return "";
}
```

不加注解默认Scheduler上执行

```java
@GetMapping("/baz")
public String bizBatching() {
    // ....
    return "";
}
```

### 自定义`Scheduler`

将`Scheduler`实现注入`Spring`

```java
@Bean
public Scheduler scheduler() {
    return Schedulers.fromExecutor("foo", Executors.newCachedThreadPool());
}
```

在自定义`Scheduler`上执行

```java
// 在业务线程池中执行
@Scheduled("foo")
@GetMapping("/foo")
public String foo() {
    // ....
    return "";
}
```

```tip
- 自定义`Scheduler`时请勿使用`IO`, `BIZ`, `Restlight`等作为name（作为`Restlight`中`Scheduler`的保留字)
- 定义线程池建议使用`RestlightThreadFactory`以获得更高的性能
```

```warning
基于`ThreadPoolExecutor`类型自定义`Scheduler`时， 不管是否设置`RejectExecutionHandler`，`Restlight`都会覆盖`ThreadPoolExecutor`中的`RejectExecutionHandler`， 即不允许用户自定义实现`RejectExecutionHandler`实现拒绝策略（因为`Restlight`需要保证每个请求都能被正确的完成，否则可能会导致链接等资源无法被释放等问题）， 相反如果自定义实现`Scheduler`时请保证每个请求都被正确的完成。
```

## 配置

所有配置均以`restlight.server.scheduling`开头

| 配置项                               | 默认                         | 说明                                                       |
| ------------------------------------ | ---------------------------- | ---------------------------------------------------------- |
| default-scheduler                    | BIZ        | 在不加`@Scheduled`注解时采用的Scheduler                                     |