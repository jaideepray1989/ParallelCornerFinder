package cornerfinders.recognizers;

import java.awt.Shape;
import java.util.ArrayList;

import edu.mit.sketch.language.shapes.DrawnShape;
import edu.tamu.hammond.sketch.shapes.TArc;
import edu.tamu.hammond.sketch.shapes.THelix;

/**
 * HelixFit class - fit a stroke to a helix
 * @author bpaulson
 */
public class HelixFit implements Fit, StrokeThresholds {

	private BStroke stroke;
	private StrokeFeatures sf;
	private double err;
	private boolean passed;
	private boolean debug = false;
	private double avgRadius;
	private EllipseFit ellFit;
	private ArrayList<CircularFit> subStrokeFits;
	private Shape beautified;

	/**
	 * Constructor for the helix fit
	 * @param stroke stroke to fit helix to
	 * @param spiralFit spiral fit of the stroke (to avoid re-computation)
	 * @param ellFit ellipse fit of the stroke (to avoid re-computation)
	 */
	public HelixFit(BStroke stroke, SpiralFit spiralFit, EllipseFit ellFit) {
		passed = true;
		this.stroke = stroke;
		this.ellFit = ellFit;
		sf = stroke.getFeatures();
		calcAvgRadius();
		subStrokeFits = spiralFit.getSubStrokeFits();

		// test 1: stroke must be overtraced
		if (!sf.isOvertraced()) {
			passed = false;
			if (debug)
				System.out.println("HelixFit: passed = " + passed + "  overtraced = " + sf.isOvertraced() +
						"  numRevs = " + sf.numRevolutions());
			return;
		}

		// test 2: make sure NDDE is high (close to 1.0) or low (close to 0.0)
		if (sf.getNDDE() < M_NDDE_HIGH )//&& sf.getNDDE() > M_NDDE_LOW)
			passed = false;

		// test 3: make sure the centers of each substroke are not close
		//if (spiralFit.centerClosenessTestPassed())
		//	passed = false;

		// test 4: largest direction change should be low
		//if (sf.getLargestDirectionChange() > M_LARGEST_DIRECTION_CHANGE)
		//	passed = false;

		// test 5: make sure endpoints are not close (opposite spiral)
		double maxBB;
		if (sf.getBounds().getWidth() > sf.getBounds().getHeight())
			maxBB = sf.getBounds().getWidth();
		else
			maxBB = sf.getBounds().getHeight();
		//double endptDis = stroke.get(0).distance(stroke.get(stroke.size()-1)) / (maxBB);
		double endptDis = stroke.get(0).distance(stroke.get(stroke.size()-1)) / sf.getStrokeLength();
		if (endptDis < M_SPIRAL_DIAMETER_CLOSENESS)
			passed = false;

		beautified = ((THelix)getShape()).get2DShape();

		if (debug)
			System.out.println("HelixFit: passed = " + passed + "  overtraced = " + sf.isOvertraced() +
					"  NDDE = " + sf.getNDDE() + "  numRevs = " +
					sf.numRevolutions() + "  center closeness test = " + !spiralFit.centerClosenessTestPassed() +
					" num substrokes = " +
					subStrokeFits.size() + " closed = " + sf.isComplete() + " endpt dis = " + endptDis +
					"  dcr = " + sf.getDCR());
	}

	public double getError() {
		return err;
	}

	public String getName() {
		return Fit.HELIX;
	}

	public DrawnShape getShape() {
		THelix th = new THelix(stroke.get(0).getX(), stroke.get(0).getY(), stroke.get(stroke.size()-1).getX(),
				stroke.get(stroke.size()-1).getY(), avgRadius, (int)sf.numRevolutions(),
				sf.isClockwise());
		return th;
	}

	public Shape get2DShape() {
		return beautified;
	}

	public boolean passed() {
		return passed;
	}

	public double getAvgRadius() {
		return avgRadius;
	}

	private void calcAvgRadius() {
		double sum = 0.0;
		for (int i = 0; i < stroke.size(); i++) {
			sum += ellFit.getMajorAxis().ptSegDist(stroke.get(i).getX(), stroke.get(i).getY());
		}
		avgRadius = sum/stroke.size();
	}

	public int getID() {
		return Fit.HELIX_INT;
	}
}
