---
sort: 3
---

# Access Log

使用Restlight访问日志拦截器时请确保已经引入了依赖：

```xml
<dependency>
	<groupId>io.esastack</groupId>
	<artifactId>restlight-ext-filter-starter</artifactId>
	<version>${restlight.version}</version>
</dependency>
```

## Quick Start

AccessLog拦截器在每个请求结束后记录访问日志，内容包含：客户端地址、请求协议、请求url（不包含路径参数）、请求方法、请求耗时、响应状态码、响应body大小以及访问时间。使用时需要做如下配置：

```properties
#开启AccessLog
restlight.server.ext.accesslog.enable=true
```

```note
访问日志默认会打印日志到logs/access.log文件中
```

## 配置

```note
所有配置均以`restlight.server.ext.accesslog` 开头
```

| 配置项       | 默认       | 说明                                                         |
| ------------ | ---------- | ------------------------------------------------------------ |
| enable       | false      | 是否启用                                                     |
| directory    | logs       | 日志文件路径                                                 |
| fileName     | access.log | 日志文件名                                                   |
| charset      |            | 日志编码                                                     |
| rolling      | true       | 是否按照时间滚动生成文件                                     |
| date-pattern | yyyy-MM-dd | 日期滚动格式，yyyy-MM-dd表示按天为单位滚动，生成的文件名为access.yyyy-MM-dd.log， 仅支持按天和小时为单位滚动，因此可选值：yyyy-MM-dd或者yyyy-MM-dd_HH（注意不要使用yyyy-MM-dd HH， 生成的文件名可能不符合操作系统文件命名规范） |
| max-history  | 10         | 最大历史文件个数                                             |
| full-uri     | false      | 是否打印uri中所有的内容（包含url参数）                       |
