package cornerfinders.recognizers;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import edu.mit.sketch.language.shapes.DrawnShape;
import edu.tamu.hammond.sketch.shapes.TEllipse;

/**
 * CircleFit class - fit a stroke to a circle
 * @author bpaulson
 */
public class CircleFit implements Fit, CircularFit, StrokeThresholds {

	private BStroke stroke;
	private StrokeFeatures sf;
	private double err;
	private boolean passed;
	private boolean debug = false;
	private EllipseFit ellFit;
	private double radius;
	private double avgRadius;
	private ArrayList<BStroke> subStrokes;
	private ArrayList<CircularFit> subStrokeFits;
	private Shape beautified;
	private double axisRatio;

	/**
	 * Constructor for the circle fit
	 * @param stroke stroke to fit circle to
	 * @param ellFit ellipse fit (used so features do not need to be recomputed)
 	 */
	public CircleFit(BStroke stroke, EllipseFit ellFit) {
		passed = true;
		this.stroke = stroke;
		this.ellFit = ellFit;
		sf = stroke.getFeatures();
		calcRadius();

		// test 1: stroke must be closed
		if (!sf.isComplete())
			passed = false;

		// test 2: make sure NDDE is high (close to 1.0) or low (close to 0.0)
		if (sf.getNDDE() < M_NDDE_HIGH && /*sf.getNDDE() > M_NDDE_LOW &&*/ radius > M_CIRCLE_SMALL)
			passed = false;

		// test 3: check major axis to minor axis ratio
		axisRatio = Math.abs(1.0-ellFit.getMajorAxisLength()/ellFit.getMinorAxisLength());
		if (axisRatio > M_AXIS_RATIO_TO_BE_CIRCLE)
			passed = false;

		// test 4: feature area test (results used for error)
		if (!sf.isOvertraced()) {
			err = calcFeatureArea(radius, avgRadius, stroke, ellFit);
			if (err > M_CIRCLE_FEATURE_AREA)
				passed = false;
		}
		else {
			subStrokes = ellFit.getSubStrokes();
			subStrokeFits = new ArrayList<CircularFit>();
			double subErr = 0.0;
			double centerDis = 0.0, radDiff = 0.0;
			for (int i = 0; i < subStrokes.size(); i++) {
				if (i != subStrokes.size()-1) {
					subStrokes.get(i).calcCircleFit(subStrokes.get(i).getEllipseFit());
					CircleFit cf = subStrokes.get(i).getCircleFit();
					subStrokeFits.add(cf);
					centerDis += ellFit.getCenter().distance(cf.getCenter());
					radDiff += Math.abs(radius-subStrokes.get(i).getCircleFit().getRadius());
					//if (!cf.passed())
					//	passed = false;
					subErr += cf.getError();
				}
				else {		// fit last substroke to an arc
					subStrokes.get(i).calcArcFit();
					ArcFit af = subStrokes.get(i).getArcFit();
					subStrokeFits.add(af);
					centerDis += ellFit.getCenter().distance(af.getCenter());
					radDiff += Math.abs(radius-subStrokes.get(i).getArcFit().getRadius());
					//if (!af.passed())
					//	passed = false;
					//subErr += af.getError();
				}
			}
			err = subErr/(subStrokes.size()-1);
			if (err > M_CIRCLE_FEATURE_AREA)
				passed = false;

			// test 4.5 (overtraced only) - make sure center and radius are relatively constant
			/*if (centerDis/(sf.numRevolutions()*sf.getStrokeLength()) > M_OVERTRACED_CIRCLE_CENTER_CLOSENESS)
				passed = false;
			if (radDiff/(sf.numRevolutions()*sf.getStrokeLength()) > M_OVERTRACED_CIRCLE_RADIUS_DIFFERENCE)
				passed = false;

			System.out.println("c1 = " + centerDis/(sf.numRevolutions()*sf.getStrokeLength()) + " c2 = " + radDiff/(sf.numRevolutions()*sf.getStrokeLength()));
			*/
		}

		// test 5: largest direction change should be low
		//if (sf.getLargestDirectionChange() > M_LARGEST_DIRECTION_CHANGE)
		//	passed = false;

		beautified = ((TEllipse)getShape()).get2DShape();

		// NEW TEST:
		double ratio = sf.numCorners()/radius;
		if (ratio < M_CIRCLE_CORNER_LENGTH_RATIO)
			passed = false;

		// ANOTHER NEW TEST
		//if (sf.getNonAbsCurvRatio() < M_NON_ABS_CURV_RATIO)
		//	passed = false;

		if (!sf.dirWindowPassed())
			passed = false;

		//if (sf.getSlopeDiff() > M_SLOPE_DIFF)
		//	passed = false;

		//if (sf.numRevolutions() < M_REVS_TO_BE_CIRCULAR)
		//	passed = false;

		if (debug) {
			System.out.println("CircleFit: passed = " + passed + "  center = (" + ellFit.getCenter().getX() +
					"," + ellFit.getCenter().getY() + ")  radius = " + radius +
					"  closed = " + sf.isComplete() + "  overtraced = " + sf.isOvertraced() +
					"  NDDE = " + sf.getNDDE() + "  axis ratio = " + axisRatio + "  feature area err = " + err +
					"  num revs = " + sf.getNumRevs() + " DCR = " + sf.getDCR() +
					"  corner/length = " + ratio + "  non-abs curv ratio = " + sf.getNonAbsCurvRatio() +
					"  dir window passed = " + sf.dirWindowPassed() + " slope diff: " + sf.getSlopeDiff());
		}
	}

	public double getAxisRatio() {
		if (Double.isInfinite(axisRatio))
			axisRatio = Double.NaN;
		return axisRatio;
	}

	public double getError() {
		return err;
	}

	public String getName() {
		return Fit.CIRCLE;
	}

	public DrawnShape getShape() {
		double tmpR = (ellFit.getMajorAxisLength()/2+ellFit.getMinorAxisLength()/2)/2;
		TEllipse te = new TEllipse(ellFit.getCenter().getX(), ellFit.getCenter().getY(),
				tmpR*2, tmpR*2, 0, sf.isOvertraced());
		return te;
	}

	public Shape get2DShape() {
		return beautified;
	}

	public boolean passed() {
		return passed;
	}

	public double getRadius() {
		return radius;
	}

	public double getAvgRadius() {
		return avgRadius;
	}

	public Point2D getCenter() {
		return ellFit.getCenter();
	}

	public ArrayList<BStroke> getSubStrokes() {
		return subStrokes;
	}

	public ArrayList<CircularFit> getSubStrokeFits() {
		return subStrokeFits;
	}

	private void calcRadius() {
		boolean useAvgRadius = true;

		// calc average radius
		avgRadius = 0.0;
		for (int i = 0; i < stroke.size(); i++)
			avgRadius += Point2D.distance(stroke.get(i).getX(), stroke.get(i).getY(),
					ellFit.getCenter().getX(), ellFit.getCenter().getY());
		avgRadius /= stroke.size();

		// use average radius (distance from each point to center)
		if (useAvgRadius)
			radius = avgRadius;

		// use 1/2 minor and 1/2 major axis average as radius
		else
			radius = (ellFit.getMajorAxisLength()/2+ellFit.getMinorAxisLength()/2)/2;
	}

	private double calcFeatureArea(double radius, double avgRadius, BStroke stroke, EllipseFit ellFit) {
		double err1 = FeatureArea.toPoint(stroke.getXValues(), stroke.getYValues(), ellFit.getCenter());
		double err2 = err1;
		err1 /= (Math.PI*radius*radius);
		if (radius != avgRadius) {
			err2 /= (Math.PI*avgRadius*avgRadius);
			if (err2 < err1)
				err1 = err2;
		}
		err1 = Math.abs(1.0-err1);
		return err1;
	}

	public int getID() {
		return Fit.CIRCLE_INT;
	}
}
