package com.zhongtai.util;

import com.zhongtai.main.Main;
import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    private static Logger log = Logger.getLogger(DateUtils.class);

    public static String getCurrentDateTime(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        return df.format(new Date());// new Date()为获取当前系统时间
    }

    public static void main(String[] args) {
        System.out.println(getCurrentDateTime());
    }

    public static long getMsTimeByDateTime(String pointTime){
        Date date = null;
        try {
            String dateTime = pointTime.split("\\.")[0];
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date = simpleDateFormat.parse(dateTime);
        } catch (ParseException e) {
            log.error(e.getMessage());
        }
        return date.getTime();
    }
}
