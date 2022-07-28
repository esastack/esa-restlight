---
tags: ["extension"]
title: "文件及表单参数解析"
linkTitle: "文件及表单参数解析"
weight: 50
---

`Restlight`提供了表单参数解析的功能，使用时需要单独引入相应的包：

 ```xml
<dependency>
	<groupId>io.esastack</groupId>
	<artifactId>restlight-ext-multipart-starter</artifactId>
	<version>${restlight.version}</version>
</dependency>
```

## 文件参数

使用示例如下：
 
```java
@Controller
@RequestMapping("/restlight/file/")
public class FileSupportController
    // 上传单个文件
    @RequestMapping("/upload")
    public String fileUpload(@UploadFile MultipartFile multipartFile) throws IOException {
        File temp = new File("D:\\" + multipartFile.originalFilename());
        multipartFile.transferTo(temp);
        return "SUCCESS";
    }
	
    // 上传一组文件
    @RequestMapping("/uploads")
    public String fileUploads(@UploadFile List<MultipartFile> files) throws IOException {
        for (MultipartFile file : files) {
            File temp = new File("D:\\" + file.originalFilename());
            file.transferTo(temp);
        }
        return "SUCCESS";
    }
}
```

{{< alert title="Warning" color="warning" >}}
对于超大文件的上传可能会导致OOM，因为Restlight底层基于Netty实现，会将整个请求body转换成byte[]数组存放在内存中，再将对应的数据转成文件格式。
{{< /alert >}}

`Restlight`默认请求body大小为4MB，当上传文件时需要根据需要调整该值的大小；默认的编码格式为：UTF-8。使用时，也可以通过配置文件改变上述参数值：

```properties
#设置请求body大小 4MB = 4 * 1024 * 1024 = 4194304
restlight.server.max-content-length=4194304

#编码方式
restlight.server.ext.multipart.charset=utf-8

#单个文件大小限制，默认-1（没有限制） 4KB = 4 * 1024 = 4096
restlight.server.ext.multipart.max-size=4096

#是否使用临时文件，为true时任何大小的文件都使用临时文件，默认为false
restlight.server.ext.multipart.use-disk=true

#临时文件目录
restlight.server.ext.multipart.temp-dir=D:\\temp

#当multipart-use-disk为false且单个文件大小超过该值时使用临时文件，默认2MB
restlight.server.ext.multipart.memory-threshold=2097152
```

### 非文件参数
 
在需要接收的方法参数上加上`@FormParam`注解，如下：

```java
@RequestMapping("/upload")
public String uploadFormParams(@FormParam String formParam0, @FormParam String formParam1) {
    return formParam0 + "; " + formParam1;
}
```
