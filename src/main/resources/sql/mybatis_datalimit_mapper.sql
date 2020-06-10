-- 创建数据权限映射关系表
CREATE TABLE `mybatis_datalimit_mapper` (
  id bigint(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  mapper_id varchar(64) NOT NULL COMMENT 'mapper全局id',
  meta_id varchar(32) NOT NULL COMMENT '元数据id',
  delete_flag tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '状态(0:有效，1:无效)',
  PRIMARY KEY (id)
  UNIQUE KEY UNQ_MAPPER_ID (MAPPER_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;