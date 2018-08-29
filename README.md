# consul-pusher

## 运行环境

* JDK 1.8
* Maven 3.3.9
* Spring Boot 2.0.0.RELEASE
* Spring cloud Finchley.M8

## 目录结构

整个工程依照`Spring Boot`工程的目录结构创建，在`com.consul.pusher`包下有四个类，其中`ConsulApplication`的作用就不用说了，它是整个服务的启动类。剩下的三个类说明如下：

* `ConsulConfig`：提供加载配置到`Consul`中的操作。它定义了一个私有的`init`方法，这个方法被`@PostConstruct`注解所标记，主要作用是在`ConsulConfig`依赖注入完成之后读取指定配置文件，将文件里的配置信息推送到`Consul`配置中心中。该方法在整个应用生命周期中只执行一次，定义为私有的主要是不允许外部调用，保证安全性

* `ConsulService`和`ConsulServiceImpl`：`ConsulServiceImpl`实现了`ConsulService`接口，主要提供`Consul`配置中心业务逻辑操作，它主要封装了`Consul java client`的一些方法。

`resources`目录下的`bootstrap.yml`和`application.yml`分别为微服务全局配置文件和业务信息配置文件。在服务启动期间，`application.yml`配置文件的内容会被`ConsulConfig`类的`init`方法读取并推送到`Consul`配置中心。在这里我们没有将`bootstrap.yml`的文件内容推送到`Consul`配置中心，主要是考虑到：

* `bootstrap.yml`里定义的配置信息不经常修改

* `Spring Boot`天生不支持动态修改数据库、`ES`连接信息。`bootstrap.yml`里的定义的数据库连接、`ES`连接等信息通过`Consul`配置中心修改后不能生效，还需要重启应用才能生效。并且，需要修改数据库连接、`ES`连接信息的场景的大部分是因为当前服务不可用产生的，服务的高可用不应由`Consul`来维护

## 构建

```
mvn clean package
```
