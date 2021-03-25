package com.zhongtai.mysql;

import com.zhongtai.entity.PIPoints;
import com.zhongtai.entity.PITimes;
import org.apache.log4j.Logger;

import java.sql.*;

public class MysqlUtil {

    private static Logger log = Logger.getLogger(MysqlUtil.class);

    public static PIPoints queryPIPointsByName(String pointName){
        PIPoints piPoints = new PIPoints();
        try {
            // SQL测试语句
            String sql = "SELECT point_name,point_time,point_value FROM pipoints WHERE point_name='" + pointName + "'";

            Connection conn = MysqlConnection.getConn();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String point_time = rs.getString("point_time");
                String point_value = rs.getString("point_value");
                piPoints.setPointName(pointName);
                piPoints.setPointTime(point_time);
                piPoints.setPointValue(point_value);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return piPoints;
    }

    public static PITimes queryPITimesByName(String pointName){
        PITimes piTimes = new PITimes();
        try {
            // SQL测试语句
            String sql = "SELECT point_name, previous_time FROM pitimes WHERE point_name='" + pointName + "'";

            Connection conn = MysqlConnection.getConn();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String point_name = rs.getString("point_name");
                String previous_time = rs.getString("previous_time");
                piTimes.setPointName(point_name);
                piTimes.setPreviousTime(previous_time);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return piTimes;
    }

    public static void updatePreviousTimeByCondition(PITimes pitimes){
        try {
            // SQL测试语句
            String sql = "REPLACE INTO pitimes(point_name, previous_time) VALUES(?, ?)";

            Connection conn = MysqlConnection.getConn();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, pitimes.getPointName());
            ps.setString(2, pitimes.getPreviousTime());
            ps.executeUpdate();

            ps.close();
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }
}
