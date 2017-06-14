package com.kyy.demo.listener;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kyy.demo.websocket.ChannelMap;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

@Component
public class BroadcastListener {

	@Autowired
	private ChannelMap<Long> channelMap;

	@RabbitListener(bindings = @QueueBinding(value = @Queue(autoDelete = "true"), exchange = @Exchange(value = "${rabbitmq.exchange.websocket}", type = ExchangeTypes.FANOUT)))
	public void broadcast(Message message) {
		channelMap.writeAndFlush(new TextWebSocketFrame(Unpooled.copiedBuffer(message.getBody())));
	}

}
