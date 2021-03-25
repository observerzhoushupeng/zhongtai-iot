package com.zhongtai.tool;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class SSL {
	//echo -n | openssl s_client -connect 223.203.218.93:8883 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > /tmp/mqttserver.crt
	//public static final String CA_PATH = "server.crt";
	//public static final String CRT_PATH = "client-cert.crt";
	//public static final String KEY_PATH = "client-key-pkcs8.pem";
	//public static final String PASSWORD = "";
	
	public SSL() {}

	public SSLSocketFactory getSSLSocktet(InputStream serverCertificate) throws Exception {
		// CA certificate is used to authenticate server
		//FileInputStream caIn = new FileInputStream(serverCertificate);
		X509Certificate ca = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(serverCertificate);
		KeyStore trustStore = KeyStore.getInstance("JKS");
		trustStore.load(null, null);
		trustStore.setCertificateEntry("ca-certificate", ca);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
		tmf.init(trustStore);
		
		SSLContext context = SSLContext.getInstance("TLS");
		//context.init(null,  tmf.getTrustManagers(), new SecureRandom());
		
		// Get hold of the default trust manager
		X509TrustManager x509Tm = null;
		for (TrustManager tm : tmf.getTrustManagers()) {
		    if (tm instanceof X509TrustManager) {
		        x509Tm = (X509TrustManager) tm;
		        break;
		    }
		}
		
		final X509TrustManager finalTm = x509Tm;
		X509TrustManager customTm = new X509TrustManager() {
		    @Override
		    public X509Certificate[] getAcceptedIssuers() {
		        return finalTm.getAcceptedIssuers();
		    }

		    @Override
		    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		    	//不验证服务器证书
		        //finalTm.checkServerTrusted(chain, authType);
		    }

		    @Override
		    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		        finalTm.checkClientTrusted(chain, authType);
		    }
		};
		
		// create SSL socket factory
		context.init(null,  new TrustManager[] { customTm }, new SecureRandom());


		return context.getSocketFactory();
	}
	public SSLSocketFactory getSSLSocktet(File serverCertificate) throws Exception {		
		return getSSLSocktet(new FileInputStream(serverCertificate));
	}

	public SSLSocketFactory getSSLSocktetBidirectional(File serverCertificate, File clientCertificate, File clientKey, String clientKeyPass) throws Exception {
		// CA certificate is used to authenticate server
		CertificateFactory cAf = CertificateFactory.getInstance("X.509");
		FileInputStream caIn = new FileInputStream(serverCertificate);
		X509Certificate ca = (X509Certificate) cAf.generateCertificate(caIn);
		KeyStore caKs = KeyStore.getInstance("JKS");
		caKs.load(null, null);
		caKs.setCertificateEntry("ca-certificate", ca);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
		tmf.init(caKs);

		// client key and certificates are sent to server so it can authenticate us
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		FileInputStream crtIn = new FileInputStream(clientCertificate);
		X509Certificate caCert = (X509Certificate) cf.generateCertificate(crtIn);
		crtIn.close();
		
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null, null);
		ks.setCertificateEntry("certificate", caCert);
		ks.setKeyEntry("private-key", getPrivateKey(clientKey), clientKeyPass.toCharArray(), new java.security.cert.Certificate[] { caCert });
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("PKIX");
		kmf.init(ks, clientKeyPass.toCharArray());

		// finally, create SSL socket factory
		SSLContext context = SSLContext.getInstance("TLSv1");
		context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

		return context.getSocketFactory();
	}

	private PrivateKey getPrivateKey(File file) throws Exception {
		byte[] buffer = Base64.getDecoder().decode(getPem(file));

		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePrivate(keySpec);

	}

	private String getPem(File file) throws Exception {
		FileInputStream fin = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fin));
		String readLine = null;
		StringBuilder sb = new StringBuilder();
		while ((readLine = br.readLine()) != null) {
			if (readLine.charAt(0) == '-') {
				continue;
			} else {
				sb.append(readLine);
				sb.append('\r');
			}
		}
		fin.close();
		return sb.toString();
	}
}
