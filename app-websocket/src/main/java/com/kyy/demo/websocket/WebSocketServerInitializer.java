package com.kyy.demo.websocket;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.kyy.demo.service.TokenService;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;

@Component
public class WebSocketServerInitializer extends ChannelInitializer<Channel> {

	@Value("${websocket.path}")
	private String websocketPath;

	@Value("${websocket.guest}")
	private boolean guest;

	@Autowired
	private TokenService tokenService;

	@Autowired(required = false)
	private SslContext sslCtx;

	@Autowired
	private ChannelMap<Long> channelMap;

	@Override
	public void initChannel(Channel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		if (sslCtx != null) {
			pipeline.addLast(sslCtx.newHandler(ch.alloc()));
		}
		pipeline.addLast(new HttpServerCodec());
		pipeline.addLast(new HttpObjectAggregator(65536));
		pipeline.addLast(new HttpRequestHandler(websocketPath, tokenService, guest));
		pipeline.addLast(new WebSocketServerCompressionHandler());
		pipeline.addLast(new WebSocketServerProtocolHandler(websocketPath, null, true));
		pipeline.addLast(new IdleStateHandler(0, 0, 180, TimeUnit.SECONDS));
		pipeline.addLast(new WebSocketFrameHandler(channelMap));
	}
}