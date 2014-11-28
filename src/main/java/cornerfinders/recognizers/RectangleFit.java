package cornerfinders.recognizers;

import java.awt.Shape;
import java.util.ArrayList;

import edu.mit.sketch.geom.Point;
import edu.mit.sketch.language.shapes.DrawnShape;
import edu.mit.sketch.uml.ShapeCollection;
import edu.mit.sketch.uml.UMLCreateObjectChecker;
import edu.mit.sketch.uml.UMLStrokeCollection;
import edu.tamu.hammond.sketch.shapes.TRectangle;

public class RectangleFit implements Fit, StrokeThresholds {

	private boolean debug = false;
	private boolean useTracy = false;
	private double err = 0.0;
	private Shape beautified;
	private boolean passed = false;
	private UMLCreateObjectChecker checker;
	private BStroke stroke;

	public RectangleFit(BStroke stroke, EllipseFit ellipseFit) {
		if (useTracy) {
			ArrayList<Point> pts = new ArrayList<Point>();
			for (int i = 0; i < stroke.size(); i++) {
				pts.add(new Point(stroke.get(i).getX(), stroke.get(i).getY(), stroke.get(i).getTime()));
			}
			edu.mit.sketch.uml.Stroke s = new edu.mit.sketch.uml.Stroke(pts.get(0).getTimeStamp(), pts);
			ArrayList<edu.mit.sketch.uml.Stroke> strks = new ArrayList<edu.mit.sketch.uml.Stroke>();
			strks.add(s);
			ShapeCollection coll = new ShapeCollection();
			UMLStrokeCollection c = new UMLStrokeCollection(strks, coll);
			checker = new UMLCreateObjectChecker(c);
			if (checker.isObject(1))
				passed = true;
			//System.out.println("Rectangle test passed: " + passed);
		}
		else {

			this.stroke = stroke;
			passed = true;
			// test 1: compare lsqe of bounding box to shape
			double lsqe = 0.0;
			for (int i = 0; i < stroke.size(); i++) {
				lsqe += StrokeFeatures.rectToPtdistance(stroke.getFeatures().getBounds(), stroke.get(i).getX(), stroke.get(i).getY());
			}
			lsqe /= stroke.getFeatures().getStrokeLength();
			//if (lsqe > 1.25)
			//	passed = false;

			//if (ellipseFit.passed())
			//	passed = false;


			// test 2: feature area error must be low
			err = FeatureArea.toRectangle(stroke.getXValues(), stroke.getYValues(), stroke.getFeatures().getBounds())
			        / (stroke.getFeatures().getBounds().getWidth()*stroke.getFeatures().getBounds().getHeight());
			if (err > 0.35)
				passed = false;

			// test 3: diagonal of bounding box should be similar to major axis
			double diagLength = Math.sqrt(stroke.getFeatures().getBounds().getWidth() *
					stroke.getFeatures().getBounds().getWidth() +
					stroke.getFeatures().getBounds().getHeight() *
					stroke.getFeatures().getBounds().getHeight());
			double ratio = Math.abs(1.0 - ellipseFit.getMajorAxisLength()/diagLength);
			if (ratio > 0.14)
				passed = false;

			// test 3: dcr should be high
			// if (m_features.getDCR() < M_DCR_TO_BE_RECTANGLE)
			// m_passed = false;

			// test 4: ndde should be low
			// if (m_features.getNDDE() > M_NDDE_VERY_HIGH)
			// m_passed = false;

			// test 5: endpoints of stroke must be close
			if (stroke.getFeatures().getEndptStrokeLengthRatio() > M_POLYGON_PCT)
				passed = false;

			// test 6: check slope difference between start and end slope

			double area = stroke.getFeatures().getBounds().getWidth() * stroke.getFeatures().getBounds().getHeight();
			/*if (stroke.getFeatures().getDCR() < 6.8 &&
				(stroke.getFeatures().getNDDE() > 0.8 && area > 800) &&
				err > 0.15)
				passed = false;*/

			if (debug)
				System.out.println("Rectangle Fit: passed = " + passed + " err = " + err
			          + " lsqe = " + lsqe  + " major:bbdiag ratio = " + ratio
			          + " dcr = " + stroke.getFeatures().getDCR() + " ndde = "
			          + stroke.getFeatures().getNDDE() + " endpt:sl ratio = "
			          + stroke.getFeatures().getEndptStrokeLengthRatio()
			          + " area = " + area);
		}
	}

	public Shape get2DShape() {
		return beautified;
	}

	public double getError() {
		return err;
	}

	public String getName() {
		return Fit.RECTANGLE;
	}

	public DrawnShape getShape() {
		TRectangle tr;
		if (!useTracy) {
			tr = new TRectangle((int)stroke.getFeatures().getBounds().getX(),
				(int)stroke.getFeatures().getBounds().getY(),
				(int)stroke.getFeatures().getBounds().getWidth(),
				(int)stroke.getFeatures().getBounds().getHeight());
		}
		else {
			tr = new TRectangle(checker.getX(), checker.getY(), checker.getWidth(), checker.getHeight());
		}
		return tr;
	}

	public boolean passed() {
		return passed;
	}

	public int getID() {
		return Fit.RECTANGLE_INT;
	}

}
