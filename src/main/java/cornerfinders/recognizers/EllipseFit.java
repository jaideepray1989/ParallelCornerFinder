package cornerfinders.recognizers;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import edu.mit.sketch.language.shapes.DrawnShape;
import edu.tamu.hammond.sketch.shapes.TEllipse;

/**
 * EllipseFit class - fit a stroke to an ellipse
 * @author bpaulson
 */
public class EllipseFit implements Fit, StrokeThresholds {

	private BStroke stroke;
	private StrokeFeatures sf;
	private double err;
	private boolean passed;
	private boolean debug = false;
	private Line2D majorAxis;
	private Line2D minorAxis;
	private Point2D center;
	private Point2D avgCenter;
	private double majorAxisLength;
	private double minorAxisLength;
	private double majorAxisAngle;
	private ArrayList<BStroke> subStrokes;
	private double avgRadius;
	private Shape beautified;

	/**
	 * Constructor for the ellipse fit
	 * @param stroke stroke to fit to an ellipse
	 */
	public EllipseFit(BStroke stroke) {
		passed = true;
		this.stroke = stroke;
		sf = stroke.getFeatures();
		calcMajorAxis();
		calcCenter();
		calcMinorAxis();

		// test 1: stroke must be closed
		if (!sf.isComplete())
			passed = false;

		// test 2: make sure NDDE is high (close to 1.0) or low (close to 0.0)
		if (sf.getNDDE() < M_NDDE_HIGH && /*sf.getNDDE() > M_NDDE_LOW &&*/ majorAxisLength > M_ELLIPSE_SMALL)
			passed = false;

		// test 3: feature area test (results used for error)
		if (!sf.isOvertraced()) {
			err = calcFeatureArea(center, avgCenter, stroke, majorAxisLength, minorAxisLength);
			if (err > M_ELLIPSE_FEATURE_AREA)
				passed = false;
		}
		else {
			subStrokes = sf.get2PIsubStrokes();
			double subErr = 0.0;
			double centerDis = 0.0, axisDiff = 0.0;
			for (int i = 0; i < subStrokes.size()-1; i++) {
				subStrokes.get(i).calcEllipseFit();
				EllipseFit ef = subStrokes.get(i).getEllipseFit();
				centerDis += center.distance(ef.getCenter());
				axisDiff += Math.abs(majorAxisLength-ef.getMajorAxisLength());
				//if (!ef.passed())
				//	passed = false;
				subErr += ef.getError();
			}
			err = subErr/(subStrokes.size()-1);
			if (err > M_ELLIPSE_FEATURE_AREA)
				passed = false;

			// test 3.5 (overtraced only) - make sure center and axis are relatively constant
			/*if (centerDis/(sf.numRevolutions()*sf.getStrokeLength()) > M_OVERTRACED_ELLIPSE_CENTER_CLOSENESS)
				passed = false;
			if (axisDiff/(sf.numRevolutions()*sf.getStrokeLength()) > M_OVERTRACED_ELLIPSE_AXIS_DIFFERENCE)
				passed = false;

			System.out.println("e1 = " + centerDis/(sf.numRevolutions()*sf.getStrokeLength()) + " e2 = " + axisDiff/(sf.numRevolutions()*sf.getStrokeLength()));
			*/
		}

		// test 4: largest direction change should be low
		//if (sf.getLargestDirectionChange() > M_LARGEST_DIRECTION_CHANGE)
		//	passed = false;

		beautified = ((TEllipse)getShape()).get2DShape();

		// NEW TEST:
		double ratio = sf.numCorners()/majorAxisLength;
		/*if (ratio < M_ELLIPSE_CORNER_LENGTH_RATIO)
			passed = false;*/

		// NEW TEST:
		/*calcAvgRadius();
		double ratio = sf.numCorners()/avgRadius;
		if (ratio < M_CIRCLE_CORNER_LENGTH_RATIO)
			passed = false;*/

		// ANOTHER NEW TEST
		/*if (sf.getNonAbsCurvRatio() < M_NON_ABS_CURV_RATIO)
			passed = false;*/

		if (!sf.dirWindowPassed())
			passed = false;

		//if (sf.getSlopeDiff() > M_SLOPE_DIFF)
		//	passed = false;

		//if (sf.numRevolutions() <= M_REVS_TO_BE_CIRCULAR)
		//	passed = false;

		if (debug) {
			System.out.println("EllipseFit: passed = " + passed + "  center = (" + center.getX() +
					"," + center.getY() + ")  major axis = (" + majorAxis.getX1() + "," +
					majorAxis.getY1() + ") (" + majorAxis.getX2() + "," + majorAxis.getY2() +
					")  major axis length = " + majorAxisLength + "  minor axis length = " +
					minorAxisLength + "  closed = " + sf.isComplete() + "  overtraced = " + sf.isOvertraced() +
					"  NDDE = " + sf.getNDDE() + "  revs = " + sf.numRevolutions() + "  feature area err = " + err +
					"  DCR = " + sf.getDCR() + " num corners = " + sf.numFinalCorners() + " num revs = " + sf.getNumRevs()
					+ "  corner/length = " + ratio + "  non-abs curv ratio = " + sf.getNonAbsCurvRatio() +
					"  dir window passed = " + sf.dirWindowPassed() + " slope diff: " + sf.getSlopeDiff());
			//stroke.getFeatures().debug(2, true, false);
			//stroke.getFeatures().debug(3, true, false);
		}
	}

	public double getError() {
		return err;
	}

	public String getName() {
		return Fit.ELLIPSE;
	}

	public DrawnShape getShape() {
		TEllipse te = new TEllipse(center.getX(), center.getY(), majorAxisLength,
				minorAxisLength, majorAxisAngle, sf.isOvertraced());
		return te;
	}

	public Shape get2DShape() {
		return beautified;
	}

	public boolean passed() {
		return passed;
	}

	public Line2D getMajorAxis() {
		return majorAxis;
	}

	public Line2D getMinorAxis() {
		return minorAxis;
	}

	public double getMajorAxisLength() {
		if (majorAxisLength < 0.00000001)
			majorAxisLength = 0;
		return majorAxisLength;
	}

	public double getMinorAxisLength() {
		if (minorAxisLength < 0.00000001)
			minorAxisLength = 0;
		return minorAxisLength;
	}

	public double getMajorAxisAngle() {
		return majorAxisAngle;
	}

	public Point2D getCenter() {
		return center;
	}

	public ArrayList<BStroke> getSubStrokes() {
		return subStrokes;
	}

	private void calcMajorAxis() {
		boolean useBestFitLine = false;

		// use best fit line as the major axis
		if (useBestFitLine)
			majorAxis = (Line2D)sf.getBestFitLine().clone();

		// use the greatest distance between two points as major axis
		else {
			double maxDistance = Double.MIN_VALUE;
			int max1 = 0, max2 = 0;
			for (int i = 0; i < stroke.size(); i++) {
				for (int j = 0; j < stroke.size(); j++) {
					if (i != j) {
						double d = stroke.get(i).distance(stroke.get(j));
						if (d > maxDistance) {
							maxDistance = d;
							max1 = i;
							max2 = j;
						}
					}
				}
			}
			majorAxis = new Line2D.Double(stroke.get(max1).getX(), stroke.get(max1).getY(),
					stroke.get(max2).getX(), stroke.get(max2).getY());
		}

		majorAxisLength = majorAxis.getP1().distance(majorAxis.getP2());
		majorAxisAngle = Math.atan2(majorAxis.getY2()-majorAxis.getY1(), majorAxis.getX2()-majorAxis.getX1());
	}

	private void calcCenter() {
		boolean useCenterOfMajorAxis = false;

		// calculate average center
		double avgX=0, avgY=0;
		for (int i = 0; i < stroke.size(); i++) {
			avgX += stroke.get(i).getX();
			avgY += stroke.get(i).getY();
		}
		avgX /= stroke.size();
		avgY /= stroke.size();
		avgCenter = new Point2D.Double(avgX, avgY);

		// center is midpoint of major axis
		if (useCenterOfMajorAxis) {
			Point2D p1 = majorAxis.getP1();
			Point2D p2 = majorAxis.getP2();
			double midX = (p2.getX()+p1.getX())/2;
			double midY = (p2.getY()+p1.getY())/2;
			center = new Point2D.Double(midX, midY);
		}

		// center is (avgX, avgY) of all points in the stroke
		else {
			center = avgCenter;
		}
	}

	private void calcMinorAxis() {
		PerpendicularBisector bisect = new PerpendicularBisector(majorAxis.getP1(),
				majorAxis.getP2(), sf.getBounds());
		ArrayList<Point2D> intersectPts = stroke.getIntersection(bisect.getBisector());
		if (intersectPts.size() < 2) {
			minorAxis = null;
			minorAxisLength = Double.NaN;
		}
		else {
			double d1, d2;
			d1 = center.distance(intersectPts.get(0));
			d2 = center.distance(intersectPts.get(1));
			minorAxis = new Line2D.Double(intersectPts.get(0).getX(), intersectPts.get(0).getY(),
					intersectPts.get(1).getX(), intersectPts.get(1).getY());
			minorAxisLength = d1+d2;
		}
	}

	private double calcFeatureArea(Point2D center, Point2D avgCenter, BStroke stroke,
			double majorAxisLength, double minorAxisLength) {
		double err1 = FeatureArea.toPoint(stroke.getXValues(), stroke.getYValues(), center);
		if (center != avgCenter) {
			double err2 = FeatureArea.toPoint(stroke.getXValues(), stroke.getYValues(), avgCenter);
			if (err2 < err1)
				err1 = err2;
		}
		err1 /= (Math.PI*(minorAxisLength/2.0)*(majorAxisLength/2.0));
		err1 = Math.abs(1.0-err1);
		return err1;
	}

	private void calcAvgRadius() {
		avgRadius = 0.0;
		for (int i = 0; i < stroke.size(); i++)
			avgRadius += Point2D.distance(stroke.get(i).getX(), stroke.get(i).getY(),
					getCenter().getX(), getCenter().getY());
		avgRadius /= stroke.size();
	}

	public int getID() {
		return Fit.ELLIPSE_INT;
	}
}
