package com.zhongtai.pidata;

import com.google.gson.JsonObject;
import com.zhongtai.entity.PIPoints;
import com.zhongtai.mqtt.Device;
import com.zhongtai.mqtt.MessageBuilder;
import com.zhongtai.mqtt.MqttMain;
import com.zhongtai.redis.RedisUtils;
import com.zhongtai.util.DateUtils;
import com.zhongtai.util.IntialConfig;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.*;

public class PIData {

    private static Logger log = Logger.getLogger(PIData.class);

    private static Connection connection = PIConnection.getPIConnection();
    private static Map<String, String> tagPlatform = IntialConfig.getTagPlatform();//PIPOINT对应设备编码和平台点位名集合
    private static String tags = IntialConfig.readTags();//所有的PIPOINT

    //获得上一次数据最后更新时间
    public static String getPreviousLastTime(){
        String previousLastTime = RedisUtils.getRedis("previousLastTime");
        if(previousLastTime == null){
            previousLastTime = DateUtils.getCurrentDateTime();
        }
        return  previousLastTime;
    }

    public static void reflushPIPoint(String tags){
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try{
            String previousLastTime = getPreviousLastTime();
            String lastTime = DateUtils.getCurrentDateTime();
            log.info("-------------------previousLastTime:" + previousLastTime);
            log.info("-------------------lastTime:" + lastTime);

            String sql = "select tag,time,value from piarchive..picomp2 where tag in (" + tags + ") and time > '" + previousLastTime
                    + "' and time <= '" + lastTime + "'";
            log.info("------------------sql:" + sql);
            pStatement = connection.prepareStatement(sql);
            resultSet = pStatement.executeQuery();

            while(resultSet.next()) {
                try{
                    String tag = resultSet.getString(1);
                    String time = resultSet.getString(2);
                    String value = resultSet.getString(3);
                    PIPoints piPoints = new PIPoints();
                    piPoints.setPointName(tag);
                    piPoints.setPointTime(time);
                    piPoints.setPointValue(value);
                    log.info("tag:" + tag);
                    log.info("tagPlatform.get(tag)" + tagPlatform.get(tag));
                    String[] platform = null;
                    try{
                        platform = tagPlatform.get(tag).split(",");
                    }catch (Exception e){
                        log.info("没有此点位：" + tag + e.getMessage());
                        continue;
                    }
                    String clientUuid = platform[0];
                    String platformPointName = platform[1];
                    piPoints.setClientUuid(clientUuid);
                    piPoints.setPlatformPointName(platformPointName);

                    log.info("-----------------------piPoint:" + piPoints.toString());
                    try{
                        MqttMain.publishPIPoint(piPoints);
                    }catch (Exception e){
                        log.error("mqtt发送数据失败：" + e.getMessage());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    log.error(e.getMessage());
                }
            }

            RedisUtils.insertRedis("previousLastTime", lastTime);//更新最后数据刷新时间
            log.info("更新previousLastTime最新时间为：" + RedisUtils.getRedis("previousLastTime"));
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }

                if (pStatement != null) {
                    pStatement.close();
                }

            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
    }

    public static void start() {
        while(true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.info(e.getMessage());
            }
            long start = System.currentTimeMillis();
            reflushPIPoint(tags);
            log.info("刷新一遍耗时：" + (System.currentTimeMillis() - start) + "ms");
        }
    }

    public static void main(String[] args) {
        while(true){
            System.out.println(new Random().nextInt(3));
        }
    }
}
