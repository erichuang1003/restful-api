package com.kyy.demo.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import com.kyy.demo.config.CacheConfig;
import com.kyy.demo.dao.CrudDao;
import com.kyy.demo.model.Generatable;
import com.kyy.demo.vo.Page;

@Repository
@org.springframework.cache.annotation.CacheConfig(cacheNames = CacheConfig.CACHE_REMOTE)
public class CrudDaoImpl extends SqlSessionDaoSupport implements CrudDao {

	@Autowired
	public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
		super.setSqlSessionFactory(sqlSessionFactory);
	}

	@Cacheable(cacheNames = CacheConfig.CACHE_LOCAL)
	public <E> List<E> list(Class<E> clazz, Page page) {
		if (page == null) {
			return this.getSqlSession().selectList(sqlName(clazz, "selectList"));
		} else {
			Map<String, Object> map = new HashMap<>();
			if (page != null) {
				map.put("page", page);
			}
			return this.getSqlSession().selectList(sqlName(clazz, "selectList"), map);
		}
	}

	@Cacheable(cacheNames = CacheConfig.CACHE_LOCAL)
	public <E> Long count(Class<E> clazz) {
		return this.getSqlSession().selectOne(sqlName(clazz, "selectCount"));
	}

	@Cacheable
	public <E, ID> E get(Class<E> clazz, ID id) {
		return this.getSqlSession().selectOne(sqlName(clazz, "selectByPrimaryKey"), id);
	}

	public <E> void create(E entity) {
		if (entity instanceof Generatable) {
			Generatable e = (Generatable) entity;
			if (e.getId() == null || e.getId() == 0) {
				Long id = this.getSqlSession().selectOne(sqlName(entity.getClass(), "selectSequenceNextVal"));
				e.setId(id);
				this.getSqlSession().insert(sqlName(entity.getClass(), "insert"), e);
				return;
			}
		}
		this.getSqlSession().insert(sqlName(entity.getClass(), "insert"), entity);
	}

	@CacheEvict
	public <E> void update(E entity) {
		this.getSqlSession().update(sqlName(entity.getClass(), "updateByPrimaryKey"), entity);
	}

	@CacheEvict
	public <E, ID> void delete(Class<E> clazz, ID id) {
		this.getSqlSession().delete(sqlName(clazz, "deleteByPrimaryKey"), id);
	}

	protected String sqlName(Class<?> clazz, String sqlId) {
		return clazz.getPackage().getName() + ".mapper." + clazz.getSimpleName() + "Mapper." + sqlId;
	}

}
