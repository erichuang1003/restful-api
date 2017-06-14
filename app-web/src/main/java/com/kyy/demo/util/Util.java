package com.kyy.demo.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;

public class Util {
	private static final String COOKIE_NAME_SESSION_ID = "token";

	private static final String ATTRIBUTE_NAME_CURRENT_USER_ID = "cuid";

	public static String getToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}
		String token = null;
		for (Cookie cookie : cookies) {
			if (COOKIE_NAME_SESSION_ID.equals(cookie.getName())) {
				token = cookie.getValue();
				break;
			}
		}
		if (token != null) {
			return token;
		} else {
			return null;
		}
	}

	public static void setToken(HttpServletResponse response, String token) {
		Cookie cookie = new Cookie(COOKIE_NAME_SESSION_ID, token);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		response.addCookie(cookie);
	}

	public static Object getCurrentUserId(NativeWebRequest webRequest) {
		return webRequest.getAttribute(ATTRIBUTE_NAME_CURRENT_USER_ID, RequestAttributes.SCOPE_REQUEST);
	}

	public static void setCurrentUserId(HttpServletRequest request, Long uid) {
		if (uid != null) {
			request.setAttribute(ATTRIBUTE_NAME_CURRENT_USER_ID, uid);
		}
	}

}
