package com.zhongtai.entity;

import lombok.Data;

@Data
public class PIPoints {

    //点位名
    private String pointName;

    //更新时间
    private String pointTime;

    //最新值
    private String pointValue;

    //设备编码
    private String clientUuid;

    //平台上的点位名
    private String platformPointName;
}
