import com.zhongtai.entity.PIPoints;
import com.zhongtai.pidata.PIConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QueryPIData {

    private static Connection connection = PIConnection.getPIConnection();

    public static void queryPIPoint(String tags){
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try{
            String sql = "select tag,time,value from piarchive..picomp2 where tag = '" + tags + "' limit 10";
            pStatement = connection.prepareStatement(sql);
            resultSet = pStatement.executeQuery();

            while(resultSet.next()) {
                System.out.println("---------------------------------------------------");
                try{
                    String tag = resultSet.getString(1);
                    String time = resultSet.getString(2);
                    String value = resultSet.getString(3);
                    PIPoints piPoints = new PIPoints();
                    piPoints.setPointName(tag);
                    piPoints.setPointTime(time);
                    piPoints.setPointValue(value);
                    System.out.println(piPoints.toString());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }

                if (pStatement != null) {
                    pStatement.close();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        queryPIPoint("TS_DL_FCS0101_FI2544A.PV");
    }
}
