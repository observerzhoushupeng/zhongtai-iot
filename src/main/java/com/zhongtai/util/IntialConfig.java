package com.zhongtai.util;

import com.google.gson.JsonObject;
import com.zhongtai.entity.MqttConfig;
import com.zhongtai.mqtt.Device;
import com.zhongtai.mqtt.MessageBuilder;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class IntialConfig {

    private static Logger log = Logger.getLogger(IntialConfig.class);
    private static String mqttConfigPath = "conf/mqtt.conf";
    private static String DeviceDataPointPath = "conf/DeviceDataPoint.conf";

    //获得所有POPINT点位
    public static String readTags(){
        File file = new File(DeviceDataPointPath);
        FileInputStream fis = null;
        String result = "";
        try{
            fis = new FileInputStream(file);
            Scanner scanner = new Scanner(fis,"utf-8");
            scanner.nextLine();
            while(scanner.hasNext()){
                String line = scanner.nextLine();
                String[] lines = line.split("\t");
                String tag = lines[2];
                result = result + "'" + tag + "',";
            }
            result = result.substring(0, result.length()-1);
        }catch (FileNotFoundException e){
            log.error(e.getMessage());
        }finally {
            try{
                fis.close();
            }catch (IOException e){
                log.error(e.getMessage());
            }
        }
        return result;
    }

    //获得PIPOINT点位对应的设备编码和平台点位名集合
    public static Map<String, String> getTagPlatform(){
        File file = new File(DeviceDataPointPath);
        FileInputStream fis = null;
        Map<String, String> result = new HashMap<>();
        try{
            fis = new FileInputStream(file);
            Scanner scanner = new Scanner(fis,"utf-8");
            scanner.nextLine();
            while(scanner.hasNext()){
                String line = scanner.nextLine();
                String[] lines = line.split("\t");
                String clientUuid = lines[0];
                String platformPointName = lines[1];
                String tag = lines[2];
                result.put(tag, clientUuid + "," + platformPointName);
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
        return result;
    }

    //获得设备编码对应的设备证书集合
    public static Map<String, Device> getclientUuidDevice(){
        Map<String, Device> result = new HashMap();

        List<MqttConfig> mqttConfigs = readConfig();
        for(MqttConfig mqttConfig : mqttConfigs){
            String registerServer = mqttConfig.getServer();
            String mqttBroker = mqttConfig.getBroker();
            String clientUuid = mqttConfig.getClientuuid();
            String deviceName = mqttConfig.getDevicename();
            String productCertificate = mqttConfig.getProductCertificate();

            Device device = new Device();
            // 动态注册新设备，使用一型一密证书换取一机一密证书
            String deviceCertificate = null;
            try{
                deviceCertificate = device.register(registerServer, clientUuid, deviceName, productCertificate);
            }catch (Exception e){
                log.error("动态注册新设备失败。。。。。。。");
                log.error(e.getMessage());
            }
            if (deviceCertificate == null) {
                log.error("failed to get device certificate");
                continue;
            }

            // 使用一机一密证书连接平台
            try{
                device.connect2Mqtt(mqttBroker, deviceCertificate);
            }catch (Exception e){
                log.error("使用一机一密证书连接平台失败。。。。。");
                log.error(e.getMessage());
            }

//            String platform = device.getPlatform();
//            String namespace = device.getNamespace();
//            String productKey = device.getProductKey();
//            String accessKey = device.getAccessKey();
//            MessageBuilder messageBuilder = new MessageBuilder(platform, namespace, productKey, accessKey);

            // 上报认证平台消息 - 可选步骤
//            String verifyText = "The quick brown fox jumps over the lazy dog";
//            String topic = messageBuilder.buildDeviceMessageTopic() + "/verify";
//            JsonObject msg = messageBuilder.buildDeviceVerifyMessage(verifyText);
//            device.publish(topic, msg.toString().getBytes());
//            // 等待平台返回认证消息
//            device.lock();

//            boolean verifyResult = device.verify(verifyText);
//            if (verifyResult) {
//                log.info("server is valid!");
//            } else {
//                log.info("server is INVALID!!!");
//            }
//
//            // 调用时间同步服务，用以校正本地时间
//            topic = messageBuilder.buildDeviceMessageTopic() + "/ntp";
//            msg = messageBuilder.buildNtpMessage();
//            device.publish(topic, msg.toString().getBytes());
//
//            // 上报设备基本信息
//            topic = messageBuilder.buildDeviceMessageTopic() + "/event";
//            msg = messageBuilder.buildDeviceInfoMessage();
//            device.publish(topic, msg.toString().getBytes());
//
//            // 上报设备模块信息
//            msg = messageBuilder.buildDeviceFirmwareMessage();
//            device.publish(topic, msg.toString().getBytes());
//
//            // 上报设备资源占用信息
//            msg = messageBuilder.buildDeviceStatusMessage();
//            device.publish(topic, msg.toString().getBytes());
//
//            // 上报设备扩展信息
//            msg = messageBuilder.buildDeviceExtentionMessage();
//            device.publish(topic, msg.toString().getBytes());
//
//            // 上报设备事件
//            msg = messageBuilder.buildDeviceEventMessage();
//            device.publish(topic, msg.toString().getBytes());

            result.put(clientUuid, device);
        }

        return result;
    }

    //读取设备连接信息
    public static List<MqttConfig> readConfig(){
        List<MqttConfig> mqttConfigs = new ArrayList<>();
        File file = new File(mqttConfigPath);
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(file);
            Scanner scan = new Scanner(fis);
            while(scan.hasNext()){
                MqttConfig mqttConfig = new MqttConfig();
                String line = scan.nextLine();
                String[] configs = line.split(" ");
                mqttConfig.setServer( configs[0]);
                mqttConfig.setBroker(configs[1]);
                mqttConfig.setClientuuid(configs[2]);
                mqttConfig.setDevicename(configs[3]);
                mqttConfig.setProductCertificate(configs[4]);
                mqttConfigs.add(mqttConfig);
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }finally {
            try {
                fis.close();
            }catch (IOException e){
                log.error(e.getMessage());
            }
        }
        return mqttConfigs;
    }
}
