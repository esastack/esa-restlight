---
tags: ["feature"]
title: "Mock测试"
linkTitle: "Mock测试"
weight: 160
---

同Spring MVC一样，Restlight也提供了单元测试的功能，用于构造请求并将请求映射到对应的Handler，得到Handler的执行结果并测试。如果您对Spring-Test不熟悉，请参考[Spring-Testing](https://docs.spring.io/spring/docs/5.1.3.RELEASE/spring-framework-reference/testing.html#testing)。由于Restlight与Spring-web天生存在冲突，因此MockMvc的使用方式与Spring Mvc的略有差异，详情如下文所示。需要注意的是：
- 使用MockMvc及MockMvcBuilders时请正确引入restlight包下的，而不是spring-web测试包下的。
- 由于Restlight暂不支持RestTemplate，因此与该功能有关的测试同样暂不支持，如@AutoConfigureWebClient、@WebMvcTest等。

使用该功能需要额外引入如下依赖：
```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
    <version>${spring.boot.version}</version>
</dependency>

<dependency>
    <groupId>io.esastack</groupId>
    <artifactId>restlight-test-starter</artifactId>
    <scope>test</scope>
    <version>${restlight.version}</version>
</dependency>
```


## 1. 通过Context构造测试环境
该方式与Spring Mvc测试方式几乎无差异，需要注意的是：正确引入restlight包下的MockMvc及MockMvcBuilders。示例如下：
```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class BootstrapWithContextTest {

    @Autowired
    private ApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.contextSetup(context);
    }

    @Test
    public void testListAll() {
        mockMvc.perform(MockAsyncRequest.aMockRequest().withUri("/demo1/list").build())
                .addExpect(r -> assertTrue(((List) r.result()).isEmpty()))
                .addExpect(r -> assertEquals(200, r.response().status()));
    }

    @Test
    public void testListAll2() {
        mockMvc.perform(MockAsyncRequest.aMockRequest().withUri("/demo2/list").build())
                .addExpect(r -> assertTrue(((List) r.result()).isEmpty()))
                .addExpect(r -> assertEquals(200, r.response().status()));
    }
}
```


## 2. 通过Controller列表构造测试环境
该方式与Spring Mvc测试方式几乎无差异，需要注意的是：正确引入restlight包下的MockMvc及MockMvcBuilders。示例如下：
```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class BootstrapWithSingletonTest {

    @Autowired
    private DemoController1 demoController1;

    @Autowired
    private DemoController2 demoController2;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(demoController1, demoController2).build();
    }

    @Test
    public void testListAll() {
        mockMvc.perform(MockAsyncRequest.aMockRequest().withUri("/demo1/list").build())
                .addExpect(r -> assertTrue(((List) r.result()).isEmpty()))
                .addExpect(r -> assertEquals(200, r.response().status()));
    }

    @Test
    public void testListAll2() {
        mockMvc.perform(MockAsyncRequest.aMockRequest().withUri("/demo2/list").build())
                .addExpect(r -> assertTrue(((List) r.result()).isEmpty()))
                .addExpect(r -> assertEquals(200, r.response().status()));
    }
}
```

## 3. 自动注入MockMvc
该方式与Spring Mvc测试方式几乎无差异，需要注意的是：正确引入restlight包下的MockMvc。示例如下：
```java
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MockMvcAutowiredTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testListAll() {
        mockMvc.perform(MockAsyncRequest.aMockRequest().withUri("/demo1/list").build())
                .addExpect(r -> assertTrue(((List) r.result()).isEmpty()))
                .addExpect(r -> assertEquals(200, r.response().getStatus()));
    }

    @Test
    public void testListAll2() {
        mockMvc.perform(MockAsyncRequest.aMockRequest().withUri("/demo2/list").build())
                .addExpect(r -> assertTrue(((List) r.result()).isEmpty()))
                .addExpect(r -> assertEquals(200, r.response().getStatus()));
    }
}
```

## 4. 异步方法测试
与同步方法不同，如果原始Controller为异步方法，执行完perform()方法后直接对执行结果进行判断不会得到预期结果，因为原始Controller并未执行完，响应内容也尚未写入。因此如果原始Controller方法为异步，Restlight在执行完perform()方法后会阻塞等待异步方法执行完成，而后继续执行用户自定义的判断逻辑，使用时可以通过MockAsyncRequest的asynTimeout属性设置阻塞等待的时间（默认为-1）。

使用示例：
```java
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RestlightDemoApplication.class)
@AutoConfigureMockMvc
public class AsyncDemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testListAll() {
        mockMvc.perform(MockAsyncRequest.aMockRequest().withUri("/async/list").build())
                .addExpect(r -> assertTrue(((List) r.result()).isEmpty()))
                .addExpect(r -> assertEquals(200, r.response().status()));
    }
}
```


