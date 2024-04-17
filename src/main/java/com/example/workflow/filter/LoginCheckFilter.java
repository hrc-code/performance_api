package com.example.workflow.filter;

/*@Slf4j
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter{
    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();//进行路径匹配
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest) servletRequest;//向下转型，获取子类中的方法
        HttpServletResponse response=(HttpServletResponse) servletResponse;

        //获取本次请求的URL
        String requestURI=request.getRequestURI();
        log.info("拦截到请求：{}",requestURI);
        //定义不需要处理的请求路径
        String[] urls=new String[]{
            "/employee/login",
            "/employee/logout",
        };
        //判断本次请求是否需要处理
        boolean check=check(urls,requestURI);
        //无需处理，直接放行
        if(check){
            filterChain.doFilter(request,response);
            return;
        }
        //已登录，直接放行
        if(request.getSession().getAttribute("employee")!=null){

            Long empId=(Long)request.getSession().getAttribute("employee");
            BaseContent.setCurrentId(empId);

            filterChain.doFilter(request,response);
            return;
        }
        //未登录则返回未登录结果，通过输出流方式向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    //路径匹配，检查本次请求是否需要放行
    public boolean check(String[] urls,String requestUrl){
        for(String url : urls){
            boolean match=PATH_MATCHER.match(url,requestUrl);
            if(match){
                return true;
            }
        }
        return false;
    }

}*/
