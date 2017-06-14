package com.kyy.demo.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.kyy.demo.annotation.IgnoreAuthorization;
import com.kyy.demo.model.User;
import com.kyy.demo.service.TokenService;
import com.kyy.demo.util.Util;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("tokens")
public class TokenController extends Controller {

	@Autowired
	private TokenService service;

	@PostMapping
	@IgnoreAuthorization
	@ApiOperation(value = "登录")
	public ResponseEntity<?> create(HttpServletRequest request, HttpServletResponse response,
			@RequestBody @JsonView(User.Authorzation.class) User user) {
		String token = service.create(user);
		if (token != null) {
			Util.setToken(response, token);
			return CREATED;
		} else {
			return UNAUTHORIZED;
		}
	}

	@DeleteMapping
	@ApiOperation(value = "注销")
	public ResponseEntity<?> delete(HttpServletRequest request) {
		service.delete(Util.getToken(request));
		return OK;
	}
}
