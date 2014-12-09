import cornerfinders.core.shapes.TStroke;
import cornerfinders.core.shapes.xml.parser.ShapeParser;
import cornerfinders.impl.CornerFinder;
import cornerfinders.impl.ShortStrawCornerFinder;
import utils.dbconnector.ConnectDB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class BuildXmlTree {

    public static void main(String[] args) {
        ConnectDB dbConnect = new ConnectDB();
        Connection conn = dbConnect.startConnection();

        String query = "SELECT data from Storage LIMIT 2";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String data = rs.getString("data");
                System.out.println(data.substring(7));
                ShapeParser p = new ShapeParser();
                List<TStroke> newParserStrokes = p.parseIntoStrokes(data.substring(7));
                List<TStroke> strokes = TStroke.getTStrokesFromFile(data.substring(7));

                CornerFinder cornerFinder = new ShortStrawCornerFinder();

                for (TStroke s : strokes) {
                    ArrayList<Integer> corners = cornerFinder.findCorners(s);

                    for (Integer index : corners) {
                        System.out.println("printing corner :: ");
                        s.getPoint(index).printPoint();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            dbConnect.closeConnection();
        }
    }
}