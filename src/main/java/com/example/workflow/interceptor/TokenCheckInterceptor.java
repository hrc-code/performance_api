package com.example.workflow.interceptor;

import com.example.workflow.exception.BaseException;
import com.example.workflow.utils.JwtHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;

@Component
public class TokenCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            return true;
        }
        // 获取请求头中的令牌(token)
        String token = request.getHeader("Authorization");
        //判断令牌是否存在，如果不存在，返回错误结果(未登陆)
        if (!StringUtils.hasLength(token)) {
            //去url中获取
            token= request.getParameter("token");
            if (!StringUtils.hasLength(token)) {
                throw new BaseException("403", "登录信息无效，请先登录");
            }
        }
        token = token.replace("Bearer ","");

        return !JwtHelper.isExpired(token);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
