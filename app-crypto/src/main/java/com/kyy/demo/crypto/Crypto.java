package com.kyy.demo.crypto;

public interface Crypto {
	/**
	 * 加密
	 */
	public byte[] encrypt(byte[] data) throws Exception;

	/**
	 * 加密
	 * 
	 * @return base64字符串
	 */
	public String encrypt(String data) throws Exception;

	public byte[] decrypt(byte[] data) throws Exception;

	/**
	 * 解密base64字符串
	 */
	public String decrypt(String data) throws Exception;
}
