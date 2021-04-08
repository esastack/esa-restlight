---
sort: 11
---

# `@QueryBean`

支持将Url中的参数与Form表单中的参数（仅当`Content-Type`为`application/x-www-form-urlencoded`时有效）聚合到Bean中。

eg：

请求 `/test?id=1&msg=hello` 中的id和message的值将绑定到pojo参数中

```java
@GetMapping(value = "/test")
public String handle(@QueryBean Pojo Pojo) {
    return "";
}

private static class Pojo {

    private int id;

    @QueryBean.Name("msg")
    private String message;

    public int getId() {
        return id;
    }

    //getter & setter
}
```

```note
加了@QueryBean.Name("name")之后将使用提供的name作为参数名进行匹配， 原来的字段名字将不会使用
```