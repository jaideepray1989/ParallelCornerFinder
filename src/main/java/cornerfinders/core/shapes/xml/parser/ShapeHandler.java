package cornerfinders.core.shapes.xml.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;

public class ShapeHandler extends DefaultHandler {

	private static final Map<String, Integer> qNameMap = new HashMap<String, Integer>();

	static {
		int i = 0;
		qNameMap.put("stroke", i++);
		qNameMap.put("point", i++);

	}

	private TStroke stroke = null;

	private TPoint point = null;

	private List<TStroke> strokesList = new ArrayList<TStroke>();

	public List<TStroke> getAllStrokes() {
		return strokesList;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		Integer attrType = qNameMap.get(qName);

		if (attrType != null) {
			switch (attrType) {
			case 0:
				stroke = new TStroke();
				//System.out.println("Adding a Stroke");
				break;
			case 1:

				if (attributes != null) {
					String xVal = attributes.getValue("x");
					String yVal = attributes.getValue("y");
					String timeVal = attributes.getValue("time");
					if (xVal != null) {
						//System.out.println("Adding a point - x : " + xVal + " y : " + yVal + " time: " + timeVal);
						point = new TPoint(Double.parseDouble(xVal),
								Double.parseDouble(yVal),
								Long.parseLong(timeVal));
						stroke.addPoint(point);
					}

				}
				break;
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		Integer attrType = qNameMap.get(qName);
		if (attrType != null) {
			switch (attrType) {
			case 0:
				if (!stroke.getPoints().isEmpty()) {
					strokesList.add(stroke);
				}
				stroke = null;
				break;
			case 1:
				point = null;
				break;
			}
		}

	}

}
