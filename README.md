# consul-pusher

## 运行环境

* JDK 1.8
* Maven 3.3.9
* Spring Boot 2.0.0.RELEASE
* Spring cloud Finchley.M8
* Consul 1.2.2

## 目录结构

整个工程依照spring boot工程的目录结构创建，在com.consul.pusher包下有四个类，其中ConsulApplication的作用就不用说了，它是整个服务的启动类。剩下的三个类说明如下：

* ConsulConfig：提供加载配置到consul中的操作。它定义了一个私有的init方法，这个方法被@PostConstruct注解所标记，主要作用是在ConsulConfig依赖注入完成之后读取指定配置文件，将文件里的配置信息推送到consul配置中心中。该方法在整个应用生命周期中只执行一次，定义为私有的主要是不允许外部调用，保证安全性
* ConsulService和ConsulServiceImpl：ConsulServiceImpl实现了ConsulService接口，主要提供consul配置中心业务逻辑操作，它主要封装了consul java client的一些方法。

resources目录下的bootstrap.yml和application.yml分别为微服务全局配置文件和业务信息配置文件。在服务启动期间，application.yml配置文件的内容会被ConsulConfig类的init方法读取并推送到consul配置中心。在这里我们没有将bootstrap.yml的文件内容推送到consul配置中心，主要是考虑到：

* bootstrap.yml里定义的配置信息不经常修改
* bootstrap.yml里的定义的数据库等信息通过consul配置中心修改后不能生效，还需要重启应用才能生效。并且，需要修改数据库信息的场景的大部分是因为当前数据库不可用产生的，数据库的高可用不应由consul来维护

## 构建

```
mvn clean package
```