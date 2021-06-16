---
sort: 1
---

# Spring MVC 注解支持

## 支持的注解

- `@Controller`
- `@RestController`
- `@RequestMapping`
- `@GetMapping`
- `@PostMapping`
- `@PutMapping`
- `@DeleteMapping`
- `@PatchMapping`
- `@ResponseStatus`
- `@ControllerAdvice`
- `@ExceptionHandler`
- `@RequestParam`
- `@RequestHeader`
- `@PathVariable`
- `@CookieValue`
- `@MatrixVariable`
- `@RequestAttribute`

## 注解能力扩展

`@RequestParam`, `@ReqeustHeader`,`@PathVariable`,`@CookieValue`, `@MatrixVariable`,`@RequestAttribute`

参数绑定相关注解除`SpringMVC`用法外，同时支持

### `Constructor`接收一个`String`类型的参数

eg.

```java
public void foo(@RequestParam User user) {
    //...
}

static class User {
    final String name;
    public User(String str) {
        this.name = name;
    }
}
```

### 存在静态的`valueOf()`或者`fromString()`方法

eg.

```java
public void foo(@RequestParam User user, @RequestHeader Car car) {
    //...
}

static class User {
    String name;
    
    public static User valueOf(String str) {
        User user = new User();
        user.name = name;
        return user;
    }
}

static class Car {
    String name;
    
    public static Car fromString(String str) {
        Car car = new Car();
        car.name = name;
        return car;
    }
}
```

### 个别注解说明

### `@RequestParam`

除普通用法外， 当未指定`value()`或者`name`且参数对象为`Map<String, List<String>`类型时， 将整个ParameterMap（即AsyncRequest.getParameterMap）作为参数的值。

eg.

```java
public void foo(@RequestParam Map<String, List<String> params) {
    //...
}
```

```note
除url中的参数之外同时支持Post中Content-Type为application/x-www-form-urlencoded的form表单参数
```

### `@CookieValue`

普通String类型

```java
public void foo(@CookieValue String c) {
    //...
}
```

Cookie对象(io.netty.handler.codec.http.cookie.Cookie)

```java
public void foo(@CookieValue Cookie c) {
    //...
}
```

获取所有的Cookie

```java
public void foo(@CookieValue Set<Cookie> cookies) {
    //...
}
```

### `@RequestHeader`

除获取单个header之外， 可以如果参数类型为`io.netty.handler.codec.http.HttpHeaders`则以所有的Header作为参数的值

eg:

```java
public void foo(@RequestHeader HttpHeaders headers) {
    //...
}
```