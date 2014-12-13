import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.core.shapes.xml.parser.ShapeParser;
import utils.dbconnector.ConnectDB;
import utils.validator.SketchDataValidator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DBUtils {

    public static Map<String, List<TStroke>> fetchStrokes(int numData) {
        ConnectDB dbConnect = new ConnectDB();
        Connection conn = dbConnect.startConnection();
        Map<String, List<TStroke>> parsedMap = Maps.newHashMap();
        String query = "SELECT data from Storage LIMIT " + numData;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            Integer counter = 1;
            while (rs.next()) {
                String data = rs.getString("data");
                System.out.println(data.substring(7));
                ShapeParser p = new ShapeParser();
                List<TStroke> strokes = p.parseIntoStrokes(data
                        .substring(7));
                List<TStroke> validatedStrokes = SketchDataValidator.validateSketch(strokes);
                if (validatedStrokes != null) {
                    parsedMap.put("Shape".concat(counter.toString()), validatedStrokes);
                    counter++;
                }
            }
        } catch (Exception e) {

        } finally {
            dbConnect.closeConnection();
        }
        return parsedMap;
    }
}


