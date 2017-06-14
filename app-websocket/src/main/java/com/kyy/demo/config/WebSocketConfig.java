package com.kyy.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kyy.demo.websocket.ChannelMap;
import com.kyy.demo.websocket.DefaultChannelMap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

@Configuration
public class WebSocketConfig {

	@Bean("bossGroup")
	public EventLoopGroup bossGroup() {
		return new NioEventLoopGroup(1);
	}

	@Bean("workerGroup")
	public EventLoopGroup workerGroup() {
		return new NioEventLoopGroup();
	}

	@Bean
	public ServerBootstrap bootstrap() {
		return new ServerBootstrap();
	}

	@Bean(destroyMethod = "close")
	public ChannelMap<Long> channelMap() {
		return new DefaultChannelMap<>();
	}

}
