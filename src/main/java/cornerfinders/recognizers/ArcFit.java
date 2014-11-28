package cornerfinders.recognizers;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import edu.mit.sketch.language.shapes.DrawnShape;
import edu.tamu.bpaulson.newrecognizer.PerpendicularBisector;
import edu.tamu.hammond.sketch.shapes.TArc;

/**
 * ArcFit class - fit a stroke to an arc
 * @author bpaulson
 */
public class ArcFit implements Fit, CircularFit, StrokeThresholds {

	private BStroke stroke;
	private StrokeFeatures sf;
	private double err;
	private boolean passed;
	private boolean debug = false;
	private double radius;
	private Point2D center;
	private Shape beautified;

	/**
	 * Constructor for the arc fit
	 * @param stroke stroke to fit arc to
 	 */
	public ArcFit(BStroke stroke) {
		passed = true;
		this.stroke = stroke;
		sf = stroke.getFeatures();
		calcCenter();
		calcRadius();

		// test 1: stroke must not be closed or overtraced
		if (sf.isComplete() || sf.isOvertraced())
			passed = false;

		// test 2: make sure NDDE is high (close to 1.0) or low (close to 0.0)
		if (sf.getNDDE() < M_NDDE_HIGH /*&& sf.getNDDE() > M_NDDE_LOW*/ && radius > M_CIRCLE_SMALL)
			passed = false;

		// test 3: dcr must be low
		if (sf.getDCR() > M_DCR_TO_BE_POLYLINE)
			passed = false;

		// test 4: feature area test (results used for error)
		err = calcFeatureArea();
		if (err > M_ARC_FEATURE_AREA)
			passed = false;

		beautified = ((TArc)getShape()).get2DShape();

		// NEW TEST
		/*double arcAreaRatio = 0;
		double area = sf.getBounds().getHeight()*sf.getBounds().getWidth();
		if (((TArc)getShape()).getArcArea() < area)
			arcAreaRatio = ((TArc)getShape()).getArcArea()/area;
		else
			arcAreaRatio = area/((TArc)getShape()).getArcArea();
		if (arcAreaRatio < M_ARC_AREA_RATIO)
			passed = false;*/

		// ANOTHER NEW TEST
		if (!sf.dirWindowPassed())
			passed = false;

		if (debug)
			System.out.println("ArcFit: passed = " + passed + "  center = (" + center.getX() +
					"," + center.getY() + ")  radius = " + radius +
					"  closed = " + sf.isComplete() + "  overtraced = " + sf.isOvertraced() +
					"  length = " + sf.getStrokeLength() + "  NDDE = " +
					sf.getNDDE() + "  DCR = " + sf.getDCR() + "  feature area err = " + err +
					" radius = " + radius + " num revs = " + sf.numRevolutions() +
					/*" arc area ratio = " + arcAreaRatio +*/ " dir window passed = " + sf.dirWindowPassed());
	}

	public double getError() {
		return err;
	}

	public String getName() {
		return Fit.ARC;
	}

	public DrawnShape getShape() {
		TArc ta = new TArc(stroke.get(0).getX(), stroke.get(0).getY(), stroke.get(stroke.size()-1).getX(),
				stroke.get(stroke.size()-1).getY(), center.getX(), center.getY(),
				sf.isClockwise());
		return ta;
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

	public Point2D getCenter() {
		return center;
	}

	private void calcRadius() {
		double sum = 0.0;
		for (int i = 0; i < stroke.size(); i++)
			sum += stroke.get(i).distance(center.getX(), center.getY());
		radius = sum / stroke.size();
	}

	private void calcCenter() {
		Point2D first = new Point2D.Double(stroke.get(0).getX(), stroke.get(0).getY());
		Point2D last = new Point2D.Double(stroke.get(stroke.size()-1).getX(), stroke.get(stroke.size()-1).getY());
		if (sf.getBounds()==null)
			sf.calcBounds();
		PerpendicularBisector pb = new PerpendicularBisector(first, last, sf.getBounds());
		ArrayList<Point2D> intersects = stroke.getIntersection(pb.getBisector());
		if (intersects.size() > 0) {
			Point2D pbIntersect = intersects.get(0);
			if (first != null && last != null && pbIntersect != null) {
				PerpendicularBisector pb1 = new PerpendicularBisector(first, pbIntersect, sf.getBounds());
				PerpendicularBisector pb2 = new PerpendicularBisector(pbIntersect, last, sf.getBounds());
				center = BStroke.getIntersectionPt(pb1.getBisector(), pb2.getBisector());
			}
			if (center == null)
				calcAvgCenter();
		}
		else {
			calcAvgCenter();
		}
	}

	private void calcAvgCenter() {
		double avgX=0, avgY=0;
		for (int i = 0; i < stroke.size(); i++) {
			avgX += stroke.get(i).getX();
			avgY += stroke.get(i).getY();
		}
		avgX /= stroke.size();
		avgY /= stroke.size();
		center = new Point2D.Double(avgX, avgY);
	}

	private double calcFeatureArea() {
		double err1 = FeatureArea.toPoint(stroke.getXValues(), stroke.getYValues(), center);
		err1 /= (Math.PI*radius*radius*sf.numRevolutions());
		err1 = Math.abs(1.0-err1);
		if (Double.isInfinite(err1))
			err1 = Double.NaN;
		return err1;
	}

	public int getID() {
		return Fit.ARC_INT;
	}
}
