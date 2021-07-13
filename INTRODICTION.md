# 源码解析

## [DataLimitlDefinitionHolder.java](https://github.com/caixunshi/mybatis-datalimit/blob/master/src/main/java/com/caixunshi/datalimit/threadlocal/DataLimitlDefinitionHolder.java)
思考一个问题，在业务代码中不出现任何跟数据权限有关的代码，如何将数据权限需要的业务参数，传递到拦截器？例如查询我创建的数据，肯定需要知道当前用户是谁.

ThreadLocal是JDK包提供的，它提供线程本地变量，如果创建一个ThreadLocal变量，那么访问这个变量的每个线程都会有这个变量的一个副本。而InheritableThreadLocal则是在ThreadLocal做了增强，能够将变量传递到子线程；

mybatis-datalimit通过持有一个InheritableThreadLocal对象来将参数在线程中进行传递。在springmvc中，我们一般会通过拦截器去做鉴权，所以我也提供了一个基于模版方法的拦截器[AbstractDataLimitFilter.java](https://github.com/caixunshi/mybatis-datalimit/blob/master/src/main/java/com/caixunshi/datalimit/filter/AbstractDataLimitFilter.java)，我们只要实现这个抽象类，并重写getParams方法，就可以将数据放入到DataLimitlDefinitionHolder中，例如：我们可以从session中获取当前登陆用户，传递到mybatis拦截器。

## mybatis拦截器

mybatis提供了拦截器机制提供给用户进行扩展，自定义拦截器的实现步骤：

1. 实现org.apache.ibatis.plugin.Interceptor接口；
2. 添加拦截器注解org.apache.ibatis.plugin.Intercepts和@Signature
3. 将拦截器注册到mybatis中；

对应实现类：[PrepareInterceptor](https://github.com/caixunshi/mybatis-datalimit/blob/master/src/main/java/com/caixunshi/datalimit/interceptor/PrepareInterceptor.java)

主要步骤：

1. 拿到当前执行sql的全局mapperid；
2. 尝试从配置类中获取权限配置，没有配置，直接放过，否则，执行下面的逻辑；
3. 根据权限配置给目标sql拼接上权限sql;

## 权限配置

权限配置相关的类：[config](https://github.com/caixunshi/mybatis-datalimit/tree/master/src/main/java/com/caixunshi/datalimit/config)

1. DataLimitMeta是数据权限的元数据，比如说：我创建的，我下属创建的，我部门创建的等，对应mybatis_datalimit_meta表；
2. DataLimitMapper表示元数据和需要加上权限的sql的mapperid的对应关系，对应mybatis_datalimit_mapper表；


这里将元数据和对应关系分成两个配置表是因为元数据和映射关系是1:N的关系；

3. DataLimitConfig: 
* 监听spring的ContextRefreshedEvent事件，初始化配置类，注册拦截器到mybatis，并获取容器对象application（当配置的数据权限元数据为类方法类型时，就是通过这个地方获取的application从容器中获取对应的类对象）
* 定义获取权限sql的方法getAuthSql，这里根据mapperid获取权限配置，然后从DataLimitlDefinitionHolder中获取参数完成替换；
* refreshMapper和refreshMapper，为了速度，我们将权限配置和映射关系放到了本地缓存中，这两个方法就是用来刷新本地缓存的数据；
