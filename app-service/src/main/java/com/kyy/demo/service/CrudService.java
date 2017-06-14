package com.kyy.demo.service;

import com.kyy.demo.vo.Page;
import com.kyy.demo.vo.PageResult;

public interface CrudService extends Service {

	public <E> PageResult<E> list(Class<E> clazz, Page page);

	public <E, ID> E get(Class<E> clazz, ID id);

	public <E> void create(E entity);

	public <E> void update(E entity);

	public <E, ID> void delete(Class<E> clazz, ID id);

	public <E, ID> void delete(Class<E> clazz, ID[] ids);

}
