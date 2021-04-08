---
sort: 800
---

# IP白名单

使用Restlight内置的IP白名单时请先确保引入了依赖：

```xml
<dependency>
	<groupId>io.esastack</groupId>
	<artifactId>restlight-ext-filter-starter</artifactId>
	<version>${restlight.version}</version>
</dependency>
```

IP白名单拦截器可以过滤非法IP的访问。同时支持IP地址和正则表达式两种匹配方式，需要配置的内容如下：

```properties
#开启IP白名单拦截器的必需配置
restlight.server.ext.whitelist.enable=true

#IP白名单列表（多值请用逗号分隔,正则表达式regex:开头）
restlight.server.ext.whitelist.ips=10.10.1.1,regex:10.12.*

#缓存最近访问的IP地址（默认1024个）
restlight.server.ext.whitelist.cache-size=1024

#缓存的失效时间（单位：ms，默认为60s）
restlight.server.ext.whitelist.expire=60000
```