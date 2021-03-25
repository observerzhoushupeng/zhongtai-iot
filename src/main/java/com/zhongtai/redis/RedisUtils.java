package com.zhongtai.redis;

import com.zhongtai.entity.PIPoints;
import com.zhongtai.entity.PITimes;
import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.Set;

public class RedisUtils {


    public static JedisCluster jedis;

    static {
        int a = 0;
        JedisPoolConfig i2 = new JedisPoolConfig();
        i2.setMaxTotal(-1);
        i2.setMinIdle(2);
        i2.setMaxIdle(-1);
        i2.setMaxWaitMillis(10000);
        i2.setTestOnBorrow(true);
        i2.setTestOnReturn(true);
        Set<HostAndPort> nodes = new HashSet<>();
        nodes.add(new HostAndPort("10.50.217.105", 6379));
        nodes.add(new HostAndPort("10.50.217.106", 6379));
        nodes.add(new HostAndPort("10.50.217.107", 6379));
        nodes.add(new HostAndPort("10.50.217.105", 6380));
        nodes.add(new HostAndPort("10.50.217.106", 6380));
        nodes.add(new HostAndPort("10.50.217.107", 6380));
        jedis = new JedisCluster(nodes, 10000, 10000, 100, "iotplatform", i2);
    }

    public static PIPoints queryPIPointsByName(String pointName){
        PIPoints piPoints = new PIPoints();
        piPoints.setPointName(pointName);
        String value = jedis.get(pointName);
        if(value == null){
            return piPoints;
        }
        String pointTime = value.split(",")[0];
        String pointValue = value.split(",")[1];
        piPoints.setPointTime(pointTime);
        piPoints.setPointValue(pointValue);
        return piPoints;
    }

    public static PITimes queryPITimesByName(String pointName){
        String PreviousTime = jedis.get(pointName+".PreviousTime");
        PITimes piTimes = new PITimes();
        piTimes.setPointName(pointName);
        piTimes.setPreviousTime(PreviousTime);
        return piTimes;
    }

    //记录刷新数据更新时间PreviousTime
    public static void updatePreviousTimeByCondition(PITimes piTimes){
        jedis.set(piTimes.getPointName()+".PreviousTime", piTimes.getPreviousTime());
    }

    //插入数据
    public static void insertRedis(String key, String value){
        jedis.set(key, value);
    }

    //查询数据
    public static String getRedis(String key){
        return jedis.get(key);
    }
}
