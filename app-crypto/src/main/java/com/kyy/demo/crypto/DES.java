package com.kyy.demo.crypto;

import java.security.SecureRandom;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DES implements Crypto {

	/**
	 * DES加密算法
	 */
	public static final String DES_KEY_ALGORITHM = "DES";

	/**
	 * DES私钥串
	 */
	@Value("${des.key:12345678}")
	private String key;

	/**
	 * DES
	 */
	private SecretKey secretKey;

	private static final SecureRandom random = new SecureRandom();

	@PostConstruct
	public void init() throws Exception {
		DESKeySpec desKey = new DESKeySpec(key.getBytes());
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES_KEY_ALGORITHM);
		secretKey = keyFactory.generateSecret(desKey);
	}

	public byte[] encrypt(byte[] data) throws Exception {
		Cipher cipher = Cipher.getInstance(DES_KEY_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, random);
		return cipher.doFinal(data);
	}

	public String encrypt(String data) throws Exception {
		return Base64.encodeBase64String(encrypt(data.getBytes()));
	}

	public byte[] decrypt(byte[] data) throws Exception {
		Cipher cipher = Cipher.getInstance(DES_KEY_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, secretKey, random);
		return cipher.doFinal(data);
	}

	public String decrypt(String data) throws Exception {
		return new String(decrypt(Base64.decodeBase64(data)));
	}

}
