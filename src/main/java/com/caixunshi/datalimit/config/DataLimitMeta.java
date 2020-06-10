package com.caixunshi.datalimit.config;

/**
 * @Author: caixunshi
 * @Date: 2020/6/10 20:09
 */
public class DataLimitMeta {
    // 自定义元数据Id
    private String metaId;

    // 权限控制的元数据
    private String metaData;

    // 元数据类型(1:sql, 2:类方法)
    private Integer metaType;

    // 扩展参数
    private String metaParam;

    public String getMetaId() {
        return metaId;
    }

    public void setMetaId(String metaId) {
        this.metaId = metaId;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public Integer getMetaType() {
        return metaType;
    }

    public void setMetaType(Integer metaType) {
        this.metaType = metaType;
    }

    public String getMetaParam() {
        return metaParam;
    }

    public void setMetaParam(String metaParam) {
        this.metaParam = metaParam;
    }
}
