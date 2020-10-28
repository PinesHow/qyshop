package com.qiguliuxing.dts.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Created by zhengboao on 2019-04-12 17:12.
 */

/**
 * 资源映射路径
 * addResourceHandler：访问映射路径
 * addResourceLocations：资源绝对路径
 */
//@Configuration
public class WebAppConfig implements WebMvcConfigurer {

    @Value("${file.staticAccessPath}")
    private String staticAccessPath;

    @Value("${file.rootPath}")
    private String rootPath;  //文件保存根目录

    @Value("${file.sonPath}")
    private String sonPath;  //文件保存子目录

//    @Autowired
//    AccessInterceptor AccessInterceptor;

    // 访问图片方法
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(staticAccessPath).addResourceLocations("file:" + rootPath + sonPath);
        WebMvcConfigurer.super.addResourceHandlers(registry);
    }



//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        InterceptorRegistration registration = registry.addInterceptor(AccessInterceptor);
//        //设置拦截路径
//        registration.addPathPatterns("/**");
//        //设置不拦截路径
//        registration.excludePathPatterns("/Admin/login");
//        registration.excludePathPatterns("/wangan/sty/images/*");
//        registration.excludePathPatterns("/Wec/*");
////        //静态资源不拦截
////        registration.excludePathPatterns("/static/**");
////        registration.excludePathPatterns("/Wopop_files/**");
////        registration.excludePathPatterns("/treetable/**");
////        registration.excludePathPatterns("/lib/**");
//
//    }
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedHeaders("Content-Type","X-Requested-With","accept,Origin","Access-Control-Request-Method","Access-Control-Request-Headers","token")
//                .allowedMethods("*")
//                .allowedOrigins("*")
//                .allowCredentials(true);
//
//    }



}
