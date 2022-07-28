---
tags: ["feature"]
title: "配置一览"
linkTitle: "配置一览"
weight: 180
---

## Server相关配置

所有配置均以`restlight.server`开头, 基于properties的配置（yml以此类推）

| 配置项                               | 默认                         | 说明                                                       |
| ------------------------------------------------------------ | ---------------------------- | ---------------------------------------------------------- |
| host                                 | 0.0.0.0                      | 服务绑定的ip                                               |
| port                                 | 8080                         | 服务绑定的端口                                             |
| unix-domain-socket-file              |                              | 不为空则使用Unix Domain Socket绑定到此文件(优先级高于ip:port的方式）  |
| use-native-transports                | Linux环境下为true其余为false  | 是否使用原生epoll支持, 否则使用NIO的selector  |
| connector-threads                    | 1                            | 连接线程池大小                               |
| io-threads                           | cpu*2（默认不超过64）         | IO线程池大小                                 |
| biz-termination-timeout-seconds      | 60                           | 优雅停机等待超时时间                                       |
| http2-enable                         | false                        | 是否开启Http2           |
| compress                             | false                        | 是否启用HTTP响应压缩                                         |
| decompress                           | false                        | 是否启用HTTP请求解压                         |
| max-content-length                   | 4 * 1024 * 1024              | 最大contentLength限制(b)                                  |
| max-initial-line-length              | 4096                         | 最大request line限制(b)                                  |
| max-header-size                      | 8192                         | 最大header size限制(b)                                  |
| route.use-cached-routing             | true                         | 开启路由缓存                                       |
| route.compute-rate                   | 1                            | 路由计算率，取值范围0-1000，固定的概率之下更新路由 |
| warm-up.enable                       | false                        | 是否开启服务预热功能                                       |
| warm-up.delay                        | 0                            | 服务延迟暴露时间（单位：毫秒）                             |
| keep-alive-enable                    | true                         | false服务器将强制只支持短链接                                     |
| soBacklog                            | 128                          | 对应netty的ChannelOption.SO_BACKLOG                        |
| write-buffer-high-water-mark         | -1                           | netty中channel的高水位值                        |
| write-buffer-low-water-mark          | -1                           | netty中channel的低水位值                        |
| idle-time-seconds                    | 60                           | 连接超时时间                                               |
| logging                              |                              | 设置LoggingHandler用于打印连接及读写信息                                               |

## 核心功能配置

所有配置均以`restlight.server`开头, 基于properties的配置（yml以此类推）

| 配置项                               |             默认             |                 说明            |
| ------------------------------------ | ---------------------------- | ------------------------------- |
| context-path                         |                              |       全局path前缀               |
| biz-threads.core                     | cpu*4（默认在64-128之间）     |     业务线程池核心线程数          |
| biz-threads.max                      | cpu*6（默认在128-256之间）    |      业务线程池最大线程数         |
| biz-threads.blocking-queue-length    | 512                          |      业务线程池阻塞队列大小       |
| biz-threads.keep-alive-time-seconds  | 180                          | 业务线程池keepAliveTime 单位：秒  |
| serialize.request.negotiation        | false                        |     请求序列化协商                |
| serialize.request.negotiation-param  | format                       | 请求序列化协商参数名称             |
| serialize.response.negotiation       | false                        | 响应序列化协商                    |
| serialize.response.negotiation-param | format                       | 响应序列化协商参数名称             |
| print-banner                         | true                         | 是否启动打印logo                  |

## SSL配置

所有配置均以`restlight.server.ssl`开头, 基于properties的配置（yml以此类推）

| 配置项                               | 默认                         | 说明                                                       |
| ------------------------------------ | ---------------------------- | ---------------------------------------------------------- |
| enable                         | false                        | 是否使用https                                              |
| ciphers                        |                              | 支持的加密套件，不设置表示使用默认                         |
| enable-protocols               |                              | 支持的加密协议，不设置表示使用默认                         |
| cert-chain-path                |                              | 证书路径，https-enable为true时必须                         |
| key-path                       |                              | 私钥路径，https-enable为true时必须                         |
| key-password | | 私钥文件密钥（如果需要的话） |
| trust-certs-path | | Trust Store |
| session-timeout                |                             | session过期时间, 0表示使用默认                          |
| session-cache-size             |                             | session缓存大小， 0表示使用默认                      |
| handshake-timeout-millis | | SSL握手超时时间 |
| client-auth                    |                              | 客户端认证类型，不设置默认无                        |

{{< alert title="Tip" >}}
路径如果在classpath下请使用`classpath:conf/foo.pem`的形式
{{< /alert >}}

