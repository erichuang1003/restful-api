package com.kyy.demo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.kyy.demo.dao.TokenDao;
import com.kyy.demo.model.User;
import com.kyy.demo.service.TokenService;

@org.springframework.stereotype.Service
public class TokenServiceImpl implements TokenService {

	@Autowired
	private TokenDao dao;

	@Override
	public Long get(String token) {
		return dao.get(token);
	}

	@Override
	public String create(User user) {
		return dao.create(user);
	}

	@Override
	public void delete(String token) {
		dao.delete(token);
	}

}
