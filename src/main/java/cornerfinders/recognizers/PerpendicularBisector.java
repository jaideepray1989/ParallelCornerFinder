package cornerfinders.recognizers;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.Double;

/**
 * Class originally by Mark Eaton, but slightly modified
 */
public class PerpendicularBisector {

	private Line2D.Double bisector;
	private Point2D midPoint;
	private double bisectorSlope;
	private double bisectorYIntercept;	
	
	/**
	 * Constructor for perpendicular bisector
	 * @param p1 first endpoint of the line
	 * @param p2 second endpoint of the line
	 * @param bounds maximum bounds for bisector
	 */
	public PerpendicularBisector(Point2D p1, Point2D p2, Rectangle2D bounds) {
		double xMid = ((p2.getX() - p1.getX()) / 2) + p1.getX();
		double yMid = ((p2.getY() - p1.getY()) / 2) + p1.getY();
		midPoint = new Point2D.Double(xMid, yMid);
		
		if((p2.getY() - p1.getY()) == 0) {
			//line is horizontal, slope 0
			//perpendicular bisector is slope infinity
			bisectorSlope = Double.NaN;
		}
		else if((p2.getX() - p1.getX()) == 0) {
			//line is vertical, slope infinity
			//perpendicular bisector is slope 0
			bisectorSlope = 0;
		}
		else {
			bisectorSlope = -1 * (p2.getX() - p1.getX()) / (p2.getY() - p1.getY());
		}

		double x1, y1, x2, y2;
		if(Double.isNaN(bisectorSlope)) {
			//vertical
			bisectorYIntercept = Double.NaN;
			y1 = bounds.getMinY();
			y2 = bounds.getMaxY();
			x1 = midPoint.getX();
			x2 = midPoint.getX();
		}
		else if(bisectorSlope == 0) {
			//horizontal
			bisectorYIntercept = midPoint.getY();
			y1 = midPoint.getY();
			y2 = midPoint.getY();
			x1 = bounds.getMinX();
			x2 = bounds.getMaxX();
		}
		else {
			//solve for y intercept
			//y = mx + b
			double y = midPoint.getY();
			double x = midPoint.getX();
			double m = bisectorSlope;
			double b = y - (m * x);
			bisectorYIntercept = b;
			
			x1 = bounds.getMinX();
			x2 = bounds.getMaxX();
			
			if (Double.isNaN(m)) {
				y1 = bounds.getMinY();
				y2 = bounds.getMaxY();
			}
			else {
				y1 = m * x1 + b;
				y2 = m * x2 + b;
			}
		}
		bisector = new Line2D.Double(x1, y1, x2, y2);
	}

	/**
	 * Get the midpoint of the bisector
	 * @return midpoint
	 */
	public Point2D getMidPoint() {
		return midPoint;
	}

	/**
	 * Get the slope of the bisector
	 * @return slope of bisector
	 */
	public double getBisectorSlope() {
		return bisectorSlope;
	}

	/**
	 * Get the Y-intercept of the bisector
	 * @return Y-intercept
	 */
	public double getBisectorYIntercept() {
		return bisectorYIntercept;
	}

	/**
	 * Get the bisector found
	 * @return bisector found
	 */
	public Line2D.Double getBisector() {
		return bisector;
	}
}
