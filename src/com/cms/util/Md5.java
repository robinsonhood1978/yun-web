package com.cms.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.jfinal.core.JFinal;
/**
 * MD5密码加密
 * 
 * @author avic
 * 
 */
public class Md5 {
	public static String encodePassword(String rawPass) {
		return encodePassword(rawPass, defaultSalt);
	}

	public static String encodePassword(String rawPass, String salt) {
		String saltedPass = mergePasswordAndSalt(rawPass, salt, false);
		MessageDigest messageDigest = getMessageDigest();
		byte[] digest;
		try {
			digest = messageDigest.digest(saltedPass.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("UTF-8 not supported!");
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < digest.length; i++) {
			int val = ((int) digest[i]) & 0xff;
			if (val < 16)
			sb.append("0");
			sb.append(Integer.toHexString(val));
		}
		return sb.toString();
	}

	public static boolean isPasswordValid(String encPass, String rawPass) {
		return isPasswordValid(encPass, rawPass, defaultSalt);
	}

	public static boolean isPasswordValid(String encPass, String rawPass, String salt) {
		if (encPass == null) {
			return false;
		}
		String pass2 = encodePassword(rawPass, salt);
		return encPass.equals(pass2);
	}

	protected final static MessageDigest getMessageDigest() {
		String algorithm = "MD5";
		try {
			return MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("No such algorithm ["
					+ algorithm + "]");
		}
	}
	protected static String mergePasswordAndSalt(String password, Object salt,
			boolean strict) {
		if (password == null) {
			password = "";
		}
		if (strict && (salt != null)) {
			if ((salt.toString().lastIndexOf("{") != -1)
					|| (salt.toString().lastIndexOf("}") != -1)) {
				throw new IllegalArgumentException(
						"Cannot use { or } in salt.toString()");
			}
		}
		if ((salt == null) || "".equals(salt)) {
			return password;
		} else {
			return password + "{" + salt.toString() + "}";
		}
	}
	public static void main(String[] args) {
		System.out.println(Md5.encodePassword("53004d00-4400-4b00-3600-3400310030000000534d444b363431303000"));
	}
	
	/**
	 * 混淆码。防止破解。
	 */
	private static String defaultSalt="Manage";
}
