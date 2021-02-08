### 项目地址：https://github.com/sunshinemine/springcloud-eureka-feign-mybatis-seata.git
### 本项目参考：https://github.com/seata/seata-samples.git
### 概览
- 1.创建数据库表(项目脚本.sql)
- 2.启动注册中心eureka-server
- 3.[下载seata-server](https://github.com/seata/seata/releases),修改/seata/conf下配置(数据源和注册中心),启动服务
- 4.微服务引入file.conf、registry.conf配置文件
- 5.数据源代理设置(DataSourceConfiguration.java)


### 1.此demo技术选型及版本信息

注册中心：eureka

服务间调用：openfeign

持久层：mybatis 2.0.0

数据库：mysql 8.0.22

Springboot：2.1.2.RELEASE

Springcloud：Greenwich.SR2

jdk：1.8 

seata：1.2

### 2.demo概况
- eureka:作为注册中心，注册微服务和seata
- order:订单服务，全局事务管理@GlobalTransactional,调用storage和account服务;
- storage:库存服务，用户扣减库存；
- account:账户服务，用于扣减账户余额；
- common:公共组件


### 3.seata server端配置信息修改
seata-server中，/conf目录下，有两个配置文件，需要结合自己的情况来修改：

##### file.conf 

里面有事务组配置，锁配置，事务日志存储等相关配置信息，由于此demo使用db存储事务信息，我们这里要修改store中的配置：
```java
## transaction log store
store {
  ## store mode: file、db
  mode = "db"   修改这里，表明事务信息用db存储

  ## file store 当mode=db时，此部分配置就不生效了，这是mode=file的配置
  file {
    dir = "sessionStore"

    # branch session size , if exceeded first try compress lockkey, still exceeded throws exceptions
    max-branch-session-size = 16384
    # globe session size , if exceeded throws exceptions
    max-global-session-size = 512
    # file buffer size , if exceeded allocate new buffer
    file-write-buffer-cache-size = 16384
    # when recover batch read size
    session.reload.read_size = 100
    # async, sync
    flush-disk-mode = async
  }

  ## database store  mode=db时，事务日志存储会存储在这个配置的数据库里
  db {
    ## the implement of javax.sql.DataSource, such as DruidDataSource(druid)/BasicDataSource(dbcp) etc.
    datasource = "dbcp"
    ## mysql/oracle/h2/oceanbase etc.
    db-type = "mysql"
    driver-class-name = "com.mysql.cj.jdbc.Driver"
    url = "jdbc:mysql://127.0.0.1:3306/seata?serverTimezone=Asia/Shanghai"  修改这里
    user = "root"  修改这里
    password = "root"  修改这里
    min-conn = 1
    max-conn = 3
    global.table = "global_table"
    branch.table = "branch_table"
    lock-table = "lock_table"
    query-limit = 100
  }
}
```

##### registry.conf

registry{}中是注册中心相关配置，config{}中是配置中心相关配置。

我们这里用eureka作注册中心，所以，只用修改registry{}中的：
```java
registry {
  # file 、nacos 、eureka、redis、zk、consul、etcd3、sofa
  type = "eureka"  修改这里，指明注册中心使用什么

  nacos {
    serverAddr = "localhost"
    namespace = ""
    cluster = "default"
  }
  eureka {
    serviceUrl = "http://localhost:8761/eureka"  修改这里
    application = "default"  
    weight = "1"
  }
  redis {
    serverAddr = "localhost:6379"
    db = "0"
  }
  zk {
    cluster = "default"
    serverAddr = "127.0.0.1:2181"
    session.timeout = 6000
    connect.timeout = 2000
  }
  consul {
    cluster = "default"
    serverAddr = "127.0.0.1:8500"
  }
  etcd3 {
    cluster = "default"
    serverAddr = "http://localhost:2379"
  }
  sofa {
    serverAddr = "127.0.0.1:9603"
    application = "default"
    region = "DEFAULT_ZONE"
    datacenter = "DefaultDataCenter"
    cluster = "default"
    group = "SEATA_GROUP"
    addressWaitTime = "3000"
  }
  file {
    name = "file.conf"
  }
}
```

### 4.client端相关配置
#### 4.1 普通配置
client端的几个服务，都是普通的springboot整合了springCloud组件的正常服务，所以，你需要配置eureka，数据库，mapper扫描等，即使不使用seata，你也需要做，这里不做特殊说明，看代码就好。

#### 4.2 特殊配置
##### application.yml
以order服务为例，除了常规配置外，这里还要配置下事务组信息：
```java
spring:
    application:
        name: order-server
    cloud:
        alibaba:
            seata:
                tx-service-group: my_test_tx_group  
```
##### file.conf
自己新建的项目是没有这个配置文件的，copy过来，修改下面配置：
```java
service {
  #vgroup->rgroup
  vgroup_mapping.my_test_tx_group = "default"   
  #only support single node
  default.grouplist = "127.0.0.1:8091"
  #degrade current not support
  enableDegrade = false
  #disable
  disable = false
  disableGlobalTransaction = false
}
```
##### registry.conf

使用eureka做注册中心，仅需要修改eureka的配置即可：
```java
registry {
  # file 、nacos 、eureka、redis、zk
  type = "eureka"   修改这里

  nacos {
    serverAddr = "localhost"
    namespace = "public"
    cluster = "default"
  }
  eureka {
    serviceUrl = "http://localhost:8761/eureka"  修改这里
    application = "default"
    weight = "1"
  }
  redis {
    serverAddr = "localhost:6381"
    db = "0"
  }
  zk {
    cluster = "default"
    serverAddr = "127.0.0.1:2181"
    session.timeout = 6000
    connect.timeout = 2000
  }
  file {
    name = "file.conf"
  }
}
```

#### 4.3.数据源代理
这个是要特别注意的地方，seata对数据源做了代理和接管，在每个参与分布式事务的服务中，都要做如下配置：
```java
/**
 * 数据源代理
 * @author wangzhongxiang
 */
@Configuration
public class DataSourceConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource druidDataSource(){
        DruidDataSource druidDataSource = new DruidDataSource();
        return druidDataSource;
    }

    @Primary
    @Bean("dataSource")
    public DataSourceProxy dataSource(DataSource druidDataSource){
        return new DataSourceProxy(druidDataSource);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSourceProxy dataSourceProxy)throws Exception{
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSourceProxy);
        sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
        .getResources("classpath*:/mapper/*.xml"));
        sqlSessionFactoryBean.setTransactionFactory(new SpringManagedTransactionFactory());
        return sqlSessionFactoryBean.getObject();
    }

}
```

### 5.启动测试
- 1.启动eureka;
- 2.启动seata-server;
- 3.启动order,storage,account服务;
- 4.调用参数
/order/create
{
    "userId": 1,
    "productId": 1,
    "money": 20,
    "count": 2,
    "status": 0
}

### 6.模拟异常
在order、account、storage中模拟异常
### 7.调用成环
前面的调用链为order->storage->account;
这里测试的成环是指order->storage->account->order，
这里的account服务又会回头去修改order在前面添加的数据。
经过测试，是支持此种场景的。
```java
    /**
     * 扣减账户余额
     * @param userId 用户id
     * @param money 金额
     */
    @Override
    public void decrease(Long userId, BigDecimal money) {
        LOGGER.info("------->扣减账户开始account中");
        //模拟超时异常，全局事务回滚
//        try {
//            Thread.sleep(30*1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        accountDao.decrease(userId,money);
        LOGGER.info("------->扣减账户结束account中");

        //修改订单状态，此调用会导致调用成环
        LOGGER.info("修改订单状态开始");
        String mes = orderApi.update(userId, money.multiply(new BigDecimal("0.09")),0);
        LOGGER.info("修改订单状态结束：{}",mes);
    }
```
在最初的order会创建一个订单，然后扣减库存，然后扣减账户，账户扣减完，会回头修改订单的金额和状态，这样调用就成环了。


