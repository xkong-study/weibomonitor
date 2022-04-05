package cn.marwin.service;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;


/**
 * Created by fanclys on 2018/2/23 16:36:16
 * 拦截器配置
 */
@Configuration
public class WebSecurityConfig implements WebMvcConfigurer{
    @Bean
    public SecurityInterceptor getSecurityInterceptor() {
        return  new SecurityInterceptor();
    }
    private class SecurityInterceptor extends HandlerInterceptorAdapter {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)throws IOException{
            HttpSession session = request.getSession();
            //判断是否已有该用户登录的session
            if(session.getAttribute("USER_SESSION") !=null){
                return  true;
            }
            else {
                System.out.println("没有session");
                response.sendRedirect("http://localhost:8080/login");
                return false;
            }
        }
   }
    @Override
    public  void addInterceptors(InterceptorRegistry registry){
        InterceptorRegistration registration = registry.addInterceptor(getSecurityInterceptor());
        //拦截配置
        registration.addPathPatterns("/**").excludePathPatterns("/login").excludePathPatterns("/login.html");
    }
}

