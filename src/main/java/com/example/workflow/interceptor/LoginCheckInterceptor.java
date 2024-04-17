package com.example.workflow.interceptor;

import com.auth0.jwt.interfaces.Claim;
import com.example.workflow.exception.BaseException;
import com.example.workflow.utils.JWTHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component//注入IOC容器
public class LoginCheckInterceptor implements HandlerInterceptor {
    @Override//目标资源（controller接口）放行前运行，返回true:放行，返回false:不放行
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取请求头中的令牌(token)
        String jwt = request.getHeader("Authorization");
        jwt = jwt.replace("Bearer ","");

        //判断令牌是否存在，如果不存在，返回错误结果(未登陆)
        if(!StringUtils.hasLength(jwt)) {
            throw new BaseException("403", "未登录，请先登录");
        }

        //解析token,如果解析失败，返回错误结果（token失效）
        Map<String, Claim> map = JWTHelper.verifyToken(jwt);

        if (map == null) {
            throw new BaseException("403", "登录信息无效，请先登录");
        }

        return true;
    }

    @Override//目标资源方法运行后运行
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override//视图渲染完毕后运行，最后运行
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
