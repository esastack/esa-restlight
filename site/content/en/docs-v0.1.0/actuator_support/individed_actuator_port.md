---
tags: ["Actuator"]
title: "使用独立端口"
linkTitle: "使用独立端口"
weight: 40
description: >
    默认情况下健康检查的接口都将与`Restight`使用同一个HttpServer服务， 如果需要将健康检查接口与业务接口分别使用不同的端口则需要添加自定义配置
---

详细配置如下：
```properties
#配置健康检查暴露的端口
management.server.port=8081
```

启动后看到日志打印

```properties
Started Restlight(Actuator) server in 386 millis on port:8081
```
{{< alert title="Warning" color="warning">}}
独立端口启动后所有的`Filter`, 序列化， `ArgumentResolver`等扩展将与`Restlight`隔离
{{< /alert >}}

## 辅助配置

`SpringBoot`场景下大多数的配置可通过`application.properties`（或者yaml）配置文件即可完成配置，但是配置文件配置还是会有其缺陷

- 无法动态配置（这里的动态指的是通过代码计算等方式决定配置）
- 语法表达能力有限
- 配置过多变得冗杂

等问题。

### `ManagementConfigure`

用于支持`SpringBoot`场景显式配置

eg.

```java
@Bean
public ManagementConfigure configure() {
    return restlight -> {
        restlight.address(8081)
                .addFilter((request, response, chain) -> {
                    // biz logic
                    return chain.doFilter(request, response);
                });
        restlight.options().setCoreBizThreads(16);
        restlight.options().setMaxBizThreads(32);
        // more...
    };
}
```
