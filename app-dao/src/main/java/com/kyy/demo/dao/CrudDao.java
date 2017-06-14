package com.kyy.demo.dao;

import java.util.List;

import com.kyy.demo.vo.Page;

public interface CrudDao extends Dao {

	public <E> List<E> list(Class<E> clazz, Page page);

	public <E> Long count(Class<E> clazz);

	public <E, ID> E get(Class<E> clazz, ID id);

	public <E> void create(E entity);

	public <E> void update(E entity);

	public <E, ID> void delete(Class<E> clazz, ID id);

}
