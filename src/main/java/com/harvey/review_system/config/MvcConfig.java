package com.harvey.review_system.config;

import com.harvey.review_system.interceptor.ExpireInterceptor;
import com.harvey.review_system.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-01-03 14:12
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ExpireInterceptor(stringRedisTemplate));
        // 默认拦截所有请求
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns(// 排除不需要拦截的路径
                        "/user/code",// 发送验证码
                        "/user/login",// 登录
                        "/blog/hot",//热点
                        "/shop/**",//店铺相关
                        "/shop-type/**",// 店铺信息
                        "/voucher/**",// 优惠券信息的查询
                        "/upload/**"// 上传,为了测试就放行吧
                        );
    }
}
