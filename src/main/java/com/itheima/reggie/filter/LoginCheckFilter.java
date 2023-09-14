package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
/*
* 检查是否
* */
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/")
@Slf4j
public class LoginCheckFilter implements Filter {
    //比较路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest) servletRequest; //强转的原因是session调用的getSession()方法，是httpservletRequest提供的
        HttpServletResponse response=( HttpServletResponse) servletResponse;
       //1.获取请求的uri
        String requestURI = request.getRequestURI();
        log.info("拦截到请求：{}",requestURI);
        //定义不需要处理的路径
        String urls[]=new String[]{
             "/employee/login.html",
                "/employee/logout.html",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",  //移动端发送短信
                "/user/login"  //移动端登录
        };
        //2.判断本次请求是否需要处理
        boolean check = check(urls, requestURI);
        //3。若不需要处理，直接放行
            if (check){
                log.info("本次请求{}不需要处理",requestURI);
                filterChain.doFilter(request,response);
                return;
            }
       //4-1.判断登录状态，若已登陆，则直接放行
         if (request.getSession().getAttribute("employee")!=null){
             log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("employee"));
             Long empId = (Long) request.getSession().getAttribute("employee");
             BaseContext.setCurrentId(empId);

           long id=Thread.currentThread().getId();
            log.info("线程id为：{}",id);

             filterChain.doFilter(request,response);
             return;
         }

        //4-2.判断用户端登录状态，若已登陆，则直接放行
        if (request.getSession().getAttribute("user")!=null){
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("user"));
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

//            long userId=Thread.currentThread().getId();
//            log.info("线程id为：{}",userId);

            filterChain.doFilter(request,response);
            return;
        }
       //5.如果未登录，则返回未登录结果,通过输出流方式向客户端页面响应数据
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));

        log.info("拦截到请求: {}",request.getRequestURI());  //url给uri之间区别是，uri是url去掉首部的一部分
        filterChain.doFilter(request,response);   //出现了一个拦截图标问题，需要CTRL+F5清除浏览器缓存
        return;
    }
    /*
    * 路径匹配，检查本次请求是否需要放行
    * */

    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            boolean match=PATH_MATCHER.match(url,requestURI);
            if (match){
                return true;
            }
        }
        return false;
    }
}
