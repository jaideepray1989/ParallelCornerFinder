package cornerfinders.recognizers;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import edu.mit.sketch.language.shapes.DrawnShape;
import edu.tamu.hammond.sketch.shapes.TArc;
import edu.tamu.hammond.sketch.shapes.TSpiral;
import edu.tamu.hammond.sketch.shapes.TPoint;

/**
 * SpiralFit class - fit a stroke to a spiral
 * @author bpaulson
 */
public class SpiralFit implements Fit, StrokeThresholds {

	private BStroke stroke;
	private StrokeFeatures sf;
	private double err;
	private double err2;
	private boolean passed;
	private boolean centerClosenessTest;
	private boolean debug = false;
	private Point2D center;
	private Point2D avgCenter;
	private double avgRadius;
	private boolean desc;
	private ArrayList<BStroke> subStrokes;
	private ArrayList<CircularFit> subStrokeFits;
	private Shape beautified;
	private double avgRadBBRadRatio;
	private double maxDistanceToCenter;
	private boolean radiusTest;

	/**
	 * Constructor for the spiral fit
	 * @param stroke stroke to fit a spiral to
	 * @param cf circle fit (used to avoid re-computation)
	 */
	public SpiralFit(BStroke stroke, CircleFit cf) {
		passed = true;
		desc = false;
		this.stroke = stroke;
		sf = stroke.getFeatures();
		calcCenter();
		calcAvgRadius();

		// test 1: stroke must be overtraced
		if (!sf.isOvertraced()) {
			passed = false;
			if (debug)
				System.out.println("SpiralFit: passed = " + passed + "  overtraced = " + sf.isOvertraced() +
						"  numRevs = " + sf.numRevolutions());
			return;
		}

		// test 2: make sure NDDE is high (close to 1.0) or low (close to 0.0)
		if (sf.getNDDE() < M_NDDE_HIGH )//&& sf.getNDDE() > M_NDDE_LOW)
			passed = false;

		// calculate substrokes - fit all to circles
		subStrokes = cf.getSubStrokes();
		subStrokeFits = cf.getSubStrokeFits();

		// test 3: make sure all radii are either descending or ascending
		radiusTest = true;
		if (subStrokeFits.size() > 1) {
			double first = subStrokeFits.get(0).getRadius();
			double next = subStrokeFits.get(1).getRadius();
			desc = false;
			if (next < first)
				desc = true;
			// subStrokes.size()-1 because last substroke may be incomplete
			for (int i = 1; i < subStrokes.size()-1 && passed; i++) {
				next = subStrokeFits.get(i).getRadius();
				if (desc && next > first)
					passed = false;
				else if (!desc && next < first)
					passed = false;
				first = next;
			}
			radiusTest = passed;
		}
		// since subStrokeFits.size() may be <= 1 we need to manually calculate desc
		TPoint midpt = new TPoint(sf.getBounds().getCenterX(), sf.getBounds().getCenterY(), 0);
		TPoint start = stroke.get(0);
		TPoint last = stroke.get(stroke.size()-1);
		if (midpt.distance(start) > midpt.distance(last))
			desc = true;
		else
			desc = false;

		// I DO NOT THINK THIS TEST IS RELEVANT - DOES NOT SEEM TO DISTINGUISH BETWEEN
		// HELIX AND SPIRAL
		// test 4: verify that the sum of the differences of the radii of each substroke
		//         is close to the average radius of the entire stroke
		/*double sum = subStrokeFits.get(0).getRadius();
		for (int i = 0; i < subStrokeFits.size()-1; i++)
			sum += Math.abs(subStrokeFits.get(i).getRadius()-subStrokeFits.get(i+1).getRadius());
		err = Math.abs(1-sum/(avgRadius*subStrokeFits.size()));
		if (err > M_SPIRAL_RADIUS_ERROR)
			passed = false;*/

		// test 4: average radius should be less than radius based on bounding box
		//         helps to distiguish between spiral and overtraced shape
		double bbRadius = (sf.getBounds().getHeight()/2+sf.getBounds().getWidth()/2)/2;
		avgRadBBRadRatio = avgRadius/bbRadius;
		if (avgRadius/bbRadius > M_SPIRAL_RADIUS_RATIO)
			passed = false;

		// test 5: verify that centers are close to each other (and close to average center)
		double sum = 0.0;
		double sum2 = 0.0;
		centerClosenessTest = true;
		if (subStrokeFits.size() > 0)
			sum2 += subStrokeFits.get(0).getCenter().distance(avgCenter);
		// subStrokes.size()-2 because last substroke may be incomplete
		for (int i = 0; i < subStrokes.size()-2; i++) {
			sum += subStrokeFits.get(i).getCenter().distance(subStrokeFits.get(i+1).getCenter());
			sum2 += subStrokeFits.get(i+1).getCenter().distance(avgCenter);
		}
		if (subStrokes.size()-2 != 0) {
			sum /= (subStrokes.size()-2);
			sum2 /= (subStrokes.size()-2);
		}
		else {
			sum = 0.0;
			sum2 = 0.0;
		}
		err = sum / avgRadius;
		err2 = sum2 / avgRadius;
		err /= sf.numRevolutions();
											   // NOTE: this not as relevant because of incomplete circles
		if (err > M_SPIRAL_CENTER_CLOSENESS) { //|| err2 > M_SPIRAL_AVG_CENTER_CLOSENESS) {
			passed = false;
			centerClosenessTest = false;
		}

		// test 6: max distance between two centers should not be greater than diameter of spiral
		double maxDistance = 0;
		for (int i = 0; i < subStrokeFits.size(); i++) {
			for (int j = 0; j < subStrokeFits.size(); j++) {
				if (i != j) {
					double dis = subStrokeFits.get(i).getCenter().distance(subStrokeFits.get(j).getCenter());
					if (dis > maxDistance)
						maxDistance = dis;
				}
			}
		}
		maxDistanceToCenter = maxDistance;
		if (maxDistance > 2*avgRadius)
			passed = false;

		// test 7: endpoints of stroke should less than diameter
		double maxBB;
		if (sf.getBounds().getWidth() > sf.getBounds().getHeight())
			maxBB = sf.getBounds().getWidth();
		else
			maxBB = sf.getBounds().getHeight();
		//double endptDis = stroke.get(0).distance(stroke.get(stroke.size()-1)) / (maxBB);
		double endptDis = stroke.get(0).distance(stroke.get(stroke.size()-1)) / sf.getStrokeLength();
		if (endptDis > M_SPIRAL_DIAMETER_CLOSENESS)
			passed = false;

		// test 8: largest direction change should be low
		//if (sf.getLargestDirectionChange() > M_LARGEST_DIRECTION_CHANGE)
		//	passed = false;

		beautified = ((TSpiral)getShape()).get2DShape();

		if (debug)
			System.out.println("SpiralFit: passed = " + passed + "  overtraced = " + sf.isOvertraced() +
					"  radius test = " + radiusTest + "  NDDE = "
					+ sf.getNDDE() + "  maxDist = " + maxDistance + "  diameter = " + avgRadius*2 +
					"  numRevs = " + sf.numRevolutions() +
					"  center closeness error = " + err + " radius ratio = " +
					(avgRadius/bbRadius) + "  close to avg center = " + err2 + "  endpt dis = " + endptDis);
	}

	public double getError() {
		return err;
	}

	public double getAvgRadBBRadRatio() {
		return avgRadBBRadRatio;
	}

	public double getMaxDistanceToCenter() {
		return maxDistanceToCenter;
	}

	public String getName() {
		return Fit.SPIRAL;
	}

	public double radiusTestPassed() {
		if (radiusTest)
			return 1.0;
		else
			return 0.0;
	}

	public DrawnShape getShape() {
		double radius = (sf.getBounds().getHeight()/2+sf.getBounds().getWidth()/2)/2;
		double ox, oy;
		if (isDescending()) {
			ox = stroke.get(0).getX();
			oy = stroke.get(0).getY();
		}
		else {
			ox = stroke.get(stroke.size()-1).getX();
			oy = stroke.get(stroke.size()-1).getY();
		}
		TSpiral ts = new TSpiral(center.getX(), center.getY(), ox, oy, radius,
				(int)sf.numRevolutions(), sf.isClockwise(), isDescending());
		return ts;
	}

	public Shape get2DShape() {
		return beautified;
	}

	public boolean passed() {
		return passed;
	}

	public boolean isDescending() {
		return desc;
	}

	public double getAvgRadius() {
		return avgRadius;
	}

	public ArrayList<BStroke> getSubStrokes() {
		return subStrokes;
	}

	public ArrayList<CircularFit> getSubStrokeFits() {
		return subStrokeFits;
	}

	public boolean centerClosenessTestPassed() {
		return centerClosenessTest;
	}

	private void calcCenter() {
		boolean useBoundingBox = true;

		// calculate avg center
		double avgX=0, avgY=0;
		for (int i = 0; i < stroke.size(); i++) {
			avgX += stroke.get(i).getX();
			avgY += stroke.get(i).getY();
		}
		avgX /= stroke.size();
		avgY /= stroke.size();
		avgCenter = new Point2D.Double(avgX, avgY);

		// calculate center as center of the bounding box
		if (useBoundingBox)
			center = new Point2D.Double(sf.getBounds().getCenterX(), sf.getBounds().getCenterY());

		// calculate center as average center
		else
			center = avgCenter;
	}

	private void calcAvgRadius() {
		avgRadius = 0;
		for (int i = 0; i < stroke.size(); i++) {
			avgRadius += Point2D.distance(stroke.get(i).getX(), stroke.get(i).getY(),
								   center.getX(), center.getY());
		}
		avgRadius /= stroke.size();
	}

	public int getID() {
		return Fit.SPIRAL_INT;
	}
}
