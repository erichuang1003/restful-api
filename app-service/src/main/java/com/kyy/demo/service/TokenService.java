package com.kyy.demo.service;

import com.kyy.demo.model.User;

public interface TokenService extends Service {

	public Long get(String token);

	public String create(User user);

	public void delete(String token);

}
