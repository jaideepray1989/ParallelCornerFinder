package cornerfinders.recognizers;

import java.awt.Shape;
import java.awt.geom.Point2D;

import Jama.Matrix;

import edu.mit.sketch.language.shapes.DrawnShape;
import edu.tamu.hammond.sketch.shapes.TArc;
import edu.tamu.hammond.sketch.shapes.TCurve;
import edu.tamu.hammond.sketch.shapes.TPoint;

/**
 * CurveFit class - fit a stroke to a curve
 * @author bpaulson
 */
public class CurveFit implements Fit, StrokeThresholds {

	private BStroke stroke;
	private StrokeFeatures sf;
	private double err;
	private double fitErr;
	private boolean passed;
	private boolean debug = false;
	private Point2D[] P;
	private int degree = 4;
	private final int minDegree = 4;
	private final int maxDegree = 5;
	private boolean curveFailed = false;
	private Shape beautified;

	/**
	 * Constructor for the curve fit
	 * @param stroke stroke to fit curve to
	 */
	public CurveFit(BStroke stroke) {
		passed = true;
		this.stroke = stroke;
		sf = stroke.getFeatures();
		//TPoint p1 = stroke.get(0);
		//TPoint p2 = stroke.get(stroke.size()-1);
		boolean allFailed = true;
		double minErr = Double.MAX_VALUE;
		int minErrDegree = minDegree;
		for (degree = minDegree; degree <= maxDegree; degree++) {
			calcControlPts();
			if (curveFailed)
				continue;
			calcError();
			if (err < minErr) {
				minErr = err;
				minErrDegree = degree;
				allFailed = false;
			}
		}
		if (allFailed) {
			passed = false;
			if (debug)
				System.out.println("CurveFit: passed = " + passed + "  Failed to calculate control points!");
		}
		else {
			err = minErr;
			degree = minErrDegree;
			//calcError();

			// test 1: make sure NDDE is high (close to 1.0) or low (close to 0.0)
			//if (sf.getNDDE() < M_NDDE_HIGH && sf.getNDDE() > M_NDDE_LOW)
			//	passed = false;

			// test 2: must fit a 3rd degree polynomial
			/*CurveFitter cf = new CurveFitter(stroke.getXValues(), stroke.getYValues());
			cf.doFit(CurveFitter.POLY4);
			err = Math.sqrt(cf.getSumResidualsSqr())/sf.getStrokeLength();
			fitErr = Math.abs(1.0-cf.getFitGoodness());
			if (fitErr > M_CURVE_FIT_ERROR)
				passed = false;
			if (err > M_CURVE_ERROR)
				passed = false;*/


			// test x: see if estimated control points (0 and 3) are close to actual endpoints
			/*double dis1 = P[0].distance(p1.getX(), p1.getY());
			double dis2 = P[3].distance(p2.getX(), p2.getY());
			System.out.println("dis 1 = " + dis1 + "  dis 2 = " + dis2);
			err = 0.0;*/

			// test 1: stroke must not be closed
			if (sf.isComplete())
				passed = false;

			// test 3: dcr must be low
			if (sf.getDCR() > M_DCR_TO_BE_POLYLINE)
				passed = false;

			// test 4: make sure error between stroke and curve is low
			if (err > M_CURVE_ERROR)
				passed = false;

			beautified = ((TCurve)getShape()).get2DShape();

			// NEW TEST:
			double ratio = sf.numCorners()/sf.getStrokeLength();
			//if (ratio < M_CURVE_CORNER_LENGTH_RATIO)
			//	passed = false;

			if (debug) {
				System.out.println("CurveFit: passed = " + passed + "  NDDE = " + sf.getNDDE() +
						" fitErr = " + fitErr + " degree = " + degree + " num corners = "
						+ sf.numFinalCorners() + " err = " + err + " dcr = " + sf.getDCR() +
						" max curv = " + sf.getMaxCurv() + " curve/length = " + ratio);
				stroke.getFeatures().debug(2, true, false);
				stroke.getFeatures().debug(3, true, false);
			}
		}
	}

	public double getError() {
		return err;
	}

	public String getName() {
		return Fit.CURVE;
	}

	public DrawnShape getShape() {
		//TCurve tc = new TCurve(P[0].getX(), P[0].getY(), P[1].getX(), P[1].getY(),
		//		P[2].getX(), P[2].getY(), P[3].getX(), P[3].getY());
		TCurve tc = new TCurve(P);
		return tc;
	}

	public Shape get2DShape() {
		return beautified;
	}

	public boolean passed() {
		return passed;
	}

	private void calcError() {
		err = ((TCurve)getShape()).calcError(stroke);
	}

	// Note: Bezier curve formula
	// B(t) = P0*(1-t)^3 + P1*3t(1-t)^2 + P2*3t^2(1-t) + P3t^3
	// where t = parametirc value, B(t) are the actual curve points,
	// and P0...P4 are the control points
	private void calcControlPts() {
		double[] lengthSoFar = sf.getLengthSoFar();
		TPoint[] B = new TPoint[degree+1];
		double[] tvals = new double[degree+1];
		for (int i = 0; i < tvals.length; i++)
			tvals[i] = Double.NaN;
		P = new Point2D[degree+1];

		// control point for t = 0
		B[0] = stroke.get(0);
		tvals[0] = 0.0;
		if (lengthSoFar==null) {
			curveFailed = true;
			return;
		}
		// find points closest to t = 1/degree and t = (degree-1)/degree
		for (int i = 0; i < lengthSoFar.length; i++) {
			for (int d = 1; d < degree; d++) {
				if (lengthSoFar[i]/sf.getStrokeLength() > ((double)d/(double)degree) &&
						Double.isNaN(tvals[d])) {
					tvals[d] = lengthSoFar[i]/sf.getStrokeLength();
					B[d] = stroke.get(i);
				}

			}
		}
		// control point for t = 1
		B[degree] = stroke.get(stroke.size()-1);
		tvals[degree] = 1.0;


		// find control points
		Matrix A = new Matrix(degree+1,degree+1);
		for (int i = 0; i < degree+1; i++) {
			double t = tvals[i];
			for (int j = 0; j < degree+1; j++) {
				double val = Math.pow(1-t,degree-j)*Math.pow(t,j)*TCurve.binomialCoeff(degree,j);
				A.set(i, j, val);
			}
		}
		Matrix bX = new Matrix(degree+1,1);
		Matrix bY = new Matrix(degree+1,1);
		for (int i = 0; i < degree+1; i++) {
			bX.set(i, 0, B[i].getX());
			bY.set(i, 0, B[i].getY());
		}
		//A.print(8,3);
		//bX.print(8,3);
		try {
			Matrix XX = A.solve(bX);
			Matrix YY = A.solve(bY);
			for (int i = 0; i < degree+1; i++)
				P[i] = new Point2D.Double(XX.get(i,0), YY.get(i,0));
		}
		catch (RuntimeException e) {
			curveFailed = true;
		}
	}

	public int getID() {
		return Fit.CURVE_INT;
	}
}
