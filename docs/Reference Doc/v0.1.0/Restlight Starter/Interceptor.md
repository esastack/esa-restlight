---
sort: 3
---

# 拦截器

`Restlight`支持多种拦截器，适用于不同性能/功能场景

- RouteInterceptor
- MappingInterceptor
- HandlerInterceptor
- InterceptorFactory

```tip
实现对应的拦截器接口并注入Spring即可
```

## 拦截器定位

面向`Controller/Route`， 同时支持按需匹配的拦截器

1. **面向`Controller/Route`**: 拦截器一定能让用户根据需求选择匹配到哪个`Controller`接口
2. **按需匹配**： 支持按照`AsyncRequest`条件让用户灵活决定拦截器的匹配规则

## `InternalInterceptor`

核心拦截器实现， 封装了拦截器核心行为

- `CompletableFuture<Boolean> preHandle0(AsyncRequest, AsyncResponse, Object)`

  `Controller`执行前执行。返回布尔值表示是否允许当前请求继续往下执行。

- `CompletableFuture<Void> postHandle0(AsyncRequest, AsyncResponse, Object)`

  `Controller`刚执行完之后执行

- `CompletableFuture<Void> afterCompletion0(AsyncRequest, AsyncResponse, Exception)`

  请求行完之后执行

- `int getOrder()`

  返回当前拦截器的优先级（默认为最低优先级），我们保证`getOrder`返回值决定拦截器的执行顺序，但是不支持使用`@Order(int)`注解情况下的顺序（虽然有时候看似顺序和`@Order`一致，但那只是巧合）。

```note
- 框架在真正执行拦截器调用的时候只会调用上述方法(而不是preHandle(xxx), postHandle(xxx), afterCompletion(xxx))
- 请勿直接使用此接口，此接口仅仅是拦截器的核心实现类
```

## 拦截器匹配

**初始化阶段**： 初始化阶段为每一个`Controller`确定所有可能匹配到当前`Controller`的拦截器列表（包含可能匹配以及一定会匹配到当前`Controller`的拦截器）。

**运行时阶段**： 一个请求`AsyncRequest`到来时将通过将`AsyncRequest`作为参数传递到拦截器做路由判定， 决定是否匹配。

```tip
通过初始化与运行时两个阶段的匹配行为扩展满足用户多样性匹配需求， 用户既可以直接将拦截器绑定到固定的`Controller`也可以让拦截器随心所欲的根据请求去选择匹配。
```

### `Affinity`亲和性

```java
public interface Affinity {

    /**
     * Current component is always attaching with the target subject.
     */
    int ATTACHED = 0;

    /**
     * Current component has a high affinity with the target subject this is the highest value.
     */
    int HIGHEST = 1;
    /**
     * Current component has no affinity with the target subject.
     */
    int DETACHED = -1;

    /**
     * Gets the affinity value.
     *
     * @return affinity.
     */
    int affinity();
}
```

用于表达拦截器与`Controller/Route`之间的亲和性

- `affinity()`小于0表示拦截器**不可能**与`Controller`匹配
- `affinity()`等于0表示拦截器**一定**会与`Controller`匹配
- `affinity()`大于0表示拦截器**可能**会与`Controller`匹配， 并且值越小匹配可能性越高（因此1为最高可能性）， 相反值越大匹配可能性越小， 同时匹配的开销越大（用于拦截器匹配性能优化）。

### `InterceptorPredicate`

```java
public interface InterceptorPredicate extends RequestPredicate {
    InterceptorPredicate ALWAYS = request -> Boolean.TRUE;
}

public interface RequestPredicate extends Predicate<AsyncRequest> {
    // ignore this
    default boolean mayAmbiguousWith(RequestPredicate another) {
        return false;
    }
}
```

`test(AsyncRequest)`方法用于对每个请求的匹配。可以满足根据`AsyncRequest`运行时的任意条件的匹配（而不仅仅是局限于`URL`匹配）

## `Interceptor`

`Interceptor`接口为同时拥有`Affinity`（`Controller/Route`匹配）以及`InterceptorPredicate`（请求`AsyncRequest`匹配）的接口

```java
public interface Interceptor extends InternalInterceptor, Affinity {

    /**
     * Gets the predicate of current interceptor. determines whether current interceptor should be matched to a {@link
     * esa.httpserver.core.AsyncRequest}.
     *
     * @return predicate, or {@code null} if {@link #affinity()} return's a negative value.
     */
    InterceptorPredicate predicate();

    /**
     * Default to highest affinity.
     * <p>
     * Whether a {@link Interceptor} should be matched to a {@link esa.restlight.server.route.Route} is depends on it.
     *
     * @return affinity
     */
    @Override
    default int affinity() {
        return HIGHEST;
    }
}
```

由于`int affinity()`根据不同的`Controller/Route`可能得出不同的结果， 因此需要使用`InterceptorFactory`进行创建

eg.

实现一个拦截器， 拦截所有GET接口（仅包含GET）且Header中包含`X-Foo`请求头的请求

```java
@Bean
public InterceptorFactory interceptor() {
    return (ctx, route) -> new Interceptor() {
        @Override
        public CompletableFuture<Boolean> preHandle0(AsyncRequest request,
                                                     AsyncResponse response,
                                                     Object handler) {
            // biz logic
            return CompletableFuture.completedFuture(null);
        }


        @Override
        public InterceptorPredicate predicate() {
            return request -> request.containsHeader("X-Foo");
        }

        @Override
        public int affinity() {
            HttpMethod[] method = route.mapping().method();
            if (method.length == 1 && method[0] == HttpMethod.GET) {
                return ATTACHED;
            }
            return DETACHED;
        }
    };
}
```

```note
运行时仅在`request.containsHeader("X-Foo")`上做匹配， 性能损耗极低。
```

## `RouteInterceptor`

只绑定到固定的`Controller/Route`的拦截器

```java
public interface RouteInterceptor extends InternalInterceptor {

    /**
     * Gets the affinity value between current interceptor and the given {@link Route}.
     *
     * @param ctx   context
     * @param route route to match
     *
     * @return affinity value.
     */
    boolean match(DeployContext<? extends RestlightOptions> ctx, Route route);
}
```

```tip
运行时无性能损耗， 用于需要将拦截器固定的绑定到`Controller`的场景， 这里的`boolean match(xx)`方法返回的`true`和`false`实际上相当于`Affinity`接口返回`Affinity.ATTACHED`以及`Affinity.DETACHED`（即只有一定匹配和一定不匹配）
```

eg.
实现一个拦截器， 拦截所有GET请求（仅包含GET）

```java
@Bean
public RouteInterceptor interceptor() {
    return new RouteInterceptor() {

        @Override
        public CompletableFuture<Boolean> preHandle0(AsyncRequest request,
                                                     AsyncResponse response,
                                                     Object handler) {
            // biz logic
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public boolean match(DeployContext<? extends RestlightOptions> ctx, Route route) {
            HttpMethod[] method = route.mapping().method();
            return method.length == 1 && method[0] == HttpMethod.GET;
        }
    };
}
```

## `MappingInterceptor`

绑定到所有`Controller/Route`， 并匹配请求的拦截器

```java
public interface MappingInterceptor extends InternalInterceptor, InterceptorPredicate {
}
```
相当于`affinity()`固定返回`Affinity.ATTACHED`

eg.

实现一个拦截器， 拦截所有Header中包含`X-Foo`请求头的请求

```java
@Bean
public MappingInterceptor interceptor() {
    return new MappingInterceptor() {

        @Override
        public CompletableFuture<Boolean> preHandle0(AsyncRequest request,
                                                     AsyncResponse response,
                                                     Object handler) {
            // biz logic
            return CompletableFuture.completedFuture(null);
        }
        
        @Override
        public boolean test(AsyncRequest request) {
            return request.containsHeader("X-Foo");
        }
    };
}
```

## `HandlerInterceptor`

支持基于`URI`匹配的拦截器接口

- `includes()`: 指定拦截器作用范围的Path， 默认作用于所有请求。
- `excludes()`: 指定拦截器排除的Path（优先级高于`includes`）默认为空。

```java
public interface HandlerInterceptor extends InternalInterceptor {

    String PATTERN_FOR_ALL = "/**";

    default String[] includes() {
        return null;
    }

    default String[] excludes() {
        return null;
    }

}
```

```warning
涉及到正则匹配， 会有较大性能损失， 且仅支持URI匹配。
```

eg.

实现一个拦截器， 拦截除`/foo/bar`意外所有`/foo/`开头的请求

```java
@Bean
public HandlerInterceptor interceptor() {
    return new HandlerInterceptor() {

        @Override
        public CompletableFuture<Boolean> preHandle0(AsyncRequest request,
                                                     AsyncResponse response,
                                                     Object handler) {
            // biz logic
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public String[] includes() {
            return new String[] {"/foo/**"};
        }

        @Override
        public String[] excludes() {
            return new String[] {"/foo/bar"};
        }
    };
}
```

## `InterceptorFactory`

拦截器工厂类， 用于为每一个`Controller/Route`生成一个`Interceptor`

```java
public interface InterceptorFactory {

    /**
     * Create an instance of {@link Interceptor} for given target handler before starting Restlight server..
     *
     * @param ctx deploy context
     *
     * @param route target route.
     * @return interceptor
     */
    Optional<Interceptor> create(DeployContext<? extends RestlightOptions> ctx, Route route);
}
```
