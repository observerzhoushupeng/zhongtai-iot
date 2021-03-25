package com.zhongtai.mqtt;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zhongtai.entity.PIPoints;
import com.zhongtai.entity.PITimes;
import com.zhongtai.mysql.MysqlUtil;
import com.zhongtai.redis.RedisPoolUtil;
import com.zhongtai.redis.RedisUtils;
import com.zhongtai.util.DateUtils;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

public class MessageBuilder {

	private static Logger log = Logger.getLogger(MessageBuilder.class);

	private static String DeviceDataPointPath = "conf/DeviceDataPoint.conf";

	private Random R = new Random();
	private AtomicLong sequenceId = new AtomicLong(0);
	private String platform, namespace, productKey, clientUuid;

	public MessageBuilder(String platform, String namespace, String productKey, String clientUuid) {
		this.platform = platform;
		this.namespace = namespace;
		this.productKey = productKey;
		this.clientUuid = clientUuid;
	}

	public String buildDeviceMessageTopic() {
		return Device.TOPIC_PREFIX_GATEWAY + platform + "/" + namespace + "/" + productKey + "/" + clientUuid;
	}

	public String buildSubdeviceMessageTopic() {
		return Device.TOPIC_PREFIX_SUBDEVICE + platform + "/" + namespace + "/" + productKey + "/" + clientUuid;
	}

	public JsonObject buildDeviceVerifyMessage(String text) {
		JsonObject result = new JsonObject();
		result.addProperty("version", 1);
		result.addProperty("sequenceId", sequenceId.incrementAndGet());
		result.addProperty("payload", text);

		return result;
	}

	// 设备基本信息消息
	public JsonObject buildNtpMessage() {
		JsonObject result = new JsonObject();
		result.addProperty("version", 1);
		result.addProperty("sequenceId", sequenceId.incrementAndGet());
		result.addProperty("deviceSendTime", System.currentTimeMillis());

		return result;
	}
	
	// 设备基本信息消息
	public JsonObject buildDeviceInfoMessage() {
		JsonObject result = new JsonObject();
		result.addProperty("version", 1);
		result.addProperty("sequenceId", sequenceId.incrementAndGet());
		result.addProperty("edgeTime", System.currentTimeMillis());
		result.addProperty("event", "DEVICEINFO");

		JsonObject eventPayload = new JsonObject();
		eventPayload.addProperty("cpu", "Intel Core Processor (Broadwell)");
		eventPayload.addProperty("memory", 4096);
		eventPayload.addProperty("storage", 120000);
		eventPayload.addProperty("AAAAA", "状态更新列表");
		eventPayload.addProperty("BBBBB", "你当前所在位置：设备作业>状态更新列表");
		eventPayload.addProperty("CCCCC", "配置信息");
		result.add("eventPayload", eventPayload);

		return result;
	}

	// 设备软件模块消息
	public JsonObject buildDeviceFirmwareMessage() {
		JsonObject result = new JsonObject();
		result.addProperty("version", 1);
		result.addProperty("sequenceId", sequenceId.incrementAndGet());
		result.addProperty("edgeTime", System.currentTimeMillis());
		result.addProperty("event", "FIRMWARE");

		JsonArray eventPayload = new JsonArray();
		JsonObject app1 = new JsonObject();
		app1.addProperty("appKey", "QQ");
		app1.addProperty("version", "2.0");
		eventPayload.add(app1);
		JsonObject app2 = new JsonObject();
		app2.addProperty("appKey", "Wechat");
		app2.addProperty("version", "6.0");
		eventPayload.add(app2);
		result.add("eventPayload", eventPayload);

		return result;
	}

	// 设备资源占用状态消息
	public JsonObject buildDeviceStatusMessage() {
		JsonObject result = new JsonObject();
		result.addProperty("version", 1);
		result.addProperty("sequenceId", sequenceId.incrementAndGet());
		result.addProperty("edgeTime", System.currentTimeMillis());
		result.addProperty("event", "DEVICESTATUS");

		JsonObject eventPayload = new JsonObject();
		eventPayload.addProperty("cpu", 5);
		eventPayload.addProperty("memory", 3072);
		eventPayload.addProperty("storage", 90000);
		result.add("eventPayload", eventPayload);

		return result;
	}

	// 设备扩展消息
	public JsonObject buildDeviceExtentionMessage() {
		JsonObject result = new JsonObject();
		result.addProperty("version", 1);
		result.addProperty("sequenceId", sequenceId.incrementAndGet());
		result.addProperty("edgeTime", System.currentTimeMillis());
		result.addProperty("event", "EXTENTION");

		JsonObject eventPayload = new JsonObject();
		eventPayload.addProperty("action", "ADD");
		
		JsonArray items = new JsonArray();
		JsonObject item1 = new JsonObject();
		item1.addProperty("key", "channel1");
		item1.addProperty("property1", "属性值1");
		item1.addProperty("property2", "属性值2");
		items.add(item1);
		JsonObject item2 = new JsonObject();
		item2.addProperty("key", "channel2");
		item2.addProperty("property1", "属性值3");
		item2.addProperty("property2", "属性值4");
		items.add(item2);
		
		result.add("eventPayload", eventPayload);

		return result;
	}

	// 设备事件消息
	public JsonObject buildDeviceEventMessage() {
		JsonObject result = new JsonObject();
		result.addProperty("version", 1);
		result.addProperty("sequenceId", sequenceId.incrementAndGet());
		result.addProperty("edgeTime", System.currentTimeMillis());
		result.addProperty("event", "DEVICEALARM");

		JsonObject eventPayload = new JsonObject();
		eventPayload.addProperty("type", "ERROR");
		eventPayload.addProperty("code", "100001"); //异常代码由设备自定义
		eventPayload.addProperty("message", "这是一个设备异常信息" + System.currentTimeMillis());
		result.add("eventPayload", eventPayload);

		return result;
	}

	// 设备点位值消息
	public JsonObject buildDeviceDataMessage(PIPoints piPoints) {
		String pointTime = piPoints.getPointTime();
		String platformPointName = piPoints.getPlatformPointName();
		String pointValue = piPoints.getPointValue();
		String clientUuid = piPoints.getClientUuid();
		long timeMs = DateUtils.getMsTimeByDateTime(pointTime);

		JsonObject result = new JsonObject();
		result.addProperty("version", 1);
		result.addProperty("sequenceId", sequenceId.incrementAndGet());
		result.addProperty("clientUuid", clientUuid);
		result.addProperty("edgeTime", timeMs/1000);

		JsonArray reported = new JsonArray();
		JsonObject data = new JsonObject();

		data.addProperty(platformPointName, pointValue);
		data.addProperty("ts", timeMs);//时间秒转为毫秒

		reported.add(data);

		result.add("reported", reported);
		return result;
	}

	public static List<String> getPoints(String clientUuid){
		File file = new File(DeviceDataPointPath);
		FileInputStream fis = null;
		List<String> list = new ArrayList<>();
		try{
			fis = new FileInputStream(file);
			Scanner scanner = new Scanner(fis,"utf-8");
			scanner.nextLine();
			while(scanner.hasNext()){
				String line = scanner.nextLine();
				String[] lines = line.split("\t");
				if(lines[0].equals(clientUuid))list.add(lines[1] + "," + lines[2]);
			}
		}catch (FileNotFoundException e){
			log.error(e.getMessage());
		}finally {
			try{
				fis.close();
			}catch (IOException e){
				log.error(e.getMessage());
			}
		}
		return list;
	}
}
