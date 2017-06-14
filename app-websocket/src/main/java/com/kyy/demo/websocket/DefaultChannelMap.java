package com.kyy.demo.websocket;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.ChannelMatchers;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

public class DefaultChannelMap<ID> implements ChannelMap<ID> {

	private final Map<ID, ChannelGroup> map = new ConcurrentHashMap<>();

	private final ChannelGroup guests = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	public DefaultChannelMap() {
	}

	@Override
	public void close() {
		close(ChannelMatchers.all());
	}

	@Override
	public void close(ChannelMatcher matcher) {
		for (Entry<ID, ChannelGroup> entry : map.entrySet()) {
			entry.getValue().close(matcher);
		}
	}

	@Override
	public void write(Object message) {
		write(message, false, null);
	}

	@Override
	public void write(Object message, ChannelMatcher matcher) {
		write(message, false, matcher);
	}

	@Override
	public void writeAndFlush(Object message) {
		writeAndFlush(message, null);
	}

	@Override
	public void writeAndFlush(Object message, ChannelMatcher matcher) {
		write(message, true, matcher);
	}

	protected void write(Object message, boolean flush, ChannelMatcher matcher) {
		try {
			for (Entry<ID, ChannelGroup> entry : map.entrySet()) {
				for (Channel channel : entry.getValue()) {
					if (matcher == null || matcher.matches(channel)) {
						write(channel, safeDuplicate(message), flush);
					}
				}
			}
			for (Channel channel : guests) {
				if (matcher == null || matcher.matches(channel)) {
					write(channel, safeDuplicate(message), flush);
				}
			}
		} finally {
			ReferenceCountUtil.release(message);
		}
	}

	protected void write(Channel channel, Object message, boolean flush) {
		channel.eventLoop().execute(new Runnable() {
			@Override
			public void run() {
				if (flush) {
					channel.writeAndFlush(message, channel.voidPromise());
				} else {
					channel.write(message, channel.voidPromise());
				}
			}
		});
	}

	private static Object safeDuplicate(Object message) {
		if (message instanceof ByteBuf) {
			return ((ByteBuf) message).retainedDuplicate();
		} else if (message instanceof ByteBufHolder) {
			return ((ByteBufHolder) message).retainedDuplicate();
		} else {
			return ReferenceCountUtil.retain(message);
		}
	}

	@Override
	public void put(ID id, Channel channel) {
		if (id != null) {
			synchronized (map) {
				ChannelGroup group = map.get(id);
				if (group == null) {
					group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
					map.put(id, group);
				}
				group.add(channel);
			}
		} else {
			guests.add(channel);
		}
	}

	@Override
	public void remove(ID id, Channel channel) {
		if (id != null) {
			synchronized (map) {
				ChannelGroup group = map.get(id);
				if (group != null && group.isEmpty()) {
					map.remove(id);
				}
			}
		}
	}

}
