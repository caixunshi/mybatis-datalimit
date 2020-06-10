package com.caixunshi.datalimit.interceptor;

import com.caixunshi.datalimit.config.DataLimitConfig;
import com.caixunshi.datalimit.util.PluginUtils;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.util.Properties;

/**
 * @Author: caixunshi
 * @Date: 2020/6/10 21:10
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class PrepareInterceptor implements Interceptor {

    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        // 从配置信息中获取配置，没有配置则不进行处理
        String authSql;
        try {
            authSql = DataLimitConfig.getAuthSql(mappedStatement.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute generateSql " + e.getMessage());
        }
        if (authSql == null || "".equals(authSql)) {
            return invocation.proceed();
        }

        // 给目标sql加上数据权限
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        String sql = boundSql.getSql();
        // 对sql中的limit和order by做处理
        String suffixSql = "";
        int lastOrderByIndex = sql.lastIndexOf("order by");
        if (lastOrderByIndex > 0) {
            suffixSql = sql.substring(lastOrderByIndex);
            sql = sql.substring(0, lastOrderByIndex);
            // 如果suffixSql中包含select，就无法确定加上数据权限sql之后是正确的，这里退出sql处理
            // todo 判断suffixSql是否合法需要优化
            if (suffixSql.indexOf("select") > 0) {
                throw new RuntimeException("add controller sql error");
            }
        }

        // 对sql中的limit做处理
        int lastLimitIndex = sql.lastIndexOf("limit");
        if (lastLimitIndex > 0 && "".equals(suffixSql)) {
            suffixSql = sql.substring(lastLimitIndex);
            sql = sql.substring(0, lastLimitIndex);
            // 如果suffixSql中包含? ，空格 之外的其他字符，就无法确定加上数据权限sql之后是正确的，这里退出sql处理
            char[] arr = suffixSql.toCharArray();
            for (int i = 5; i < arr.length; i++) {
                if (arr[i] != 44 && arr[i] != 32 && arr[i] != 63) {
                    throw new RuntimeException("add control sql error");
                }
            }
        }
        String convertSql = sql + authSql + suffixSql;
        metaObject.setValue("delegate.boundSql.sql", convertSql);
        return invocation.proceed();
    }

    public Object plugin(Object o) {
        if (o instanceof StatementHandler) {
            return Plugin.wrap(o, this);
        }
        return o;
    }

    public void setProperties(Properties properties) {

    }
}
