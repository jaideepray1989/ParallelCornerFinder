package cornerfinders.recognizers;

import java.awt.Shape;
import java.util.ArrayList;

import edu.mit.sketch.geom.Point;
import edu.mit.sketch.language.shapes.DrawnShape;
import edu.mit.sketch.uml.UMLArrowChecker;
import edu.tamu.hammond.sketch.shapes.TArrow;
import edu.tamu.hammond.sketch.shapes.TPoint;

/**
 * Fit stroke to an arrow
 * @author bpaulson
 */
public class ArrowFit implements Fit, StrokeThresholds {

	private double err;
	private boolean passed;
	private UMLArrowChecker checker;
	private int arrowType = -1;

	/**
	 * Constructor for arrow fit
	 * @param stroke stroke to fit arrow to
	 */
	public ArrowFit(BStroke stroke) {
		passed = false;
		err = 0;
		ArrayList<Point> pts = new ArrayList<Point>();
		for (int i = 0; i < stroke.size(); i++) {
			pts.add(new Point(stroke.get(i).getX(), stroke.get(i).getY(), stroke.get(i).getTime()));
		}
		edu.mit.sketch.uml.Stroke s = new edu.mit.sketch.uml.Stroke(pts.get(0).getTimeStamp(), pts);
		ArrayList<edu.mit.sketch.uml.Stroke> strks = new ArrayList<edu.mit.sketch.uml.Stroke>();
		strks.add(s);
		checker = new UMLArrowChecker(strks);
		if (checker.isArrow(null))
			passed = true;
		arrowType = checker.getType();
		//System.out.println("Arrow test passed: " + passed);
	}

	public Shape get2DShape() {
		return ((TArrow)getShape()).get2DShape();
	}

	public double getError() {
		return err;
	}

	public String getName() {
		return Fit.ARROW;
	}

	public DrawnShape getShape() {
		TPoint tail = new TPoint(checker.getA().x, checker.getA().y, checker.getA().time_stamp);
		TPoint head = new TPoint(checker.getB().x, checker.getB().y, checker.getB().time_stamp);
		TArrow ta = new TArrow(tail, head, arrowType);
		return ta;
	}

	public boolean passed() {
		return passed;
	}

	public int getID() {
		return Fit.ARROW_INT;
	}
}
