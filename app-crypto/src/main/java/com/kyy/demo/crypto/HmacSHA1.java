package com.kyy.demo.crypto;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HmacSHA1 implements Crypto {

	private static final String HMAC_KEY_ALGORITHM = "HmacSHA1";

	@Value("${hmacsha1.key:12345678}")
	private String key;

	private SecretKey secretKey;

	@PostConstruct
	public void init() {
		secretKey = new SecretKeySpec(key.getBytes(), HMAC_KEY_ALGORITHM);
	}

	@Override
	public byte[] encrypt(byte[] data) throws Exception {
		Mac mac = Mac.getInstance(HMAC_KEY_ALGORITHM);
		mac.init(secretKey);
		return mac.doFinal(data);
	}

	public String encrypt(String data) throws Exception {
		Mac mac = Mac.getInstance(HMAC_KEY_ALGORITHM);
		mac.init(secretKey);
		byte[] text = data.getBytes();
		return Base64.encodeBase64String(mac.doFinal(text));
	}

	@Override
	public byte[] decrypt(byte[] data) throws Exception {
		throw new UnsupportedOperationException();
	}

	public String decrypt(String data) throws Exception {
		throw new UnsupportedOperationException();
	}

}
