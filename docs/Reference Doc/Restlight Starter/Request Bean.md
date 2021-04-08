---
sort: 1000
---

# `@RequestBean`

支持将请求的参数聚合到Bean中


eg.

```java
@GetMapping(value = "/test")
public String foo(@RequestBean Pojo Pojo) {
    return "";
}

private static class Pojo {

    @QueryParam("id")
    private int id;

    @HeaderParam("message")
    private String message;

    private AsyncRequest request;
    private AsyncResponse response;

    public int getId() {
        return id;
    }

    //getter & setter
}
```

```note
由于`SpringMVC`的注解大多不支持在`Field`上使用， 因此仅支持`JAX-RS`注解以及自定义参数解析等场景。
```
