package com.kyy.demo.websocket;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

@Component
public class WebSocketServer {

	private static Logger logger = Logger.getLogger(WebSocketServer.class);

	@Value("${websocket.port}")
	private int port;

	private Channel ch;

	@Autowired
	@Qualifier("bossGroup")
	private EventLoopGroup bossGroup;

	@Autowired
	@Qualifier("workerGroup")
	private EventLoopGroup workerGroup;

	@Autowired
	private ServerBootstrap bootstrap;

	@Autowired
	private ChannelHandler channelHandler;

	public int getPort() {
		return port;
	}

	@PostConstruct
	public void start() throws InterruptedException {
		bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(channelHandler);

		ch = bootstrap.bind(port).sync().channel();

		logger.info("websocket server started 127.0.0.1:" + port);
	}

	@PreDestroy
	public void stop() {
		if (ch != null && ch.isActive()) {
			ch.close();
		}
		if (bossGroup != null) {
			bossGroup.shutdownGracefully();
		}
		if (workerGroup != null) {
			workerGroup.shutdownGracefully();
		}
	}

}
