package com.kyy.demo.model;

import java.util.Date;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonView;

public class User implements Generatable {
	private Long id;

	private String name;

	private Integer age;

	private String account;

	private String password;
	
	static Random random = new Random();
	
	private Date date = new Date(/*System.currentTimeMillis()+random.nextInt(100000)*1000*/);

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	@JsonView(Authorzation.class)
	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	@JsonView(Authorzation.class)
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String toString() {
		return this.getClass().getName() + '_' + id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public interface Authorzation {
	}
}