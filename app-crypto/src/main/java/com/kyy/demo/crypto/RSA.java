package com.kyy.demo.crypto;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("tokenCrypto")
public class RSA implements Crypto {

	/**
	 * RSA加密算法
	 */
	private static final String RSA_KEY_ALGORITHM = "RSA";

	/**
	 * RSA私钥串
	 */
	@Value("${rsa.private.key:MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBALRdwKOBk9erwqDlAv5XO0x+Y8YwJhN85EmJUb6wqan4pD7WGeRs3MscPfxVflGDXPcb5vpt3wSpBn8XmCTtmE9ghjSo9vmlcqs1T82TJ1T33PNbfVCPbh6Z1xK74hHjBMe7CRKHyBaZw3FLLuxHgfIGrqCRfP2CJmUXC3N+A6vzAgMBAAECgYB3XM/+pOMdSGr+JXCMt1fj8ITBBN496Vn+nGS/qJQviv/PIcty0uRveho/Yqi88w1T72A1fP6pS96jvw6N6brwNzqrZkjHTdIlEBebklth30x4XfXxd3vrdE7UmaPyo2pCi4440ZjtCZR/ztLnLbhRlAiLMbHKYuwokRj7xYP5kQJBAOyZHPWuFpmRS4xTsQl2gb8KuamfeWKo5dh8K56FkE3Frb/cUW7gl/6VfLCAFGycS2xf878xBgw5CfQ0Rq5mmcsCQQDDKCroVHne+5r3txI57f91J7hU6sbDY8JLofngS85ijk7mEZIerzQ4Fw0fHtafp/GTNLDPaRVgZDOBOaLuG5F5AkEAgIvj7xupzb/iUxi5jFGlxr02CJHLqq3nS0qTjGo27/piH2a9m8dM7ZYci+zaq/PzCqvLS+p/xa5L2TF54ZF/JwJBALKlGmICsm0ENR99XhnZW3eLHJnfMIO71igV5f8EZbCiRGcmvnOIPmz57PEH8b/EpMbz/MZgk0jhcKRidlswsUECQQCjeFxjndAiVeQ9fvdJYx2wR1TYwGXo6lB1nIsuGs7Jo4BTR8GCWh1p2N/eQ8CahRJnTQWMF3TQni3adTvUDGo4}")
	private String privateKey;

	/**
	 * RSA公钥串
	 */
	@Value("${rsa.public.key:MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC0XcCjgZPXq8Kg5QL+VztMfmPGMCYTfORJiVG+sKmp+KQ+1hnkbNzLHD38VX5Rg1z3G+b6bd8EqQZ/F5gk7ZhPYIY0qPb5pXKrNU/NkydU99zzW31Qj24emdcSu+IR4wTHuwkSh8gWmcNxSy7sR4HyBq6gkXz9giZlFwtzfgOr8wIDAQAB}")
	private String publicKey;

	/**
	 * RSA私钥
	 */
	private RSAPrivateKey rsaPrivateKey;

	/**
	 * RSA公钥
	 */
	private RSAPublicKey rsaPublicKey;

	@PostConstruct
	public void init() throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM, new BouncyCastleProvider());
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey));
		rsaPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
		X509EncodedKeySpec keySpec2 = new X509EncodedKeySpec(Base64.decodeBase64(publicKey));
		rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec2);
	}

	public byte[] encrypt(byte[] data) throws Exception {
		Cipher cipher = Cipher.getInstance(RSA_KEY_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, rsaPrivateKey);
		return cipher.doFinal(data);
	}

	public String encrypt(String data) throws Exception {
		return Base64.encodeBase64String(encrypt(data.getBytes()));
	}

	public byte[] decrypt(byte[] data) throws Exception {
		Cipher cipher = Cipher.getInstance(RSA_KEY_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, rsaPublicKey);
		return cipher.doFinal(data);
	}

	public String decrypt(String data) throws Exception {
		return new String(decrypt(Base64.decodeBase64(data)));
	}

}
