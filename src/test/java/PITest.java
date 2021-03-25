import com.zhongtai.pidata.PIConnection;

import java.sql.*;

public class PITest {

    public static void main(String[] args) {
        Connection connection = PIConnection.getPIConnection();
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;
        try{
            pStatement = connection.prepareStatement("select tag,time,value from piarchive..picomp2 where tag = 'TS_BTF_FCS0403_FI313502.PV'");
            resultSet = pStatement.executeQuery();

            while(resultSet.next()) {
                String tag = resultSet.getString(1);
                String value = resultSet.getString(2);
                System.out.println(tag + " " + value);
            }
        } catch (Exception e) {
            e.getMessage();
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
                e.printStackTrace();
            }

        }
    }
}
