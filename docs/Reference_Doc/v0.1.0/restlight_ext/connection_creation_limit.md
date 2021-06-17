---
sort: 1
---

# 新建连接数限制

使用新建连接数限制时请先确保引入了依赖：

```xml
<dependency>
	<groupId>io.esastack</groupId>
	<artifactId>restlight-ext-filter-starter</artifactId>
	<version>${restlight.version}</version>
</dependency>
```

当前服务的新建连接数进行QPS限制。超过连接数限制的请求将被拒绝。

使用方式：

```properties
#开启新建连接数限制
restlight.server.ext.connection-creation-limit.enable=true
#设置每秒限制4000个新建连接,默认为20000
restlight.server.ext.connection-creation-limit.max-per-second=40000
```