import cornerfinders.core.shapes.TStroke;
import cornerfinders.core.shapes.xml.parser.ShapeParser;
import cornerfinders.impl.CornerFinder;
import cornerfinders.impl.KimCornerFinder;
import cornerfinders.impl.AngleCornerFinder;
import cornerfinders.impl.SezginCornerFinder;
import cornerfinders.impl.ShortStrawCornerFinder;
import utils.dbconnector.ConnectDB;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


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

                // Code block start
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder;
                builder = factory.newDocumentBuilder();
                // Use String reader
                Document document = builder.parse( new InputSource(
                        new StringReader( data.substring(7) ) ) );
                TransformerFactory tranFactory = TransformerFactory.newInstance();
                Transformer aTransformer = tranFactory.newTransformer();
                Source src = new DOMSource( document );
                Result dest = new StreamResult( new File( "xmlFileTemp.xml" ) );
                aTransformer.transform( src, dest );
                //code block end

                ShapeParser p = new ShapeParser();
                List<TStroke> newParserStrokes = p.parseIntoStrokes("xmlFileTemp.xml");
                //List<TStroke> strokes = TStroke.getTStrokesFromXML(data.substring(7));

                CornerFinder cornerFinder = new KimCornerFinder();

                //for (TStroke s : strokes) {
                for(TStroke s: newParserStrokes){
                    ArrayList<Integer> corners = cornerFinder.findCorners(s);
                    if(null == corners)
                        continue;
                        for (Integer index : corners) {
                        if(0<= index && index <= s.getSize()) {
                            System.out.println("printing corner :: ");
                            s.getPoint(index).printPoint();
                        }
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