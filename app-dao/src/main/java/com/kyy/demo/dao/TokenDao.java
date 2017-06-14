package com.kyy.demo.dao;

import com.kyy.demo.model.User;

public interface TokenDao {

	public Long get(String token);

	public String create(User user);

	public void delete(String token);

}
