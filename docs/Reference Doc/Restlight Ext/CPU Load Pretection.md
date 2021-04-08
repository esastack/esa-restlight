---
sort: 200
---

# CPU Load保护

当服务宿主机Cpu负载达到一定阈值之后开始随机丢弃连接（新建连接， 已经建立的连接不受影响）。

使用新建连接数限制时请先确保引入了依赖：

```xml
<dependency>
	<groupId>io.esastack</groupId>
	<artifactId>restlight-ext-filter-starter</artifactId>
	<version>${restlight.version}</version>
</dependency>
```

```properties
#开启Cpu Load自我保护
restlight.server.ext.cpu-load-protection.enable=true
#cpu负载阈值，默认为80.0D cpu超过此负载之后将开始随机丢弃连接
restlight.server.ext.cpu-load-protection.threshold=80.0D
#初始连接丢弃率,默认为10.0D（0代表0%， 100代表100%， 可以传小数）
restlight.server.ext.cpu-load-protection.initial-discard-rate=10.0D
#最大连接丢弃率,默认为80.0D（0代表0%， 100代表100%， 可以传小数）
restlight.server.ext.cpu-load-protection.max-discard-rate=80.0D
```

上面的配置将会在cpu负载到达75%时开始随机丢弃20%的新建连接， 随着cpu负载的升高达到100%则将会丢弃80%的连接。

说明：

当cpu负载到达或者超过`cpu-load-threshold`的值时开始丢弃连接，初始连接丢弃概率为`initial-discard-rate`,  随着cpu负载升高， 丢弃率将随着cpu负载的升高而成正比的升高， 当cpu负载达到100%时丢弃率将达到`max-discard-rate`.

```tip
平滑的连接丢弃比率计算有助于根据cpu使用率的变化来调节连接的丢弃比率。
```
