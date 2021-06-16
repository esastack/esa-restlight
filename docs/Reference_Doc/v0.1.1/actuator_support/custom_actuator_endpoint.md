---
sort: 3
---

# 自定义`Endpoint`

用户可以自己定义`Endpoint`实现定制化的健康检查接口

eg

```java
@Endpoint(id = "appId")
public class AppIdEndpoint {

    @ReadOperation
    public String appId() {
        return "esa-restlight";
    }
}
```

上面的代码自定义了一个`Endpoint`接口并返回appid

将上面接口注入Spring容器

```java
@Bean
public AppIdEndpoint endpoint() {
    return new AppIdEndpoint();
}
```

启动之后访问`curl -X GET localhost:8080/actuator/appId`

返回

```properties
esa-restlight
```

## 自定义异步`EndPoint`

用户可以自己定义基于`Completablefture`的`Endpoint`实现定制化的健康检查接口

eg

```java
@Endpoint(id = "appId")
public class AppIdEndpoint {

    @ReadOperation
    public CompletableFuture<String> appId() {
        return CompletableFuture.supplyAsync(() -> {

            // do something...

            return "esa-restlight";
        });
    }
}
```

上面的代码自定义了一个异步的`Endpoint`接口并返回appid

将上面接口注入Spring容器

```java
@Bean
public AppIdEndpoint endpoint() {
    return new AppIdEndpoint();
}
```

启动之后访问`curl -X GET localhost:8080/actuator/appId`

返回

```pro
esa-restlight
```
