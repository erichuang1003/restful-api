package com.kyy.demo.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.HandshakeComplete;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class WebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketFrameHandler.class);

	private static final int MAX_LENGTH = 20;

	private final ChannelMap<Long> channelMap;

	private Long uid;

	public WebSocketFrameHandler(ChannelMap<Long> channelMap) {
		this.channelMap = channelMap;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
		String text = frame.text();
		ByteBuf buf = frame.content();
		String module = readStringUntilColon(buf);
		logger.info(module);
		String function = readStringUntilColon(buf);
		logger.info(function);
		channelMap.writeAndFlush(new TextWebSocketFrame(text.toUpperCase()));
	}

	private String readStringUntilColon(ByteBuf buf) {
		if (buf == null || !buf.isReadable()) {
			return null;
		}
		byte[] bytes = new byte[MAX_LENGTH];
		int i = 0;
		while (i <= MAX_LENGTH && buf.isReadable()) {
			byte b = buf.readByte();
			if (b == ':') {
				break;
			}
			if (i == MAX_LENGTH) {
				throw new IllegalArgumentException("Illegal ByteBuf, max length " + MAX_LENGTH);
			}
			bytes[i++] = b;
		}
		return new String(bytes, 0, i);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		channelMap.remove(uid, ctx.channel());
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof HandshakeComplete) {
			HttpRequestHandler handler = ctx.pipeline().remove(HttpRequestHandler.class);
			if (handler != null) {
				uid = handler.getUid();
				channelMap.put(uid, ctx.channel());
			} else {
				ctx.close();
			}
		} else if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			if (e.state() == IdleState.ALL_IDLE) {
				ctx.channel().writeAndFlush(new PingWebSocketFrame())
						.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
			}
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
		logger.error(ctx.channel().toString(), cause);
	}
}