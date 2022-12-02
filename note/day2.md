# 1 Nacos的统一配置管理
随着微服务的实例越来越多，分别来修改微服务的配置是不显示的，而且很容易出错，Nacos为我们提供了一种配置文件的统一管理方案，可以集中管理所有实例的配置
![在这里插入图片描述](https://img-blog.csdnimg.cn/81a005603e0f4be7a7eecedbf065d527.png)
### 1.1 在Nacos中添加配置文件
要管理配置文件，我们并不需要将配置文件中的所有项都使用nacos来管理，一些不会发生变化的项我们还是写在本地的配置文件中。而nacos中配置的普遍是一些需要后续更改的，比如多环境的配置等。
![在这里插入图片描述](https://img-blog.csdnimg.cn/6764550f483f4777955690c343079102.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/8a99bd83df8943199c3c1bf9432f6199.png)

> 注意：项目的核心配置，需要热更新的配置才有放到nacos管理的必要。基本不会变更的一些配置还是保存在微服务本地比较好。


### 1.2 从微服务中拉取配置信息
我们刚才已经在Nacos中配置了相关的配置文件，那么如何在我们的项目中取使用呢？
微服务要拉取到nacos中的配置文件，然后和本地的`application.yaml`文件合并，才能完成项目的启动。然而我们微服务的nacos配置都是写在`application.yaml`中的。那么尚未读取`application.yml`，又如何得知nacos地址呢？
因此Spring中引入了一个新的配置文件，`bootstrap.yaml`，他会在`application.yaml`读取之前读取，因此我们可以把nacos地址写在这个文件里面。
![在这里插入图片描述](https://img-blog.csdnimg.cn/7b24f44209034b34ab2c6afd444217b0.png)
#### 1.2.1 具体步骤
**引入nacos-config依赖**
首先，在user-service服务中，引入nacos-config的客户端依赖：

```yaml
<!--nacos配置管理依赖-->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```
**添加bootstrap.yaml**
然后，在user-service中添加一个bootstrap.yaml文件，内容如下：

```yaml
spring:
  application:
    name: userservice # 服务名称
  profiles:
    active: dev #开发环境，这里是dev 
  cloud:
    nacos:
      server-addr: localhost:8848 # Nacos地址
      config:
        file-extension: yaml # 文件后缀名
```
通过这个配置文件可以指定指定到我们在nacos中设置的配置文件。
`server-addr`+`name`+`active`+`file-extension`  = localhost:8848/userservice-dev.yaml
![在这里插入图片描述](https://img-blog.csdnimg.cn/6ae2a44499814be4b2e4d9752e9afb89.png)
**读取nacos配置**
为了验证是否真的加载了nacos配置文件，我们在user-service中的UserController中添加业务逻辑，读取pattern.dateformat配置：

```java
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Value("${pattern.dateformat}")
    private String dateformat;
    
    @GetMapping("now")
    public String now(){
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(dateformat));
    }
    // ...略
}
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/66ce55ecda794dd9bfc6248ced25b5ff.png)
可以看到我们定义的日期格式生效了，说明配置文件是成功访问到的
### 1.3 Nacos配置的热更新
之前我们做的这些配置，必须要重启服务以后才能生效，那么nacos能不能做到热更新呢？实际上是可以的。

Nacos实现热更新有两种方式：
**方式1**：使用@RefreshScope注解
![在这里插入图片描述](https://img-blog.csdnimg.cn/631a6076cf3c4ece9f1d0464d8a1b328.png)
**方式二** 使用了一个类专门用来读取配置文件的类
使用@ConfigurationProperties注解代替@Value注解。

```java
package cn.itcast.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "pattern")
public class PatternProperties {
    private String dateformat;
}
```
在userController中通过注入这个配置类的方式拿到配置文件内容

```java
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PatternProperties patternProperties;

    @GetMapping("now")
    public String now(){
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(patternProperties.getDateformat()));
    }
}
```
### 1.4 配置共享
上面我们配置配置文件是根据不同的生产环境进行的，那么如果针对一些在所有环境中都通用的配置该如何配置呢？需要我们在不同环境配置文件中重复写相同的配置吗？
实际上，nacos还提供了配置共享的功能。
#### 1.4.1 具体操作
我们可以给userservice创建一个配置文件，这个配置文件会在所有的环境生效，如dev，test，prod。
![在这里插入图片描述](https://img-blog.csdnimg.cn/457955838e804732a984bcf3ccf47cba.png)
我们在之前的配置类中添加接收我们新添加的配置信息的代码
![在这里插入图片描述](https://img-blog.csdnimg.cn/66e055842ac84107806f0d3203d8518d.png)
在user-service服务中，修改UserController，添加一个方法：
![在这里插入图片描述](https://img-blog.csdnimg.cn/29d0323546ed43e180d270a278416e20.png)
创建不同环境的userservice实例
![在这里插入图片描述](https://img-blog.csdnimg.cn/5d7a9e68df4e445998e727c9e6d7c57a.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/b1ff3961ded64f4c82e5cfa4d9e11213.png)
访问 http://localhost:8081/user/prop，结果：
![在这里插入图片描述](https://img-blog.csdnimg.cn/146ba5b4c9fd445c9951e62f2258744b.png)
访问http://localhost:8082/user/prop，结果：
![在这里插入图片描述](https://img-blog.csdnimg.cn/d02712b420c84ba08895983980f13dd4.png)
可以看出来，不管是dev，还是test环境，都读取到了`envSharedValue`这个属性的值。
#### 1.4.2 配置共享的优先级
当nacos、服务本地同时出现相同属性时，优先级有高低之分：
![在这里插入图片描述](https://img-blog.csdnimg.cn/b80fe1fce15f4ad398d62136696b1ada.png)
# 2. Feign的远程方法调用
### 2.1 使用RestTemplate的问题
我们先看看之前使用RestTemplate的代码

```java
public Order queryOrderById(Long orderId) {
        // 1.查询订单
        Order order = orderMapper.findById(orderId);
        String url = "http://userservice/user/" + order.getUserId();
        User user = restTemplate.getForObject(url, User.class);
        // 4.返回
        order.setUser(user);
        return order;
    }
```
可以看出一下问题
* 代码的可读性很差，URL复杂难以维护，如果URL后面跟了很多参数，那么就是噩梦
* 不够**优雅**。

为了解决上面的问题，我们可以利用Feign帮助我们进行远程方法调用。让我们的代码更加优雅。
Feign是一个声明式的http客户端，官方地址：https://github.com/OpenFeign/feign
其作用就是帮助我们优雅的实现http请求的发送，解决上面提到的问题。
![在这里插入图片描述](https://img-blog.csdnimg.cn/dd4b020be04f49e08bcb1223b637c827.png)
### 2.2 使用Feign代替RestTemplate
**引入依赖**

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```
**添加注解**
在order-service的启动类添加注解开启Feign的功能：

```java
@MapperScan("cn.itcast.order.mapper")
@EnableFeignClients(clients = {UserClient.class})
@SpringBootApplication
public class OrderApplication {
......
}
```
**编写Feign的客户端**
在order-service中新建一个接口，内容如下：
```java
package cn.itcast.order.client;

import cn.itcast.order.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("userservice")
public interface UserClient {
    @GetMapping("/user/{id}")
    User findById(@PathVariable("id") Long id);
}
```

> @FeignClien为我们的接口创建实现类并加入到IOC容器中

**测试**
```java
public Order queryOrderByIdWithFeign(Long orderId) {
        // 1.查询订单
        Order order = orderMapper.findById(orderId);
        // String url = "http://userservice/user/" + order.getUserId();
        // User user = restTemplate.getForObject(url, User.class);
        User user = (User) userClient.getUserById(order.getUserId());
        // 4.返回
        order.setUser(user);
        return order;
    }
```
### 2.3 步骤总结
使用Feign的步骤：
① 引入依赖
② 添加@EnableFeignClients注解
③ 编写FeignClient接口
④ 使用FeignClient中定义的方法代替RestTemplate

### 2.4 配置Feign的日志
#### 2.4.1 日志级别
Feign支持不同级别的日志

| 类型                   | 作用             | 说明                                                   |
| ---------------------- | ---------------- | ------------------------------------------------------ |
| **feign.Logger.Level** | 修改日志级别     | 包含四种不同的级别：NONE、BASIC、HEADERS、FULL         |

- NONE：不记录任何日志信息，这是默认值。
- BASIC：仅记录请求的方法，URL以及响应状态码和执行时间
- HEADERS：在BASIC的基础上，额外记录了请求和响应的头信息
- FULL：记录所有请求和响应的明细，包括头信息、请求体、元数据。

#### 2.4.2 修改日志级别
那么如何修改Feign的日志级别呢
**方式一** 修改配置文件
这里我们可以指定日志级别只针对单个服务

```yaml
feign:  
  client:
    config: 
      userservice: # 针对某个微服务的配置
        loggerLevel: FULL #  日志级别 
```
或者针对所有服务

```yaml
feign:  
  client:
    config: 
      default: # 这里用default就是全局配置，如果是写服务名称，则是针对某个微服务的配置
        loggerLevel: FULL #  日志级别 
```
**方式二** 使用Java代码方式
首先创建一个配置类

```java
public class DefaultFeignConfiguration  {
    @Bean
    public Logger.Level feignLogLevel(){
        return Logger.Level.BASIC; // 日志级别为BASIC
    }
}
```
如果要**全局生效**，将其放到启动类的@EnableFeignClients这个注解中：

```java
@EnableFeignClients(defaultConfiguration = DefaultFeignConfiguration .class) 
```
如果是**局部生效**，则把它放到对应的@FeignClient这个注解中：

```java
@FeignClient(value = "userservice", configuration = DefaultFeignConfiguration .class) 
```

### 2.5 Feign的优化
Feign的底层还是通过HTTP请求的方式发送，他依赖于其他的HTTP框架，其底层实现主要包括：
* URLConnection：默认实现，不支持连接池
* Apache HttpClient ：支持连接池
* OKHttp：支持连接池

提高Feign性能主要是通过使用连接池替代默认的`URLConnection`
#### 2.5.1 具体步骤
这里我们用Apache的HttpClient来演示。
**引入依赖**
在order-service的pom文件中引入Apache的HttpClient依赖：

```xml
<!--httpClient的依赖 -->
<dependency>
    <groupId>io.github.openfeign</groupId>
    <artifactId>feign-httpclient</artifactId>
</dependency>
```
**配置连接池**
在order-service的application.yml中添加配置：

```yaml
feign:
  client:
    config:
      default: # default全局的配置
        loggerLevel: BASIC # 日志级别，BASIC就是基本的请求和响应信息
  httpclient:
    enabled: true # 开启feign对HttpClient的支持
    max-connections: 200 # 最大的连接数
    max-connections-per-route: 50 # 每个路径的最大连接数
```
#### 2.5.2 总结
* Feign的日志级别尽量使用NULL或者BASIC即可，因为过高的日志级别会导致性能的下降
* 使用连接池替代默认的`URLConnection`

### 2.6 Feign的最佳实践
#### 2.6.1 问题分析
通过观察代码我们可以发现，Feign的客户端代码和我们在Controller中的代码非常相似
**Feign 客户端代码** UserClient.java

```java
@FeignClient("userservice")
public interface UserClient {

    @GetMapping("/user/{id}")
    User getUserById(@PathVariable(name = "id") Long id);
}
```
**UserController客户端代码** UserController.java

```java
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    
    @GetMapping("/{id}")
    public User queryById(@PathVariable("id") Long id) {
        return userService.queryById(id);
    }
}
```
#### 2.6.2 继承方式
一样的代码可以通过继承来共享：
* 定义一个API接口，利用定义方法，并基于SpringMVC注解做声明。
* Feign客户端和Controller都集成改接口
  ![在这里插入图片描述](https://img-blog.csdnimg.cn/d0bc38c980c04b1c8b31af5679efe526.png)

`UserAPI`接口中定义了相关方法，`UserController`可以实现这个接口，那么所有的操作就都实现了。`UserClient`可以继承这个接口，那么`Controller`中的方法也就都有了。
这种方法的优点是，简单，实现了代码共享。
缺点是：服务提供方、服务消费方紧耦合，参数列表中的注解映射并不会继承，因此Controller中必须再次声明方法、参数列表、注解


#### 2.6.3 抽取成独立模块方式
将Feign的Client抽取为独立模块，并且把接口有关的POJO、默认的Feign配置都放到这个模块中，提供给所有消费者使用。
例如，将UserClient、User、Feign的默认配置都抽取到一个feign-api包中，所有微服务引用该依赖包，即可直接使用。
![在这里插入图片描述](https://img-blog.csdnimg.cn/5ceeac3688d74b10a5eaed31e1021614.png)
**抽取**
首先创建一个module，命名为feign-api：
![在这里插入图片描述](https://img-blog.csdnimg.cn/f745dbf8a19147b2aa088fe8de703543.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/53e1098ba9a14fca8d16c6544ea93600.png)
**在feign-api中然后引入feign的starter依赖**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```
**然后，order-service中编写的UserClient、User、DefaultFeignConfiguration都复制到feign-api项目中**
![在这里插入图片描述](https://img-blog.csdnimg.cn/5b28c05a3559476499181fe28117bacb.png)
**在order-service中使用feign-api**
首先，删除order-service中的UserClient、User、DefaultFeignConfiguration等类或接口。

在order-service的pom文件中中引入我们自己创建的feign-api的依赖：

```xml
<dependency>
    <groupId>cn.itcast.demo</groupId>
    <artifactId>feign-api</artifactId>
    <version>1.0</version>
</dependency>
```
修改order-service中的所有与上述三个组件有关的导包部分，改成导入feign-api中的包

重启后，发现服务报错了：
![在这里插入图片描述](https://img-blog.csdnimg.cn/e5d66a72d98140ae822a21ae3b5d3a8b.png)
这是因为UserClient现在在cn.itcast.feign.clients包下，

而order-service的@EnableFeignClients注解是在cn.itcast.order包下，不在同一个包，无法扫描到UserClient。

**解决扫描包问题**
**方式一**：指定Feign应该扫描的包：

```java
@EnableFeignClients(basePackages = "cn.itcast.feign.clients")
```
**方式二**：指定需要加载的Client接口：

```java
@EnableFeignClients(clients = {UserClient.class})
```


# 3 Gateway服务网关
### 3.1 为什么需要网关
Gateway为所有的微服务提供了统一的入口，其核心功能主要有：
* 请求路由：一切请求都必须先经过gateway，但网关不处理业务，而是根据某种规则，把请求转发到某个微服务，这个过程叫做路由。当然路由的目标服务有多个时，还需要做负载均衡。
* 权限控制：网关作为微服务入口，需要校验用户是是否有请求资格，如果没有则进行拦截。
* 限流：当请求流量过高时，在网关中按照下流的微服务能够接受的速度来放行请求，避免服务压力过大。
  ![在这里插入图片描述](https://img-blog.csdnimg.cn/ef7b2b42a11b4b6ebacb083c397895f0.png)
### 3.2 Gateway的搭建
**创建gateway模块**
![在这里插入图片描述](https://img-blog.csdnimg.cn/9f52e588541b4dc4890628289bfb12ca.png)
**引入依赖**
引入网关依赖和nacos依赖，引入nacos依赖的目的是让网关也可以访问注册中心，因为网关要根据请求路径找到对应的微服务
```xml
<!--网关-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
<!--nacos服务发现依赖-->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```
**编写启动类**

```java
package cn.itcast.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}
}
```
**编写基础配置和路由规则**
创建application.yml文件，内容如下：

```yaml
server:
  port: 10010 # 网关端口
spring:
  application:
    name: gateway # 服务名称
  cloud:
    nacos:
      server-addr: localhost:8848 # nacos地址
    gateway:
      routes: # 网关路由配置
        - id: user-service # 路由id，自定义，只要唯一即可
          # uri: http://127.0.0.1:8081 # 路由的目标地址 http就是固定地址
          uri: lb://userservice # 路由的目标地址 lb就是负载均衡，后面跟服务名称
          predicates: # 路由断言，也就是判断请求是否符合路由规则的条件
            - Path=/user/** # 这个是按照路径匹配，只要以/user/开头就符合要求
```
1. 路由id：路由的唯一标示
2. 路由目标（uri）：路由的目标地址，http代表固定地址，lb代表根据服务名负载均衡
3. 路由断言（predicates）：判断路由的规则，
4. 路由过滤器（filters）：对请求或响应做处理

**测试**
此时我们只需要访问网关的地址，网关就可以根据我们的请求路径调用相应的微服务，并返回结果
重启网关，访问`http://localhost:10010/user/1`时，符合`/user/**`规则，请求转发到`uri：http://userservice/user/1`，得到了结果：
![在这里插入图片描述](https://img-blog.csdnimg.cn/bfe6343d7e714ae899f0f80ef0f0fbfb.png)
**步骤总结**
1. 创建网关模块
2. 引入网关依赖和nacos依赖
3. 编写路由规则的配置文件
### 3.3 网关路由的流程图
![在这里插入图片描述](https://img-blog.csdnimg.cn/1a7adac473a3471b8b8aadb129780e9d.png)
1. 用户向网关发送请求
2. 网关根据用户的URL到nacos注册中心拉取服务列表，找到对应的微服务模块
3. 网关将用户请求转发到对应的模块
### 3.4 断言工厂
我们之前在配置文件中配置`-Path`断言只是字符串，这些字符串会被Predicate Factory读取并解析，编程路由判断的条件。
如`Path=/user/**`是按照路径匹配，这个规则是由`org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory`类来处理的，像这样的断言工厂在SpringCloudGateway还有十几个:

| **名称**   | **说明**                       | **示例**                                                     |
| ---------- | ------------------------------ | ------------------------------------------------------------ |
| After      | 是某个时间点后的请求           | -  After=2037-01-20T17:42:47.789-07:00[America/Denver]       |
| Before     | 是某个时间点之前的请求         | -  Before=2031-04-13T15:14:47.433+08:00[Asia/Shanghai]       |
| Between    | 是某两个时间点之前的请求       | -  Between=2037-01-20T17:42:47.789-07:00[America/Denver],  2037-01-21T17:42:47.789-07:00[America/Denver] |
| Cookie     | 请求必须包含某些cookie         | - Cookie=chocolate, ch.p                                     |
| Header     | 请求必须包含某些header         | - Header=X-Request-Id, \d+                                   |
| Host       | 请求必须是访问某个host（域名） | -  Host=**.somehost.org,**.anotherhost.org                   |
| Method     | 请求方式必须是指定方式         | - Method=GET,POST                                            |
| Path       | 请求路径必须符合指定规则       | - Path=/red/{segment},/blue/**                               |
| Query      | 请求参数必须包含指定参数       | - Query=name, Jack或者-  Query=name                          |
| RemoteAddr | 请求者的ip必须是指定范围       | - RemoteAddr=192.168.1.1/24                                  |
| Weight     | 权重处理                       |

### 3.4 过滤器工厂
我们只需要掌握Path这种路由工程就可以了。
![在这里插入图片描述](https://img-blog.csdnimg.cn/4c7cc2bcfcce40dd9f043346397f5da9.png)
> 注意：过滤器不仅可以处理由用户发送到微服务的请求，也可以过滤由微服务发送到用户端的响应信息

#### 3.4.1 过滤器的种类
Spring提供了31种不同的路由过滤器工厂。例如：

| **名称**             | **说明**                     |
| -------------------- | ---------------------------- |
| AddRequestHeader     | 给当前请求添加一个请求头     |
| RemoveRequestHeader  | 移除请求中的一个请求头       |
| AddResponseHeader    | 给响应结果中添加一个响应头   |
| RemoveResponseHeader | 从响应结果中移除有一个响应头 |
| RequestRateLimiter   | 限制请求的流量               |


这里我们就使用`AddRequestHeader`来进行演示。

**需求** ：给所有进入userservice的请求添加一个请求头：Truth=itcast is freaking awesome!
只需要修改gateway服务的application.yml文件，添加路由过滤即可：

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: user-service 
        uri: lb://userservice 
        predicates: 
        - Path=/user/** 
        filters: # 过滤器
        - AddRequestHeader=Truth, Itcast is freaking awesome! # 添加请求头
```
当前过滤器写在userservice路由下，因此仅仅对访问userservice的请求有效。
![在这里插入图片描述](https://img-blog.csdnimg.cn/b18c511e8ef44ecbaf081119094d6eac.png)

#### 3.4.2 默认过滤器
如果我们想给所有的请求都添加一个过滤器，那么可以使用默认过滤器。
```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: user-service 
        uri: lb://userservice 
        predicates: 
        - Path=/user/**
      default-filters: # 默认过滤项
      - AddRequestHeader=Truth, Itcast is freaking awesome! // 对所有请求都生效
```

#### 3.4.5 全局过滤器
之前提到的两种过滤器的功能都是特定的，必须使用31中过滤器工厂中的操作，但是对于一些比较复杂的业务逻辑则无法实现，比如说实现登录的验证。因此在网关中还有一种全局过滤器。
全局过滤器的足有也是处理进入网关的一切请求，但是和gatewayFilter的区别是他可以自定义业务逻辑。
定义方式是实现GlobalFilter接口。

```java
public interface GlobalFilter {
    /**
     *  处理当前请求，有必要的话通过{@link GatewayFilterChain}将请求交给下一个过滤器处理
     *
     * @param exchange 请求上下文，里面可以获取Request、Response等信息
     * @param chain 用来把请求委托给下一个过滤器 
     * @return {@code Mono<Void>} 返回标示当前过滤器业务结束
     */
    Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain);
}
```
接下来我们使用一个例子来说明全局过滤器的作用

需求：定义全局过滤器，拦截请求，判断请求的参数是否满足下面条件：
- 参数中是否有authorization，
- authorization参数值是否为admin

如果同时满足则放行，否则拦截
实现：

在gateway中定义一个过滤器：

```java
package cn.itcast.gateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Order(-1)
@Component
public class AuthorizeFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1.获取请求参数
        MultiValueMap<String, String> params = exchange.getRequest().getQueryParams();
        // 2.获取authorization参数
        String auth = params.getFirst("authorization");
        // 3.校验
        if ("admin".equals(auth)) {
            // 放行
            return chain.filter(exchange);
        }
        // 4.拦截
        // 4.1.禁止访问，设置状态码
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        // 4.2.结束处理
        return exchange.getResponse().setComplete();
    }
}
```
#### 3.4.6 过滤器的执行顺序
一个请求进入gateway，会经过三种类型的过滤器：当前路由过滤器、DefaultFilter、GlobalFilter。
请求路由后，会将每一个过滤器合并到一个过滤器链表(List)中，合并的顺序如下
* 每一个过滤器都必须指定一个int类型的order值，**order值越小，优先级越高，执行顺序越靠前**。
* 当过滤器的order值一样时，会按照 **defaultFilter > 路由过滤器 > GlobalFilter的顺序执行**。
  ![在这里插入图片描述](https://img-blog.csdnimg.cn/80cdb8f6e01a45838995c1fecce574a0.png)
  设置优先级的方式
  **方式一** 让过滤器实现ordered接口，并且重写`getOrder()`方法，该方法返回值就是当前过滤器的优先级

```java
    @Override
    public int getOrder() {
        return 0;
    }
```

**方式二** 使用@Order注解，注解中的值就是优先级

```java
@Order(1)
```
