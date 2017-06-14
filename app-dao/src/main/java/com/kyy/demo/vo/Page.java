package com.kyy.demo.vo;

import java.io.Serializable;

public class Page implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final int MAX_LIMIT = 200;

	private int offset;
	private int limit;

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		if (limit > MAX_LIMIT) {
			this.limit = MAX_LIMIT;
		} else {
			this.limit = limit;
		}
	}

	public int getBegin() {
		return offset + 1;
	}

	public int getEnd() {
		return offset + limit;
	}

	@Override
	public String toString() {
		return offset + "_" + limit;
	}

}
