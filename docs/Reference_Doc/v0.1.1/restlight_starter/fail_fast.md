---
sort: 15
---

# 快速失败

Restlight 支持根据请求任务的排队时间快速失败。具体地，从接收到首字节（TTFB）或请求任务进入线程池开始排队时开始计时，
如果请求任务真正执行时的时间与起始时间的差值大于指定值（timeout），那么直接结束当前请求（返回500）。

使用时，需要配置timeout与起始计时时间（首字节时间或者开始排队时间，默认后者），示例如下：
```properties
restlight.server.scheduling.timeout.BIZ.type=QUEUED
restlight.server.scheduling.timeout.BIZ.time-millis=30

restlight.server.scheduling.timeout.IO.type=TTFB
restlight.server.scheduling.timeout.IO.time-millis=30
```
其中，`BIZ`和`IO`为Scheduler的名称，`type`为开始计时的方式，默认为`QUEUED`，
表示从请求任务进入线程池排队时开始计时，`TTFB`表示从接收到首字节时开始计时，`time-millis`
表示超时时间。
