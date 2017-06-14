package com.kyy.demo.dao.impl;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.kyy.demo.crypto.Crypto;
import com.kyy.demo.dao.TokenDao;
import com.kyy.demo.model.User;
import com.kyy.demo.vo.Token;

//@Repository
public class TokenDaoImpl extends SqlSessionDaoSupport implements TokenDao {

	@Value(value = "${session.timeout}")
	private int timeout;

	@Autowired
	@Qualifier("tokenCrypto")
	private Crypto crypto;

	@Autowired
	public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
		super.setSqlSessionFactory(sqlSessionFactory);
	}

	@Override
	public Long get(String token) {
		if (token == null) {
			return null;
		}
		try {
			Token t = Token.parse(crypto.decrypt(token));
			if (t == null || !t.validate()) {
				return null;
			}
			return t.getUid();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public String create(User user) {
		Long uid = this.getSqlSession().selectOne("com.kyy.demo.model.mapper.custom.UserMapper.authorize", user);
		if (uid != null) {
			try {
				return crypto.encrypt(new Token(uid, timeout).toString());
			} catch (Exception e) {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public void delete(String token) {
		return;
	}

}
