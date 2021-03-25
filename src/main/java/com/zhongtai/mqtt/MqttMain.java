package com.zhongtai.mqtt;

import com.google.gson.JsonObject;
import com.zhongtai.entity.MqttConfig;
import com.zhongtai.entity.PIPoints;
import com.zhongtai.util.IntialConfig;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MqttMain {

	private static Logger log = Logger.getLogger(MqttMain.class);
	private static Map<String, Device> clientUuid_device = IntialConfig.getclientUuidDevice();

	public static void publishPIPoint(PIPoints piPoints){
		String clientUuid = piPoints.getClientUuid();
		Device device = clientUuid_device.get(clientUuid);

		String platform = device.getPlatform();
		String namespace = device.getNamespace();
		String productKey = device.getProductKey();
		String accessKey = device.getAccessKey();
		MessageBuilder messageBuilder = new MessageBuilder(platform, namespace, productKey, accessKey);

		// TODO Auto-generated method stub
		String topic = messageBuilder.buildDeviceMessageTopic() + "/data";
		JsonObject msg = messageBuilder.buildDeviceDataMessage(piPoints);
		device.publish(topic, msg.toString().getBytes());
	}
}
