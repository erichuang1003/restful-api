package com.kyy.demo.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.kyy.demo.annotation.IgnoreAuthorization;
import com.kyy.demo.service.TokenService;
import com.kyy.demo.util.Util;

public class AuthorizationInterceptor implements HandlerInterceptor {

	@Autowired
	private TokenService service;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		IgnoreAuthorization annotation = null;
		if (handler instanceof HandlerMethod) {
			HandlerMethod method = (HandlerMethod) handler;
			annotation = method.getMethodAnnotation(IgnoreAuthorization.class);
			if (annotation == null) {
				annotation = method.getBeanType().getAnnotation(IgnoreAuthorization.class);
			}
		}
		if (annotation != null && !annotation.requireSession()) {
			return true;
		}
		String token = Util.getToken(request);
		Long uid = null;
		if (token != null) {
			uid = service.get(token);
			Util.setCurrentUserId(request, uid);
		}
		if (uid != null || annotation != null) {
			return true;
		} else {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			return false;
		}
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		
	}
}
