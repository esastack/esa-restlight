---
tags: ["Actuator"]
title: "Restlight Endpoint扩展"
linkTitle: "Restlight Endpoint扩展"
weight: 20
description: >
    在[Spring Boot Actuator](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-project/spring-boot-actuator)基础之上`Restlight`提供额外的功能扩展
---

## 业务线程池Metrics

{{< alert title="Note" >}}
业务线程当前采用JUC的`ThreadPoolExecutor`实现，后续版本可能会换成自定义实现，因此Metrics内容可能发生变更
{{< /alert >}}

### 1、Json 格式

**`Get` `actuator/bizthreadpool`**

Example:

```shell
curl -X GET localhost:8080/actuator/bizthreadpool
```

返回

```json
{
    "corePoolSize": 4,
    "maxPoolSize": 4,
    "queueLength": 1024,
    "keepAliveTimeSeconds": 180,
    "activeCount": 1,
    "poolSize": 4,
    "largestPoolSize": 4,
    "taskCount": 6,
    "queueCount": 0,
    "completedTaskCount": 5
}
```

## 业务线程池扩缩容

**`Post` `actuator/bizthreadpool`**

```shell
curl -X POST -H "Content-Type:application/json" -d "{\"corePoolSize\":\"1\",\"maxPoolSize\":\"2\"}" localhost:8080/actuator/bizthreadpool
```

## IO线程池Metrics
### 1、Json 格式
**`Get` `actuator/ioexecutor`**

Example:

```shell
curl -X GET localhost:8080/actuator/ioexecutor
```

返回

```json
{
    "childExecutors": [
        {
            "pendingTasks": 0,
            "maxPendingTasks": 2147483647,
            "ioRatio": 50,
            "taskQueueSize": 0,
            "tailTaskQueueSize": 0,
            "threadName": "Netty-I/O-1#0",
            "threadPriority": 5,
            "threadState": "RUNNABLE"
        },
        {
            "pendingTasks": 0,
            "maxPendingTasks": 2147483647,
            "ioRatio": 50,
            "taskQueueSize": 0,
            "tailTaskQueueSize": 0,
            "threadName": "Netty-I/O-1#1",
            "threadPriority": 5,
            "threadState": "RUNNABLE"
        }
    ],
    "threadCount": 2,
    "pendingTasks": 0,
    "threadStates": {
        "RUNNABLE": 2
    },
    "terminated": false,
    "shutDown": false
}
```

## 获取Restlight所有配置信息

**`Get` `actuator/restlightconfigs`**

Example:

```shell
curl -X GET localhost:8080/actuator/restlightconfigs
```

返回：

```json
{
    "http2Enable": false,
    "useNativeTransports": false,
    "connectorThreads": 1,
    "ioThreads": 2,
    "coreBizThreads": 4,
    "maxBizThreads": 4,
    "blockingQueueLength": 1024,
    "keepAliveTimeSeconds": 180,
    "executorTerminationTimeoutSeconds": 60,
    "compress": false,
    "decompress": false,
    "maxContentLength": 4194304,
    "maxInitialLineLength": 4096,
    "maxHeaderSize": 8192,
    "soRcvbuf": 0,
    "soSendbuf": 0,
    "soBacklog": 128,
    "writeBufferHighWaterMark": 65536,
    "idleTimeSeconds": 60,
    "keepAliveEnable": true,
    "https": {
        "enable": false,
        "handshakeTimeoutMillis": 3000,
        "certificatePath": null,
        "privateKeyPath": null,
        "sessionTicketKeyPath": null,
        "sessionTimeoutSeconds": 0,
        "sessionCacheEnable": false,
        "sessionCacheSize": 0,
        "enabledCipherSuites": [],
        "enabledProtocols": []
    },
    "scheduling": {
        "defaultStrategy": "BIZ",
        "bufferSize": 4096,
        "batchingSize": 50
    },
    "route": {
        "useCachedRouting": true,
        "cacheRatio": 10,
        "computeRate": 1
    },
    "contextPath": null,
    "validationMessageFile": null,
    "serialize": {
        "request": {
            "negotiation": false,
            "negotiationParam": "format"
        },
        "response": {
            "negotiation": false,
            "negotiationParam": "format"
        }
    },
    "ext": {},
    "host": null,
    "port": 8080,
    "unixDomainSocketFile": null,
    "printBanner": true,
    "warmUp": {
        "enable": false,
        "delay": 0
    }
}
```

## 强制Full GC

**`Post` `actuator/forcefgc`**

Example:

```shell
curl -X POST localhost:8080/actuator/forcefgc
```

## 修改优雅停机等待时间

**`Post` `actuator/terminationtimeout`**。

Example：

```shell
curl -X POST -H "Content-Type:application/json" -d "{\"timeout\": 120}" localhost:8080/actuator/terminationtimeout
```

返回

```
Success
```
