# mybatis-datalimit: 基于myabtis的数据权限控制模块

## mybatis-datalimit是什么？

数据权限控制是几乎每一个业务系统都要实现的一个功能，很多业务系统采用硬编码的方式将数据权限控制逻辑写入业务代码中，这种实现方式让我们的业务代码中充斥着大量的与业务无关的重复代码，并且数据权限有变化需求就必须 修改代码->测试->发布，增加了需求响应时间。

mybatis-datalimit是一个基于mybatis的数据权限模块，可以做到：

* 对业务代码无侵入：通过mybatis拦截器插入权限控制逻辑，对业务代码0侵入；
* 实时调整数据权限：将数据权限相关逻辑配置到数据库中，通过更改配置实时调整数据权限，并且可以随时去掉某个SQL的数据权限控制，实现数据权限的热插拔。

## Quick Start

下面的例子将展示如何在项目中应用mybatis-datalimit。

#### STEP 1. 在项目中引入mybatis-datalimit Jar包

将项目clone到本地之后执行mvn install将jar包构建到本地maven仓库，然后在pom.xml中添加依赖：

```xml
<dependency>
    <groupId>com.caixunshi</groupId>
    <artifactId>mybatis-datalimit</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### STEP 2. 添加表结构

mybatis-datalimit将数据权限控制逻辑配置在了数据库中，需要添加以下两张表：

```sql
-- 创建数据权限映射关系表
CREATE TABLE `mybatis_datalimit_mapper` (
  id bigint(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  mapper_id varchar(64) NOT NULL COMMENT 'mapper全局id',
  meta_id varchar(32) NOT NULL COMMENT '元数据id',
  delete_flag tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '状态(0:有效，1:无效)',
  PRIMARY KEY (id)
  UNIQUE KEY UNQ_MAPPER_ID (MAPPER_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

```sql
-- 创建数据权限元数据表
CREATE TABLE mybatis_datalimit_meta (
  id BIGINT(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  meta_id VARCHAR(32) NOT NULL COMMENT '自定义元数据Id',
  meta_data VARCHAR(512) NOT NULL COMMENT '权限元数据(可以是具体的sql或者beanName)',
  meta_type SMALLINT(1) UNSIGNED NOT NULL COMMENT '元数据类型(1:sql, 2:类方法)',
  meta_param VARCHAR(512) DEFAULT NULL COMMENT '扩展参数',
  PRIMARY KEY (id)
  UNIQUE KEY UNQ_META_ID (meta_id)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;
```

#### STEP 3. 添加拦截器

在业务代码中不出现任何跟数据权限有关的代码，如何将数据权限需要的业务参数传递到拦截器？

mybatis-datalimit通过InheritableThreadLocal来将参数在线程中进行传递，只要实现默认的拦截器抽象类AbstractDataLimitFilte，并重写getParams方法，然后将其注册到web框架的拦截器链中即可。

3.1 例如下面将session中的当前登陆人添加到参数中：

```java
public class DefaultAuthControlFilter extends AbstractAuthControlFilter {
    @Override
    protected Map<String, String> getParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>(16);
        String userCode = request.getSession().getAttribute(GlobalStatic.SESSION_USER_NAME).toString();
        if (empCode != null) {
            params.put("userCode", userCode);
        }
        return params;
    }
}
```

3.2 然后用注解的方式将注册拦截器，这里最好将order设置成大一点：

```java
/**
     * 数据权限插件
     * @return
     */
    @Bean
    public FilterRegistrationBean mybastisFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean(new DefaultAuthControlFilter());
        registration.setOrder(99);
        return registration;
    }
```

#### STEP 4. 配置数据权限

mybatis_datalimit_meta为数据权限的元数据表

4.1 如下面配置一个当前创建人的数据权限元数据：

```sql
insert into mybatis_datalimit_meta (meta_id, meta_data, meta_type, meta_param) 
values('my_create_data', 'and user_code = ${userCode}', '1', NULL);
```

mybatis_datalimit_mapper为数据权限与Mapper方法的对应关系表

4.2 接下来配置一个映射关系：

```sql
insert into mybatis_datalimit_mapper(mapper_id, meta_id, delete_flag) values('com.caixunshi.mybatisdatalimit.MyStudentInfoMapper.listInfo','my_create_data','0');
```

这样我们就完成了对mapper全局id为com.caixunshi.mybatisdatalimit.MyStudentInfoMapper.listInfo的这个方法加上了数据权限my_create_data，表示查看我创建的数据。

还有一些复杂的数据权限，包含很多计算逻辑，无法在sql中完成，这种就可以配置类方法形式的元数据

4.3 接下来配置一个类方法的数据权限元数据：

```sql
insert into mybatis_datalimit_meta (meta_id, meta_data, meta_type, meta_param) 
values('my_create_data2', 'mycreateGenerateSql', '2', "{column: 'user_code'}");
```

其中mycreateGenerateSql表示注册spring容器中的beanName，该bean必须实现了IDataLimitSql。

IDataLimitSql的定义如下：

```java
public interface IDataLimitSql {
    /**
     * 动态生成sql
     * @param config 权限配置数据
     * @param params 拦截器设置的参数
     * @return
     */
    String generateSql(DataLimitMeta config, Map<String, String> params);
}
```

mycreateGenerateSql的代码如下：

```java
@Service
public class MycreateGenerateSql implements IDataLimitSql{
    public String generateSql(DataLimitMeta config, Map<String, String> params) {
        // 元数据库的扩展参数
        String paramsStr = config.getMetaParam();
        JSONObject jsonObject = JSON.parseObject("{column: 'user_code'}");
        String columns = jsonObject.getString("column");
        String userCode = params.get("userCode");
        // ... 执行复杂的业务逻辑
        return " and " + columns + "=" + userCode;
    }
}
```

这样就达到了跟4.1一样的效果。

## [源码解析](https://github.com/caixunshi/mybatis-datalimit/blob/master/INTRODICTION.md)
 
