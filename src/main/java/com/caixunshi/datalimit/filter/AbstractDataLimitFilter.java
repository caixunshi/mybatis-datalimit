package com.caixunshi.datalimit.filter;

import com.caixunshi.datalimit.threadlocal.DataLimitlDefinitionHolder;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * @Author: caixunshi
 * @Date: 2020/6/10 21:01
 */
public abstract class AbstractDataLimitFilter implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException {}
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        Map<String, String> params = getParams(request);
        DataLimitlDefinitionHolder.setParams(params);
        chain.doFilter(servletRequest, servletResponse);
    }
    public void destroy() {}
    /**
     * 留给子类实现，放入数据权限控制需要的参数
     * @param request
     * @return
     */
    protected abstract Map<String, String> getParams(HttpServletRequest request);
}
