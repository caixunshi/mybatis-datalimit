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