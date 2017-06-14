package com.kyy.demo.dao;

import java.util.List;

public interface CustomCrudDao extends Dao {

	public <E> List<E> list(Class<E> clazz, Object params);

	public <E> Long count(Class<E> clazz, Object params);

	public <E> E get(Class<E> clazz, Object params);

	public <E> void create(E entity);

	public <E> void update(E entity);

	public <E> void delete(Class<E> clazz, Object params);

}