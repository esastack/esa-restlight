---
tags: ["asynchronous"]
title: "CompletableFuture使用注意"
linkTitle: "CompletableFuture使用注意"
weight: 20
---

- `CompletableFuture`中**不带**`xxxAsync(xxx)`的方法默认会在当前线程执行(当前线程指的是调用`CompletableFuture.complete(xxx)`或者`CompletableFuture.completeExceptionally(xxx)`的线程)， 因此此种方式`CompletableFuture`的线程执行策略为尽量不切换线程（这里的尽量并不是完全一定， 因为如果当前future已经为完成的状态那么`CompletableFuture`会直接执行后续逻辑）
- `CompletableFuture`中**带**`xxxAsync(xxx)`的方法要求传入一个`Executor`， 后续回掉逻辑将在这个`Executor`中执行。 默认不传的情况下会使用`ForkJoinPool`中的公共线程池。 因此应当对所有的`xxxAsync(xxx)`方法调用格外注意， 一旦使用了错误的线程池可能导致隔离性的缺失， 性能不符合预期等问题。

