package com.zhongtai.tool;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;

public class HttpClient {
	public static HttpResult Post(final String uri, final String data) throws Exception {
		// 忽略 HTTPS 合法性校验
		HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
		SSLContext sc = SSLContext.getInstance("TLS");
		sc.init(null, new TrustManager[] { new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) {
				// TODO Auto-generated method stub
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) {
				// TODO Auto-generated method stub
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				// TODO Auto-generated method stub
				return null;
			}
		} }, new SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		URL url = new URL(uri);

		byte[] postData = data.getBytes(StandardCharsets.UTF_8);
		int postDataLength = postData.length;
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setInstanceFollowRedirects(false);
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("charset", "utf-8");
		conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");
		conn.connect();

		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.write(postData);
		wr.close();

		String response = "";
		InputStream inStream;
		int status_code = conn.getResponseCode();
		if (status_code == HttpURLConnection.HTTP_OK) {
			inStream = conn.getInputStream();
		} else {
			inStream = conn.getErrorStream();
		}
		if (inStream != null) {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len;
			while ((len = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, len);
			}
			inStream.close();

			byte[] res = outStream.toByteArray();
			response = new String(res);
		}

		conn.disconnect();
		
		HttpResult result = new HttpResult();
		result.setStatus_code(status_code);
		result.setResult(response);
		
		return result;
	}

	public static void testConnectionTo(String url) throws Exception {
		URL destinationURL = new URL(url);
		HttpsURLConnection conn = (HttpsURLConnection) destinationURL.openConnection();
		conn.connect();
		Certificate[] certs = conn.getServerCertificates();
		System.out.println("nb = " + certs.length);
		int i = 1;
		for (Certificate cert : certs) {
			System.out.println("Certificate is: " + cert);
			if(cert instanceof X509Certificate) {
				try {
					((X509Certificate)cert).checkValidity();
					System.out.println("Certificate is active for current date");
					FileOutputStream os = new FileOutputStream("D:\\myCert"+i);
					i++;
					os.write(cert.getEncoded());
				} catch(CertificateExpiredException cee) {
					System.out.println("Certificate is expired");
				}
			} else {
				System.err.println("Unknown certificate type: " + cert);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		testConnectionTo("ssl://172.17.170.224:9993");
	}
}
