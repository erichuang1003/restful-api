package com.kyy.demo.dao.impl;

import java.util.List;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kyy.demo.config.CacheConfig;
import com.kyy.demo.dao.CustomCrudDao;
import com.kyy.demo.model.Generatable;

@Repository
@org.springframework.cache.annotation.CacheConfig(cacheNames = CacheConfig.CACHE_REMOTE)
public class CustomCrudDaoImpl extends SqlSessionDaoSupport implements CustomCrudDao {

	@Autowired
	public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
		super.setSqlSessionFactory(sqlSessionFactory);
	}

	public <E> List<E> list(Class<E> clazz, Object params) {
		return this.getSqlSession().selectList(customSqlName(clazz, "selectList"), params);
	}

	public <E> Long count(Class<E> clazz, Object params) {
		return this.getSqlSession().selectOne(customSqlName(clazz, "selectCount"), params);
	}

	public <E> E get(Class<E> clazz, Object params) {
		return this.getSqlSession().selectOne(customSqlName(clazz, "select"), params);
	}

	public <E> void create(E entity) {
		if (entity instanceof Generatable) {
			Generatable e = (Generatable) entity;
			if (e.getId() == null) {
				Long id = this.getSqlSession().selectOne(customSqlName(entity.getClass(), "selectSequenceNextVal"));
				e.setId(id);
				this.getSqlSession().insert(customSqlName(entity.getClass(), "insert"), e);
				return;
			}
		}
		this.getSqlSession().insert(customSqlName(entity.getClass(), "insert"), entity);
	}

	public <E> void update(E entity) {
		this.getSqlSession().update(customSqlName(entity.getClass(), "update"), entity);
	}

	public <E> void delete(Class<E> clazz, Object params) {
		this.getSqlSession().delete(customSqlName(clazz, "delete"), params);
	}

	protected String customSqlName(Class<?> clazz, String sqlId) {
		return clazz.getPackage().getName() + ".mapper.custom." + clazz.getSimpleName() + "Mapper." + sqlId;
	}

}
