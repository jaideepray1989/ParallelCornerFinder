package utils.dbconnector;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import cornerfinders.core.shapes.TStroke;
import cornerfinders.core.shapes.xml.parser.ShapeParser;

public class StrokeFetcher {

	public static List<TStroke> fetchAllStrokes() {

		List<TStroke> newParserStrokes = null;
		ConnectDB dbConnect = new ConnectDB();
		Connection conn = dbConnect.startConnection();

		String query = "SELECT data from Storage LIMIT 1";
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				String data = rs.getString("data");
				System.out.println(data.substring(7));

				ShapeParser p = new ShapeParser();
				newParserStrokes = p.parseIntoStrokes(data.substring(7));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbConnect.closeConnection();
		}
		return newParserStrokes;
	}
}