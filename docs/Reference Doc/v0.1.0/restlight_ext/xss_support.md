---
sort: 7
---

# XSS过滤

引入依赖：

```xml
<dependency>
	<groupId>io.esastack</groupId>
	<artifactId>restlight-ext-filter-starter</artifactId>
	<version>${restlight.version}</version>
</dependency>
```

使用方式：
```properties
#开启Xss过滤
restlight.server.ext.xss.enable=true
#Xss过滤模式，默认escape（转义模式），filter为过滤模式
restlight.server.ext.xss.mode=escape
```
配置好后自动对所有请求进行转义或者过滤。

## XSS过滤范围

支持`URL`参数过滤及**不完整**的`Header`过滤

```note
Header相关参数仅支持`AsyncRequest.getHeader(CharSequence name)`以及`AsyncRequest.getHeader(String name)`方法。
```

## `Escape` & `Filter`模式

### `Escape` 模式

该模式会对用户请求的 `URL`参数 和 `Header `进行转义，转义的字符集如下：

|转义前|转义后|
|-|-|
|>|\&gt;|
|<|\&lt;|
|"|\&quot;|
|&|\&amp;|

### `Filter` 模式

该模式会对用户请求的 `URL`参数 和 `Header `进行过滤，删除容易引起 Xss 的标签或者表达式，以空串代替，比如 `name=<script>...</script>` ，过滤以后会直接将 `<script>...</script>` 以空串替换，即 `name=""`，需要以空串替换的标签和表达式如下：

| 标签或表达式           |
| ---------------------- |
| \<script>...\</script> |
| \</script>             |
| \<script ...>          |
| src='...'              |
| src="..."              |
| eval(...)              |
| e­xpression(...)       |
| javascript:            |
| alert                  |
| onload=                |
| vbscript:              |