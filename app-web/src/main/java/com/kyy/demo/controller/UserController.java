package com.kyy.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kyy.demo.model.User;
import com.kyy.demo.vo.Page;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("users")
public class UserController extends CrudController {

	@Value("${rabbitmq.exchange.request.http}")
	private String exchange;

	@Value("${rabbitmq.routing.key.user.create}")
	private String createRoutingKey;

	@Value("${rabbitmq.routing.key.user.update}")
	private String updateRoutingKey;

	@Value("${rabbitmq.routing.key.user.delete}")
	private String deleteRoutingKey;

	@ApiOperation(value = "用户列表分页查询")
	@GetMapping
	public ResponseEntity<?> list(Page page) {
		return super.list(User.class, page);
	}

	@ApiOperation(value = "获取某个用户")
	@GetMapping("{id}")
	public ResponseEntity<?> get(@PathVariable("id") Long id) {
		return super.get(User.class, id);
	}

	@ApiOperation(value = "创建用户")
	@PostMapping("{id}")
	public ResponseEntity<?> create(@PathVariable("id") Long id, @RequestBody User user) {
		return super.create(user);
	}

	@ApiOperation(value = "修改用户")
	@PutMapping("{id}")
	public ResponseEntity<?> update(@PathVariable("id") Long id, @RequestBody User user) {
		user.setId(id);
		return super.update(user);
	}

	@ApiOperation(value = "删除用户")
	@DeleteMapping("{id}")
	public ResponseEntity<?> delete(@PathVariable("id") Long id) {
		return super.delete(User.class, id);
	}

	@ApiOperation(value = "批量删除用户")
	@DeleteMapping
	public ResponseEntity<?> delete(@RequestBody Long[] ids) {
		return super.delete(User.class, ids);
	}

}
