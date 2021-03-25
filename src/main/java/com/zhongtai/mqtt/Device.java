package com.zhongtai.mqtt;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.zhongtai.tool.HttpClient;
import com.zhongtai.tool.HttpResult;
import com.zhongtai.tool.RSA;
import com.zhongtai.tool.SSL;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.io.InputStream;
import java.util.Base64;

public class Device {
	private static Logger log = Logger.getLogger(Device.class);
	public static final String TOPIC_PREFIX_GATEWAY = "$LEAP/";
	public static final String TOPIC_PREFIX_SUBDEVICE = "$LEAP/gateway/";
	
	private static final String DEVICE_REGISTER_URI = "/edgeconnect/api/device/register";
	private static final String DEVICE_EXCHANGE_URI = "/edgeconnect/api/device/exchange";

	private Object lock = new Object();
	private String platform, namespace, productKey, accessKey;
	private MqttAsyncClient mqttClient;
	private MessageProcessor messageProcessor;
	private String deviceCertificate, verifyTextSignature, subdeviceCertificate;
	
	public String getPlatform() {
		return platform;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getProductKey() {
		return productKey;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public String getVerifyTextSignature() {
		return verifyTextSignature;
	}

	public String getSubdeviceCertificate() {
		return subdeviceCertificate;
	}

	public MessageProcessor getMessageProcessor() {
		return messageProcessor;
	}

	private MqttCallback mqttCallback = new MqttCallback() {

		public void connectionLost(Throwable cause) {
			System.out.println("disconnected from mqtt server");
		}

		// 处理平台返回的设备消息
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			// TODO Auto-generated method stub
			System.out.println("Received:");
			System.out.println(topic);
			System.out.println(new String(message.getPayload()));

			try {
				if (topic.startsWith(TOPIC_PREFIX_GATEWAY) && !topic.startsWith(TOPIC_PREFIX_SUBDEVICE)) {
					topic = topic.substring(TOPIC_PREFIX_GATEWAY.length());
					String[] topicInfo = topic.split("/");
					
					// 网关（直连设备）消息处理
					JsonObject response;
					String response_topic;

					switch (topicInfo[4]) {
					case "ntp":
						// 时间同步服务响应消息
						if (topicInfo[5].equals("response")) {
							messageProcessor.processNtpResponseMessage(topic, new String(message.getPayload()));
						}
						break;
					case "verify":
						// 设备校验平台的响应消息
						if (topicInfo[5].equals("response")) {
							verifyTextSignature = messageProcessor.processDeviceVerifyResponseMessage(topic, new String(message.getPayload()));
							unlock();
						}
						break;
					case "event":
						// 设备事件上报的响应消息
						if (topicInfo[5].equals("response")) {
						}
						break;
					case "task":
						if (topicInfo[5].equals("post")) {
							// 设备任务下发消息
							response = messageProcessor.processDeviceTaskPostMessage(topic, new String(message.getPayload()));
							response_topic = messageProcessor.buildDeviceMessageTopic() + "/task/post/response";
							publish(response_topic, response.toString().getBytes());
						} else if (topicInfo[6].equals("status")) {
							// 设备任务状态查询消息
							messageProcessor.processDeviceTaskQueryMessage(topic, new String(message.getPayload()));
						}
						break;
					}
				}
			} catch (Exception e) {
				System.out.println("failed to process device message");
				System.out.println(new String(message.getPayload()));
			}
		}

		public void deliveryComplete(IMqttDeliveryToken token) {
			// TODO Auto-generated method stub
		}
	};

	public Device() {
	}

	public void lock() {
		try {
			synchronized (lock) {
				lock.wait(10000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void unlock() {
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	public String register(String registerServer, String clientUuid, String deviceName, String productCertificate) throws Exception {
		JsonObject params = new JsonObject();
		params.addProperty("certificate", productCertificate);
		params.addProperty("clientUuid", clientUuid);
		params.addProperty("deviceName", deviceName);
		HttpResult response = HttpClient.Post(registerServer + DEVICE_REGISTER_URI, params.toString());
		if (response.getStatus_code() == 200) {
			this.deviceCertificate = response.getResult();
		} else if (response.getStatus_code() == 430 && new JsonParser().parse(response.getResult()).getAsJsonObject().get("code").getAsInt() == 20010) {
			response = HttpClient.Post(registerServer + DEVICE_EXCHANGE_URI, params.toString());
			this.deviceCertificate = response.getResult();
		} else {
			this.deviceCertificate = null;
			throw new RuntimeException(response.getResult());
		}
		
		return this.deviceCertificate;
	}

	public void connect2Mqtt(String mqttBroker, String certificateContent) throws Exception {
		JsonObject cer = parseCertificate(certificateContent).getAsJsonObject();

		String version = cer.get("version").getAsString();
		String productType = cer.get("productType").getAsString();
		platform = cer.get("platform_id").getAsString();
		namespace = cer.get("namespace").getAsString();
		productKey = cer.get("productKey").getAsString();
		accessKey = cer.get("accessKey").getAsString();
		String secretKey = cer.get("secretKey").getAsString();
		String clientId = version + "." + productType + "." + platform + "." + namespace + "." + productKey + "." + accessKey;

		MqttConnectOptions conOpt = new MqttConnectOptions();
		conOpt.setAutomaticReconnect(false);
		conOpt.setCleanSession(true);
		conOpt.setUserName(accessKey);
		conOpt.setPassword(secretKey.toCharArray());
		conOpt.setKeepAliveInterval(60);

		if (mqttBroker.startsWith("ssl")) {
			InputStream crt = this.getClass().getClassLoader().getResourceAsStream("cacert.cer");
			conOpt.setSocketFactory(new SSL().getSSLSocktet(crt));
			conOpt.setHttpsHostnameVerificationEnabled(false);
		}

		String tmpDir = System.getProperty("java.io.tmpdir");
		MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);

		// Construct an MQTT with asynchronous mode
		mqttClient = new MqttAsyncClient(mqttBroker, clientId, dataStore);
		mqttClient.setCallback(mqttCallback);
		mqttClient.connect(conOpt, this, new IMqttActionListener() {

			@Override
			public void onSuccess(IMqttToken asyncActionToken) {
				messageProcessor = new MessageProcessor(platform, namespace, productKey, accessKey);
				System.out.println("connected with mqtt server:" + mqttBroker);
			}

			@Override
			public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
				System.out.println("failed to connect to mqtt server:" + mqttBroker);
			}

		}).waitForCompletion(-1);

		// 订阅平台发送给设备的消息
		String topic1 = String.format(TOPIC_PREFIX_GATEWAY + "%s/%s/%s/%s/+/+", this.platform, this.namespace, this.productKey, this.accessKey);
		String topic2 = String.format(TOPIC_PREFIX_SUBDEVICE + "%s/%s/%s/%s/+/+", this.platform, this.namespace, this.productKey, this.accessKey);
		mqttClient.subscribe(new String[] { topic1, topic2 }, new int[] { 1, 1 });
	}
	
	public boolean verify(String text) {
		if (this.verifyTextSignature == null) {
			System.out.println("failed to get signature from server");
			return false;
		}
		
		try {
			JsonObject cer = parseCertificate(this.deviceCertificate);
			String publicKey = cer.get("pubkey").getAsString();
			return RSA.verifyWithPublicKey(text, this.verifyTextSignature, publicKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	public JsonObject parseCertificate(String content) throws Exception {
		content = content.replaceAll("\n", "");
		content = content.replaceAll("\t", "");
		content = content.replaceAll("\r", "");
		content = content.replace(System.getProperty("line.separator"), "");

		String plainText = null;
		try {
			plainText = new String(Base64.getDecoder().decode(content.getBytes()));
		} catch (Exception e) {
			throw new IllegalArgumentException("invalid device certificate format");
		}

		JsonObject cer;
		try {
			cer = new JsonParser().parse(plainText).getAsJsonObject();
		} catch (JsonSyntaxException e) {
			throw new IllegalArgumentException("invalid device certificate format");
		}

		return cer;
	}

	public boolean publish(String topic, byte[] payload) {
		log.info("Published:");
		log.info(topic);
		log.info(new String(payload));
//		System.out.println("Published:");
//		System.out.println(topic);
//		System.out.println(new String(payload));

		try {
			MqttMessage mqttMessage = new MqttMessage(payload);
			mqttMessage.setQos(0);
			mqttMessage.setRetained(true);
			this.mqttClient.publish(topic, mqttMessage);

			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return false;
	}
}