package cornerfinders.recognizers;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;


/**
 * FeatureArea class - contains static methods used to conduct
 * feature area tests
 * @author bpaulson
 */
public class FeatureArea {

	/**
	 * Calculate the feature area of the (x,y) points to the input line
	 * @param x x values
	 * @param y y values
	 * @param line line to find feature area to
	 * @return feature area from the (x,y) points to the input line
	 */
	public static double toLine(double[] x, double[] y, Line2D line) {
		double area = 0;
		double b1, b2, d, h;
		for (int i = 0; i < x.length-1; i++) {
			b1 = line.ptSegDist(x[i], y[i]);
			b2 = line.ptSegDist(x[i+1], y[i+1]);
			d = Point2D.distance(x[i], y[i], x[i+1], y[i+1]);
			h = Math.sqrt(Math.abs(Math.pow(d, 2)-Math.pow(Math.abs(b1-b2), 2)));
			area += Math.abs(0.5*(b1+b2)*h);
		}
		return area;
	}

	/**
	 * Calculate the feature area of the (x,y) points to the input point
	 * @param x x values
	 * @param y y values
	 * @param p point to find feature area to
	 * @return feature area from the (x,y) points to the input point
	 */
	public static double toPoint(double[] x, double[] y, Point2D p) {
		double area = 0;
		double a, b, c, s;
		for (int i = 0; i < x.length-1; i++) {
			a = Point2D.distance(x[i], y[i], x[i+1], y[i+1]);
			b = Point2D.distance(x[i], y[i], p.getX(), p.getY());
			c = Point2D.distance(x[i+1], y[i+1], p.getX(), p.getY());
			s = (a+b+c)/2;
			area += Math.sqrt(s*(s-a)*(s-b)*(s-c));
		}
		return area;
	}

	public static double toRectangle(double[] x, double[] y, Rectangle2D rect) {
		double area = 0;
		List<List<Double>> xList = new ArrayList<List<Double>>();
		List<List<Double>> yList = new ArrayList<List<Double>>();
		for (int i = 0; i < 4; i++) {
			xList.add(new ArrayList<Double>());
			yList.add(new ArrayList<Double>());
		}
		Line2D l1 = new Line2D.Double(rect.getX(), rect.getY(),
		        rect.getX() + rect.getWidth(), rect.getY());
		Line2D l2 = new Line2D.Double(rect.getX() + rect.getWidth(), rect
		        .getY(), rect.getX() + rect.getWidth(), rect.getY()
		                                                + rect.getHeight());
		Line2D l3 = new Line2D.Double(rect.getX() + rect.getWidth(),
		        rect.getY() + rect.getHeight(), rect.getX(), rect.getY()
		                                                     + rect.getHeight());
		Line2D l4 = new Line2D.Double(rect.getX(), rect.getY()
		                                           + rect.getHeight(), rect
		        .getX(), rect.getY());
		List<Line2D> lList = new ArrayList<Line2D>();
		lList.add(l1);
		lList.add(l2);
		lList.add(l3);
		lList.add(l4);

		// assign each point to the line it is closest with
		for (int i = 0; i < x.length; i++) {
			int index = 0;
			double dist = lList.get(0).ptSegDist(x[i], y[i]);
			for (int j = 1; j < 4; j++) {
				if (lList.get(j).ptSegDist(x[i], y[i]) < dist) {
					index = j;
					dist = lList.get(j).ptSegDist(x[i], y[i]);
				}
			}
			xList.get(index).add(x[i]);
			yList.get(index).add(y[i]);
		}

		// compute feature area of each set of points to its corresponding line
		for (int i = 0; i < 4; i++) {
			double[] xA = new double[xList.get(i).size()];
			double[] yA = new double[yList.get(i).size()];
			for (int j = 0; j < xList.get(i).size(); j++) {
				xA[j] = xList.get(i).get(j).doubleValue();
				yA[j] = yList.get(i).get(j).doubleValue();
			}
			area += FeatureArea.toLine(xA, yA, lList.get(i));
		}
		return area;
	}
}
