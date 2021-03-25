package com.zhongtai.main;

import com.zhongtai.pidata.PIData;
import org.apache.log4j.Logger;

public class Main {

    private static Logger log = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        //读取redis数据到redis
        try{
            PIData.start();
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }
}
