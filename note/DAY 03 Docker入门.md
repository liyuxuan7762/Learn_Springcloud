# 1. Docker简介
### 1.1 微服务带来的问题
之前虽然讲过采用微服务方式的各种优势，但是这种拆分的方式也给部署带来的巨大的麻烦
* 分布式系统中通常会依赖很多的组件，这些组件在部署的时候可能会产生各种的冲突
* 分布式项目部署的服务器的环境不一定一致，有的是乌班图，有的是CentOS，不同的操作系统需要不同版本的组件，这给部署带来了巨大的麻烦。
![在这里插入图片描述](https://img-blog.csdnimg.cn/77a1b59275ae4ee7aeb06a9f2bff3f0c.png)
### 1.2 使用Docker解决依赖问题
Docker巧妙的解决了上述的问题，它是如何实现的？
* Docker将应用所需要的所有的LibLibs（函数库）、Deps（依赖）、配置与应用一起打包。
* 将每一个应用都放到一个隔离的容器中，避免互相干扰。
![在这里插入图片描述](https://img-blog.csdnimg.cn/9b5abae781954943a6b09b77da26ea99.png)打包后的应用既包含应用本身，也保护应用所需要的Libs、Deps，无需在操作徐彤上安装这些应用。
### 1.3 Docker解决操作系统环境差异
要解决不同操作系统环境差异问题，必须先了解操作系统结构。以一个Ubuntu操作系统为例，结构如下：
![在这里插入图片描述](https://img-blog.csdnimg.cn/0b7dc8f8ddf94e1083ed8316f4c2419e.png)
应用于计算机交互的流程如下：
1. 应用调用操作系统应用（函数库），实现各种功能
2. 系统函数库是对内核指令集的封装，会调用内核指令
3. 内核指令操作计算机硬件
> 所有Linux发行版的内核都是Linux，例如CentOS、Ubuntu、Fedora等。内核可以与计算机硬件交互，对外提供**内核指令**，用于操作计算机硬件。

不同操作系统软件的差异仅仅是在调用操作系统函数上，如果将操作系统的函数也封装起来，那么就可以实现在不同的Linux操作系统上运行，因为所有的Linux操作系统的内核是一样的。Docker就是基于这个原理。

Docker如何解决不同系统环境的问题？
- Docker将用户程序与所需要调用的系统(比如Ubuntu)函数库一起打包
- Docker运行到不同操作系统时，直接基于打包的函数库，借助于操作系统的Linux内核来运行。
> Docker在执行的时候直接调用打包好的操作系统函数，由操作系统函数直接调用Linux内核进行执行。
> ![在这里插入图片描述](https://img-blog.csdnimg.cn/c272473fd56e42c2a535a74564af9ac2.png)
### 1.4 总结
Docker如何解决大型项目依赖关系复杂，不同组件依赖的兼容性问题？

- Docker允许开发中将应用、依赖、函数库、配置一起**打包**，形成可移植镜像
- Docker应用运行在容器中，使用沙箱机制，相互**隔离**

Docker如何解决开发、测试、生产环境有差异的问题？
- Docker镜像中包含完整运行环境，包括系统函数库，仅依赖系统的Linux内核，因此可以在任意Linux操作系统上运行。（**Docker在运行的时候只依赖于Linux内核**）
### 1.5 Docker和虚拟机的区别
Docker可以让一个应用在任何操作系统中非常方便的运行。而以前我们接触的虚拟机，也能在一个操作系统中，运行另外一个操作系统，保护系统中的任何应用。

**虚拟机**（virtual machine）是在操作系统中**模拟**硬件设备，然后运行另一个操作系统，比如在 Windows 系统里面运行 Ubuntu 系统，这样就可以运行任意的Ubuntu应用了。
**Docker**仅仅是封装函数库，并没有模拟完整的操作系统，如图：
![在这里插入图片描述](https://img-blog.csdnimg.cn/ddffc0dec12b493a909d70b19567039d.png)
虚拟机通过HyperVisor模拟一整套的计算机硬件，而Docker仅仅封装应用所必须的依赖和函数库。
![在这里插入图片描述](https://img-blog.csdnimg.cn/bad8c68121014ffda7807e6909a38576.png)
# 2. Docker架构
### 2.1 镜像和容器
**镜像(Image)**：Docker将应用程序和所有的依赖，函数库，环境，操作系统函数库打包在一起的文件，就成为镜像。
**容器(Container)**：镜像中的应用程序运行起来后形成的就是容器。
![在这里插入图片描述](https://img-blog.csdnimg.cn/2048dea491d749aea94f0aa3e5efe231.png)
### 2.2 DockerHub
类似于GitHub，是一个Docker镜像的托管平台。：DockerHub是一个官方的Docker镜像的托管平台。这样的平台称为Docker Registry。
我们可以直接在DockerHub拉取别人制作好的镜像来直接使用。
![在这里插入图片描述](https://img-blog.csdnimg.cn/6ca093556b62480ea6156e1c2ecb1c30.png)
### 2.3 Docker架构
我们要使用Docker来操作镜像、容器，就必须要安装Docker。
Docker是一个C/S架构的程序，包含两部分
* 服务器端：Docker服务器端是一个守护进程，负责处理Docker指令，管理容器和镜像
* 客户端：通过命令或RestAPI向Docker服务端发送指令。可以在本地或远程向服务端发送指令。
![在这里插入图片描述](https://img-blog.csdnimg.cn/9ebe2853cd1e44b3a9ce7049ef5d7ee2.png)
### 2.4 小结
**镜像**：将应用程序及其依赖、环境、配置打包在一起

**容器**：镜像运行起来就是容器，一个镜像可以运行多个容器

**Docker结构**：
- 服务端：接收命令或远程请求，操作镜像或容器
- 客户端：发送命令或者请求到Docker服务端

**DockerHub**：一个镜像托管的服务器，类似的还有阿里云镜像服务，统称为DockerRegistry

# 3. Docker基本操作
### 3.1 镜像操作
#### 3.1.1 镜像名称
* 镜像名称由两部分组成： [repository]:[tag]。
* 在没有指定tag时，默认是latest，代表最新版本的镜像
![在这里插入图片描述](https://img-blog.csdnimg.cn/9eb75e31bd5b4f1980c68d6a9dc24e49.png)
#### 3.1.2 镜像命令
**拉取和查看镜像**
首先去镜像仓库搜索nginx镜像，比如[DockerHub](https://hub.docker.com/):
![在这里插入图片描述](https://img-blog.csdnimg.cn/2476556da4d642e6b28d06194d1e2427.png)根据查看到的镜像名称，拉取自己需要的镜像，通过命令：`docker pull nginx`
![在这里插入图片描述](https://img-blog.csdnimg.cn/761e92c72b07402fa7583bbcd60be09c.png)
通过命令：`docker images` 查看拉取到的镜像
![在这里插入图片描述](https://img-blog.csdnimg.cn/0051cc143bcf4c6faa336f7ec1cb742d.png)
利用docker save将nginx镜像导出磁盘，然后再通过load加载回来

1）利用docker xx --help命令查看docker save和docker load的语法

命令格式：

```shell
docker save -o [保存的目标文件名称] [镜像名称]
```

2）使用docker save导出镜像到磁盘 

运行命令：

```sh
docker save -o nginx.tar nginx:latest
```

结果如图：
![在这里插入图片描述](https://img-blog.csdnimg.cn/a04feb806a3146818cd303b267f17909.png)
使用docker load加载镜像

先删除本地的nginx镜像：

```sh
docker rmi nginx:latest
```


然后运行命令，加载本地文件：

```sh
docker load -i nginx.tar
```

结果：
![在这里插入图片描述](https://img-blog.csdnimg.cn/4d2fe87bb6e54c129d8512fb893351c7.png)
练习
1）利用docker pull命令拉取镜像

```sh
docker pull redis
```

2）利用docker save命令将 redis:latest打包为一个redis.tar包

```sh
docker save -o redis.tar redis:latest
```

3）利用docker rmi 删除本地的redis:latest

```sh
docker rmi redis:latest
```
4）利用docker load 重新加载 redis.tar文件
```sh
docker load -i redis.tar
```
5）查看现有镜像
```sh
docker images
```
### 3.2 容器操作
![在这里插入图片描述](https://img-blog.csdnimg.cn/a1ea649b201f4a039acf1f815c731e0a.png)
Docker容器的三个状态：
* 运行：进程正常运行
* 暂停：进程暂停，CPU不再运行，并不释放内存
* 停止：进程终止，回收进程占用的内存、CPU等资源

Docker容器状态的相关命令
- `docker run`：创建并运行一个容器，处于运行状态
- `docker pause`：让一个运行的容器暂停
- `docker unpause`：让一个容器从暂停状态恢复运行
- `docker stop`：停止一个运行的容器
- `docker start`：让一个停止的容器再次运行
- `docker rm`：删除一个容器

#### 3.2.1 创建并运行一个容器
创建并运行nginx容器的命令：

```sh
docker run --name containerName -p 80:80 -d nginx
```

命令解读：

- `docker run` ：创建并运行一个容器
- `--name` : 给容器起一个名字，比如叫做mn
- `-p` ：将宿主机端口与容器端口映射，冒号**左侧是宿主机端口，右侧是容器端口**
- `-d`：后台运行容器
- `nginx`：镜像名称，例如nginx


> 这里的`-p`参数，是将容器端口映射到宿主机端口。
>
> 默认情况下，容器是隔离环境，我们直接访问宿主机的80端口，肯定访问不到容器中的nginx。
>
> 现在，将容器的80与宿主机的80关联起来，当我们访问宿主机的80端口时，就会被映射到容器的80，这样就能访问到nginx了：

![在这里插入图片描述](https://img-blog.csdnimg.cn/c1dde3ce74ea4f958e3a3cbc1b3749e1.png)
#### 3.2.2 进入容器内部 修改文件
**需求**：进入Nginx容器，修改HTML文件内容，将标题修改为Hello World
**提示**：进入容器要用到docker exec命令。

****STEP1**：进入容器。进入我们刚刚创建的nginx容器的命令为：**

```sh
docker exec -it mn bash
```

命令解读：

- `docker exec` ：进入容器内部，执行一个命令

- `-it` : 给当前进入的容器创建一个标准输入、输出终端，允许我们与容器交互

- `mn` ：要进入的容器的名称

- `bash`：进入容器后执行的命令，bash是一个linux终端交互命令。

这个bash也可以替换成别的命令，比如如果我们想进入容器以后然后再进入redis客户端，可以这样写
```sh
docker exec -it mn redis-cli
```
**STEP 2：进入HTML文件所在的目录**
进入nginx的HTML所在目录 /usr/share/nginx/html
容器内部会模拟一个独立的Linux文件系统，看起来如同一个linux服务器一样：
![在这里插入图片描述](https://img-blog.csdnimg.cn/46d10269535f4ce594861d076dcad557.png)

nginx的环境、配置、运行文件全部都在这个文件系统中，包括我们要修改的html文件。

查看DockerHub网站中的nginx页面，可以知道nginx的html目录位置在`/usr/share/nginx/html`

我们执行命令，进入该目录：

```sh
cd /usr/share/nginx/html
```

 查看目录下文件：
 ![在这里插入图片描述](https://img-blog.csdnimg.cn/cdafd427e85f422f9daae89b4bc0152d.png)
**STEP 3：修改html的内容**
我们之前说过容器中只保留应用运行锁必须的函数，因此容器内没有vi命令，无法直接修改，我们用下面的命令来修改：

```sh
sed -i -e 's#Welcome to nginx#Hello World#g' -e 's#<head>#<head><meta charset="utf-8">#g' index.html
```

在浏览器访问自己的虚拟机地址，例如我的是：http://95.179.138.182，即可看到结果：
![在这里插入图片描述](https://img-blog.csdnimg.cn/6f8b89e054e04740acec262a166926ba.png)
#### 3.2.3 查看当前的容器状态
我们可以通过`docker ps`命令查看所有正在运行的容器，可以通过`docker ps -a`查看所有容器，包括已经停止运行的容器
![在这里插入图片描述](https://img-blog.csdnimg.cn/6cca2c5a0e9f41948ed9d17e50222896.png)
#### 3.2.4 查看容器内部日志
可以通过`docker logs 容器名` 查看容器内部日志
![在这里插入图片描述](https://img-blog.csdnimg.cn/5c32fbde6cf1437b8a437dc13feb3760.png)

#### 3.2.5 小结
docker run命令的常见参数有哪些？

- `--name`：指定容器名称
- `-p`：指定端口映射
- `-d`：让容器后台运行

查看容器日志的命令：

- `docker logs`
- 添加 `-f` 参数可以持续查看日志

查看容器状态：

- `docker ps` 查看正在运行的程序
- `docker ps -a` 查看所有容器，包括已经停止的

### 3.3 数据卷
在之前的案例中，我们看到如果想修改容器内部的文件是需要进入到容器内部，且没有相关的编辑器的支持，非常麻烦。
如果很多容器都需要管理，那么非常工作量非常大。
此外，如果我们删除一个容器，那么里面的文件也随之消失。
![在这里插入图片描述](https://img-blog.csdnimg.cn/aca34da255bf46549d9987fb94acce02.png)
要解决这个问题，必须将数据与容器解耦，这就要用到数据卷了。
#### 3.3.1 什么是数据卷
**数据卷（volume）**是一个虚拟目录，指向宿主机文件系统中的某个目录。
![在这里插入图片描述](https://img-blog.csdnimg.cn/52ee831fc47e43ff9dce205eb3986264.png)
一旦完成数据卷挂载，对容器的一切操作都会作用在数据卷对应的宿主机目录了。
这样，我们操作宿主机的`/var/lib/docker/volumes/html`目录，就等于操作容器内的`/usr/share/nginx/html`目录了。
> 数据卷相当于在两个目录建立了同步机制。在宿主机上的操作会同步到容器内的文件中
#### 3.3.2 数据卷操作命令
数据卷操作的基本语法如下：

```sh
docker volume [COMMAND]
```

`docker volume`命令是数据卷操作，根据命令后跟随的command来确定下一步的操作：

- `create` 创建一个volume
- `inspect` 显示一个或多个volume的信息
- `ls` 列出所有的volume
- `prune` 删除未使用的volume
- `rm` 删除一个或多个指定的volume

#### 3.3.3 创建和查看数据卷
**需求**：创建一个数据卷，并查看数据卷在宿主机的目录位置

1.创建数据卷

```sh
docker volume create html
```


2.查看所有数据

```sh
docker volume ls
```

结果：
![在这里插入图片描述](https://img-blog.csdnimg.cn/948b17860b634eb293865e2e968058de.png)
3.查看数据卷详细信息卷

```sh
docker volume inspect html
```

结果：
![在这里插入图片描述](https://img-blog.csdnimg.cn/04a48d0eac744f37bbaac951020dcb31.png)

可以看到，我们创建的html这个数据卷关联的宿主机目录为`/var/lib/docker/volumes/html/_data`目录
#### 3.3.4 挂载数据卷
我们在创建容器时，可以通过 -v 参数来挂载一个数据卷到某个容器内目录，命令格式如下：

```sh
docker run \
  --name mn \
  -v html:/root/html \
  -p 8080:80
  nginx \
```

这里的-v就是挂载数据卷的命令：
`-v html:/root/htm` ：把html数据卷挂载到容器内的/root/html这个目录中
通过上述的挂载命令，实际上就是把宿主机的`/var/lib/docker/volumes/html/_data`目录和容器中的`/root/html`进行挂载，这样在`/var/lib/docker/volumes/html/_data`目录中的所有操作都会自动的同步到器中的`/root/html`目录。
#### 3.3.5 例子-给nginx挂载数据卷
**需求**：创建一个nginx容器，修改容器内的html目录内的index.html内容。
**分析**：上个案例中，我们进入nginx容器内部，已经知道nginx的html目录所在位置/usr/share/nginx/html ，我们需要把这个目录挂载到html这个数据卷上，方便操作其中的内容。
步骤：

**STEP 1：创建容器并挂载数据卷到容器内的HTML目录**

```sh
docker run --name mn -v html:/usr/share/nginx/html -p 80:80 -d nginx
```

**STEP 2：进入html数据卷所在位置，并修改HTML内容**

```sh
# 查看html数据卷的位置
docker volume inspect html
# 进入该目录
cd /var/lib/docker/volumes/html/_data
# 修改文件
vi index.html
```

#### 3.3.6 挂载本地目录
之前我们使用的volume创建数据卷的方式数据卷的目录由docker来管理目录，但是目录较深，不好找。我们可以直接挂载我们在宿主机上自己创建的文件和文件夹，方便我们查找。

容器不仅仅可以挂载数据卷，也可以直接挂载到宿主机目录上。关联关系如下：

- 带数据卷模式：宿主机目录 --> 数据卷 ---> 容器内目录
- 直接挂载模式：宿主机目录 ---> 容器内目录
![在这里插入图片描述](https://img-blog.csdnimg.cn/8cc9a380debe40ab9bc767e0a9344d19.png)
上图中，红色线就是使用volume数据卷挂载方式；绿色的线就是直接挂载宿主机目录的方式。
**语法**：

目录挂载与数据卷挂载的语法是类似的：

- `-v [宿主机目录]:[容器内目录]`
- `-v [宿主机文件]:[容器内文件]`

**案例：创建并运行一个MySQL容器，将宿主机目录直接挂载到容器**
1）下载MySQL镜像

```sh
docker pull mysql
```

2）创建目录/tmp/mysql/data

```sh
mkdir -p /tmp/mysql/data
```

3）创建目录/tmp/mysql/conf，上传hmy.cnf文件到/tmp/mysql/conf

```sh
mkdir -p/tmp/mysql/conf
```

4）去DockerHub查阅资料，创建并运行MySQL容器，要求：
① 挂载/tmp/mysql/data到mysql容器内数据存储目录
② 挂载/tmp/mysql/conf/hmy.cnf到mysql容器的配置文件
③ 设置MySQL密码

```sh
docker run --name mysql-test \
-v /tmp/mysql/data:/var/lib/mysql \
-v /tmp/mysql/conf/hmy.cnf:/ect/mysql/config.d/hmy.cnf
-p 3306:3306
-e MYSQL_ROOT_PASSWORD=123
-d \
mysql:latest
```
#### 3.3.7 小结
数据卷的作用：

- 将容器与数据分离，解耦合，方便操作容器内数据，保证数据安全

数据卷操作：

- `docker volume create`：创建数据卷
- `docker volume ls`：查看所有数据卷
- `docker volume inspect`：查看数据卷详细信息，包括关联的宿主机目录位置
- `docker volume rm`：删除指定数据卷
- `docker volume prune`：删除所有未使用的数据卷

docker run的命令中通过 -v 参数挂载文件或目录到容器中：

- `-v volume名称:容器内目录`
- `-v 宿主机文件:容器内文`
- `-v 宿主机目录:容器内目录`

数据卷挂载与目录直接挂载的

- 数据卷挂载耦合度低，由docker来管理目录，但是目录较深，不好找
- 目录挂载耦合度高，需要我们自己管理目录，不过目录容易寻找查看