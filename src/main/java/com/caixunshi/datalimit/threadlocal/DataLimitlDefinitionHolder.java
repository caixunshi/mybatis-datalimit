package com.caixunshi.datalimit.threadlocal;

import java.util.Map;

/**
 * @Author: caixunshi
 * @Date: 2020/6/10 19:26
 */
public class DataLimitlDefinitionHolder {
    private static final InheritableThreadLocal<Map<String, String>> threadParams = new InheritableThreadLocal<Map<String, String>>();

    public static Map<String, String> getParams() {
        return threadParams.get();
    }

    public static void setParams(Map<String, String> params) {
        threadParams.set(params);
    }
}
