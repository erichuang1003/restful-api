package com.kyy.demo.listener;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kyy.demo.model.User;
import com.kyy.demo.service.CrudService;

@Component
public class UserListener {

	@Autowired
	private CrudService service;

	@RabbitListener(bindings = @QueueBinding(value = @Queue(value = "app.user.create", durable = "true"), exchange = @Exchange(value = "${rabbitmq.exchange.request.http}"), key = "${rabbitmq.routing.key.user.create}"))
	public void create(User user) {
		service.create(user);
	}

	@RabbitListener(bindings = @QueueBinding(value = @Queue(value = "app.user.update", durable = "true"), exchange = @Exchange(value = "${rabbitmq.exchange.request.http}"), key = "${rabbitmq.routing.key.user.update}"))
	public void update(User user) {
		service.update(user);
	}

	@RabbitListener(bindings = @QueueBinding(value = @Queue(value = "app.user.delete", durable = "true"), exchange = @Exchange(value = "${rabbitmq.exchange.request.http}"), key = "${rabbitmq.routing.key.user.delete}"))
	public void delete(Long id) {
		service.delete(User.class, id);
	}
}
