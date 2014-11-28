package cornerfinders.recognizers;

import java.awt.Shape;
import java.util.ArrayList;

import edu.mit.sketch.language.shapes.DrawnShape;
import edu.tamu.hammond.sketch.shapes.TPoint;
import edu.tamu.hammond.sketch.shapes.TPolyline;

/**
 * PolygonFit class - fit a stroke to a polygon
 * @author bpaulson
 */
public class PolygonFit implements Fit, StrokeThresholds {

	private StrokeFeatures sf;
	private double err;
	private boolean passed;
	private boolean debug = false;
	private PolylineFit pf;
	private Shape beautified;
	private ArrayList<BStroke> subStrokes;
	private ArrayList<Integer> cornersIndex;

	/**
	 * Constructor for the circle fit
	 * @param stroke stroke to fit circle to
	 * @param ellFit ellipse fit (used so features do not need to be recomputed)
 	 */
	public PolygonFit(BStroke stroke, PolylineFit pFit) {
		passed = true;
		this.pf = pFit;
		sf = stroke.getFeatures();

		// test 1: must have passed a polyline test
		//passed = pFit.passed();

		// test 2: endpoints of stroke must be close
		err = stroke.get(0).distance(stroke.get(stroke.size()-1)) / sf.getStrokeLength();
		if (err > M_POLYGON_PCT)
			passed = false;

		beautified = ((TPolyline)getShape()).get2DShape();
		subStrokes = sf.getSubStrokes(cornersIndex);

		if (debug) {
			System.out.println("PolygonFit: passed = " + passed + " err = " + err);
		}
	}

	public Shape get2DShape() {
		return beautified;
	}

	public double getError() {
		return err;
	}

	public String getName() {
		return Fit.POLYGON;
	}

	public DrawnShape getShape() {
		ArrayList<TPoint> corners = pf.getCorners();
		cornersIndex = pf.getCornersIndex();
		if (corners.size() > 1) {
			corners.remove(corners.size()-1);
			corners.add(corners.get(0));
			cornersIndex.remove(cornersIndex.size()-1);
			cornersIndex.add(cornersIndex.get(0));
		}
		TPolyline pl = new TPolyline(corners);
		return pl;
	}

	public boolean passed() {
		return passed;
	}

	public ArrayList<BStroke> getSubStrokes() {
		return subStrokes;
	}

	public int getID() {
		return Fit.POLYGON_INT;
	}

}
