package com.caixunshi.datalimit.util;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Proxy;

/**
 * @Author: caixunshi
 * @Date: 2020/6/10 20:02
 */
public class PluginUtils {
    /**
     * 存在多层代理时，递归获取真正的处理对象
     * @param target
     * @return
     */
    public static Object realTarget(Object target) {
        if (Proxy.isProxyClass(target.getClass())) {
            MetaObject metaObject = SystemMetaObject.forObject(target);
            return realTarget(metaObject.getValue("h.target"));
        }
        return target;
    }
}
