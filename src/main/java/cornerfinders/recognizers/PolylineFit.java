package cornerfinders.recognizers;

import java.awt.Shape;
import java.util.ArrayList;

import edu.mit.sketch.language.shapes.DrawnShape;
import edu.tamu.hammond.sketch.shapes.TPoint;
import edu.tamu.hammond.sketch.shapes.TPolyline;

/**
 * PolylineFit class - fit a stroke to a polyline
 * @author bpaulson
 */
public class PolylineFit implements Fit, StrokeThresholds {

	private StrokeFeatures sf;
	private double err;
	private double lsqe;
	private boolean passed;
	private boolean debug = false;
	private boolean allLinesPassed = true;
	private int numPassed = 0;
	private ArrayList<BStroke> subStrokes;
	private ArrayList<TPoint> corners;
	private ArrayList<Integer> cornersIndex;
	private String cornersUsed = "Normal";
	private Shape beautified;

	/**
	 * Constructor for polyline fit
	 * @param stroke stroke to fit a polyline to
	 */
	public PolylineFit(BStroke stroke) {
		passed = true;
		allLinesPassed = true;
		sf = stroke.getFeatures();
		subStrokes = sf.getCornerSubStrokes(true);
		cornersIndex = (ArrayList<Integer>)sf.m_sezginCorners;
		//sf.calcBrandonCorners();
		//cornersIndex = sf.getBrandonCornersInt();
		//subStrokes = sf.getBrandonCornerSubStrokes();
		//subStrokes = sf.getTracyCornerSubStrokes();
		corners = new ArrayList<TPoint>();
		for (int i = 0; i < subStrokes.size(); i++)
			corners.add(subStrokes.get(i).get(0));
		if (subStrokes.size() > 0)
			corners.add(subStrokes.get(subStrokes.size()-1).get(subStrokes.get(subStrokes.size()-1).size()-1));
		err = 0.0;
		lsqe = 0.0;

		// test 1: we need at least 2 substrokes
		if (subStrokes.size() < 2)
			passed = false;
		else {
			// test 2: run line test on all sub strokes and get a cumulative error
			for (int i = 0; i < subStrokes.size(); i++) {
				subStrokes.get(i).calcLineFit();
				err += subStrokes.get(i).getLineFit().getError();
				lsqe += subStrokes.get(i).getLineFit().getLSQE();
				if (!subStrokes.get(i).getLineFit().passed()) {
					allLinesPassed = false;
					passed = false;
				}
				else
					numPassed++;
			}
			//err /= subStrokes.size();
			//lsqe /= subStrokes.size();
			err /= sf.getStrokeLength();
			lsqe /= sf.getStrokeLength();
			/*if (!passed) {
			passed = true;
			allLinesPassed = true;
			double tracyErr = 0.0;
			double tracyLsqe = 0.0;
			ArrayList<TPoint> tracyCorners = new ArrayList<TPoint>();
			ArrayList<BStroke> tracySubStrokes = sf.getTracyCornerSubStrokes();
			for (int i = 0; i < tracySubStrokes.size(); i++)
				tracyCorners.add(tracySubStrokes.get(i).get(0));
			if (tracySubStrokes.size() > 0)
				tracyCorners.add(tracySubStrokes.get(tracySubStrokes.size()-1).get(tracySubStrokes.get(tracySubStrokes.size()-1).size()-1));
			for (int i = 0; i < tracySubStrokes.size(); i++) {
				tracySubStrokes.get(i).calcLineFit();
				tracyErr += tracySubStrokes.get(i).getLineFit().getError();
				tracyLsqe += tracySubStrokes.get(i).getLineFit().getLSQE();
				if (!tracySubStrokes.get(i).getLineFit().passed()) {
					allLinesPassed = false;
					passed = false;
				}
			}
			tracyErr /= sf.getStrokeLength();
			tracyLsqe /= sf.getStrokeLength();
			//tracyErr /= tracySubStrokes.size();
			//tracyLsqe /= tracySubStrokes.size();
			if (passed) {
				err = tracyErr;
				lsqe = tracyLsqe;
				subStrokes = tracySubStrokes;
				corners = tracyCorners;
				cornersUsed = "Tracy";
			}
			}*/
			/*if (!passed) {
				passed = true;
				allLinesPassed = true;
				double curvErr = 0.0;
				double curvLsqe = 0.0;
				ArrayList<TPoint> curvCorners = new ArrayList<TPoint>();
				ArrayList<BStroke> curvSubStrokes = sf.getCurvCornerSubStrokes();
				for (int i = 0; i < curvSubStrokes.size(); i++)
					curvCorners.add(curvSubStrokes.get(i).get(0));
				if (curvSubStrokes.size() > 0)
					curvCorners.add(curvSubStrokes.get(curvSubStrokes.size()-1).get(curvSubStrokes.get(curvSubStrokes.size()-1).size()-1));
				for (int i = 0; i < curvSubStrokes.size(); i++) {
					curvSubStrokes.get(i).calcLineFit();
					curvErr += curvSubStrokes.get(i).getLineFit().getError();
					curvLsqe += curvSubStrokes.get(i).getLineFit().getLSQE();
					if (!curvSubStrokes.get(i).getLineFit().passed()) {
						allLinesPassed = false;
						passed = false;
					}
				}
				//curvErr /= sf.getStrokeLength();
				//curvLsqe /= sf.getStrokeLength();
				curvErr /= curvSubStrokes.size();
				curvLsqe /= curvSubStrokes.size();
				if (passed) {
					err = curvErr;
					lsqe = curvLsqe;
					subStrokes = curvSubStrokes;
					corners = curvCorners;
					cornersUsed = "Curvature";
				}
			}*/
			/*if (!passed) {
				sf.calcBrandonCorners();
				passed = true;
				double brandonErr = 0.0;
				double brandonLsqe = 0.0;
				ArrayList<BStroke> brandonSubStrokes = sf.getBrandonCornerSubStrokes();
				ArrayList<TPoint> brandonCorners = new ArrayList<TPoint>();
				for (int i = 0; i < brandonSubStrokes.size(); i++)
					brandonCorners.add(brandonSubStrokes.get(i).get(0));
				if (brandonSubStrokes.size() > 0)
					brandonCorners.add(brandonSubStrokes.get(brandonSubStrokes.size()-1).get(brandonSubStrokes.get(brandonSubStrokes.size()-1).size()-1));
				for (int i = 0; i < brandonSubStrokes.size(); i++) {
					brandonSubStrokes.get(i).calcLineFit();
					brandonErr += brandonSubStrokes.get(i).getLineFit().getError();
					brandonLsqe += brandonSubStrokes.get(i).getLineFit().getLSQE();
					if (!brandonSubStrokes.get(i).getLineFit().passed())
						passed = false;
				}
				brandonErr /= sf.getStrokeLength();
				brandonLsqe /= sf.getStrokeLength();
				if (passed) {
					err = brandonErr;
					lsqe = brandonLsqe;
					subStrokes = brandonSubStrokes;
					corners = brandonCorners;
					cornersUsed = "Brandon";
				}
			}*/

			// test 3: make sure error is low
			/*if (err > M_POLYLINE_ERROR) {
				passed = false;
			}*/

			if (sf.getDCR() < M_DCR_TO_BE_POLYLINE)
				passed = false;

			// test 3.2: if total lsqe is low then accept even if not all substrokes passed polyline test
			if (lsqe < M_POLYLINE_LS_ERROR || allLinesPassed)
				passed = true;

			// test 3: test curvature corners as well for better fit
			/*if (!passed) {
				boolean passed2 = true;
				double err2 = 0.0, lsqe2 = 0.0;
				ArrayList<BStroke> curvSubStrokes = sf.getCurvCornerSubStrokes();
				for (int i = 0; i < curvSubStrokes.size(); i++) {
					curvSubStrokes.get(i).calcLineFit();
					err2 += curvSubStrokes.get(i).getLineFit().getError();
					lsqe2 += curvSubStrokes.get(i).getLineFit().getLSQE();
					if (!curvSubStrokes.get(i).getLineFit().passed())
						passed2 = false;
				}
				err2 /= curvSubStrokes.size();
				lsqe2 /= curvSubStrokes.size();
				passed = passed2;
			}*/

			//if (err > M_POLYLINE_ERROR)
			//	passed = false;
		}

		beautified = ((TPolyline)getShape()).get2DShape();

		// since tails may have been removed, remove first and last corner and replace with actual end points
		if (corners.size() >= 2) {
			corners.remove(corners.size()-1);
			corners.add(stroke.getFeatures().getOrigEndPoint());
			corners.remove(0);
			corners.add(0, stroke.getFeatures().getOrigStartPoint());
		}

		if (debug)
			System.out.println("PolylineFit: passed = " + passed + "  error = " + err
					+ "  lsqe = " + lsqe + "  dcr = " + sf.getDCR() + "  sub strokes = " +
					subStrokes.size() + "  corners used = " + cornersUsed + " lines passed = " + allLinesPassed);
	}

	public double getError() {
		return err;
	}

	public double getLSQE() {
		return lsqe;
	}

	public String getName() {
		return Fit.POLYLINE;
	}

	public DrawnShape getShape() {
		TPolyline tp = new TPolyline(corners);
		return tp;
	}

	public Shape get2DShape() {
		return beautified;
	}

	public boolean passed() {
		return passed;
	}

	public boolean allLinesPassed(){
		return allLinesPassed;
	}

	public double getPercentPassed() {
		return (double)numPassed/(double)subStrokes.size();
	}

	public ArrayList<BStroke> getSubStrokes() {
		return subStrokes;
	}

	public ArrayList<TPoint> getCorners() {
		return corners;
	}

	public ArrayList<Integer> getCornersIndex() {
		return cornersIndex;
	}

	public int getID() {
		return Fit.POLYLINE_INT;
	}
}
