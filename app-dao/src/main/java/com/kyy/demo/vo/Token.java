package com.kyy.demo.vo;

import java.util.Random;

public class Token {

	private static final Random RD = new Random();

	private static final int MIN_NONCE = 100000;

	private static final int MAX_NONCE = 999999;

	private static final int RD_NUM = MAX_NONCE - MIN_NONCE + 1;

	private static final char SEPARATOR = '_';

	private static final int MAX_TIMEOUT = 3600 * 24 * 7;

	/**
	 * 用户ID
	 */
	private long uid;
	/**
	 * 创建时间(秒)
	 */
	private long timestamp;
	/**
	 * 到期时间(秒)
	 */
	private long expire;
	/**
	 * 随机数[100000, 999999]
	 */
	private int nonce;

	public Token(long uid, long timeout) {
		super();
		this.uid = uid;
		this.timestamp = System.currentTimeMillis() / 1000;
		this.expire = this.timestamp + timeout;
		nonce = RD.nextInt(RD_NUM) + MIN_NONCE;
	}

	private Token(long uid, long timestamp, long expire, int nonce) {
		super();
		this.uid = uid;
		this.timestamp = timestamp;
		this.expire = expire;
		this.nonce = nonce;
	}

	public long getUid() {
		return uid;
	}

	public static Token parse(String token) {
		int index1 = token.indexOf(SEPARATOR);
		if (index1 <= 0) {
			return null;
		}
		int index2 = token.indexOf(SEPARATOR, index1 + 1);
		if (index2 <= 2) {
			return null;
		}
		int index3 = token.indexOf(SEPARATOR, index2 + 1);
		if (index3 <= 4) {
			return null;
		}
		int nonce = Integer.parseInt(token.substring(index3 + 1));

		return new Token(Long.parseLong(token.substring(0, index1)),
				Long.parseLong(token.substring(index1 + 1, index2)),
				Long.parseLong(token.substring(index2 + 1, index3)), nonce);
	}

	public boolean validate() {
		return nonce >= MIN_NONCE && nonce <= MAX_NONCE && expire > timestamp && expire - timestamp <= MAX_TIMEOUT
				&& expire > System.currentTimeMillis() / 1000;
	}

	public String toString() {
		return new StringBuilder().append(uid).append(SEPARATOR).append(timestamp).append(SEPARATOR).append(expire)
				.append(SEPARATOR).append(nonce).toString();
	}

}
