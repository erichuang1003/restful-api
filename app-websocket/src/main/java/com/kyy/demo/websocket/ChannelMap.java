package com.kyy.demo.websocket;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelMatcher;

public interface ChannelMap<ID> {

	void close();

	void close(ChannelMatcher matcher);

	void write(Object message);

	void write(Object message, ChannelMatcher matcher);

	void writeAndFlush(Object message);

	void writeAndFlush(Object message, ChannelMatcher matcher);

	void put(ID id, Channel channel);

	void remove(ID id, Channel channel);

}