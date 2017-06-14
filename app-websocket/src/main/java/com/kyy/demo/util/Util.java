package com.kyy.demo.util;

import java.util.Set;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

public class Util {
	public static String getToken(String cookieHeader) {
		if (cookieHeader == null) {
			return null;
		}
		Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieHeader);
		String sid = null;
		for (Cookie cookie : cookies) {
			if ("token".equals(cookie.name())) {
				sid = cookie.value();
				break;
			}
		}
		return sid;
	}
}
