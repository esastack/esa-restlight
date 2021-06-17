---
sort: 4
---

# 跨域

引入依赖

```xml
<dependency>
	<groupId>io.esastack</groupId>
	<artifactId>restlight-ext-filter-starter</artifactId>
	<version>${restlight.version}</version>
</dependency>
```

开启跨域功能

```properties
restlight.server.ext.cors.enable=true
```

更多跨域相关配置

```properties
restlight.server.ext.cors.rules[0].anyOrigin=false
restlight.server.ext.cors.rules[0].origins=www.example.com,www.demo.com
restlight.server.ext.cors.rules[0].expose-headers=foo,bar
restlight.server.ext.cors.rules[0].allow-credentials=false
restlight.server.ext.cors.rules[0].allow-methods=GET,POST
restlight.server.ext.cors.rules[0].allow-headers=foo,bar
restlight.server.ext.cors.rules[0].max-age=3600
```

```note
不配置则应用默认配置
```
