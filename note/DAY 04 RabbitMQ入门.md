# 1. 消息队列(MQ)简介
### 1.1 同步和异步通讯
微服务之间的通讯有两种方式
* 同步通信：就像打电话，需要实时的响应。程序调用另一个微服务，在另一个微服务响应过程中等待。
* 异步通信：就像发微信，发过去就结束了，然后继续干别的事情。
![在这里插入图片描述](https://img-blog.csdnimg.cn/0c3e7accf9944073a67e983f4ca3491b.png)
两种方式各有优劣，打电话可以立即得到响应，但是你却不能跟多个人同时通话。发送邮件可以同时与多个人收发邮件，但是往往响应会有延迟。

### 1.2 同步通信
之前学习的Feign就属于同步通讯的方式，它可以实时的得到结果，但是存在以下的问题
* **耦合度高**，每次添加新的需求，都需要修改代码。比如之前下单业务只需要保存订单信息，现在要添加一个发送短信提醒的功能，那么就不得不修改代码。
* **性能下降**：当调用者等待服务者的响应的时候，只能等待，且如果调用者需要调用多个服务，那么整个的耗时就是多个服务响应时间之和。
* **资源浪费**，在等待过程中不能释放其他的资源，导致资源浪费
* **级联失败**：如果其中一个服务出现问题，那么所有的服务都会失败。
![在这里插入图片描述](https://img-blog.csdnimg.cn/a231bc7a5258421f82a2b0cf94d08f81.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/b9a3e9d051e44080a1b40dd078c1ac91.png)

同步调用的优点：

- 时效性较强，可以立即得到结果

同步调用的问题：

- 耦合度高
- 性能和吞吐能力下降
- 有额外的资源消耗
- 有级联失败问题

### 1.3 异步通讯
采用异步通讯可以避免上述的问题。
比如购买商品，用户完成支付后，需要调用订单服务完成订单状态的修改，调用物流服务准备发货，减少库存等。

在事件模式中，支付服务是事件发布者（publisher），在支付完成后只需要发布一个支付成功的事件（event），事件中带上订单id。然后支付服务就执行完毕了。

订单服务和物流服务是事件订阅者（Consumer），订阅支付成功的事件，监听到事件后完成自己业务即可。
![在这里插入图片描述](https://img-blog.csdnimg.cn/460fb0a37c0640b6ab2d5dd683642485.png)

此外，为了降低发送者和消费者之间的耦合，两者不是直接通讯的，需要有一个中间人，也就是消息队列。发送者将消息发送到队列，该队列的订阅者从队列中取出消息。
![在这里插入图片描述](https://img-blog.csdnimg.cn/cb45894a76db49068dd201d37f0bb6cd.png)
采用队列方式的好处：

**服务解耦**：在引入消息队列以后，这时候如果要添加新的功能，只需要让这个新功能的服务订阅这个消息队列即可，不需要修改支付服务的代码。
![在这里插入图片描述](https://img-blog.csdnimg.cn/19df222f76e0472a9ceec94337f649a5.png)
**提高性能**：不同的任务可以同时执行，支付服务的耗时不再是所有的服务耗时之和
![在这里插入图片描述](https://img-blog.csdnimg.cn/b873f7d5a0fb40cda88a966d7b6c4773.png)
**解决了级联失败**：不同的服务之间没有依赖关系，即使某一个服务挂掉了，其他的服务依然可以运行
![在这里插入图片描述](https://img-blog.csdnimg.cn/e205832e10f34981a65ccf9cfea1abbd.png)
**流量削峰**：当大量并发时，消息队列可以起到缓冲区的作用。
![在这里插入图片描述](https://img-blog.csdnimg.cn/5744d759c132419f806e7c7a9acff0c0.png)

缺点：
- 架构复杂了，业务没有明显的流程线，不好管理
- 需要依赖于Broker的可靠、安全、性能

### 1.4 常见的消息队列
MQ，中文是消息队列（MessageQueue），字面来看就是存放消息的队列。也就是事件驱动架构中的Broker。

比较常见的MQ实现：

- ActiveMQ
- RabbitMQ
- RocketMQ
- Kafka
几种常见MQ的对比：

|            | **RabbitMQ**            | **ActiveMQ**                   | **RocketMQ** | **Kafka**  |
| ---------- | ----------------------- | ------------------------------ | ------------ | ---------- |
| 公司/社区  | Rabbit                  | Apache                         | 阿里         | Apache     |
| 开发语言   | Erlang                  | Java                           | Java         | Scala&Java |
| 协议支持   | AMQP，XMPP，SMTP，STOMP | OpenWire,STOMP，REST,XMPP,AMQP | 自定义协议   | 自定义协议 |
| 可用性     | 高                      | 一般                           | 高           | 高         |
| 单机吞吐量 | 一般                    | 差                             | 高           | 非常高     |
| 消息延迟   | 微秒级                  | 毫秒级                         | 毫秒级       | 毫秒以内   |
| 消息可靠性 | 高                      | 一般                           | 高           | 一般       |


# 2. RabbitMQ入门
### 2.1 RabbitMQ中的角色和消息模型
RabbitMQ中的一些角色：

- publisher：生产者
- consumer：消费者
- exchange个：交换机，负责消息路由
- queue：队列，存储消息
- virtualHost：虚拟主机，隔离不同租户的exchange、queue、消息的隔离
![在这里插入图片描述](https://img-blog.csdnimg.cn/05cdd0b1161743e0b66de996db45e0b4.png)
RabbitMQ官方提供了5个不同的Demo示例，对应了不同的消息模型：
![在这里插入图片描述](https://img-blog.csdnimg.cn/7fabcf9f43d34abebc1020bac90ed6e2.png)
### 2.2 基本消息队列
基本消息队列只包含三个角色：发送者，消费者和队列
![在这里插入图片描述](https://img-blog.csdnimg.cn/fa498e98ae3b4d8dabd59b13cce1e4b5.png)
**向队列中发送一条消息**
我们在发送者的代码中实现发送消息到到名为“simple.queue”队列
**思路：**
- 建立连接
- 创建Channel
- 声明队列
- 发送消息
- 关闭连接和channel
```java
public class PublisherTest {
    @Test
    public void testSendMessage() throws IOException, TimeoutException {
        // 1.建立连接
        ConnectionFactory factory = new ConnectionFactory();
        // 1.1.设置连接参数，分别是：主机名、端口号、vhost、用户名、密码
        factory.setHost("95.179.138.182");
        factory.setPort(5672);
        factory.setVirtualHost("/");
        factory.setUsername("itcast");
        factory.setPassword("123321");
        // 1.2.建立连接
        Connection connection = factory.newConnection();

        // 2.创建通道Channel
        Channel channel = connection.createChannel();

        // 3.创建队列
        String queueName = "simple.queue";
        channel.queueDeclare(queueName, false, false, false, null);

        // 4.发送消息
        String message = "hello, rabbitmq!";
        channel.basicPublish("", queueName, null, message.getBytes());
        System.out.println("发送消息成功：【" + message + "】");

        // 5.关闭通道和连接
        channel.close();
        connection.close();

    }
}
```
**从队列中读取一条消息**
**思路：**
- 建立连接
- 创建Channel
- 声明队列
- 订阅消息

**在消费者的代码中添加一下代码**

```java
public class ConsumerTest {

    public static void main(String[] args) throws IOException, TimeoutException {
        // 1.建立连接
        ConnectionFactory factory = new ConnectionFactory();
        // 1.1.设置连接参数，分别是：主机名、端口号、vhost、用户名、密码
        factory.setHost("95.179.138.182");
        factory.setPort(5672);
        factory.setVirtualHost("/");
        factory.setUsername("itcast");
        factory.setPassword("123321");
        // 1.2.建立连接
        Connection connection = factory.newConnection();

        // 2.创建通道Channel
        Channel channel = connection.createChannel();

        // 3.创建队列
        String queueName = "simple.queue";
        channel.queueDeclare(queueName, false, false, false, null);

        // 4.订阅消息
        channel.basicConsume(queueName, true, new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                // 5.处理消息
                String message = new String(body);
                System.out.println("接收到消息：【" + message + "】");
            }
        });
        System.out.println("等待接收消息。。。。");
    }
}
```
> 注意：这里为什么在消费者的代码里面也有创建队列的代码呢？
>
> 原因是发布者和消费者的执行顺序是随机的，因此不一定谁先执行，因此都要创建队列。创建重复名字的队列不会覆盖原有队列的消息

#### 2.2.3 小结
基本消息队列的消息发送流程：

1. 建立connection

2. 创建channel

3. 利用channel声明队列

4. 利用channel向队列发送消息

基本消息队列的消息接收流程：

1. 建立connection

2. 创建channel

3. 利用channel声明队列

4. 定义consumer的消费行为handleDelivery()

5. 利用channel将消费者与队列绑定

# 3. Spring AMQP
上一章我们演示了如何使用Java代码到队列中发送和读取消息，代码非常的复杂不够优雅，Spring为我们提供了一个框架封装了对RabbitMQ的一系列操作。
![在这里插入图片描述](https://img-blog.csdnimg.cn/c953c22b43b94aa4af75a3078be98b97.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/b4efa4811d3c4135a36ec8fd465291f4.png)
SpringAMQP提供了三个功能：

- 自动声明队列、交换机及其绑定关系
- 基于注解的监听器模式，异步接收消息
- 封装了RabbitTemplate工具，用于发送消息 

### 3.1 简单队列模型
#### 3.1.1 消息发送
**在父工程，引入Spring AMQP先依赖，这样发布者和消费者子工程就不需要再次引入**

```xml
<!--AMQP依赖，包含RabbitMQ-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```
**在publisher服务的application.yml中添加配置：**


```yaml
spring:
  rabbitmq:
    host: 95.179.138.182 # 主机名
    port: 5672 # 端口
    virtual-host: / # 虚拟主机
    username: itcast # 用户名
    password: 123321 # 密码
```



**在publisher服务中编写测试类SpringAmqpTest，并利用RabbitTemplate实现消息发送：**

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringAmqpTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSimpleQueue() {
        // 队列名称
        String queueName = "simple.queue";
        // 消息
        String message = "hello, spring amqp!";
        // 发送消息
        rabbitTemplate.convertAndSend(queueName, message);
    }
}
```
对比之前发送消息的代码，可以看出使用Spring AMQP以后优雅了很多
#### 3.1.2 消息接收
**在consumer服务的application.yml中添加配置：**


```yaml
spring:
  rabbitmq:
    host: 95.179.138.182 # 主机名
    port: 5672 # 端口
    virtual-host: / # 虚拟主机
    username: itcast # 用户名
    password: 123321 # 密码
```

然后在消费者的`cn.itcast.mq.listener`包中新建一个类`SpringRabbitListener`，代码如下：

```java
package cn.itcast.mq.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SpringRabbitListener {

    @RabbitListener(queues = "simple.queue")
    public void listenSimpleQueueMessage(String msg) throws InterruptedException {
        System.out.println("spring 消费者接收到消息：【" + msg + "】");
    }
}
```
### 3.2 工作队列模型
和简单队列模型不同，工作队列模型可以有多个消费者。
![在这里插入图片描述](https://img-blog.csdnimg.cn/3167ace26a8c4eecac9ce065cd5dd6f3.png)
当消息比较多的时候，发送消息的速度可能远远大于一个消费者处理的速度，这个时候可以考虑使用这个模型，让多个消费者同时消费一个队列里面的消息，来加速消息处理的速度
#### 3.2.1 消息发送
我们在发送者的代码中设置在1秒之内发送50条消息

```java
@Test
public void testWorkQueue() throws InterruptedException {
    // 队列名称
    String queueName = "simple.queue";
    // 消息
    String message = "hello, message_";
    for (int i = 0; i < 50; i++) {
        // 发送消息
        rabbitTemplate.convertAndSend(queueName, message + i);
        Thread.sleep(20);
    }
}
```
#### 3.2.2 消息接收
在消费者中添加两个消费者同时消费。其中消费者1的处理速度是消费者2处理速度的10倍。

```java
@RabbitListener(queues = "simple.queue")
public void listenWorkQueue1(String msg) throws InterruptedException {
    System.out.println("消费者1接收到消息：【" + msg + "】" + LocalTime.now());
    Thread.sleep(20);
}

@RabbitListener(queues = "simple.queue")
public void listenWorkQueue2(String msg) throws InterruptedException {
    System.err.println("消费者2........接收到消息：【" + msg + "】" + LocalTime.now());
    Thread.sleep(200);
}
```
#### 3.2.3 测试
经过测试发现，消费者1很快处理完了25条消息，消费者2缓慢处理25条消息。也就是说**消息是平均分配给每个消费者，并没有考虑到消费者的处理能力。这样显然是有问题的。**
#### 3.2.4 能者多劳
在spring中有一个简单的配置，可以解决这个问题。我们修改consumer服务的application.yml文件，添加配置：

```yaml
spring:
  rabbitmq:
    listener:
      simple:
        prefetch: 1 # 每次只能获取一条消息，处理完成才能获取下一个消息
```

#### 3.2.5 小结
工作队列的使用
- 多个消费者绑定到一个队列，同一条消息只会被一个消费者处理
- 通过设置prefetch来控制消费者预取的消息数量

### 3.3 发布/订阅
在发布/订阅模型中，角色发生了变化，多了一个exchange角色。消息发布和接收的过程如下
* 发布者将消息发送给exchange
* exchange接收发布者发送的消息，然后根据exchange的类型，选择如何处理该消息
	* Fanout：广播模式，exhange将消息发送到所有和其绑定的队列中
	* Direct：exchange将消息交给符合指定的 routing key 的队列
	* Topic：exchange把消息交给符合routing pattern（路由模式） 的队列
* 消费者依然是从队列中读取数据
* 队列也和之前一样
![在这里插入图片描述](https://img-blog.csdnimg.cn/cde0baca89e14db4bc9955a2f6da9658.png)
**Exchange（交换机）只负责转发消息，不具备存储消息的能力**，因此如果没有任何队列与Exchange绑定，或者没有符合路由规则的队列，那么消息会丢失！

### 3.4 Fanout模型
![在这里插入图片描述](https://img-blog.csdnimg.cn/ced93834e1854da6a67bafc15d8c6fa9.png)
在Fanout模式下，消息发送流程是这样的：
* 可以存在多个队列，每一个队列都需要绑定Exchange
* 发布者发布的消息交给exchange，由exhange转发到所有和其绑定的队列中
* 消费者从队列中消费消息
#### 3.4.1 案例
- 创建一个交换机 itcast.fanout，类型是Fanout
- 创建两个队列fanout.queue1和fanout.queue2，绑定到交换机itcast.fanout
![在这里插入图片描述](https://img-blog.csdnimg.cn/c071c93c0bfe476ca057d50d43f62c31.png)
**创建exchange和队列并将其绑定在一起**

```java
@Configuration
public class FanoutConfig {
    /**
     * 声明交换机
     * @return Fanout类型交换机
     */
    @Bean
    public FanoutExchange fanoutExchange(){
        return new FanoutExchange("itcast.fanout");
    }

    /**
     * 第1个队列
     */
    @Bean
    public Queue fanoutQueue1(){
        return new Queue("fanout.queue1");
    }

    /**
     * 绑定队列和交换机
     */
    @Bean
    public Binding bindingQueue1(Queue fanoutQueue1, FanoutExchange fanoutExchange){
        return BindingBuilder.bind(fanoutQueue1).to(fanoutExchange);
    }

    /**
     * 第2个队列
     */
    @Bean
    public Queue fanoutQueue2(){
        return new Queue("fanout.queue2");
    }

    /**
     * 绑定队列和交换机
     */
    @Bean
    public Binding bindingQueue2(Queue fanoutQueue2, FanoutExchange fanoutExchange){
        return BindingBuilder.bind(fanoutQueue2).to(fanoutExchange);
    }
}
```
**消息发送**
在publisher服务的SpringAmqpTest类中添加测试方法：

```java
@Test
public void testFanoutExchange() {
    // 队列名称
    String exchangeName = "itcast.fanout";
    // 消息
    String message = "hello, everyone!";
    rabbitTemplate.convertAndSend(exchangeName, "", message);
}
```
**消息接收**

在consumer服务的SpringRabbitListener中添加两个方法，作为消费者：

```java
@RabbitListener(queues = "fanout.queue1")
public void listenFanoutQueue1(String msg) {
    System.out.println("消费者1接收到Fanout消息：【" + msg + "】");
}

@RabbitListener(queues = "fanout.queue2")
public void listenFanoutQueue2(String msg) {
    System.out.println("消费者2接收到Fanout消息：【" + msg + "】");
}
```


运行代码可以发现，两个消费者都收到了消息
![在这里插入图片描述](https://img-blog.csdnimg.cn/827677a826034efa9f4277cd8ba99644.png)
### 3.5 Direct模型
在Fanout模式中，一条消息，会被所有订阅的队列都消费。但是，在某些场景下，我们希望不同的消息被不同的队列消费。这时就要用到Direct类型的Exchange。
![在这里插入图片描述](https://img-blog.csdnimg.cn/3cec2cf1a77b49dcacfb10573b35a85e.png)
在Direct模型下：
* 消费者和队列绑定时需要为自己设定一个`RoutingKey`，标明队列只接受符合`RoutingKey`的消息。
* 消息的发送方在 向 Exchange发送消息时，也必须指定消息的 `RoutingKey`。
* Exchange不再把消息交给每一个绑定的队列，而是根据消息的`Routing Key`进行判断，只有队列的`Routingkey`与消息的 `Routing key`完全一致，才会接收到消息
#### 3.5.1 案例
1. 利用@RabbitListener声明Exchange、Queue、RoutingKey

2. 在consumer服务中，编写两个消费者方法，分别监听direct.queue1和direct.queue2

3. 在publisher中编写测试方法，向itcast. direct发送消息
![在这里插入图片描述](https://img-blog.csdnimg.cn/7e999ba0667248daa32d28db9429f0d5.png)

基于@Bean的方式声明队列和交换机比较麻烦，Spring还提供了基于注解方式来声明。
在consumer的SpringRabbitListener中添加两个消费者，同时基于注解来声明队列和交换机：
> 采用这种注解方式可以直接写队列名称，如果队列或exchange不存在则自动创建

**消费者代码**
```java
@RabbitListener(bindings = @QueueBinding(
    value = @Queue(name = "direct.queue1"),
    exchange = @Exchange(name = "itcast.direct", type = ExchangeTypes.DIRECT),
    key = {"red", "blue"}
))
public void listenDirectQueue1(String msg){
    System.out.println("消费者接收到direct.queue1的消息：【" + msg + "】");
}

@RabbitListener(bindings = @QueueBinding(
    value = @Queue(name = "direct.queue2"),
    exchange = @Exchange(name = "itcast.direct", type = ExchangeTypes.DIRECT),
    key = {"red", "yellow"}
))
public void listenDirectQueue2(String msg){
    System.out.println("消费者接收到direct.queue2的消息：【" + msg + "】");
}
```
消息发送代码

```java
@Test
public void testSendDirectExchange() {
    // 交换机名称
    String exchangeName = "itcast.direct";
    // 消息
    String message = "红色警报！日本乱排核废水，导致海洋生物变异，惊现哥斯拉！";
    // 发送消息
    rabbitTemplate.convertAndSend(exchangeName, "blue", message);
}
```
> 我们在代码中的`Rounting Key`是blue，因此只有消费者1可以收到我们发送的消息


### 3.6 Topic模型
和Direct模型相比，Topic模型允许绑定`Routing key` 的时候使用通配符！
 通配符规则：

`#`：匹配一个或多个词

`*`：匹配不多不少恰好1个词



举例：

`China.#`：能够匹配`China.News` 或者 `China.weather`

`#.weather`：只能匹配`Japan.weather` `China.weather`

![在这里插入图片描述](https://img-blog.csdnimg.cn/4ea662a0063e4f5ebb97245205083237.png)
- Queue1：绑定的是`china.#` ，因此凡是以 `china.`开头的`routing key` 都会被匹配到。包括china.news和china.weather
- Queue2：绑定的是`#.news` ，因此凡是以 `.news`结尾的 `routing key` 都会被匹配。包括china.news和japan.news

#### 3.6.1 案例
1. 并利用@RabbitListener声明Exchange、Queue、RoutingKey

2. 在consumer服务中，编写两个消费者方法，分别监听topic.queue1和topic.queue2

3. 在publisher中编写测试方法，向itcast. topic发送消息
![在这里插入图片描述](https://img-blog.csdnimg.cn/5253fdb02c8a497ea0f7c4f1c26de330.png)
**消息发送**

```java
@Test
public void testSendTopicExchange() {
    // 交换机名称
    String exchangeName = "itcast.topic";
    // 消息
    String message = "喜报！孙悟空大战哥斯拉，胜!";
    // 发送消息
    rabbitTemplate.convertAndSend(exchangeName, "china.news", message);
}
```
**消息接收**
在consumer服务的SpringRabbitListener中添加方法：

```java
@RabbitListener(bindings = @QueueBinding(
    value = @Queue(name = "topic.queue1"),
    exchange = @Exchange(name = "itcast.topic", type = ExchangeTypes.TOPIC),
    key = "china.#"
))
public void listenTopicQueue1(String msg){
    System.out.println("消费者接收到topic.queue1的消息：【" + msg + "】");
}

@RabbitListener(bindings = @QueueBinding(
    value = @Queue(name = "topic.queue2"),
    exchange = @Exchange(name = "itcast.topic", type = ExchangeTypes.TOPIC),
    key = "#.news"
))
public void listenTopicQueue2(String msg){
    System.out.println("消费者接收到topic.queue2的消息：【" + msg + "】");
}
```
> 我们发送的`Rounting Key`是`china.news`，因此消费者1和消费者2都能收到我们发送的消息


### 3.7 消息转换器
之前说过，Spring会把你发送的消息序列化为字节发送给MQ，接收消息的时候，还会把字节反序列化为Java对象。
我们修改消息发送的代码，发送一个Map对象：

```java
@Test
public void testSendMap() throws InterruptedException {
    // 准备消息
    Map<String,Object> msg = new HashMap<>();
    msg.put("name", "Jack");
    msg.put("age", 21);
    // 发送消息
    rabbitTemplate.convertAndSend("simple.queue","", msg);
}
```
到RabbitMQ的UI界面中查看消息内容
![在这里插入图片描述](https://img-blog.csdnimg.cn/205ecb39aa984e179d398236ad3bcee5.png)
发现RabbitMQ使用是JDK的序列化方式，这种方式由一下缺点：
- 数据体积过大
- 有安全漏洞
- 可读性差

如何更换序列化器成JSON的呢？

**引入依赖**
在publisher和consumer两个服务中都引入依赖：

```xml
<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-xml</artifactId>
    <version>2.9.10</version>
</dependency>
```

**配置消息转换器**
在启动类中添加一个Bean即可：

```java
@Bean
public MessageConverter jsonMessageConverter(){
    return new Jackson2JsonMessageConverter();
}
```