package com.cms.util;

import java.io.UnsupportedEncodingException;

//import org.junit.Before;
//import org.junit.Test;

import java.security.Key;
import java.util.Map;

//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;

/**
 * Created by lake on 17-4-12.
 */
public class RSACoderApp {
	// private String publicKey;
	// private String privateKey;
	// 公钥:

	private String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCDmfMCN1+3TXLbnW03ga8hTcFYbdaJM/TUhMwZ4ykrzTGRKjGDsiArfpV6p5pQcaeCusmK6fm2VdtKe54D/yUmIsQrNo3P4OYVNWuAmr5paZbutN4KxDtLHpcfFQCfzT1NaAdxVO6Gx0DAV/eTj90nYTeyh+CTNxyQqBynoY56+wIDAQAB";
//    	私钥： 

	private static String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIOZ8wI3X7dNctudbTeBryFNwVht1okz9NSEzBnjKSvNMZEqMYOyICt+lXqnmlBxp4K6yYrp+bZV20p7ngP/JSYixCs2jc/g5hU1a4Cavmlplu603grEO0selx8VAJ/NPU1oB3FU7obHQMBX95OP3SdhN7KH4JM3HJCoHKehjnr7AgMBAAECgYBm+onI+zHACy/MoGS9YZJ8Og8ItnKBTJHR1tSrkTE/YELgOCckybcYseYgY5SCTF1rE+Tv+eT4hDikERV2RSp4jMrsCXuxH8Mbu1BfP6JQB1iUWz2r9QV4cw7tzC5lCcaoBUBk49cxS/lgvc+QPpsOvSPXXxJLkGJbgzDmafaJMQJBALhg5neuDxIswtJFfchoWFoo3a//4QYYwvLEuJb3/085uFg+WiavhHsDw9w+wIHEz4b3dIQrDrZOiDhGDMxkmmMCQQC2uL7uNWqPYB0tK6ObH3Y7o+aUJoE6OLqEVN1ViIxnaYNXQfBeDZ+Ttse+KvBtNZqg2JpKU4St8SlZzAYhIXSJAkEAiIVHYOqCwkReZO+LeS657NhBfaYLakY/Yx6CR1aNto3Yj45rliV0BNn+1j+oru2ZdCgaIBr5o8d2WaSUqWr8XQJAOGwR4SKzdfR67fDUw1jSvJXe5I6DRwQvbB98fSX+HQyy1uXwoEpapVVClpFRzpdH2TUpZ5wkk0WtvCHJHr/i6QJADlR/9pRb2NDR/psU9Ss+8PfLZvrfulIvjHnRfIMqMNDISBg0d7D+cGh8r1zUn5vMDHDAGM9bSa+irRjiun9hxQ==";

	// @Before
	public void setUp() throws Exception {
		Map<String, Key> keyMap = RSACoder.initKey();
		publicKey = RSACoder.getPublicKey(keyMap);
		privateKey = RSACoder.getPrivateKey(keyMap);
		System.err.println("公钥: \n\r" + publicKey);
		System.err.println("私钥： \n\r" + privateKey);
	}

	// @Test
	public void test() throws Exception {
		System.err.println("公钥加密——私钥解密");
		String inputStr = "robin";
		byte[] encodedData = RSACoder.encryptByPublicKey(inputStr, publicKey);
		byte[] decodedData = RSACoder.decryptByPrivateKey(encodedData, privateKey);
		String outputStr = new String(decodedData);
		System.err.println("加密后: " + RSACoder.encryptBASE64(encodedData) );
		System.err.println("加密前: " + inputStr + "\n\r" + "解密后: " + outputStr);
		// assertEquals(inputStr, outputStr);
	}

	// @Test
	public void testSign() throws Exception {
		System.err.println("私钥加密——公钥解密");
		String inputStr = "dounine";
		byte[] data = inputStr.getBytes();
		byte[] encodedData = RSACoder.encryptByPrivateKey(data, privateKey);
		byte[] decodedData = RSACoder.decryptByPublicKey(encodedData, publicKey);
		String outputStr = new String(decodedData);
		System.err.println("加密前: " + inputStr + "\n\r" + "解密后: " + outputStr);
		// assertEquals(inputStr, outputStr);
		System.err.println("私钥签名——公钥验证签名");
		// 产生签名
		String sign = RSACoder.sign(encodedData, privateKey);
		System.err.println("签名:" + sign);
		// 验证签名
		boolean status = RSACoder.verify(encodedData, publicKey, sign);
		System.err.println("状态:" + status);
		// assertTrue(status);
	}
	public static String decrypt(String str) {
		String s = "";
		try {
			s= RSACoder.decryptByPrivK(str,privateKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}

	public static void main(String[] args) throws Exception {
		//String data="AojRzZ7O/pvGAI5bS5wMPyxNVn/lhA/3/WgVZAVXNbv+xvIujFNhSb8uvTMR+Rt21NHPOFPpJxAGJITVIDM0dyIq8mo3XWfriv/qeB56HOSLs6yvoWGBOaxEo4xFHow+p4Kj1sJpUwX4ocG3sCzf8QZkbcFAV6EMvc7TYJocWyI=";
		String data="XjqdUA/Wb4tAfezO9x6nAfFhOrDIA1lMrL2SCwfde9Nb2cj/haamFSV45+SCa257DZqQo8lG/e00tbbfPN5b3npwRkvHgCK8ysHTuBzBjqeA6+SvMg7EMpdJj2nZXZ7ZrXFFZxKRfMUscmkix6FSUcHT/y6ubC42DyGgR73f7ec=";
		String origin = RSACoderApp.decrypt(data);
		System.out.println(origin);
		//RSACoderApp app = new RSACoderApp();
		//app.test();
	}
}
