package cornerfinders.recognizers;

import java.awt.Shape;
import java.awt.geom.Line2D;

import edu.mit.sketch.language.shapes.DrawnShape;
import edu.tamu.hammond.sketch.shapes.TArc;
import edu.tamu.hammond.sketch.shapes.TLine;

/**
 * LineFit class - fit a stroke to a single line
 * @author bpaulson
 */
public class LineFit implements Fit, StrokeThresholds {

	private BStroke stroke;
	private double err;
	private double lsqe;
	private boolean passed;
	private boolean debug = false;
	private Shape beautified;

	/**
	 * Constructor for the line fit
	 * @param stroke stroke to fit a line to
	 */
	public LineFit(BStroke stroke) {
		passed = true;
		this.stroke = stroke;
		StrokeFeatures sf = stroke.getFeatures();
		Line2D endpts = new Line2D.Double(stroke.get(0).getX(), stroke.get(0).getY(),
				stroke.get(stroke.size()-1).getX(), stroke.get(stroke.size()-1).getY());

		// if only two points then we have a line with no error
		if (stroke.size() <= 2) {
			if (debug)
				System.out.println("LineFit: stroke contains " + stroke.size() + " points");
			err = 0.0;
			return;
		}

		// test 1: least squares error between the stroke points and the line formed by the endpoints
		lsqe = LeastSquares.error(stroke.getXValues(), stroke.getYValues(), endpts);
		lsqe /= sf.getStrokeLength();
		if (lsqe > M_LINE_LS_ERROR_FROM_ENDPTS)
			passed = false;

		// test 2: verify that stroke is not overtraced
		if (sf.isOvertraced())
			passed = false;

		// test 3: make sure line only contains two corners (for endpoints); also allow 3 to be more flexible
		if (sf.numFinalCorners() != 2 && sf.numFinalCorners() != 3)
			passed = false;

		// test 4: test feature area (use as error for fit)
		err = FeatureArea.toLine(stroke.getXValues(), stroke.getYValues(), endpts)/sf.getStrokeLength();
		if (err > M_LINE_FEATURE_AREA)
			passed = false;

		beautified = ((TLine)getShape()).get2DShape();

		if (debug)
			System.out.println("LineFit: passed = " + passed + "  least sq error = " + lsqe +
					"  overtraced = " + sf.isOvertraced() + "  feature area error = " + err + "  endpts = (" +
					endpts.getX1() + "," + endpts.getY1() + ") (" + endpts.getX2() + "," + endpts.getY2() +
					")  corners = " + sf.numFinalCorners() + "  best fit = (" + sf.getBestFitLine().getX1() +
					"," + sf.getBestFitLine().getY1() + ") (" + sf.getBestFitLine().getX2() + "," +
					sf.getBestFitLine().getY2() + ")  is closed = " + sf.isComplete());

		// NEW: use least squares error as error for line instead of feature area
		//err = lsqe;
	}

	public double getError() {
		return err;
	}

	public double getLSQE() {
		return lsqe;
	}

	public String getName() {
		return Fit.LINE;
	}

	public DrawnShape getShape() {
		return new TLine(stroke.getFeatures().m_origX[0], stroke.getFeatures().m_origY[0],
				stroke.getFeatures().m_origX[stroke.getFeatures().m_origX.length-1],
				stroke.getFeatures().m_origY[stroke.getFeatures().m_origY.length-1]);
	}

	public Shape get2DShape() {
		return beautified;
	}

	public boolean passed() {
		return passed;
	}

	public int getID() {
		return Fit.LINE_INT;
	}
}
