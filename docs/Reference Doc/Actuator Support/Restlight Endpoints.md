---
sort: 100
---

# Restlight Endpoint扩展

在[Spring Boot Actuator](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-project/spring-boot-actuator)基础之上`Restlight`提供额外的功能扩展

## 业务线程池Metrics

```note
业务线程当前采用JUC的`ThreadPoolExecutor`实现，后续版本可能会换成自定义实现，因此Metrics内容可能发生变更
```

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
### 2、Prometheus 格式

**`Get` `actuator/bizthreadpool4prometheus`**

Example:

```shell
curl -X GET localhost:8080/actuator/bizthreadpool4prometheus
```

返回

```
# HELP core_pool_size  
# TYPE core_pool_size gauge
core_pool_size 1.0
# HELP active_count  
# TYPE active_count gauge
active_count 0.0
# HELP queue_count  
# TYPE queue_count gauge
queue_count 0.0
# HELP largest_pool_size  
# TYPE largest_pool_size gauge
largest_pool_size 0.0
# HELP reject_task_count  
# TYPE reject_task_count gauge
reject_task_count -1.0
# HELP max_pool_size  
# TYPE max_pool_size gauge
max_pool_size 2.0
# HELP pool_size  
# TYPE pool_size gauge
pool_size 0.0
# HELP queue_length  
# TYPE queue_length gauge
queue_length 1024.0
# HELP keep_alive_time_seconds  
# TYPE keep_alive_time_seconds gauge
keep_alive_time_seconds 60.0
# HELP task_count  
# TYPE task_count gauge
task_count 0.0
# HELP completed_task_count  
# TYPE completed_task_count gauge
completed_task_count 0.0
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
### 2、Prometheus 格式

**`Get` `actuator/ioexecutor4prometheus`**

Example:

```shell
curl -X GET localhost:8080/actuator/ioexecutor4prometheus
```

返回

```
# HELP pending_tasks_netty_io_1_0  
# TYPE pending_tasks_netty_io_1_0 gauge
pending_tasks_netty_io_1_0 0.0
# HELP thread_state_netty_io_1_1  
# TYPE thread_state_netty_io_1_1 gauge
thread_state_netty_io_1_1 1.0
# HELP thread_states_runnable  
# TYPE thread_states_runnable gauge
thread_states_runnable 2.0
# HELP task_queue_size_netty_io_1_1  
# TYPE task_queue_size_netty_io_1_1 gauge
task_queue_size_netty_io_1_1 0.0
# HELP thread_priority  
# TYPE thread_priority gauge
thread_priority 5.0
# HELP pending_tasks_netty_io_1_1  
# TYPE pending_tasks_netty_io_1_1 gauge
pending_tasks_netty_io_1_1 0.0
# HELP pending_tasks  
# TYPE pending_tasks gauge
pending_tasks 0.0
# HELP terminated  
# TYPE terminated gauge
terminated 0.0
# HELP max_pending_tasks  
# TYPE max_pending_tasks gauge
max_pending_tasks 2.147483647E9
# HELP thread_state_netty_io_1_0  
# TYPE thread_state_netty_io_1_0 gauge
thread_state_netty_io_1_0 1.0
# HELP io_ratio  
# TYPE io_ratio gauge
io_ratio 50.0
# HELP shutdown  
# TYPE shutdown gauge
shutdown 0.0
# HELP task_queue_size_netty_io_1_0  
# TYPE task_queue_size_netty_io_1_0 gauge
task_queue_size_netty_io_1_0 0.0
# HELP thread_count  
# TYPE thread_count gauge
thread_count 2.0
```

```warning
比较昂贵的操作， 不建议频繁调用
```

当使用的Springboot版本为2.3.X及以上时，需要在pom文件中引入micrometer-registry-prometheus 1.5.1及以上版本，此处以1.5.1版本为例，直接引入即可覆盖原有版本，具体操作如下：

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
    <version>1.5.1</version>
</dependency>
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