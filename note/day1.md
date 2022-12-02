# 写在前面
接下来我会用半个多月的时间去学习Spring Cloud框架，之前看的是虎哥的Redis，讲的实在是太精彩，于是Spring Cloud的视频就决定看虎哥的。
视频地址：https://www.bilibili.com/video/BV1LQ4y127n4
GitHub地址：https://github.com/liyuxuan7762/Learn_Springcloud
# 写在前面
接下来我会用半个多月的时间去学习Spring Cloud框架，之前看的是虎哥的Redis，讲的实在是太精彩，于是Spring Cloud的视频就决定看虎哥的。
视频地址：https://www.bilibili.com/video/BV1LQ4y127n4
GitHub地址：https://github.com/liyuxuan7762/Learn_Springcloud
# 1. 什么是微服务
随着互联网行业的发展，对服务的要求也越来越高，服务架构也从单体架构逐渐演变为现在流行的微服务架构。
### 1.1 单体架构
**单体架构**：将业务的所有功能**集中在一个项目**中开发，**打成一个包**部署。
![在这里插入图片描述](https://img-blog.csdnimg.cn/5b2c713355374918a32eea9ddfe5ca1f.png)
这种单体项目的优缺点如下：
* 优点
    * 架构简单
    * 部署成本低
* 缺点
    * 模块与模块之间耦合度很高，导致后期维护和升级的成本很高
### 1.2 分布式架构
分布式架构根据业务功能对系统进行拆分，每一个业务作为单独的模块独立开发，成为一个服务
![在这里插入图片描述](https://img-blog.csdnimg.cn/afe4aebe01a6463a97d7e330d426e93e.png)
分布式的优缺点
* 优点
    * 降低了模块之间的耦合
    * 有利于服务的升级和扩展
* 缺点
    * 正如上图中看到的，服务调用关系错中复杂，一旦模块较多，维护起来就很困难

分布式架构虽然降低了服务之间的耦合度，但是在进行服务拆分的时候需要考虑一下问题
* 服务拆分的粒度
* 服务之间如何调用
* 服务之间的这种调用关系如何管理

为了指定一套行之有效的标准来约束和优化分布式架构，这种标准就叫做微服务。
### 1.3 微服务
微服务是一种**经过良好架构设计的分布式架构**方案，微服务满足一下特征
* 单一职责：微服务拆分的粒度很小，保证每一个服务都有对应的唯一业务能力，做到单一职责原则
* 自治：
    * 团队独立：开发一个微服务模块的团队与团队之间是独立的
    * 技术独立：开发不同的微服务模块可以采用不同技术
    * 数据独立：不同的微服务模块之间可以有自己独立的数据库，进一步做到数据解耦
    * 独立部署：每一个模块都可以单独部署
* 面向服务：服务提供统一的接口标准，和语言与技术无关
* 隔离性强：服务之间可以互相隔离
  ![在这里插入图片描述](https://img-blog.csdnimg.cn/6c1ef958422340669047349e5d81541b.png)

微服务的上述特征实际上就是一个分布式架构的指定标准，这个标准可以进一步的降低服务之间的耦合度，提高服务的灵活性和独立性。做到高内聚低耦合，
### 1.4 Spring Cloud 常见组件
![在这里插入图片描述](https://img-blog.csdnimg.cn/c0777c21a888451694169c7f834a52e7.png)
### 1.5 总结
* 单体架构：简单方便，高度耦合，扩展性差，适合小型项目。例如：学生管理系统

* 分布式架构：松耦合，扩展性好，但架构复杂，难度大。适合大型互联网项目，例如：京东、淘宝

* 微服务：一种良好的分布式架构方案

  ①优点：拆分粒度更小、服务更独立、耦合度更低

  ②缺点：架构非常复杂，运维、监控、部署难度提高
* SpringCloud是微服务架构的一站式解决方案，集成了各种优秀微服务功能组件
# 2. 服务拆分和远程方法调用
### 2.1 服务拆分原则
* 不同的微服务不要重复开发相同的业务
* 微服务的数据独立，不要访问其他服务的数据库
* 微服务可以将自己的业务暴露成接口，供其他服务调用
  ![在这里插入图片描述](https://img-blog.csdnimg.cn/cd88d050f4b24a54aa5be2f0d6035bb2.png)
### 2.2 远程方法调用
#### 2.2.1 背景
```java
@GetMapping("{orderId}")
    public Order queryOrderByUserId(@PathVariable("orderId") Long orderId) {
        // 根据id查询订单并返回
        return orderService.queryOrderById(orderId);
    }
```
在order-service服务中，有一个根据id查询订单的接口：
根据id查询订单，返回值是Order对象，如图：
![在这里插入图片描述](https://img-blog.csdnimg.cn/fbad163ecc384da88570d57a80bfa4f1.png)
我们注意到，此时user为空，这是因为在order微服务中无法访问user相关的数据。我们需要调用user微服务中的接口

```java
@GetMapping("/{id}")
    public User queryById(@PathVariable("id") Long id) {
        return userService.queryById(id);
    }
```
查询的结果如图：
![在这里插入图片描述](https://img-blog.csdnimg.cn/754720e0778944368e0498ddb9ac92e2.png)
#### 2.2.2 需求
我们需要在order微服务中查询到用户信息并添加到order对象中，由于处于不同的微服务中，我们不能像之前单体项目一样直接使用`userService`对象进行调用。
我们需要使用http请求，我们需要在order-service中 向`user-service`发起一个http的请求，调用`http://localhost:8081/user/{userId}`这个接口。，获取到用户信息。并且将用户信息一并封装到order对象中返回。
![在这里插入图片描述](https://img-blog.csdnimg.cn/3a7d6379b3a44d9bbb6f04e7e7fd93ef.png)
这里我们就需要使用Spring中的`RestTemplate`对象进行远程方法调用。具体步骤如下
* 在配置类中注册一个`RestTemplate`实例到IOC容器中
* 修改order-service服务中的OrderService类中的queryOrderById方法，使用`RestTemplate`对象中的相关方法调用user微服务中的接口
* 将查询的User填充到Order对象，一起返回
#### 2.2.3 具体步骤
```java
@MapperScan("cn.itcast.order.mapper")
@SpringBootApplication
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

```java
@Service
public class OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private RestTemplate restTemplate;

    public Order queryOrderById(Long orderId) {
        // 1.查询订单
        Order order = orderMapper.findById(orderId);
        String url = "http://userservice/user/" + order.getUserId();
        User user = restTemplate.getForObject(url, User.class);
        // 4.返回
        order.setUser(user);
        return order;
    }
}
```
# 3. 提供者和消费者
在服务调用关系中，会有两个不同的角色：

**服务提供者**：一次业务中，被其它微服务调用的服务。（提供接口给其它微服务）
**服务消费者**：一次业务中，调用其它微服务的服务。（调用其它微服务提供的接口）
![在这里插入图片描述](https://img-blog.csdnimg.cn/24a2be6a007945abbc93cc8392c53115.png)
> 服务提供者与服务消费者的角色并不是绝对的，而是相对于业务而言。如果服务A调用了服务B，而服务B又调用了服务C，服务B的角色是什么对于A调用B的业务而言：
>*  A是服务消费者，B是服务提供者对于B调用C的业务而言：
>*  B是服务消费者，C是服务提供者

因此，**服务B既可以是服务提供者，也可以是服务消费者**
# 4. Eureka注册中心
之前我们在远程方法调用的时候直接是写死的http地址，这种应变法的方式显然是不合理的。

```java
String url = "http://userservice/user/" + order.getUserId();
User user = restTemplate.getForObject(url, User.class);
```
1. 假设有很多微服务，这样的写法不利于微服务的管理
2. 在集群环境下，如果user微服务有多个实例地址，那么如何调用？
3. 如何判断我们要调用的微服务模块是不是健康，会不会宕机了，导致连锁反映？
   这时候我们就想应该有一个专门的组件来负责转发我们对于微服务的请求，这个组件就是注册中心
### 4.1 简介
![在这里插入图片描述](https://img-blog.csdnimg.cn/2daac3fbf20e440d9449d4c0607d17f4.png)
我们来依次回答之前的问题
1. 在集群环境下，如果user微服务有多个实例地址，那么如何调用？
    * 每一个服务启动以后，都会到注册中心中注册，里面包含了服务的名称，和对应的IP地址以及端口号，在注册中心中，将这种映射关系保存起来。
    * 根据要调用的微服务名称，找到对应的实例的过程叫做服务发现或者服务拉取。
2. 在集群模式下如何调用？
    * 注册中心会使用负载均衡算法在多个实例中选择一个发起远程调用
3. order-service如何得知某个user-service实例是否依然健康，是不是已经宕机？
    * user-service会每隔一段时间（默认30秒）向eureka-server发起请求，报告自己状态，称为心跳
    * 当超过一定时间没有发送心跳时，eureka-server会认为微服务实例故障，将该实例从服务列表中剔除
### 4.2 Eureka注册中心的使用
#### 4.2.1 创建模块
在cloud-demo父工程下，创建一个子模块：
![在这里插入图片描述](https://img-blog.csdnimg.cn/f7bc6f1636cb482d94d130cf73c9f5e7.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/afc131b1f76d42c597cf822588313c94.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/18faaea614ce49549c4f9d5c6093319e.png)
#### 4.2.2 引入依赖，创建配置文件
在新创建的模块引入SpringCloud为eureka提供的服务端starter依赖：

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```
给eureka-server服务编写一个启动类，一定要添加一个@EnableEurekaServer注解，开启eureka的注册中心功能：

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaApplication.class, args);
    }
}
```
编写一个application.yml文件，内容如下：

```yaml
server:
  port: 10086
spring:
  application:
    name: eureka-server
eureka:
  client:
    service-url: 
      defaultZone: http://127.0.0.1:10086/eureka
```
#### 4.2.3 启动服务
启动微服务，然后在浏览器访问：http://127.0.0.1:10086
看到下面结果应该是成功了：
![在这里插入图片描述](https://img-blog.csdnimg.cn/e4744848fe874d9f8345267848df8391.png)
#### 4.2.4 注册服务
目前我们只完成了注册中心的配置，现在还要将服务注册到注册中心。
下面，我们将user-service注册到eureka-server中去。
在客户端引入依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```
在user-service中，修改application.yml文件，添加服务名称、eureka地址：
```yaml
spring:
  application:
    name: userservice
eureka:
  client:
    service-url:
      defaultZone: http://127.0.0.1:10086/eureka
```
至此，我们的微服务就已经注册到了注册中心中
![在这里插入图片描述](https://img-blog.csdnimg.cn/715add3d2d064f44b3b759bc1c9a101c.png)

> 我们需要将user和order都注册到注册中心中。

为了实现负载均衡，还需要在提供者的启动类中加上相关注解

```java
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
```
#### 4.2.4 修改之前调用代码
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
### 4.3 负载均衡原理
SpringCloudRibbon的底层采用了一个拦截器，拦截了RestTemplate发出的请求，对地址做了修改。用一幅图来总结一下：
![在这里插入图片描述](https://img-blog.csdnimg.cn/8ea66700a869409d9b191b1cb7c81738.png)
基本流程如下：

- 拦截我们的RestTemplate请求http://userservice/user/1
- RibbonLoadBalancerClient会从请求url中获取服务名称，也就是user-service
- DynamicServerListLoadBalancer根据user-service到eureka拉取服务列表
- eureka返回列表，localhost:8081、localhost:8082
- IRule利用内置负载均衡规则，从列表中选择一个，例如localhost:8081
- RibbonLoadBalancerClient修改请求地址，用localhost:8081替代userservice，得到http://localhost:8081/user/1，发起真实请求
### 4.3 自定义负载均衡策略
通过定义IRule实现可以修改负载均衡规则，有两种方式：
* 代码方式：在order-service中的OrderApplication类中，定义一个新的IRule：

```java
@Bean
public IRule randomRule(){
    return new RandomRule();
}
```
* 配置文件方式：在order-service的application.yml文件中，添加新的配置也可以修改规则

```yaml
userservice: # 给某个微服务配置负载均衡规则，这里是userservice服务
  ribbon:
    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule # 负载均衡规则 
```

> **注意**，一般用默认的负载均衡规则，不做修改。这两种方式的作用范围不同，第一种方式作用范围是全局生效。第二种方式可以针对不同的微服务模板单独设置负载均衡策略

# 5. Nacos注册中心
### 5.1 简介
Nacos是SpringCloudAlibaba的组件，而SpringCloudAlibaba也遵循SpringCloud中定义的服务注册、服务发现规范。因此使用Nacos和使用Eureka对于微服务来说，并没有太大区别。

### 5.1 使用步骤
**引入依赖**
在cloud-demo父工程的pom文件中的`<dependencyManagement>`中引入SpringCloudAlibaba的依赖：

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-alibaba-dependencies</artifactId>
    <version>2.2.6.RELEASE</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

然后在user-service和order-service中的pom文件中引入nacos-discovery依赖：

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

**配置nacos地址**
在cloud-demo父工程的pom文件中的`<dependencyManagement>`中引入SpringCloudAlibaba的依赖：

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-alibaba-dependencies</artifactId>
    <version>2.2.6.RELEASE</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

然后在user-service和order-service中的pom文件中引入nacos-discovery依赖：

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```
之后启动服务，这些服务就被注册到了nacos中
![在这里插入图片描述](https://img-blog.csdnimg.cn/ef7b9e3b0d48431b925cea1afe088da1.png)

### 5.2配置集群
修改user-service的application.yml文件，添加集群配置：

```yaml
spring:
  cloud:
    nacos:
      server-addr: localhost:8848
      discovery:
        cluster-name: HZ # 集群名称
```
这里创建了3个user实例，分别位于HZ集群和SH集群
![在这里插入图片描述](https://img-blog.csdnimg.cn/b87f2a1da1f84d7189200ea9837ef2ab.png)
### 5.3 负载均衡策略
#### 5.3.1 NacosRule
默认的`ZoneAvoidanceRule`规则并不会在不同的机房中选择，他会随机的去访问HZ和SH集群，(这里order在HZ集群)。理论上来说应该优先访问同集群的微服务。
因此我们修改默认的负载均衡策略

修改order-service的application.yml文件，修改负载均衡规则：
```yaml
userservice:
  ribbon:
    NFLoadBalancerRuleClassName: com.alibaba.cloud.nacos.ribbon.NacosRule # 负载均衡规则 
```
这个策略如下
* 优先访问同集群的微服务
* 如果同集群的微服务宕机，则访问其他集群中的
* 同集群的微服务随机访问
#### 5.3.2 权重配置
实际部署中会出现这样的场景：

服务器设备性能有差异，部分实例所在机器性能较好，另一些较差，我们希望性能好的机器承担更多的用户请求。

但默认情况下NacosRule是同集群内随机挑选，不会考虑机器的性能问题。

因此，Nacos提供了权重配置来控制访问频率，权重越大则访问频率越高。
在nacos控制台，找到user-service的实例列表，点击编辑，即可修改权重：
![在这里插入图片描述](https://img-blog.csdnimg.cn/a6e95dcbd84a486a960d91527381119e.png)

> **注意**：如果权重修改为0，则该实例永远不会被访问
### 5.4 namespace
在不同的namespace下的微服务互相隔离，不能访问。
Nacos提供了namespace来实现环境隔离功能。
* nacos中可以有多个namespace
* namespace下可以有group、service等
* 不同namespace之间相互隔离，例如不同namespace的服务互相不可见
  ![在这里插入图片描述](https://img-blog.csdnimg.cn/c63116bc56444426a4789231d52f01bd.png)
  给微服务配置namespace只能通过修改配置来实现。

例如，修改order-service的application.yml文件：

```yaml
spring:
  cloud:
    nacos:
      server-addr: localhost:8848
      discovery:
        cluster-name: HZ
        namespace: 492a7d5d-237b-46a1-a99a-fa8e98e4b0f9 # 命名空间，填ID
```

