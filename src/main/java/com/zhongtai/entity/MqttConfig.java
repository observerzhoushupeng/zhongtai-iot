package com.zhongtai.entity;

import lombok.Data;

@Data
public class MqttConfig {

    //IOT平台地址
    private String server;

    //mqtt地址
    private String broker;

    //设备编码标识
    private String clientuuid;

    //设备名称
    private String devicename;

    //产品证书
    private String productCertificate;
}
