package cornerfinders.core.shapes.xml.parser;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import cornerfinders.core.shapes.TStroke;

public class ShapeParser {

	public List<TStroke> parseIntoStrokes(String xml) {

		List<TStroke> parsedStrokes = null;
		try {
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			SAXParser parser = parserFactory.newSAXParser();

			ShapeHandler shapeHandler = new ShapeHandler();

			InputStream is = new BufferedInputStream(new ByteArrayInputStream(
					xml.getBytes()));
			parser.parse(is, shapeHandler);
			parsedStrokes = shapeHandler.getAllStrokes();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return parsedStrokes;
	}

}
