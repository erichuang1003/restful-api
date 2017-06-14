package com.kyy.demo.controller;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.kyy.demo.service.CrudService;
import com.kyy.demo.vo.Page;

public abstract class CrudController extends Controller {

	private CrudService service;

	private AmqpTemplate amqpTemplate;

	protected CrudService getService() {
		return service;
	}

	@Autowired
	protected void setService(CrudService service) {
		this.service = service;
	}

	protected AmqpTemplate getAmqpTemplate() {
		return amqpTemplate;
	}

	@Autowired
	protected void setAmqpTemplate(AmqpTemplate amqpTemplate) {
		this.amqpTemplate = amqpTemplate;
	}

	/**
	 * 分页查询接口
	 */
	protected <E> ResponseEntity<?> list(Class<E> clazz, Page page) {
		return ResponseEntity.ok(this.getService().list(clazz, page));
	}

	/**
	 * 根据ID获取对象接口
	 */
	protected <E, ID> ResponseEntity<?> get(Class<E> clazz, ID id) {
		E e = this.getService().get(clazz, id);
		if (e == null) {
			return NOT_FOUND;
		} else {
			return ResponseEntity.ok(e);
		}
	}

	/**
	 * 保存对象接口（同步）
	 */
	protected <E> ResponseEntity<?> create(E entity) {
		this.getService().create(entity);
		return CREATED;
	}

	/**
	 * 保存对象接口（异步）
	 */
	protected <E> ResponseEntity<?> create(String exchange, String routingKey, E entity) {
		amqpTemplate.convertAndSend(exchange, routingKey, entity);
		return CREATED;
	}

	/**
	 * 修改对象接口（同步）
	 */
	protected <E> ResponseEntity<?> update(E entity) {
		this.getService().update(entity);
		return OK;
	}

	/**
	 * 修改对象接口（异步）
	 */
	protected <E> ResponseEntity<?> update(String exchange, String routingKey, E entity) {
		amqpTemplate.convertAndSend(exchange, routingKey, entity);
		return OK;
	}

	/**
	 * 根据ID删除对象接口（同步）
	 */
	protected <E, ID> ResponseEntity<?> delete(Class<E> clazz, ID id) {
		this.getService().delete(clazz, id);
		return OK;
	}

	/**
	 * 批量删除
	 */
	protected <E, ID> ResponseEntity<?> delete(Class<E> clazz, ID[] ids) {
		this.getService().delete(clazz, ids);
		return OK;
	}

	/**
	 * 根据ID删除对象接口（异步）
	 */
	protected <E, ID> ResponseEntity<?> delete(String exchange, String routingKey, ID id) {
		amqpTemplate.convertAndSend(exchange, routingKey, id);
		return OK;
	}

}
