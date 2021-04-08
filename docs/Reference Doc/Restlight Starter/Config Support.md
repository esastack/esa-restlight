---
sort: 17
---

# 辅助配置

`SpringBoot`场景下大多数的配置可通过`application.properties`（或者yaml）配置文件即可完成配置，但是配置文件配置还是会有其缺陷

- 无法动态配置（这里的动态指的是通过代码计算等方式决定配置）
- 语法表达能力有限（比如`ChannelOption`无法通过配置文件表达）
- 配置过多变得冗杂

等问题。

## `RestlightConfigure`

用于支持`SpringBoot`场景显式配置

eg.

```java
@Bean
public RestlightConfigure configure() {
    return restlight -> {
        restlight.address(8081)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .channelHandler(new LoggingHandler())
                .addFilter((request, response, chain) -> {
                    // biz logic
                    return chain.doFilter(request, response);
                })
                .deployments()
                .addHandlerInterceptor(new HandlerInterceptor() {
                    @Override
                    public boolean preHandle(AsyncRequest request,
                                             AsyncResponse response,
                                             Object handler) {
                        // biz logic
                        return true;
                    }
                });
        restlight.options().setCoreBizThreads(16);
        restlight.options().setMaxBizThreads(32);
        // more...
    };
}
```