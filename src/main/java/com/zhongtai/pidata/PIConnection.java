package com.zhongtai.pidata;

import org.apache.log4j.Logger;
import java.sql.*;
import java.util.Properties;

public class PIConnection {

    private static Logger log = Logger.getLogger(PIConnection.class);

    public static Connection getPIConnection() {
        String dasName = "10.50.211.14";
//        String dasName = "10.113.131.27";//----------------------TEST
        String dataSourceName = "TKXPISRV";
//        String dataSourceName = "10.113.131.27";//-------------------TEST
        String enableCertificateValidation = "No";
        String isTrustedConnection = "No";
        String useDCA = "No";
        String userName = "Administrator";
//        String userName = "imobs";//-------------------TEST
        String password = "Tkxmes2016";
//        String password = "zhou19930315";//-------------------TEST
        String portNumber = "5461";
        String logLevel = "2";


        Connection connection = null;
        String url = "";
        String driverClassName = "com.osisoft.jdbc.Driver";
        Properties properties = new Properties();
        url = "jdbc:pioledb://" + dasName + "/Data Source=" + dataSourceName + "; Integrated Security=SSPI";
        properties.put("EnableCertificateValidation", enableCertificateValidation);
        properties.put("TrustedConnection", isTrustedConnection);
        if (isTrustedConnection.equals("No") && useDCA.equals("No")) {
            properties.put("user", userName);
            properties.put("password", password);
        }

        properties.put("Port", portNumber);
        properties.put("LogConsole", "True");
        properties.put("LogLevel", logLevel);

        try {
            Class.forName(driverClassName).newInstance();
            connection = DriverManager.getConnection(url, properties);
        } catch (Exception e) {
            log.error("PI数据库连接失败.....");
            log.error(e.getMessage());
            log.info("进行重新连接PI数据库。。。。。。。。。");
            connection = getPIConnection();
        }
        log.info("PI数据库连接成功！！！");
        return connection;
    }

    public static void main(String[] args) {
        Connection connection = getPIConnection();
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;
        try{
            pStatement = connection.prepareStatement("SELECT * FROM [piarchive]..[piavg] WHERE tag = 'sinusoid' AND time BETWEEN '*-1h' AND '*'");
            DatabaseMetaData metaData = connection.getMetaData();
            System.out.println(metaData.getDriverName() + " " + metaData.getDriverVersion());
            System.out.println(metaData.getDatabaseProductName());
            System.out.println(metaData.getDatabaseProductVersion() + "\n");
            pStatement.setString(1, "sin%");
            resultSet = pStatement.executeQuery();

            while(resultSet.next()) {
                String tag = resultSet.getString(1);
                String value = resultSet.getString(2);
                System.out.println(tag + " " + value);
            }
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }

                if (pStatement != null) {
                    pStatement.close();
                }

                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e);
            }

        }
    }
}
