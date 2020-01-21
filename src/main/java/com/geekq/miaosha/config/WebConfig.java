package com.geekq.miaosha.config;

import com.geekq.miaosha.access.AccessInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * WebMvcConfigurerAdapter是一个抽象类，它只提供了一些空的接口让用户去重写，比如如果想添加拦截器的时候，
 * 需要去重写一下addInterceptors()这个方法，去配置自定义的拦截器。
 */

/**
 public abstract class WebMvcConfigurerAdapter implements WebMvcConfigurer {
        // 配置路径匹配参数
        public void configurePathMatch(PathMatchConfigurer configurer){}

        // 配置Web Service或REST API设计中内容协商,即根据客户端的支持内容格式情况来封装响应消息体，如xml,json
        public void configureContentNegotiation(ContentNegotiationConfigurer configurer){}

        // 配置路径匹配参数
        public void configureAsyncSupport(AsyncSupportConfigurer configurer){}

        // 使得springmvc在接口层支持异步
        public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer){}

        // 注册参数转换和格式化器
        public void addFormatters(FormatterRegistry registry){}

        // 注册配置的拦截器
        public void addInterceptors(InterceptorRegistry registry){}

        //自定义静态资源映射
        public void addResourceHandlers(ResourceHandlerRegistry registry){}

        // cors跨域访问
        public void addCorsMappings(CorsRegistry registry){}

        // 配置页面直接访问，不走接口
        public void addViewControllers(ViewControllerRegistry registry){}

        // 注册自定义的视图解析器
        public void configureViewResolvers(ViewResolverRegistry registry){}

        // 注册自定义控制器(controller)方法参数类型
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers){}

        // 注册自定义控制器(controller)方法返回类型
        public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers){}

        // 重载会覆盖掉spring mvc默认注册的多个HttpMessageConverter
        public void configureMessageConverters(List<HttpMessageConverter<?>>converters){}

        // 仅添加一个自定义的HttpMessageConverter,不覆盖默认注册的HttpMessageConverter
        public void extendMessageConverters(List<HttpMessageConverter<?>>converters){}

        // 注册异常处理
        public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers){}

        // 多个异常处理，可以重写次方法指定处理顺序等
        public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers){}


}
*/
@Configuration
public class WebConfig  extends WebMvcConfigurerAdapter {

    @Autowired
    UserArgumentResolver resolver;

    @Autowired
    private AccessInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        super.addInterceptors(registry);
        registry.addInterceptor(interceptor);  //加一个拦截器，AccessInterceptor是登录与限速的拦截器
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(resolver); // 注册自定义控制器(controller)方法参数类型
    }
}
