package com.caixunshi.datalimit.sql;

import com.caixunshi.datalimit.config.DataLimitMeta;

import java.util.Map;

/**
 * 数据权限生成sql的接口，若要配置动态生成sql，则需要实现该接口
 * @Author: caixunshi
 * @Date: 2020/6/10 19:48
 */
public interface IDataLimitSql {
    /**
     * 动态生成sql
     * @param config 权限配置数据
     * @param params 拦截器设置的参数
     * @return
     */
    String generateSql(DataLimitMeta config, Map<String, String> params);
}
