package com.kyy.demo.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kyy.demo.service.TokenService;
import com.kyy.demo.util.Util;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	private static final Logger logger = LoggerFactory.getLogger(HttpRequestHandler.class);

	private final String websocketPath;

	private final TokenService tokenService;

	private final boolean guest;

	private Long uid;

	public HttpRequestHandler(String websocketPath, TokenService tokenService, boolean guest) {
		this.websocketPath = websocketPath;
		this.tokenService = tokenService;
		this.guest = guest;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
		if (websocketPath.equals(msg.uri())) {
			String cookieHeader = msg.headers().get("Cookie");
			String token = Util.getToken(cookieHeader);
			if (!guest && token == null) {
				ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED))
						.addListener(ChannelFutureListener.CLOSE);
				return;
			}
			uid = tokenService.get(token);

			if (!guest && uid == null) {
				ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED))
						.addListener(ChannelFutureListener.CLOSE);
				return;
			}
			ctx.fireChannelRead(msg.retain());
		} else {
			ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN))
					.addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
		logger.error(ctx.channel().toString(), cause);
	}

	public Long getUid() {
		return uid;
	}

}
