package com.zhongtai.tool;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSA {
	
	private static final int KEY_SIZE = 1024;
	// 单次RSA加密操作所允许的最大块长度，该值与 KEY_SIZE、padding方法有关。 1024key->117, 2048key->245, 512key->53
	private static final int BLOCK_SIZE = 117;
	// 1024key->128, 2048key->256, 512key->64
	private static final int OUTPUT_BLOCK_SIZE = 128;
	private static final String ALGORITHM = "RSA";
	private static final String ALGORITHM_CIPHER = "RSA/ECB/PKCS1PADDING";
	private static final String ALGORITHMS_SIGNATURE = "SHA256withRSA";

	public static String[] generateRSAKeyPair() throws Exception {
		String[] keypair = new String[2];

		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(ALGORITHM);
		keyPairGen.initialize(KEY_SIZE);
		KeyPair keyPair = keyPairGen.generateKeyPair();

		PublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		PrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

		String publicKeyString = getKeyString(publicKey);
		keypair[0] = publicKeyString;

		String privateKeyString = getKeyString(privateKey);
		keypair[1] = privateKeyString;

		return keypair;
	}

	public static String decodeSecret(String privateKeyString, String content) throws Exception {
		Cipher rsaCipher = Cipher.getInstance(ALGORITHM_CIPHER);

		byte[] decoded = null;
		decoded = Base64.getDecoder().decode(content);
		Key privateKey = getPrivateKey(privateKeyString);

		rsaCipher.init(Cipher.DECRYPT_MODE, privateKey, new SecureRandom());
		int blocks = decoded.length / OUTPUT_BLOCK_SIZE;
		ByteArrayOutputStream decodedStream = new ByteArrayOutputStream(decoded.length);
		for (int i = 0; i < blocks; i++) {
			decodedStream.write(rsaCipher.doFinal(decoded, i * OUTPUT_BLOCK_SIZE, OUTPUT_BLOCK_SIZE));
		}

		return new String(decodedStream.toByteArray(), StandardCharsets.UTF_8);
	}

	public static String encodeSecret(String publicKeyString, String content) throws Exception {
		Cipher rsaCipher = Cipher.getInstance(ALGORITHM_CIPHER);

		Key publicKey = getPublicKey(publicKeyString);
		rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey, new SecureRandom());
		byte[] data = content.getBytes(StandardCharsets.UTF_8);
		int blocks = data.length / BLOCK_SIZE;
		int lastBlockSize = data.length % BLOCK_SIZE;
		byte[] encryptedData = new byte[(lastBlockSize == 0 ? blocks : blocks + 1) * OUTPUT_BLOCK_SIZE];
		for (int i = 0; i < blocks; i++) {
			rsaCipher.doFinal(data, i * BLOCK_SIZE, BLOCK_SIZE, encryptedData, i * OUTPUT_BLOCK_SIZE);
		}
		if (lastBlockSize != 0) {
			rsaCipher.doFinal(data, blocks * BLOCK_SIZE, lastBlockSize, encryptedData, blocks * OUTPUT_BLOCK_SIZE);
		}

		return new String(Base64.getEncoder().encode(encryptedData));
	}

	private static String getKeyString(Key key) throws Exception {
		byte[] keyBytes = key.getEncoded();
		String s = new String(Base64.getEncoder().encode(keyBytes));
		return s;
	}

	public static PrivateKey getPrivateKey(String key) throws Exception {
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key));
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
		
		return privateKey;
	}

	public static PublicKey getPublicKey(String key) throws Exception {
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(key));
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
		PublicKey publicKey = keyFactory.generatePublic(keySpec);
		
		return publicKey;
	}

	public static String signWithPrivateKey(String content, String privateKey) throws Exception {
		PrivateKey priKey = getPrivateKey(privateKey);

		Signature signature = Signature.getInstance(ALGORITHMS_SIGNATURE);
		signature.initSign(priKey);
		signature.update(content.getBytes(StandardCharsets.UTF_8));

		byte[] signed = signature.sign();

		return new String(Base64.getEncoder().encode(signed));
	}

	public static boolean verifyWithPublicKey(String content, String sign, String publicKey) throws Exception {
		PublicKey pubKey = getPublicKey(publicKey);

		Signature signature = Signature.getInstance(ALGORITHMS_SIGNATURE);
		signature.initVerify(pubKey);
		signature.update(content.getBytes(StandardCharsets.UTF_8));

		return signature.verify(Base64.getDecoder().decode(sign));
	}
}
