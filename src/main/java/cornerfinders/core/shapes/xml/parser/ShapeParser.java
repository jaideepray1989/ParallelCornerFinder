package cornerfinders.core.shapes.xml.parser;

import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import cornerfinders.core.shapes.TStroke;

public class ShapeParser {

	public List<TStroke> parseIntoStrokes(String filePath) {

		List<TStroke> parsedStrokes = null;
		try {
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			SAXParser parser = parserFactory.newSAXParser();

			ShapeHandler shapeHandler = new ShapeHandler();

			parser.parse(filePath, shapeHandler);
			parsedStrokes = shapeHandler.getAllStrokes();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return parsedStrokes;

	}

}
