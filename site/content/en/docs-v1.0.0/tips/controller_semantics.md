---
tags: ["tips"]
title: "Controller 语义不能重复"
linkTitle: "Controller 语义不能重复"
weight: 10
---

出于性能考虑，Restlight中的所有`RequestMapping`的path属性不允许有语义上的重复（除非业务能容忍这个问题）， 如果存在语义上的重复Restlight既不会在启动时报错提示，也不保证真正调用时的`稳定性`。

{{< alert title="Note" >}}
这里的`稳定性`指的是不保证任何的优先级，比如Controller A和Controller B重复，Restlight不保证真正调用的时候会调用Controller A或者Controller B。
{{< /alert >}}

- 错误示例1：

```
@RequestMapping("/foo")
public void foo() {
    // ...
}

@PostMapping("/foo")
public void foo1() {
    // ...
}
```

{{< alert title="Warning" color="warning" >}}
Controller中的path重复
{{< /alert >}}


- 错误示例2：

```
@RequestMapping("/foo/{bar}")
public void foo(@PathVariable String bar) {
    // ...
}

@PostMapping("/foo/bar")
public void foo1() {
    // ...
}
```

{{< alert title="Warning" color="warning" >}}
Controller中的`@PathVariable`与第二个Congroller的path重复
{{< /alert >}}

{{< alert title="Note" >}}
- 同样的道理，如果Controller语义存在相交的语义（可能存在使用复杂的正则等方式定义的Controller接口，因此可能存在相交的情况）也适用。
- Restlight检测到歧义时会打印WARNING日志
{{< /alert >}}
