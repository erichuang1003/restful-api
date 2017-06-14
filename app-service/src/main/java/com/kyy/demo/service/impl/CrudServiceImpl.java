package com.kyy.demo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kyy.demo.dao.CrudDao;
import com.kyy.demo.service.CrudService;
import com.kyy.demo.vo.Page;
import com.kyy.demo.vo.PageResult;

@Service
public class CrudServiceImpl implements CrudService {

	// private static Logger logger = Logger.getLogger(ServiceImpl.class);

	@Autowired
	private CrudDao dao;

	// @Autowired
	// private AmqpTemplate amqpTemplate;

	public <E> PageResult<E> list(Class<E> clazz, Page page) {
		return new PageResult<E>(dao.list(clazz, page), dao.count(clazz));
	}

	public <E, ID> E get(Class<E> clazz, ID id) {
		return dao.get(clazz, id);
	}

	public <E> void create(E entity) {
		dao.create(entity);
		// amqpTemplate.convertAndSend("app.websocket.fanout", null, entity);
		// logger.info(entity.getClass().getSimpleName() + " save " + entity);
	}

	public <E> void update(E entity) {
		dao.update(entity);
		// amqpTemplate.convertAndSend("app.websocket.fanout", null, entity);
		// logger.info(entity.getClass().getSimpleName() + " update ");
	}

	public <E, ID> void delete(Class<E> clazz, ID id) {
		dao.delete(clazz, id);
		// amqpTemplate.convertAndSend("app.websocket.fanout", null, id);
		// logger.info(clazz.getSimpleName() + " delete " + id);
	}

	public <E, ID> void delete(Class<E> clazz, ID[] ids) {
		for (ID id : ids) {
			dao.delete(clazz, id);
		}
		// amqpTemplate.convertAndSend("app.websocket.fanout", null, id);
		// logger.info(clazz.getSimpleName() + " delete " + id);
	}
}
