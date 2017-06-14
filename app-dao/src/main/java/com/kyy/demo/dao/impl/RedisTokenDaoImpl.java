package com.kyy.demo.dao.impl;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.kyy.demo.crypto.Crypto;
import com.kyy.demo.dao.TokenDao;
import com.kyy.demo.model.User;

@Repository
public class RedisTokenDaoImpl extends SqlSessionDaoSupport implements TokenDao {

	private static final String KEY_CURRENT_USER_ID = "cuid";
	private static final byte[] KEY_CURRENT_USER_ID_BYTES = KEY_CURRENT_USER_ID.getBytes();
	private static final String KEY_PREFIX = "session_";

	@Value(value = "${session.timeout}")
	private int timeout;

	private RedisTemplate<String, String> template;

	@Autowired
	@Qualifier("hmacSHA1")
	private Crypto crypto;

	@Autowired
	public void setRedisTemplate(RedisTemplate<String, String> template) {
		this.template = template;
	}

	@Autowired
	public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
		super.setSqlSessionFactory(sqlSessionFactory);
	}

	@Override
	public Long get(String token) {
		if (token == null) {
			return null;
		}
		return template.execute(new RedisCallback<Long>() {

			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				byte[] key = (KEY_PREFIX + token).getBytes();
				byte[] b = connection.hGet(key, KEY_CURRENT_USER_ID_BYTES);
				connection.expire(key, timeout);
				if (b == null) {
					return null;
				}
				return Long.parseLong(new String(b));
			}

		});
	}

	private String generateToken(Long uid) {
		String uidStr = String.valueOf(uid);
		try {
			return new StringBuilder(uidStr).append('-').append(System.currentTimeMillis()).append('-')
					.append(crypto.encrypt(uidStr)).toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String create(User user) {
		Long uid = this.getSqlSession().selectOne("com.kyy.demo.model.mapper.custom.UserMapper.authorize", user);
		if (uid != null) {
			String token = generateToken(uid);
			template.execute(new RedisCallback<Object>() {

				@Override
				public Object doInRedis(RedisConnection connection) throws DataAccessException {
					byte[] key = (KEY_PREFIX + token).getBytes();
					connection.hSet(key, KEY_CURRENT_USER_ID_BYTES, String.valueOf(uid).getBytes());
					connection.expire(key, timeout);
					return null;
				}

			});
			return token;
		} else {
			return null;
		}
	}

	@Override
	public void delete(String token) {
		if (token == null) {
			return;
		}
		template.execute(new RedisCallback<Object>() {

			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				byte[] key = (KEY_PREFIX + token).getBytes();
				connection.del(key);
				return null;
			}

		});
	}

}
