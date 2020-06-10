package com.caixunshi.datalimit.config;

import com.caixunshi.datalimit.interceptor.PrepareInterceptor;
import com.caixunshi.datalimit.sql.IDataLimitSql;
import com.caixunshi.datalimit.threadlocal.DataLimitlDefinitionHolder;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: caixunshi
 * @Date: 2020/6/10 19:50
 */
@Component
public class DataLimitConfig implements ApplicationListener<ContextRefreshedEvent> {
    // 数据权限元数据
    private static Map<String, DataLimitMeta> meta = new HashMap<String, DataLimitMeta>();

    // 数据权限映射关系
    private static Map<String, DataLimitMapper> mapper = new HashMap<String, DataLimitMapper>();

    // Spring容器
    private static ApplicationContext applicationContext;

    // SQL模版
    private static JdbcTemplate jdbcTemplate;

    // 查询数据权限元数据SQL
    private static final String QUERY_META = "SELECT MAPPER_ID,META_DATA,META_TYPE,META_PARAM FROM MYBATIS_DATALIMIT_META";

    // 查询所有的权限配置
    private static final String QUERY_MAPPER = "SELECT MAPPER_ID, META_ID FROM MYBATIS_DATALIMIT_MAPPER WHERE DELETE_FLAG = 0";

    // 权限配置为类方法
    private static final int METHOD_META_TYPE = 2;

    // 变量替换的正则表达式（该正则只支持变量名称为字母）
    private static final Pattern REGEX = Pattern.compile("[$][{]\\w+[}]");

    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 初始化jdbcTemplate
        this.applicationContext = event.getApplicationContext();
        DataSource dataSource = applicationContext.getBean(DataSource.class);
        jdbcTemplate = new JdbcTemplate(dataSource);

        // 刷新数据权限配置
        refreshConfig();

        // 添加mybatis拦截器
        SqlSessionFactory sqlSessionFactory = applicationContext.getBean(SqlSessionFactory.class);
        sqlSessionFactory.getConfiguration().addInterceptor(new PrepareInterceptor());
    }

    /**
     * 通过权限配置获取权限sql
     * @param mapperId
     * @return
     */
    public static String getAuthSql(String mapperId) {
        // 若DataLimitlDefinitionHolder.getParams()为null，表示请求不是通过Web过来，没有经过Filter过滤器，这种情况下不生成权限SQL
        if (DataLimitlDefinitionHolder.getParams() == null) {
            return null;
        }
        // 若找不到配置，则直接返回
        DataLimitMapper dataLimitMapper = mapper.get(mapperId);
        if (dataLimitMapper == null) {
            return null;
        }
        DataLimitMeta dataLimitMeta = dataLimitMapper.getDataLimitMeta();

        // 若配置为类方法，则从容器中获取类，并执行方法获取sql
        if (dataLimitMeta.getMetaType() == METHOD_META_TYPE) {
            IDataLimitSql dataLimitSql = (IDataLimitSql) applicationContext.getBean(dataLimitMeta.getMetaData());
            return dataLimitSql.generateSql(dataLimitMeta, DataLimitlDefinitionHolder.getParams());
        }

        // meta为sql,(前面加上空格，防止配置时没有空格)
        String result = " " + dataLimitMeta.getMetaData();
        // 变量替换
        Matcher matcher = REGEX.matcher(result);
        while (matcher.find()) {
            String str = matcher.group();
            String key = str.substring(2, str.length() - 1);
            String value = DataLimitlDefinitionHolder.getParams().get(key);
            result = result.replaceAll("[$][{]" + key + "[}]", value);
        }
        return result;
    }

    /**
     * 刷新数据权限配置
     */
    public static void refreshConfig() {
        // 刷新元数据
        refreshMeta();

        // 刷新映射关系
        refreshMapper();
    }

    /**
     * 刷新映射关系
     */
    private static void refreshMapper() {
        List<DataLimitMapper> result = jdbcTemplate.query(QUERY_MAPPER, new RowMapper<DataLimitMapper>() {
            public DataLimitMapper mapRow(ResultSet rs, int rowNum) throws SQLException {
                DataLimitMapper dataLimitMapper = new DataLimitMapper();
                dataLimitMapper.setMapperId(rs.getString(1));
                String metaId = rs.getString(2);
                DataLimitMeta dataLimitMeta = meta.get(metaId);
                if (dataLimitMeta == null) {
                    throw new RuntimeException("not found DataLimitMeta of " + metaId);
                }
                dataLimitMapper.setDataLimitMeta(dataLimitMeta);
                return dataLimitMapper;
            }
        });
        Map<String, DataLimitMapper> newMapper = new HashMap<String, DataLimitMapper>();
        for (DataLimitMapper data : result) {
            newMapper.put(data.getMapperId(), data);
        }
        mapper = newMapper;
    }
    /**
     * 刷新元数据
     */
    private static void refreshMeta() {
        List<DataLimitMeta> result = jdbcTemplate.query(QUERY_META, new RowMapper<DataLimitMeta>() {
            public DataLimitMeta mapRow(ResultSet rs, int rowNum) throws SQLException {
                DataLimitMeta dataLimitMeta = new DataLimitMeta();
                dataLimitMeta.setMetaId(rs.getString(1));
                dataLimitMeta.setMetaData(rs.getString(2));
                dataLimitMeta.setMetaType(rs.getInt(3));
                dataLimitMeta.setMetaParam(rs.getString(4));
                return dataLimitMeta;
            }
        });
        Map<String, DataLimitMeta> newMeta = new HashMap<String, DataLimitMeta>();
        for (DataLimitMeta data : result) {
            newMeta.put(data.getMetaId(), data);
        }
        meta = newMeta;
    }
}
