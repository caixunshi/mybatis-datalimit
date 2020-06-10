package com.caixunshi.datalimit.config;

/**
 * @Author: caixunshi
 * @Date: 2020/6/10 20:11
 */
public class DataLimitMapper {
    // 全局mapperid
    private String mapperId;

    // 关联的数据权限元数据
    private DataLimitMeta dataLimitMeta;

    public String getMapperId() {
        return mapperId;
    }

    public void setMapperId(String mapperId) {
        this.mapperId = mapperId;
    }

    public DataLimitMeta getDataLimitMeta() {
        return dataLimitMeta;
    }

    public void setDataLimitMeta(DataLimitMeta dataLimitMeta) {
        this.dataLimitMeta = dataLimitMeta;
    }
}
