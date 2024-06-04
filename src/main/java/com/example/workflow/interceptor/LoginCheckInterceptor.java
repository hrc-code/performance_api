package com.example.workflow.interceptor;

import com.auth0.jwt.interfaces.Claim;
import com.example.workflow.exception.BaseException;
import com.example.workflow.utils.JWTHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Autowired
    private HttpSession session;

    @Override//目标资源（controller接口）放行前运行，返回true:放行，返回false:不放行
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }
        // 获取请求头中的令牌(token)
        String token = request.getHeader("Authorization");
        //判断令牌是否存在，如果不存在，返回错误结果(未登陆)
        if (!StringUtils.hasLength(token)) {
            throw new BaseException("403", "未登录，请先登录1");
        }
        token = token.replace("Bearer ","");

        //解析token,如果解析失败，返回错误结果（token失效）
        Map<String, Claim> map = JWTHelper.verifyToken(token);

        if (map == null) {
            throw new BaseException("403", "登录信息无效，请先登录2");
        }
        Long exp = (Long) session.getAttribute(token);
        if (exp == null) {
            throw new BaseException("403", "登录信息无效，请先登录3");
        }
        //自动续期
        if (exp < System.currentTimeMillis()) {
            session.setAttribute(token, String.valueOf(System.currentTimeMillis()) + JWTHelper.EXPIRE_TIME);
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
