---
tags: ["extension"]
title: "数据校验"
linkTitle: "数据校验"
weight: 90
description: >
    Restlight集成了Hibernate Validator，提供了开箱即用的数据校验功能，通过注解完成对JavaBean、Controller方法参数和返回值的校验， 并支持异常消息国际化。
---

使用Restlight内置的数据校验请先确保引入了依赖：

```xml
<dependency>
	<groupId>io.esastack</groupId>
	<artifactId>restlight-ext-validator-starter</artifactId>
	<version>${restlight.version}</version>
</dependency>
```

{{< alert title="Note" >}}
当前版本的restlight-starter中默认引入了该依赖，使用时无需重复引入
{{< /alert >}}

## 普通JavaBean的校验

使用注解声明对属性的约束

```java
private class Employee {
	@NotEmpty
	private String name;
	
	@Min(18)
	@Max(60)
	private int age;
	
	@Email
	private String email;
	
	@Length(min = 10, max = 20)
	private String address;

        // 级联校验
	@Valid
	private Object cascadingObject;
}
```

作为方法参数校验, 需要使用`@Valid`注解标记被校验的参数

```java
@PostMapping("/add")
public String add(@Valid @RequestBody Employee employee) {
    return SUCCESS;
}
```

作为返回值， 需要使用`@Valid`注解标记方法或者参数

```java
@Valid
@ResponseBody
@RequestMapping("/list")
public Employee list1() {
    return new Employee("", 16, "", "");
}
```

或者

```java
@ResponseBody
@RequestMapping("/list")
public @Valid Employee list2() {
   return new Employee("", 16, "", "");
}
```

{{< alert title="Note" >}}
当需要校验的参数为JavaBean对象时用`@Valid`来显示声明需要对该参数进行校验。
{{< /alert >}}

## 普通方法参数校验

直接使用注解

```java
@RequestMapping("/update")
public String update(@RequestParam @NotEmpty String name, @RequestParam @Length(min = 10, max = 20) String newAddress) {
    return SUCCESS;
}
```

## 分组校验

使用`@ValidGroup`指定校验方法的参数、返回值校验时的分组。该注解只能标注在方法上并且value值只能为接口类(默认为`Default.class`)。

Example:

```java
@ValidGroup(Interface.class)
@RequestMapping("/addGroup")
public String addGroup(@Valid @RequestBody Employee employee) {
    return SUCCESS;
}
```


## 自定义约束注解

当内置的约束注解不能满足业务需求时，可以使用`@Constraint`自定义约束注解，具体实现使用hibernate-validation，使用方式与Spring MVC无差异，示例如下：

自定义约束注解:

```java
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LogInSuccess.LogInSuccessValidator.class)
@Target(value = {ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Documented
public @interface LogInSuccess {

    String message() default "登录校验未通过";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class LogInSuccessValidator implements ConstraintValidator<LogInSuccess, String> {

        @Override
        public void initialize(LogInSuccess constraintAnnotation) {
            // Do nothing
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return false;
        }
    }
}
```

使用自定义约束注解：
```java
@RequestMapping("/getId")
public String getId(@RequestParam @LogInSuccess(message = "请先登录") String userName) {
    return "SUCCESS";
}
```

## 国际化


数据校验的异常消息允许自定义并且支持国际化，自定义异常处理消息的步骤如下：

自定义异常消息文件，在classpath路径下加入配置文件，如validation-message.properties

```properties
key1=value1
key2=value2
...
```
配置异常消息文件名，在application中配置异常消息文件名，如：

```properties
#该文件名称对应上面定义的validation-message.properties文件
restlight.server.ext.validation.message-file=validation-message
```

修改约束注解的message属性值，如：

```java
@NotEmpty(message="{key1}")
public String name;

@Min(value=18, message="{key2}")
public int age;
```
针对不同语言定义不同的异常消息文件，如：

- validation-message_zh_CN.properties
- validation-message_en.properties
- validation-message_cs.properties
- validation-message_en.properties
- ......

{{< alert title="Tip" >}}
更多不同语言的文件后缀可以参考jar包：hibernate-validator:5.4.1.Final下ValidationMessage中国际化文件的后缀。
{{< /alert >}}


## 数据校验注解一览

| 注解                                          | 功能                                                         | 说明              |
| --------------------------------------------- | ------------------------------------------------------------ | ----------------- |
| @AssertFalse                                  | 被注解元素必须为false                                        |                   |
| @AssertTrue                                   | 被注解的元素必须为true                                       |                   |
| @DecimalMax(value)                            | 被注解的元素必须为一个数字，其值必须小于等于指定的最小值     |                   |
| @DecimalMin(Value)                            | 被注解的元素必须为一个数字，其值必须大于等于指定的最小值     |                   |
| @Digits(integer=, fraction=)                  | 被注解的元素必须为一个数字，其值必须在可接受的范围内         |                   |
| @Future                                       | 被注解的元素必须是未来的日期                                 |                   |
| @Max(value)                                   | 被注解的元素必须为一个数字，其值必须小于等于指定的最大值     |                   |
| @Min(value)                                   | 被注解的元素必须为一个数字，其值必须大于等于指定的最小值     |                   |
| @NotNull                                      | 被注解的元素必须不为null                                     |                   |
| @Null                                         | 被注解的元素必须为null                                       |                   |
| @Past                                         | 被注解的元素必须过去的日期                                   |                   |
| @Pattern                                      | 被注解的元素必须符合正则表达式                               |                   |
| @Size(min=, max=)                             | 被注解的元素必须在指定的范围(数据类型:String, Collection, Map and arrays) |                   |
| @Email                                        | 被注解的元素被注释的元素必须是电子邮箱地址                   |         |
| @NotBlank                                     | 被注解的对象必须为字符串，不能为空，检查时会忽略空格         |  |
| @NotEmpty                                     | 被注释的对象长度不能为0(数据:String,Collection,Map,arrays)   |  |
| @Length(min=, max=)                           | 被注解的对象必须是字符串并且长度必须在指定的范围内           | Hibernate扩展注解 |
| @Range(min=, max=)                            | 被注释的元素必须在合适的范围内 (数据：BigDecimal, BigInteger, String, byte, short, int, long and 原始类型的包装类 ) | Hibernate扩展注解 |
| @URL(protocol=, host=, port=, regexp=, flags=) | 被注解的对象必须是一个有效的URL，如果提供了protocol，host等，则该URL还需满足提供的条件 | Hibernate扩展注解 |

{{< alert title="Tip" >}}
Hibernate扩展注解与javax可能存在注解名重复的情况， 请使用`org.hibernate.xxx`的注解。
{{< /alert >}}
