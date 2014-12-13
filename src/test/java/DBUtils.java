import com.google.common.collect.Lists;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.core.shapes.xml.parser.ShapeParser;
import utils.dbconnector.ConnectDB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBUtils {

    public List<TStroke> fetchStrokes(int numData) {
        ConnectDB dbConnect = new ConnectDB();
        Connection conn = dbConnect.startConnection();
        List<TStroke> newParserStrokes = Lists.newArrayList();
        String query = "SELECT data from Storage LIMIT " + numData;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String data = rs.getString("data");
                System.out.println(data.substring(7));

                ShapeParser p = new ShapeParser();
                newParserStrokes = p.parseIntoStrokes(data
                        .substring(7));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConnect.closeConnection();
        }
        return newParserStrokes;
    }
}


