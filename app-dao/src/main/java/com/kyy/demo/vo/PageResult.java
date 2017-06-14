package com.kyy.demo.vo;

import java.io.Serializable;
import java.util.List;

public class PageResult<E> implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<E> list;
	private Long total;

	public PageResult(List<E> list, Long total) {
		super();
		this.list = list;
		this.total = total;
	}

	public PageResult() {
		super();
	}

	public List<E> getList() {
		return list;
	}

	public void setList(List<E> list) {
		this.list = list;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

}
