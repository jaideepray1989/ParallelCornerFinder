import utils.dbconnector.ConnectDB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by jaideepray on 12/6/14.
 */
public class BuildXmlTree {

    public static void main(String[] args) {
        ConnectDB dbConnect = new ConnectDB();
        Connection conn = dbConnect.startConnection();

        String query = "SELECT data from Storage LIMIT 1";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String data = rs.getString("data");
                System.out.println(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
