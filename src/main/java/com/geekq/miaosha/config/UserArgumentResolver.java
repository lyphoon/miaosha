package com.geekq.miaosha.config;

import com.geekq.miaosha.access.UserContext;
import com.geekq.miaosha.domain.MiaoshaUser;
import com.geekq.miaosha.service.MiaoShaUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 这个是这样的：Controller中是一个MiarshaUser参数，但是前端可能不会传入，
 * 这时通过resolverArgument获取到真正的MiarshaUser实例
 * OrderController，RegistryController中使用到了. 这时，方法上一定会有@AccessLimit注解
 *
 * ==>OrderController是不对的， 正确的使用是RegistryController中的使用
 */
@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private MiaoShaUserService userService;

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
      Class<?> clazz =    methodParameter.getParameterType() ;
      return clazz == MiaoshaUser.class ;  //本类（UserArgumentResolver类）要处理的Conroller参数
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest webRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        /**
         *  threadlocal 存储线程副本 保证线程不冲突
         */
        return UserContext.getUser();  //如果没有，从当前线程的上下文中获取，这个在AccessInterceptor有说明
    }

}
