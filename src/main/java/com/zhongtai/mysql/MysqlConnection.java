package com.zhongtai.mysql;

import java.sql.Connection;
import java.sql.DriverManager;

public class MysqlConnection {


    private static String jdbcDriver = "com.mysql.jdbc.Driver"; // 数据库驱动
    private static String dbUrl = "jdbc:mysql://node1.iotplatform.com:3306/zhongtai_iot?useUnicode=true&characterEncoding=utf-8"; // 数据 URL
    private static String dbUsername = "root"; // 数据库用户名
    private static String dbPassword = "iotplatform"; // 数据库用户密码

    public static Connection getConn(){
        Connection conn = null;
        try{
            Class.forName(jdbcDriver);  //加载数据库驱动
            conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        }catch (Exception e){
            e.printStackTrace();
        }
        return conn;
    }
}
