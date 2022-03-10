---
tags: ["Core"]
title: "动态添加/卸载路由"
linkTitle: "动态添加/卸载路由"
weight: 1000
description: >
    `Restlight`提供了动态添加/卸载路由的功能，支持在应用运行期间实时添加/卸载路由，无需重启。
---

## 方式一：`Restlight-Core`单独使用

{{< alert title="Note" color="warning" >}}
使用该种方式需要手动生成`Route`信息。
{{< /alert >}}

eg.
#### 自定义`RouteRegistryAware`
```java
public interface RouteRegistryAware {

    /**
     * This callback method is invoked when all the internal routes have been registered to {@code registry}.
     *
     * @param registry registry
     */
    void setRegistry(RouteRegistry registry);

}
```

应用启动时通过自动注入的方式将`RouteRegistry`传递给`RouteRegistryAware`的实现类，在程序运行期间可以通过`RouteRegistry`动态的添加/卸载路由。

#### 将自定义的`RouteRegistryAware`在应用启动时添加到启动上下文中
```java
Restlight.forServer()
        .daemon(false)
        .deployments()
        .addRouteRegistryAware(new CustomRouteRegistryAware())
        .server()
        .start();
```

>> 通过将自定义的`RouteRegistryAware`或者`RouteRegistryAwareFactory`配置成SPI加载的形式或者注入`Spring`容器也可完成启动时注入的功能。

## 方式二：配合`SpringMVC`或者`JAX-RS`使用

{{< alert title="Note" >}}
使用该种方式时框架将自动根据传入的`Controller`类解析出`Route`信息，使用者只需要关心`Controller`即可。
{{< /alert >}}

eg.
#### 自定义`HandlerRegistryAware`
```java
public interface HandlerRegistryAware {

    /**
     * This callback method is invoked when all the internal handlers have been registered to {@code registry}.
     *
     * @param registry registry
     */
    void setRegistry(HandlerRegistry registry);

}
```

应用启动时通过自动注入的方式将`HandlerRegistry`传递给`HandlerRegistryAware`的实现类，在程序运行期间可以通过`HandlerRegistry`动态的添加/卸载路由。

#### 将自定义的`HandlerRegistryAware`在应用启动时添加到启动上下文中
```java
Restlight.forServer()
        .daemon(false)
        .deployments()
        .addHandlerRegistryAware(new CustomRouteRegistryAware())
        .server()
        .start();
```

{{< alert title="Note">}}
通过将自定义的`HandlerRegistryAware`或者`HandlerRegistryAwareFactory`配置成SPI加载的形式或者注入`Spring`容器也可完成启动时注入的功能。
{{< /alert >}}
