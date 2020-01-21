package com.geekq.miaosha.access;

import com.alibaba.fastjson.JSON;
import com.geekq.miaosha.common.enums.ResultStatus;
import com.geekq.miaosha.common.resultbean.ResultGeekQ;
import com.geekq.miaosha.controller.LoginController;
import com.geekq.miaosha.domain.MiaoshaUser;
import com.geekq.miaosha.redis.RedisService;
import com.geekq.miaosha.service.MiaoShaUserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

import static com.geekq.miaosha.common.enums.ResultStatus.ACCESS_LIMIT_REACHED;
import static com.geekq.miaosha.common.enums.ResultStatus.SESSION_ERROR;

@Service
public class AccessInterceptor  extends HandlerInterceptorAdapter{

	private static Logger logger = LoggerFactory.getLogger(AccessInterceptor.class);

	@Autowired
	MiaoShaUserService userService;

	@Autowired
	RedisService redisService;

    /**
     * 在处理spring mvc的请求之前，对登录与限速进行验证
     */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		/**
		 * 获取调用 获取主要方法
         *
         * handler instanceof HandlerMethod：这个代表，如果是spring mvc的请求。Object handler代表一个Controller,
         * 参考：https://blog.csdn.net/Dongguabai/article/details/81160361
		 */
		if(handler instanceof HandlerMethod) {
			logger.info("打印拦截方法handler ：{} ",handler);
			HandlerMethod hm = (HandlerMethod)handler;
			//方便mybatis 测试
//			if(hm.getMethod().getName().startsWith("test")){
//				return true;
//			}
			MiaoshaUser user = getUser(request, response);
			UserContext.setUser(user);

			AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
			if(accessLimit == null) {
				return true;
			}
			int seconds = accessLimit.seconds();
			int maxCount = accessLimit.maxCount();
			boolean needLogin = accessLimit.needLogin();
			String key = request.getRequestURI();
			if(needLogin) {  //这个地方是判断是否要登录(注解可以设置强制登录)，如果登录，则从request中取出的user不能为空，否则重定向到登录页面（没有重定向）
				if(user == null) {
				    //不是重定向，这个地方是通过response，将错误信息写回到前端。
					render(response, SESSION_ERROR);
					return false;
				}
				key += "_" + user.getNickname();
			}else {
				//do nothing
			}

            /**
             * 对url-用户nickName的进行限速
             * 限速：最大次数，当前次数（每次加1），过期时间。
             * 过期时间也就是时间，在一段时间内(过期时间，达到过期时间后，清空，又是一个周期了)，访问的次数不能超过最大值
             */
			AccessKey ak = AccessKey.withExpire(seconds);
			Integer count = redisService.get(ak, key, Integer.class);
	    	if(count  == null) {
	    		 redisService.set(ak, key, 1);
	    	}else if(count < maxCount) {
	    		 redisService.incr(ak, key);
	    	}else {
	    		render(response, ACCESS_LIMIT_REACHED);
	    		return false;
	    	}
		}
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		super.afterCompletion(request, response, handler, ex);

        // 当前用户在UserContext中，每处理一个请求，
        // 在preHandle会将用户存入到上下文中(ThreadLocal, 这样只有当前线程能看到之前的用户)，完成请求后删除
        // 上面这样，在Controller处理请求中，可以通过UserContext获取当前的用户。
        // 也就是在本地内存中会缓存一份用户的信息，不会直接从redis中获取. ==> 搜索项目，没有用到这个特性
		UserContext.removeUser();
	}

	private void render(HttpServletResponse response, ResultStatus cm)throws Exception {
		response.setContentType("application/json;charset=UTF-8");
		OutputStream out = response.getOutputStream();
		String str  = JSON.toJSONString(ResultGeekQ.error(cm));  //将错误信息通过HttpServletResponse的outputstream写加到前端
		out.write(str.getBytes("UTF-8"));
		out.flush();
		out.close();
	}

    /**
     * 获取MiaoshaUser, 从HttpServletRequest中取token，到redis中取出用户的信息。
     * 这样用户必须进行登录，才能进行之后的操作，否则在这个地方会是一个null
     */
	private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response) {
		String paramToken = request.getParameter(MiaoShaUserService.COOKIE_NAME_TOKEN);  //从request中取出token参数
		String cookieToken = getCookieValue(request, MiaoShaUserService.COOKIE_NAME_TOKEN);
		if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {  //从请求中找不到token
			return null;
		}
		String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
		return userService.getByToken(response, token);
	}

	private String getCookieValue(HttpServletRequest request, String cookiName) {
		Cookie[]  cookies = request.getCookies();  //Cookie， name/value/过期时间
		if(cookies == null || cookies.length <= 0){
			return null;
		}
		for(Cookie cookie : cookies) {
			if(cookie.getName().equals(cookiName)) {  //request中会带上所有的Cookie，只取名字为Cookie的，
				return cookie.getValue();
			}
		}
		return null;
	}

}
