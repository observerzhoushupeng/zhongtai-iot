package com.zhongtai.mqtt;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageProcessor {
	private String platform, namespace, productKey, clientUuid;

	public MessageProcessor(String platform, String namespace, String productKey, String clientUuid) {
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

	// 处理平台返回的时间同步服务响应消息
	public String processNtpResponseMessage(String topic, String message) {
		JsonObject messageObj = new JsonParser().parse(message).getAsJsonObject();
		int code = messageObj.get("code").getAsInt();
		if (code == 200) {
			Long deviceSendTime = messageObj.get("edgeSendTime").getAsLong();
			Long serverRecvTime = messageObj.get("platRecvTime").getAsLong();
			Long serverSendTime = messageObj.get("platSendTime").getAsLong();
			Long deviceRecvTime = System.currentTimeMillis();
			
			Long actualTimestamp = System.currentTimeMillis();
			Long adjustedTimestamp = (serverRecvTime + serverSendTime + deviceRecvTime - deviceSendTime) / 2;
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
			System.out.println("Actual timestamp: " + sdf.format(new Date(actualTimestamp)));
			System.out.println("Adjusted timestamp: " + sdf.format(new Date(adjustedTimestamp)));
		}
		
		return null;
	}

	// 处理平台返回的验证响应消息
	public String processDeviceVerifyResponseMessage(String topic, String message) {
		JsonObject messageObj = new JsonParser().parse(message).getAsJsonObject();
		int code = messageObj.get("code").getAsInt();
		if (code == 200) {
			return messageObj.get("signature").getAsString();
		}
		
		return null;
	}

	// 处理平台下发的设备任务消息
	public JsonObject processDeviceTaskPostMessage(String topic, String message) {
		JsonObject messageObj = new JsonParser().parse(message).getAsJsonObject();
		Long sequenceId = messageObj.get("sequenceId").getAsLong();
		String taskId = messageObj.get("taskId").getAsString();
		String taskType = messageObj.get("type").getAsString();

		return buildDeviceTaskResponseMessage(sequenceId, taskId, taskType);
	}

	// 处理平台下发的设备任务状态查询消息
	public JsonObject processDeviceTaskQueryMessage(String topic, String message) {
		JsonObject messageObj = new JsonParser().parse(message).getAsJsonObject();
		Long sequenceId = messageObj.get("sequenceId").getAsLong();
		String taskId = messageObj.get("taskId").getAsString();
		String taskType = "OTA";

		return buildDeviceTaskResponseMessage(sequenceId, taskId, taskType);
	}

	private JsonObject buildDeviceTaskResponseMessage(Long sequenceId, String taskId, String taskType) {
		JsonObject result = new JsonObject();
		result.addProperty("sequenceId", sequenceId);
		result.addProperty("taskId", taskId);
		result.addProperty("edgeTime", System.currentTimeMillis());
		result.addProperty("type", taskType);
		result.addProperty("status", "SUCCEEDED");

		JsonObject description = new JsonObject();
		description.addProperty("key1", "value1");
		description.addProperty("key2", "value2");
		result.add("description", description);

		return result;
	}

	
	
	// 处理平台返回的子设备注册响应消息
	public String processSubdeviceRegisteResponseMessage(String topic, String message) {
		JsonObject messageObj = new JsonParser().parse(message).getAsJsonObject();
		int code = messageObj.get("code").getAsInt();
		if (code == 200) {
			return messageObj.get("certificate").getAsString();
		}
		
		return null;
	}

	// 处理平台返回的子设备换密响应消息
	public String processSubdeviceExchangeResponseMessage(String topic, String message) {
		JsonObject messageObj = new JsonParser().parse(message).getAsJsonObject();
		int code = messageObj.get("code").getAsInt();
		if (code == 200) {
			return messageObj.get("certificate").getAsString();
		}
		
		return null;
	}

	// 处理平台返回的子设备查询拓扑响应消息
	public void processSubdeviceGetTopoResponseMessage(String topic, String message) {
		JsonObject messageObj = new JsonParser().parse(message).getAsJsonObject();
		int code = messageObj.get("code").getAsInt();
		if (code == 200) {
			System.out.println("topology fetch result:");

			JsonArray devices = messageObj.get("devices").getAsJsonArray();
			for (JsonElement je : devices) {
				System.out.println(je.toString());
			}
		} else {
			System.out.println("failed to get topology");
		}
	}

	// 处理平台返回的子设备添加拓扑响应消息
	public void processSubdeviceAddTopoResponseMessage(String topic, String message) {
		JsonObject messageObj = new JsonParser().parse(message).getAsJsonObject();
		int code = messageObj.get("code").getAsInt();
		if (code == 200) {
			System.out.println("adding subdevice topology finished");
		} else {
			System.out.println("failed to add subdevice topology: " + code);
		}
	}
	
	// 处理平台返回的子设备上下线响应消息
	public void processSubdeviceOnlineResponseMessage(String topic, String message) {
		JsonObject messageObj = new JsonParser().parse(message).getAsJsonObject();
		int code = messageObj.get("code").getAsInt();
		if (code == 200) {
			System.out.println("subdevice online successfully");
		} else {
			System.out.println("subdevice online failure");
		}
	}

	// 处理平台下发的子设备任务消息
	public JsonObject processSubdeviceTaskPostMessage(String topic, String message) {
		JsonObject messageObj = new JsonParser().parse(message).getAsJsonObject();
		Long sequenceId = messageObj.get("sequenceId").getAsLong();
		String taskId = messageObj.get("taskId").getAsString();
		String taskType = messageObj.get("type").getAsString();
		String productKey = messageObj.get("productKey").getAsString();
		String clientUuid = messageObj.get("clientUuid").getAsString();

		JsonObject response = buildSubdeviceTaskResponseMessage(sequenceId, taskId, taskType);
		return response;
	}

	// 处理平台下发的子设备任务状态查询消息
	public JsonObject processSubdeviceTaskQueryMessageResponse(String topic, String message) {
		JsonObject messageObj = new JsonParser().parse(message).getAsJsonObject();
		Long sequenceId = messageObj.get("sequenceId").getAsLong();
		String taskId = messageObj.get("taskId").getAsString();

		JsonObject response = buildSubdeviceTaskResponseMessage(sequenceId, taskId, "OTA");
		return response;
	}

	// 子设备自动响应
	private JsonObject buildSubdeviceTaskResponseMessage(Long sequenceId, String taskId, String taskType) {
		JsonObject result = new JsonObject();
		result.addProperty("sequenceId", sequenceId);
		result.addProperty("taskId", taskId);
		result.addProperty("edgeTime", System.currentTimeMillis());
		result.addProperty("type", taskType);
		result.addProperty("status", "SUCCEEDED");

		JsonObject description = new JsonObject();
		description.addProperty("key1", "value1");
		description.addProperty("key2", "value2");
		result.add("description", description);

		return result;
	}
}
