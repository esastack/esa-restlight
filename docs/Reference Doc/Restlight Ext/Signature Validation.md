---
sort: 600
---

# 签名认证

使用Restlight参数签名验证拦截器时请先引入依赖：

```xml
<dependency>
	<groupId>io.esastack</groupId>
	<artifactId>restlight-ext-interceptor-starter</artifactId>
	<version>${restlight.version}</version>
</dependency>
```

参数签名验证拦截器可以验证请求参数的签名，防止请求参数被篡改。使用时请做如下配置：

```properties
#开启参数签名验证功能的必需配置
restlight.server.ext.sign.enable=true

#调用方ID参数名,默认为appId
restlight.server.ext.sign.app-id-name=appId

#签名秘钥版本参数名,默认为sv
restlight.server.ext.sign.secret-version-name=sv

#请求时间戳参数名,默认为ts
restlight.server.ext.sign.timestamp-name=ts

#请求时间戳有效期：单位秒（默认为0）
restlight.server.ext.sign.expire-seconds=0

#签名参数名称,默认为sign
restlight.server.ext.sign.signature-name=sign

#是否对所有接口进行签名验证（默认为false）
restlight.server.ext.sign.verify-all=true
```

```note
配置完成后，需要自定义esa.restlight.ext.interceptor.signature.SecretProvider的实现类并注入Spring容器。该接口定义了通过appId、secretVersion、timestamp获取秘钥的方法。
```

## 服务端验签详细过程
- 第一步：从请求中获取签名值signature并去掉前后空格（先从url参数中获取，如果没有再从请求头中获取）；
- 第二步：从请求中获取时间戳timestamp并去掉前后空格（方式同上），如果配置了请求时间戳有效期，则判断是否在有效期内。
- 第三步：从请求中获取appId、secretVersion并去掉前后空格（方式同上）。
- 第四步：根据上述的appId、secretVersion、timestamp从自定义的SecretProvider获取secret。
- 第五步：根据请求参数构建签名data[]，具体步骤为：1.构建请求参数对应的paramData[]：获取所有的url参数（排除sign），按照参数名字典序升序排列，若一个参数对应多个值，则这多个值也按字典序升序排列（**注意：所有参数名和参数值均会去掉前后空格**）。如：http://api.xxx.com/getUserInfo?appId=your_appId&sv=1&ts=1555933697000&user_id=u001&sign=xxx&names=LiMing&names=ZhangSan对应的paramData[]=("api_key=your_appId&names=LiMing&names=ZhangSan&sv=1&t=1555933697000&user_id=u001").getBytes("UTF-8")；2.构建请求body对应的bodyData[]：对于类型为POST且Content-Type不包含x-www-form-urlencoded的请求，直接通过request.getBody()获取bodyData[]。**重要说明：** 对于Content-Type包含x-www-form-urlencoded的POST请求，验证签名时会将body中参数合并到url的参数中一起处理，客户端加密时需要注意此种情况；3.合并paramData[]和bodyData[]作为签名data[]。
- 第六步：使用HmacSha1算法生成data[]与secret的签名（详见esa-commons项目下SecurityUtils的getHmacSHA1方法）。
- 第七步：验证signature与第六步生成的签名是否相等。

```note
参数签名验证失败会抛出SignatureValidationException，该异常的message中保存了验签失败的详细原因，使用时可根据需要自定义该异常的处理方法
Restlight已在内部开源，签名验证的代码实现参见：restlight-support模块下AbstractSignatureInterceptor。
```

## 指定或排除需要进行签名验证的接口
Restlight提供了两种不同的方式来自定义需要进行签名验证的接口：1. 在全局接口都进行签名验证的情况下，使用@IgnoreSignValidation注解忽略指定接口的签名验证功能；2. 在全局接口都不进行签名验证的前提下，使用@SignValidation注解指定对需要进行签名验证的接口。默认使用方式2。
方式1使用示例：

```properties
restlight.server.ext.sign.verify-all=true
```

```java
@RequestMapping("/index")
@IgnoreSignValidation
public void index() {
    TestService.list();
}
```

如上配置表示：对index()方法之外的其他接口均开启签名验证功能。

方式2使用示例：

```
restlight.server.ext.sign.verify-all=false
```

```java
@RequestMapping("/index")
@SignValidation
public void index() {
    TestService.list();
}
```

如上配置表示：只对index()方法对应的接口开启签名验证功能。

### 自定义参数签名验证拦截器
`Restlight`默认使用**HmacSHA1**作为验签时原始请求的签名生成方法，当用户使用其它算法可以注入自定义的参数签名验证拦截器，使用示例如下：
```java
@Component
public class CustomizeSignatureValidationFactory extends SignValidationHandlerInterceptorFactory {

    public CustomizeSignatureValidationFactory(SecretDistributor distributor) {
        super(distributor);
    }

    @Override
    protected AbstractSignatureRouteInterceptor doCreate(SignatureOptions options, SecretDistributor distributor) {
        return new AbstractSignatureRouteInterceptor(options, distributor) {
            @Override
            protected boolean validate(byte[] data, String signature, String sk) {
                // customize validation
            }
        };
    }
}
```