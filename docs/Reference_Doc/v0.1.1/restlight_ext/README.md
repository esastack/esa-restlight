---
sort: 4
---

# 扩展能力

`Restlight`内置了常用的Filter（IP白名单、新建连接数限制、CPU过载保护）、Interceptor(访问日志、参数签名验证)和表单参数解析器，对应的扩展包分别为：


{% include list.liquid all=true %}

可以一次引入所有扩展能力

```xml

<dependency>
	<groupId>io.esastack</groupId>
	<artifactId>restlight-ext-starter</artifactId>
	<version>${restlight.version}</version>
</dependency>
```

