package cornerfinders.recognizers;

import java.awt.Shape;

import edu.mit.sketch.language.shapes.DrawnShape;

/**
 * Fit class - interface for various shape fits
 * @author bpaulson
 */
public interface Fit {

	/**
	 * Get the error of the fit
	 * @return error of fit
	 */
	public double getError();

	/**
	 * Get the name of the fit (name of shape)
	 * @return name of fit
	 */
	public String getName();

	/**
	 * Get the id of the fit
	 * @return id of the fit
	 */
	public int getID();

	/**
	 * Get the beautified (ideal) version of the shape
	 * @return beautified version of shape
	 */
	public DrawnShape getShape();

	/**
	 * Get the beautified version of the shape in a Graphics2D shape
	 * @return Graphics2D version of beautified shape
	 */
	public Shape get2DShape();

	/**
	 * Specifies whether preliminary shape fit tests passed
	 * @return true if prelim tests passed; else false
	 */
	public boolean passed();

	public static final String ARC = "Arc";
	public static final String CIRCLE = "Circle";
	public static final String COMPLEX = "Complex";
	public static final String CURVE = "Curve";
	public static final String ELLIPSE = "Ellipse";
	public static final String HELIX = "Helix";
	public static final String SPIRAL = "Spiral";
	public static final String POLYLINE = "Polyline";
	public static final String LINE = "Line";
	public static final String POLYGON = "Polygon";
	public static final String ARROW = "Arrow";
	public static final String RECTANGLE = "Rectangle";
	public static final int ARC_INT = 0;
	public static final int CIRCLE_INT = 1;
	public static final int COMPLEX_INT = 2;
	public static final int CURVE_INT = 3;
	public static final int ELLIPSE_INT = 4;
	public static final int HELIX_INT = 5;
	public static final int SPIRAL_INT = 6;
	public static final int POLYLINE_INT = 7;
	public static final int LINE_INT = 8;
	public static final int POLYGON_INT = 9;
	public static final int ARROW_INT = 10;
	public static final int RECTANGLE_INT = 11;
}
