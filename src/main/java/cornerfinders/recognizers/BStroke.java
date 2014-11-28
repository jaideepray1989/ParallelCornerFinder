package cornerfinders.recognizers;

import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;


/**
 * BStroke class -
 * Stores and returns information about a single stroke
 * including all of the points sampled
 * @author bpaulson
 */
public class BStroke extends ArrayList<TPoint> implements StrokeThresholds {

	private static final long serialVersionUID = 3893587152585613082L;
	private StrokeFeatures features;
	private LineFit lineFit;
	private CurveFit curveFit;
	private ArcFit arcFit;
	private CircleFit circleFit;
	private EllipseFit ellipseFit;
	private HelixFit helixFit;
	private SpiralFit spiralFit;
	private PolylineFit polylineFit;
	private ComplexFit complexFit;
	private PolygonFit polygonFit;
	private ArrowFit arrowFit;
	private RectangleFit rectangleFit;
	private final int LINE_SCORE = 1;
	private final int ARC_SCORE = 2;
	private final int CURVE_SCORE = 5;
	private final int ELLIPSE_SCORE = 3;
	private final int CIRCLE_SCORE = 3;
	private final int SPIRAL_SCORE = 5; // these two should be updated
	private final int HELIX_SCORE = 5;
	private double complexScore;

	/**
	 * Default Constructor
	 */
	public BStroke() {
		super();
	}

	/**
	 * Constructor for stroke
	 * @param x x values of stroke
	 * @param y y values of stroke
	 * @param t time values of stroke
	 */
	public BStroke(double[] x, double[] y, long[] t) {
		super();
		add(x,y,t);
	}

	/**
	 * Constructor for stroke
	 * @param pts points to initially add to stroke
	 */
	public BStroke(List<TPoint> pts) {
		super();
		for (int i = 0; i < pts.size(); i++)
			add(pts.get(i).getX(), pts.get(i).getY(), pts.get(i).getTime());
	}

	public BStroke(TStroke s) {
		super();
		for (int i = 0; i < s.numPoints(); i++) {
			add(s.getPoint(i));
		}
	}

	/**
	 * Add a new point to the end of the stroke
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param t time value
	 */
	public void add(double x, double y, long t) {
		add(new TPoint(x,y,t));
	}

	/**
	 * Add points to the end of a stroke
	 * @param x x values
	 * @param y y values
	 * @param t t values
	 */
	public void add(double[] x, double[] y, long[] t) {
		if (x.length != y.length || x.length != t.length)
			System.err.println("Could not add points to stroke - arrays are not equal length");
		else
			for (int i = 0; i < x.length; i++)
				add(x[i], y[i], t[i]);
	}

	/**
	 * Get an array containing the x values of the stroke
	 * @return x values of the stroke
	 */
	public double[] getXValues() {
		double[] x_vals = new double[size()];
		for (int i = 0; i < size(); i++)
			x_vals[i] = get(i).getX();
		return x_vals;
	}

	/**
	 * Get an array containing the y values of the stroke
	 * @return y values of the stroke
	 */
	public double[] getYValues() {
		double[] y_vals = new double[size()];
		for (int i = 0; i < size(); i++)
			y_vals[i] = get(i).getY();
		return y_vals;
	}

	/**
	 * Get an array containing the time values of the stroke
	 * @return time values of the stroke
	 */
	public long[] getTValues() {
		long[] t_vals = new long[size()];
		for (int i = 0; i < size(); i++)
			t_vals[i] = get(i).getTime();
		return t_vals;
	}

	/**
	 * Return the older Stroke from previous verions
	 * @return Stroke object with equivalent points
	 */
	public Stroke getOldStroke() {
		return new Stroke(get(0).getTime(), this);
	}

	/**
	 * Get the features of the stroke
	 * @return stroke features
	 */
	public StrokeFeatures getFeatures() {
		return features;
	}

	public LineFit getLineFit() {
		return lineFit;
	}

	public CurveFit getCurveFit() {
		return curveFit;
	}

	public ArcFit getArcFit() {
		return arcFit;
	}

	public CircleFit getCircleFit() {
		return circleFit;
	}

	public EllipseFit getEllipseFit() {
		return ellipseFit;
	}

	public HelixFit getHelixFit() {
		return helixFit;
	}

	public SpiralFit getSpiralFit() {
		return spiralFit;
	}

	public PolylineFit getPolylineFit() {
		return polylineFit;
	}

	public PolygonFit getPolygonFit() {
		return polygonFit;
	}

	public ArrowFit getArrowFit() {
		return arrowFit;
	}

	public RectangleFit getRectangleFit() {
		return rectangleFit;
	}

	public ComplexFit getComplexFit() {
		if (complexFit == null) {
			ArrayList<BStroke> ss;
			ss = features.getBestSubStrokes();
			calcComplexFit(ss);
			calcComplexScore();
		}
		return complexFit;
	}

	public double getComplexScore() {
		return complexScore;
	}

	public void calcLineFit() {
		lineFit = new LineFit(this);
	}

	public void calcArcFit() {
		arcFit = new ArcFit(this);
	}

	public void calcCurveFit() {
		curveFit = new CurveFit(this);
	}

	public void calcPolylineFit() {
		polylineFit = new PolylineFit(this);
	}

	public void calcPolygonFit(PolylineFit pf) {
		polygonFit = new PolygonFit(this, pf);
	}

	public void calcEllipseFit() {
		ellipseFit = new EllipseFit(this);
	}

	public void calcCircleFit(EllipseFit f) {
		circleFit = new CircleFit(this, f);
	}

	public void calcSpiralFit(CircleFit f) {
		spiralFit = new SpiralFit(this, f);
	}

	public void calcHelixFit(SpiralFit f, EllipseFit e) {
		helixFit = new HelixFit(this, f, e);
	}

	public void calcComplexFit(ArrayList<BStroke> ss) {
		complexFit = new ComplexFit(ss);
	}

	public void calcComplexFit() {
		complexFit = new ComplexFit();
	}

	public void calcArrowFit() {
		arrowFit = new ArrowFit(this);
	}

	public void calcRectangleFit(EllipseFit e) {
		rectangleFit = new RectangleFit(this, e);
	}

	public ArrayList<Fit> recognize(boolean allowComplex) {
		ArrayList<Fit> fits = new ArrayList<Fit>();
		boolean allLines = true;
		try {
		calcFeatures();
		calcLineFit();
		calcArcFit();
		calcCurveFit();
		calcPolylineFit();
		calcEllipseFit();
		calcCircleFit(ellipseFit);
		calcSpiralFit(circleFit);
		calcHelixFit(spiralFit, ellipseFit);
		calcPolygonFit(polylineFit);
		calcArrowFit();
		calcRectangleFit(ellipseFit);
		// hierarchy
		boolean polyAdded = false;
		boolean curveAdded = false;
		boolean ellipseAdded = false;
		boolean circleAdded = false;
		boolean arcAdded = false;
		boolean spiralAdded = false;
		boolean rectAdded = false;
		if (lineFit.passed()) {
			fits.add(lineFit);
		}
		/*if (arrowFit.passed()) {
			fits.add(arrowFit);
		}*/
		if ((rectangleFit.passed() && !ellipseFit.passed()) || (rectangleFit.passed() && ellipseFit.passed() && (rectangleFit.getError() < ellipseFit.getError() || rectangleFit.getError() < 0.18) && getFeatures().numRevolutions() < 0.92)) {
			fits.add(rectangleFit);
			rectAdded = true;
		}
		if (arcFit.getError() < polylineFit.getError() && arcFit.passed()) {
			fits.add(arcFit);
			arcAdded = true;
		}
		if (polylineFit.passed() && ((features.getDCR() > M_DCR_TO_BE_POLYLINE_STRICT
				&& polylineFit.getSubStrokes().size() < M_POLYLINE_SUBSTROKES_LOW) ||
				(polylineFit.allLinesPassed() && features.getDCR() > M_DCR_TO_BE_POLYLINE
				&& polylineFit.getSubStrokes().size() < M_POLYLINE_SUBSTROKES_LOW))) {
			//System.out.println("ADD1");
			fits.add(polylineFit);
			polyAdded = true;
		}
		if (circleFit.getError() < polylineFit.getError() && circleFit.passed() && !features.isOvertraced()) {
			if (//(polylineFit.allLinesPassed() || features.getDCR() > M_DCR_TO_BE_POLYLINE)
					 !polyAdded && polylineFit.passed()//&& polylineFit.getSubStrokes().size() < M_POLYLINE_SUBSTROKES_LOW
					&& polylineFit.getSubStrokes().size() < CIRCLE_SCORE &&
					circleFit.getRadius() > M_CIRCLE_SMALL) {
				//System.out.println("circle size: " + circleFit.getRadius());
				fits.add(polylineFit);
				polyAdded = true;
			}
			fits.add(circleFit);
			circleAdded = true;
		}
		if (ellipseFit.getError() < polylineFit.getError() && ellipseFit.passed()
				&& !circleFit.passed() && !features.isOvertraced()) {
			if (//(polylineFit.allLinesPassed() || features.getDCR() > M_DCR_TO_BE_POLYLINE_STRICT)
					 !polyAdded && polylineFit.passed()//&& polylineFit.getSubStrokes().size() < M_POLYLINE_SUBSTROKES_LOW
					&& polylineFit.getSubStrokes().size() < ELLIPSE_SCORE &&
					ellipseFit.getMajorAxisLength() > M_ELLIPSE_SMALL) {
				//System.out.println("ellipse size: " + ellipseFit.getMajorAxisLength());
				fits.add(polylineFit);
				polyAdded = true;
			}
			fits.add(ellipseFit);
			ellipseAdded = true;
			if (!circleAdded) {
				fits.add(circleFit);
				circleAdded = true;
			}
		}
		if (arcFit.passed() && !arcAdded) {
			/*if (polylineFit.passed() && !polyAdded && polylineFit.getError() < arcFit.getError()) {
				fits.add(polylineFit);
				polyAdded = true;
			}*/
			fits.add(arcFit);
			arcAdded = true;
		}
		if (circleFit.passed() && !circleAdded) {
			if (features.isOvertraced() && spiralFit.passed()) {
				fits.add(spiralFit);
				spiralAdded = true;
			}
			if (//(polylineFit.allLinesPassed() || features.getDCR() > M_DCR_TO_BE_POLYLINE)
					 !polyAdded && polylineFit.passed()//&& polylineFit.getSubStrokes().size() < M_POLYLINE_SUBSTROKES_LOW
					&& polylineFit.getSubStrokes().size() < CIRCLE_SCORE &&
					circleFit.getRadius() > M_CIRCLE_SMALL) {
				//System.out.println("circle size: " + circleFit.getRadius());
				fits.add(polylineFit);
				polyAdded = true;
			}
			fits.add(circleFit);
			circleAdded = true;
		}
		if (ellipseFit.passed() && !ellipseAdded) {
			if (features.isOvertraced() && spiralFit.passed() && !spiralAdded) {
				fits.add(spiralFit);
				spiralAdded = true;
			}
			if (//(polylineFit.allLinesPassed() || features.getDCR() > M_DCR_TO_BE_POLYLINE_STRICT)
					 !polyAdded && polylineFit.passed() //&& polylineFit.getSubStrokes().size() < M_POLYLINE_SUBSTROKES_LOW
					&& polylineFit.getSubStrokes().size() < ELLIPSE_SCORE &&
					ellipseFit.getMajorAxisLength() > M_ELLIPSE_SMALL) {
				//System.out.println("ellipse size: " + ellipseFit.getMajorAxisLength());
				fits.add(polylineFit);
				polyAdded = true;
			}
			fits.add(ellipseFit);
			ellipseAdded = true;
			if (!circleAdded) {
				fits.add(circleFit);
				circleAdded = true;
			}
		}
		if (helixFit.passed()) {
			if (allowComplex) {
				ArrayList<BStroke> ss;
				ss = features.getBestSubStrokes();
				calcComplexFit(ss);
				if (complexFit.getSubFits().size()>1) {
					calcComplexScore();
					if (complexScore < HELIX_SCORE)
						fits.add(complexFit);
				}
			}
			fits.add(helixFit);
		}
		if (rectangleFit.passed() && !rectAdded) {
			fits.add(rectangleFit);
		}
		if (curveFit.passed()) { //&& (curveFit.getError() < polylineFit.getError() || !polylineFit.passed())) {
			fits.add(curveFit);
			curveAdded = true;
		}
		if (spiralFit.passed() && !spiralAdded) {
			fits.add(spiralFit);
			spiralAdded = true;
		}
		if (!polyAdded && polylineFit.passed()) {
			//System.out.println("ADD2");
			fits.add(polylineFit);
			polyAdded = true;
		}
		if (!curveAdded && curveFit.passed()) {
			fits.add(curveFit);
			curveAdded = true;
		}
		if (fits.size()==0 || fits.get(0) instanceof CurveFit || fits.get(0) instanceof PolylineFit) {
			if (allowComplex) {
				//if (fits.size()==0) {
					ArrayList<BStroke> ss;
					ss = features.getBestSubStrokes();
					calcComplexFit(ss);
					if (complexFit.getSubFits().size()>1) {
				//}
				//else {
				//	calcComplexFit();
				//}
				for (int i = 0; i < complexFit.getSubFits().size() && allLines; i++) {
					if (!(complexFit.getSubFits().get(i) instanceof LineFit) &&
						!(complexFit.getSubFits().get(i) instanceof PolylineFit))
						allLines = false;
				}
				if (allLines && !polyAdded) {
					//System.out.println("ADD3");
					fits.add(polylineFit);
					polyAdded = true;
				}
				/*if (!allLines && polyAdded && complexFit.numPrimitives() < polylineFit.getSubStrokes().size()) {
					boolean found = false;
					for (int i = 0; i < fits.size() && !found; i++) {
						if (fits.get(i) instanceof PolylineFit) {
							found = true;
							fits.add(i, complexFit);
						}
					}
				}
				else*/
					fits.add(complexFit);
					calcComplexScore();
					//System.out.println("complex size: " + complexFit.getSubFits().size());
					//System.out.println("complex score: " + complexScore);
					//System.out.println("poly score: " + polylineFit.getSubStrokes().size());
					if (polyAdded && !allLines && complexScore < polylineFit.getSubStrokes().size()) {//(complexFit.getSubFits().size()*2) < polylineFit.getSubStrokes().size()) {
						//fits.remove(polylineFit);
						//polyAdded = false;
						fits.remove(complexFit);
						fits.add(fits.indexOf(polylineFit),complexFit);
					}
					if ((fits.get(0) instanceof CurveFit) && complexScore < CURVE_SCORE) {
						fits.remove(complexFit);
						fits.add(fits.indexOf(curveFit),complexFit);
					}
					if (allLines || complexFit.getSubFits().size()==1)
						fits.remove(complexFit);
				//System.out.println("complex err = " + complexFit.getError() + " curve err = " + curveFit.getError() +
				//		" poly err = " + polylineFit.getError() + " num complex = " + complexFit.getSubFits().size() +
				//		" num poly = " + polylineFit.getSubStrokes().size());

					// check for complex tails
					if (fits.get(0) == complexFit) {
						ArrayList<BStroke> s = complexFit.getStrokes();
						double first = s.get(0).getFeatures().getStrokeLength();
						double last = s.get(s.size()-1).getFeatures().getStrokeLength();
						double ratio = 0;
						if (first < last) {
							for (int i = 1; i < s.size(); i++)
								ratio += s.get(i).getFeatures().getStrokeLength();
							ratio = first/ratio;
						}
						else {
							for (int i = 0; i < s.size()-1; i++)
								ratio += s.get(i).getFeatures().getStrokeLength();
							ratio = last/ratio;
						}
						//System.out.println("ratio = " + ratio);
						if (ratio < M_RATIO_TO_REMOVE_TAIL) {
							if (complexFit.getSubFits().size()==2) {
								if (first < last)
									fits.add(0,complexFit.getSubFits().get(1));
								else
									fits.add(0,complexFit.getSubFits().get(0));
							}
							else {
								if (first < last)
									complexFit.getSubFits().remove(0);
								else
									complexFit.getSubFits().remove(complexFit.getSubFits().size()-1);
							}
						}
					}


				if (!polyAdded) {
					//System.out.println("ADD4");
					fits.add(polylineFit);
					polyAdded = true;
				}
				}
			}
		}
		if (!polyAdded) {
			fits.add(polylineFit);	// default to polyline fit
			polyAdded = true;
		}

		// check for polygon fit
		if (polygonFit.passed()) {
			boolean stop = false;
			for (int i = 0; i < fits.size() && !stop; i++) {
				if (fits.get(i).equals(polylineFit)) {
					stop = true;
					fits.add(i, polygonFit);
					if (rectangleFit.passed() && fits.indexOf(rectangleFit) > i) {
						fits.remove(rectangleFit);
						fits.add(i, rectangleFit);
					}
				}
			}
		}

		}
		catch(Exception e) {e.printStackTrace();}
		return fits;
	}

	private void calcComplexScore() {
		complexScore = 0;
		for (int i = 0; i < complexFit.getSubFits().size(); i++)  {
			Fit f = complexFit.getSubFits().get(i);
			if (f instanceof LineFit)
				complexScore += LINE_SCORE;
			else if (f instanceof ArcFit)
				complexScore += ARC_SCORE;
			else if (f instanceof CurveFit)
				complexScore += CURVE_SCORE;
			else if (f instanceof SpiralFit)
				complexScore += SPIRAL_SCORE;
			else if (f instanceof HelixFit)
				complexScore += HELIX_SCORE;
			else if (f instanceof EllipseFit)
				complexScore += ELLIPSE_SCORE;
			else if (f instanceof CircleFit)
				complexScore += CIRCLE_SCORE;
			else if (f instanceof PolylineFit)
				complexScore += ((PolylineFit)f).getSubStrokes().size();
		}
	}

	/**
	 * Method used to calculate the features of the stroke
	 */
	public void calcFeatures() {
		features = new StrokeFeatures(getXValues(), getYValues(), getTValues());
	}

	/**
	 * Method used to find the point where the given line intersects the stroke
	 * @param line line to find the intersection with
	 * @return intersection points
	 */
	public ArrayList<Point2D> getIntersection(Line2D.Double line) {
		ArrayList<Point2D> intersectionPts = new ArrayList<Point2D>();
		Point2D intersect = null;
		for (int i = 0; i < size()-1; i++) {
			Point2D p1 = new Point2D.Double(get(i).getX(), get(i).getY());
			Point2D p2 = new Point2D.Double(get(i+1).getX(), get(i+1).getY());
			if (line.intersectsLine(p1.getX(), p1.getY(), p2.getX(), p2.getY())) {
				intersect = getIntersectionPt(line, new Line2D.Double(p1.getX(), p1.getY(),
						p2.getX(), p2.getY()));
				intersectionPts.add(intersect);
			}
		}
		if (intersectionPts.size() < 2) {
			Point2D p1 = new Point2D.Double(get(0).getX(), get(0).getY());
			Point2D p2 = new Point2D.Double(get(size()-1).getX(), get(size()-1).getY());
			if (line.intersectsLine(p1.getX(), p1.getY(), p2.getX(), p2.getY())) {
				intersect = getIntersectionPt(line, new Line2D.Double(p1.getX(), p1.getY(),
						p2.getX(), p2.getY()));
				intersectionPts.add(intersect);
			}
		}
		return intersectionPts;
	}

	/**
	 * Method used to find the intersection point between two lines
	 * @param l1 line 1
	 * @param l2 line 2
	 * @return intersection point between line1 and line2
	 */
	public static Point2D getIntersectionPt(Line2D.Double l1, Line2D.Double l2) {
		Point2D.Double intersect = null;
		double l1slope = (l1.y2-l1.y1)/(l1.x2-l1.x1);
		double l2slope = (l2.y2-l2.y1)/(l2.x2-l2.x1);
		if (l1slope == l2slope)
			return null;
		double l1intercept = (-l1.x1*l1slope)+l1.y1;
		double l2intercept = (-l2.x1*l2slope)+l2.y1;
		if ((l1.x2-l1.x1)==0) {
			double x = l1.x2;
			double y = x*l2slope+l2intercept;
			return new Point2D.Double(x, y);
		}
		if ((l2.x2-l2.x1)==0) {
			double x = l2.x2;
			double y = x*l1slope+l1intercept;
			return new Point2D.Double(x, y);
		}
		Matrix a = new Matrix(2,2);
		Matrix b = new Matrix(2,1);
		a.set(0,0,-l1slope);
		a.set(0,1,1);
		a.set(1,0,-l2slope);
		a.set(1,1,1);
		b.set(0,0,l1intercept);
		b.set(1,0,l2intercept);
		Matrix result = a.solve(b);
		intersect = new Point2D.Double(result.get(0,0), result.get(1,0));
		return intersect;
	}

	/**
	 * Return a buffered image version of the stroke
	 * @return image version of the stroke
	 */
	public BufferedImage getImage() {
		if (features == null)
			calcFeatures();
		BufferedImage image =
			new BufferedImage((int)(features.getBounds().getWidth()+2),
					(int)(features.getBounds().getHeight()+2), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		g2.setColor(Color.BLACK);
		g2.setStroke(new BasicStroke(2.0f));
		paint(g2, (int)features.getBounds().getMinX()-1, (int)features.getBounds().getMinY()-1);
		int num = 0;
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				if (image.getRGB(i, j) == Color.BLACK.getRGB())
					num++;
			}
		}
		g2.dispose();
		return image;
	}

	/**
	 * Specifies how a stroke should be painted
	 * @param g graphics object to paint to
	 */
	public void paint(Graphics g) {
		for (int i = 0; i < size()-1; i++) {
			g.drawLine((int)get(i).getX(), (int)get(i).getY(), (int)get(i+1).getX(), (int)get(i+1).getY());
		}
	}

	/**
	 * Paint method with an offset
	 * @param g graphics object to paint to
	 * @param offX x offset
	 * @param offY y offset
	 */
	public void paint(Graphics g, int offX, int offY) {
		for (int i = 0; i < size()-1; i++) {
			g.drawLine((int)get(i).getX()-offX, (int)get(i).getY()-offY,
					(int)get(i+1).getX()-offX, (int)get(i+1).getY()-offY);
		}
	}

	public void writeToFile(String filename) {
		File f = new File(filename);
		try {
			BufferedImage image = getImage();
			ImageIO.write(image, "png", f);
		}
		catch (IOException e) {
			System.err.println("Error writing image to file: " + filename);
		}
	}

	public double getLength() {
		return features.getLengthSoFar()[features.getLengthSoFar().length - 1];
	}
}