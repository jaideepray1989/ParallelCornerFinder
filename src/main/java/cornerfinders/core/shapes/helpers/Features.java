package cornerfinders.core.shapes.helpers;

import cornerfinders.core.shapes.RRectangle;
import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.SezginCornerFinder;

import java.awt.geom.Point2D;
import java.util.*;

/**
 * Computes the features of a given stroke. Can choose from defined
 * feature sets, and can also standardize a stroke to a default bounding
 * box width or height.
 * 
 * @author Aaron Wolin
 */
public class Features 
{	
	// Feature sets available
	public enum FeatureSet { Rubine, Long, Aaron }
	
	// Should the stroke be standardized to a default width or height?
	private boolean standardize;
	
	// Standardized bounding box width or height
	private double stdBoundingBox = 600.0;
	
	// The feature set we use with this classifier
	private FeatureSet featureSet;
	
	// Distance differences between points within the stroke
	private Point2D.Double[] delta;
	
	// Angle differences between points within the stroke
	private double[] omega;
	
	// Time differences between points within the stroke
	private long[] time;
	
	
	/**
	 * Default Constructor
	 */
	public Features()
	{
		this(FeatureSet.Rubine, false);
	}
	
	
	/**
	 * Constructor
	 * 
	 * @param fSet			Feature set to use
	 */
	public Features(FeatureSet fSet)
	{
		this(fSet, false);
	}
	
	
	/**
	 * Constructor
	 * 
	 * @param fSet			Feature set to use
	 * @param standardize	Should the stroke be standardized?
	 */
	public Features(FeatureSet fSet, boolean standardize)
	{
		this.featureSet = fSet;
		this.standardize = standardize;
	}
	
	
	/**
	 * Calculates the features for a gesture stroke and returns
	 * the feature vector
	 * 
	 * @param stroke	Gesture	stroke
	 * @return Feature ArrayList
	 */
	public double[] getFeatures(TStroke stroke)
	{
		// Feature ArrayList
		ArrayList<Double> f = new ArrayList<Double>();
		
		// Remove overlapping points from the stroke
		TStroke s = cleanStroke(stroke);
		
		// Standardize the stroke
		if (standardize)
			s = standardizeStroke(s);
		
		int numPoints = s.numPoints();
		
		// Calculate intersegment distance changes
		delta = new Point2D.Double[numPoints];
		for (int p = 0; p < numPoints - 1; p++)
		{
			delta[p] = deltaP(s, p);
		}
		
		// Calculate intersegment angle changes
		omega = new double[numPoints];
		for (int p = 1; p < numPoints - 1; p++)
		{
			omega[p] = omegaP(s, p, delta);
		}
		
		
		// Calculate intersegment time changes
		time = new long[numPoints];
		for (int p = 0; p < numPoints - 1; p++)
		{
			time[p] = timeP(s, p);
		}
		
		// Get the features for the corresponding feature set
		switch (this.featureSet)
		{
		case Rubine:
			addRubineFeatures(s, f);
			break;
		case Long:
			addLongFeatures(s, f);
			break;
		case Aaron:
			addAaronFeatures(s, f);
			break;
		default:
			addRubineFeatures(s, f);
			break;
		}
		
		// Cast the ArrayList into a double[]
		double[] fArray = new double[f.size()];
		for (int i = 0; i < f.size(); i++)
		{
			fArray[i] = f.get(i);
		}
		
		return fArray;
	}
	
	
	/**
	 * Adds the Rubine features to the feature set
	 * 
	 * @param s Stroke to calculate features for
	 * @param f Feature set
	 */
	private void addRubineFeatures(TStroke s, ArrayList<Double> f)
	{
		// R1
		double initialCosine = initialCosine(s);
		f.add(initialCosine);
		
		// R2
		double initialSine = initialSine(s);
		f.add(initialSine);
		
		// R3
		double boundingBoxSize = boundingBoxSize(s);
		f.add(boundingBoxSize);
		
		// R4
		double boundingBoxAngle = boundingBoxAngle(s);
		f.add(boundingBoxAngle);
		
		// R5
		double firstLastPtDist = firstLastPtDist(s);
		f.add(firstLastPtDist);
		
		// R6
		double firstLastCosine = firstLastCosine(s, firstLastPtDist);
		f.add(firstLastCosine);
		
		// R7
		double firstLastSine = firstLastSine(s, firstLastPtDist);
		f.add(firstLastSine);
		
		// R8
		double totalLength = totalLength(s, delta);
		f.add(totalLength);
		
		// R9
		double totalAngle = totalAngle(s, omega);
		f.add(totalAngle);
		
		// R10
		double totalAbsAngle = totalAbsAngle(s, omega);
		f.add(totalAbsAngle);
		
		// R11
		double smoothness = smoothness(s, omega);
		f.add(smoothness);
		
		// R12
		double maxSpeed = maxSpeed(s, delta, time);
		f.add(maxSpeed);
	
		// R13
		double totalTime = totalTime(s);
		f.add(totalTime);
	}
	
	
	/**
	 * Adds the Long features to the feature set
	 * 
	 * @param s Stroke to calculate features for
	 * @param f Feature set
	 */
	private void addLongFeatures(TStroke s, ArrayList<Double> f)
	{
		// Start with the Rubine feature set
		addRubineFeatures(s, f);
		
		// Remove the last two features from the Rubine set: max speed and total time
		f.remove(12);
		f.remove(11);
		
		// R3
		double boundingBoxSize = f.get(2);
		
		// R4
		double boundingBoxAngle = f.get(3);
		
		// R5
		double firstLastPtDist = f.get(4);
		
		// R8
		double totalLength = f.get(7);
		
		// R9
		double totalAngle = f.get(8);
		
		// R10
		double totalAbsAngle = f.get(9);
		
		// L12
		double aspect = aspect(boundingBoxAngle);
		f.add(aspect);
		
		// L13
		double curviness = curviness(omega);
		f.add(curviness);
		
		// L14
		double totalAngleDivTotalLength = totalAngleDivTotalLength(totalAngle, totalLength);
		f.add(totalAngleDivTotalLength);
		
		// L15
		double density1 = density1(totalLength, firstLastPtDist);
		f.add(density1);
			
		// L16
		double density2 = density2(totalLength, boundingBoxSize);
		f.add(density2);
		
		// L17
		double openness = openness(firstLastPtDist, boundingBoxSize);
		f.add(openness);
		
		// L18
		double boundingBoxArea = boundingBoxArea(s);
		f.add(boundingBoxArea);
		
		// L19
		double logArea = logArea(boundingBoxArea);
		f.add(logArea);
		
		// L20
		double totalAngleDivTotalAbsAngle = 
			totalAngleDivTotalAbsAngle(totalAngle, totalAbsAngle);
		f.add(totalAngleDivTotalAbsAngle);
		
		// L21
		double logLength = logLength(totalLength);
		f.add(logLength);
		
		// L22
		double logAspect = logAspect(aspect);
		f.add(logAspect);
	}
	
	
	/**
	 * Adds the Aaron features to the feature set
	 * 
	 * @param s Stroke to calculate features for
	 * @param f Feature set
	 */
	private void addAaronFeatures(TStroke s, ArrayList<Double> f)
	{
		// Start with the long feature set
		addLongFeatures(s, f);
		
		// Create a new, default corner finder
		SezginCornerFinder cf = new SezginCornerFinder();
		ArrayList<Integer> corners = cf.findCorners(s);
		
		// R8
		double totalLength = f.get(7);
		
		// L18
		double boundingBoxArea = f.get(17);
		
		// A23
		double numCorners = numCorners(corners);
		f.add(numCorners);
		
		// A24
		double numIntersections = numIntersections(s, 2);
		f.add(numIntersections);
		
		// A25
		double pixelDensity = pixelDensity(totalLength, boundingBoxArea);
		f.add(pixelDensity);
		
		// A26
		f.add(cf.getAvgCurvature());
		
		// A27
		f.add(cf.getMaxCurvature());
		
		// A28
		f.add(cf.getAvgSpeed());
	}
	
	
	/**
	 * Cleans a stroke by removing overlapping points. Also updates the time values to 
	 * ensure that no two times are similar.
	 * 
	 * @param s
	 */
	public TStroke cleanStroke(TStroke s)
	{
		List<TPoint> pts = s.getPoints();
		
		int i = 0;
		boolean isChanged = false;
		do
		{
			double x0 = pts.get(i).getX();
			double y0 = pts.get(i).getY();
			
			double x1 = pts.get(i+1).getX();
			double y1 = pts.get(i+1).getY();
			
			long t0 = pts.get(i).getTime();
			long t1 = pts.get(i+1).getTime();
			
			if (x0 == x1 && y0 == y1)
			{
				pts.remove(i + 1);
				isChanged = true;
			}
			else if (t0 == t1)
			{
				pts.remove(i+1);
				isChanged = true;
				// Probably want to do some average based filtering later
				//pts.get(i+1).setTime(time);
			}
			else
			{
				i++;
			}
		}
		while (i < pts.size() - 1);
		
		// Generate the new stroke
		if(isChanged)	{
			s = new TStroke(pts);
		}
		
		return s;
	}
	
	
	/**
	 * Standardize the stroke to a new bounding box size of scaledX x scaledY
	 * at the origin (0,0).
	 * 
	 * @param s Gesture stroke
	 * @return Stroke standardized to a certain bounding box width and height
	 */
	public TStroke standardizeStroke(TStroke s)
	{
		List<TPoint> origPts = s.getPoints();
		
		int numPts = origPts.size();
		RRectangle boundingBox = s.getBoundingBox();
		
		// Origin of the original stroke
		double topY = boundingBox.getMinY();
		double leftX = boundingBox.getMinX();
		
		// Width and height of the original stroke
		double width = boundingBox.getWidth();
		double height = boundingBox.getHeight();
		
		double stdWidth, stdHeight;
		if (width > height)
		{
			stdWidth = stdBoundingBox;
			stdHeight = height * (stdBoundingBox / width);
		}
		else
		{
			stdWidth = width * (stdBoundingBox / height);
			stdHeight = stdBoundingBox;
		}
			
		
		// Transpose the stroke to the top left corner
		for (int i = 0; i < numPts; i++)
		{
			TPoint pt = origPts.get(i);
			pt.setP(pt.getX() - leftX, pt.getY() - topY);
		}
		
		// Scale everything to a standard bounding box size
		for (int i = 0; i < numPts; i++)
		{
			TPoint pt = origPts.get(i);
			
			double newX = (pt.getX() / width) * stdWidth;
			double newY = (pt.getY() / height) * stdHeight;
			
			pt.setP(newX, newY);
		}
		
		return s;
	}

	
	
	/**
	 * Rubine features
	 **/
	
	/**
	 * Calculates the cosine of the initial angle
	 * 
	 * @param s Gesture stroke
	 * @return Cosine of the initial angle
	 */
	private double initialCosine(TStroke s) 
	{	
		TPoint p0 = s.getPoint(0);
		
		double euc = 0.0;
		int i = 2;
		
		do
		{
			euc = euclidean(p0, s.getPoint(i));
			i++;
		}
		while (euc == 0.0 && i < s.getSize());
				
		double cos = (s.getPoint(i-1).getX() - p0.getX()) / euc;
		
		return cos;
	}
	
	
	/**
	 * Calculates the sine of the initial angle
	 * 
	 * @param s Gesture stroke
	 * @return Sine of the initial angle
	 */
	private double initialSine(TStroke s) 
	{	
		TPoint p0 = s.getPoint(0);
		
		double euc = 0.0;
		int i = 2;
		
		do
		{
			euc = euclidean(p0, s.getPoint(i));
			i++;
		}
		while (euc == 0.0 && i < s.getSize());
				
		double sin = (s.getPoint(i-1).getY() - p0.getY()) / euc;
		
		return sin;
	}
	
	
	/**
	 * Calculates the length of the bounding box diagonal
	 * 
	 * @param s Gesture stroke
	 * @return Length of the bounding box diagonal
	 */
	private double boundingBoxSize(TStroke s) 
	{	
		return euclidean(s.getMinX(), s.getMinY(), 
				s.getMaxX(), s.getMaxY());
	}
	
	
	/**
	 * Calculates the angle of the bounding box diagonal
	 * 
	 * @param s Gesture stroke
	 * @return Angle of the bounding box diagonal
	 */
	private double boundingBoxAngle(TStroke s) 
	{	
		double numerator = s.getMaxY() - s.getMinY();
		double denominator = s.getMaxX() - s.getMinX();
		
		if (denominator == 0)
		{
			if (numerator > 0)
				return Math.PI / 2.0;
			else
				return -Math.PI / 2.0;
		}
		
		return Math.atan2(numerator, denominator);
	}
	
	
	/**
	 * Calculates the distance between the first and last point
	 * 
	 * @param s Gesture stroke
	 * @return Distance between the first and last point
	 */
	private double firstLastPtDist(TStroke s) 
	{	
		return euclidean(s.getFirstPoint(), s.getLastPoint());
	}
	
	
	/**
	 * Calculates the cosine between the first and last point
	 * 
	 * @param s Gesture stroke
	 * @param firstLastPtDist Length between the first and last point
	 * @return Cosine between the first and last point
	 */
	private double firstLastCosine(TStroke s, double firstLastPtDist) 
	{			
		if (firstLastPtDist != 0.0)
			return (s.getLastPoint().getX() - s.getFirstPoint().getX()) / 
				firstLastPtDist;
		else
			return (s.getLastPoint().getX() - s.getFirstPoint().getX());
	}
	
	
	/**
	 * Calculates the sine between the first and last point
	 * 
	 * @param s Gesture stroke
	 * @param firstLastPtDist Length between the first and last point
	 * @return Sine between the first and last point
	 */
	private double firstLastSine(TStroke s, double firstLastPtDist) 
	{
		if (firstLastPtDist != 0.0)
			return (s.getLastPoint().getY() - s.getFirstPoint().getY()) / 
				firstLastPtDist;
		else
			return (s.getLastPoint().getY() - s.getFirstPoint().getY());
	}
	
	
	/**
	 * Computes the total gesture length
	 * 
	 * @param s Gesture stroke
	 * @param delta Array of distance changes between points
	 * @return Total gesture length
	 */
	private double totalLength(TStroke s, Point2D.Double[] delta)
	{	
		int numPoints = s.numPoints();
		double sum = 0.0;
		
		for (int p = 0; p < numPoints - 1; p++)
		{
			sum += Math.sqrt((delta[p].x * delta[p].x) + 
					(delta[p].y * delta[p].y));
		}
		
		return sum;
	}
	
	
	/**
	 * Computes the total angle traversed by the stroke
	 * 
	 * @param s Gesture stroke
	 * @param omega Array of angle changes between points
	 * @return Total angle traversed
	 */
	private double totalAngle(TStroke s, double[] omega) 
	{	
		int numPoints = s.numPoints();
		double sum = 0.0;
		
		for (int p = 1; p < numPoints - 1; p++)
		{
			sum += omega[p];
		}
		
		return sum;
	}
	
	
	/**
	 * Computes the sum of the absolute value of the angles
	 * at each mouse point
	 * 
	 * @param s Gesture stroke
	 * @param omega Array of angle changes between points
	 * @return Sum of the absolute value of the angles
	 * at each mouse point
	 */
	private double totalAbsAngle(TStroke s, double[] omega) 
	{	
		int numPoints = s.numPoints();
		double sum = 0.0;
		
		for (int p = 1; p < numPoints - 1; p++)
		{
			sum += Math.abs(omega[p]);
		}
		
		return sum;
	}


	/**
	 * Computes the sum of the squared values of the angles
	 * at each mouse point
	 * 
	 * This is a measure of smoothness
	 * 
	 * @param s Gesture stroke
	 * @param omega Array of angle changes between points
	 * @return Sum of the squared values of the angles
	 * at each mouse point
	 */
	private double smoothness(TStroke s, double[] omega) 
	{	
		int numPoints = s.numPoints();
		double sum = 0.0;
		
		for (int p = 1; p < numPoints - 1; p++)
		{
			sum += (omega[p] * omega[p]);
		}
		
		return sum;
	}
	
	
	/**
	 * Computes the maximum speed of the gesture stroke
	 * 
	 * @param s Gesture stroke
	 * @param delta Array of distance changes between points
	 * @param time Array of time changes between points
	 * @return Maximum speed of the gesture
	 */
	private double maxSpeed(TStroke s, Point2D.Double[] delta, long[] time) 
	{
		int numPoints = s.numPoints();
		double maxSpeed = 0.0;
		
		for (int p = 0; p < numPoints - 1; p++)
		{
			if (time[p] == 0)
			{
				continue;
			}
			else
			{
				double speed = ((delta[p].x * delta[p].x) + (delta[p].y * delta[p].y) / 
					time[p] * time[p]);
			
				if (speed > maxSpeed)
					maxSpeed = speed;
			}
		}
		
		return maxSpeed;
	}
	
	
	/**
	 * Computes the duration of the gesture
	 * 
	 * @param s Gesture stroke
	 * @return Duration of the gesture
	 */
	private double totalTime(TStroke s)
	{
		TPoint t0 = s.getFirstPoint();
		TPoint tPMinus1 = s.getLastPoint();
	
		return (double)(tPMinus1.getTime() - t0.getTime());
	}
	
	
	
	/**
	 * Long & Landay Features
	 **/
	
	/**
	 * Calculates the gesture aspect, which is abs(45 degrees - angle of bounding box)
	 * 
	 * @return Aspect...
	 */
	private double aspect(double boundingBoxAngle)
	{
		return Math.abs((Math.PI / 4.0) - boundingBoxAngle);
	}
	
	
	/**
	 * The sum of gesture intersegment angles whose absolute value is less than 19 degrees
	 * 
	 * @param omega Array of angle changes between points
	 * @return Curviness of the gesture
	 */
	private double curviness(double[] omega)
	{
		double threshold = (19.0 * Math.PI) / 180.0;
		double curviness = 0.0;
		
		for (int p = 1; p < omega.length - 1; p++)
		{
			if (omega[p] < threshold)
				curviness += Math.abs(omega[p]);
		}
		
		return curviness;
	}
	
	
	/**
	 * Returns the total angle traversed / total length of gesture stroke
	 * 
	 * @param totalAngle Total angle traversed
	 * @param totalLength Total length of stroke
	 * @return totalAngle / totalLength
	 */
	private double totalAngleDivTotalLength(double totalAngle, double totalLength)
	{
		if (totalLength != 0.0)
			return totalAngle / totalLength;
		else
			return 0.0;
	}
	
	
	/**
	 * A density metric for the gesture stroke that uses the stroke's length
	 * and distance between the first and last point
	 * 
	 * @param totalLength Total length of the stroke
	 * @param firstLastPtDist Distance between the first and last point
	 * @return totalLength / firstLastPtDist
	 */
	private double density1(double totalLength, double firstLastPtDist)
	{
		if (firstLastPtDist != 0.0)
			return totalLength / firstLastPtDist;
		else
			return totalLength;
	}
	
	
	/**
	 * A density metric for the gesture stroke that uses the stroke's length
	 * and bounding box size
	 * 
	 * @param totalLength Total length of the stroke
	 * @param boundingBoxSize Length of the bounding box diagonal
	 * @return totalLength / boundingBoxSize
	 */
	private double density2(double totalLength, double boundingBoxSize)
	{
		if (boundingBoxSize != 0.0)
			return totalLength / boundingBoxSize;
		else
			return totalLength;
	}
	
	
	/**
	 * How "open" or spaced out is a gesture
	 * 
	 * @param firstLastPtDist Distance between the first and last point
	 * @param boundingBoxSize Length of the bounding box diagonal
	 * @return firstLastPtDist / boundingBoxSize
	 */
	private double openness(double firstLastPtDist, double boundingBoxSize)
	{
		if (boundingBoxSize != 0.0)
			return firstLastPtDist / boundingBoxSize;
		else
			return firstLastPtDist;
	}
	
	
	/**
	 * Calculates the bounding box area
	 * 
	 * @param s Gesture stroke
	 * @return The area of the gesture bounding box
	 */
	private double boundingBoxArea(TStroke s)
	{
		return (s.getMaxX() - s.getMinX()) * (s.getMaxY() - s.getMinY());
	}
	
	
	/**
	 * Returns the log of the bounding box area
	 * 
	 * @param area Bounding box area
	 * @return log(area)
	 */
	private double logArea(double area)
	{
		if (area != 0)
			return Math.log(area);
		else
			return 0.0;
	}
	
	
	/**
	 * Returns the total angle divided by the total absolute angle
	 * 
	 * @param totalAngle Total angle
	 * @param totalAbsAngle Total absolute angle
	 * @return totalAngle / totalAbsAngle
	 */
	private double totalAngleDivTotalAbsAngle(double totalAngle, double totalAbsAngle)
	{
		if (totalAbsAngle != 0.0)
			return totalAngle / totalAbsAngle;
		else
			return totalAngle;
	}
	
	
	/**
	 * Returns the log of the total length
	 * 
	 * @param totalLength Length of the stroke
	 * @return log(totalLength)
	 */
	private double logLength(double totalLength)
	{
		if (totalLength != 0.0)
			return Math.log(totalLength);
		else
			return 0.0;
	}
	
	
	/**
	 * Returns the log of the aspect
	 * 
	 * @param aspect Aspect
	 * @return log(aspect)
	 */
	private double logAspect(double aspect)
	{
		if (aspect != 0)
			return Math.log(aspect);
		else
			return 0.0;
	}
	
	
	
	/**
	 * Aaron's new features
	 **/
	
	/**
	 * Calculates the number of corners found in the stroke
	 * 
	 * @param corners	The corners of the stroke
	 * @return The number of corners found in the stroke
	 */
	private double numCorners(ArrayList<Integer> corners)
	{
		return corners.size();
	}
	
	
	/**
	 * Calculates the number of intersections 
	 * 
	 * @param s
	 * @param segLength
	 * @return
	 */
	private double numIntersections(TStroke s, int segLength)
	{
		double numIntersections = 0.0;
		
		List<TPoint> pts = s.getPoints();
		double numPts = pts.size();
		
		for (int i = 0; i < numPts - segLength; i += segLength)
		{
			if (checkForIntersection(pts, i, segLength))
				numIntersections += 1.0;
		}
		
		return numIntersections;
	}
	
	
	/**
	 * Checks to see if a line segment with a given segment length intersects any other
	 * segments of similar length within the stroke points
	 * 
	 * @param pts			Points of the stroke
	 * @param startSegment	Starting index of our segment
	 * @param segLength		Length of the segment
	 * @return				True if there is an intersection, false otherwise
	 */
	private boolean checkForIntersection(List<TPoint> pts, int startSegment, int segLength)
	{
		int numPts = pts.size();
		int endSegment = startSegment + segLength - 1;
		
		// If we don't have enough pts
		if (endSegment > numPts)
			return false;
		
		for (int i = endSegment + segLength + 1; i < numPts; i += segLength)
		{
			int endSecondSegment = i + segLength;
			if (endSecondSegment > numPts - 1)
				endSecondSegment = numPts - 1;
			
			if (isIntersection(pts.get(startSegment), pts.get(endSegment), 
					pts.get(i), pts.get(endSecondSegment)))
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * http://www.mema.ucl.ac.be/~wu/FSA2716/Exercise1.htm
	 * 
	 * @param pt1	End point 1 of segment 1
	 * @param pt2	End point 2 of segment 1
	 * @param pt3	End point 1 of segment 2
	 * @param pt4	End point 2 of segment 2
	 * @return		True if there is an intersection, false otherwise
	 */
	private boolean isIntersection(TPoint pt1, TPoint pt2, TPoint pt3, TPoint pt4)
	{
		// A
		double Ax = pt1.getX();
		double Ay = pt1.getY();
		
		// B
		double Bx = pt2.getX();
		double By = pt2.getY();
		
		// C
		double Cx = pt3.getX();
		double Cy = pt3.getY();
		
		// D
		double Dx = pt4.getX();
		double Dy = pt4.getY();
		
		double denom = (((Bx - Ax) * (Dy - Cy)) - ((By - Ay) * (Dx - Cx)));
		
		// AB and CD are parallel
		if (denom == 0.0)
			return false;
		
		double numR = (((Ay - Cy) * (Dx - Cx)) - ((Ax - Cx) * (Dy - Cy)));
		double r = numR / denom;
		
		double numS = (((Ay - Cy) * (Bx - Ax)) - ((Ax - Cx) * (By - Ay)));
		double s = numS / denom;
	
		// An intersection exists
		if (r >= 0.0 && r <= 1.0 && s >= 0.0 && s <= 1.0)
			return true;
		
		return false;
	}
	
	
	/**
	 * Calculate the pixel density of the gesture by using the stroke length
	 * and the bounding box area.  Here we're assuming that stroke length is
	 * approximately equal to the number of pixels filled.
	 * 
	 * @param strokeLength Length of the stroke
	 * @param boundingBoxArea Area of the gesture
	 * @return Pixel density
	 */
	private double pixelDensity(double strokeLength, double boundingBoxArea)
	{
		if (boundingBoxArea != 0.0)
			return strokeLength / boundingBoxArea;
		else
			return 1.0;
	}
	
	
	
	
	/**
	 * Helper functions
	 **/
	
	/**
	 * Finds and returns the Euclidean distance between two points
	 * 
	 * @param p0 First point
	 * @param p1 Second point
	 * @return Euclidean distance between p0 and p1
	 */
	private double euclidean(TPoint p0, TPoint p1) 
	{	
		double x2 = (p1.getX() - p0.getX()) * (p1.getX() - p0.getX());
		double y2 = (p1.getY() - p0.getY()) * (p1.getY() - p0.getY());
		
		return Math.sqrt(x2 + y2);
	}
	
	
	/**
	 * Finds and returns the Euclidean distance between two points
	 * 
	 * @param x0 First point's x-coordinate
	 * @param x1 Second point's x-coordinate
	 * @param y0 First point's y-coordinate
	 * @param y1 Second point's y-coordinate
	 * @return
	 */
	private double euclidean(double x0, double y0, double x1, double y1)
	{		
		double xSq = (x1 - x0) * (x1 - x0);
		double ySq = (y1 - y0) * (y1 - y0);
		
		return Math.sqrt(xSq + ySq);
	}
	
	
	/**
	 * Calculates the distance change between a point
	 * and its following point
	 * 
	 * @param s Gesture stroke
	 * @param p Index of the initial point
	 * @return Distance change between two points
	 */
	private Point2D.Double deltaP(TStroke s, int p) 
	{	
		TPoint pt = s.getPoint(p);
		TPoint ptPlus1 = s.getPoint(p+1);
		
		double deltaX = ptPlus1.getX() - pt.getX();
		double deltaY = ptPlus1.getY() - pt.getY();
		
		return new Point2D.Double(deltaX, deltaY);
	}
	
	
	/**
	 * Calculates the angle change between a point
	 * and its following point
	 * 
	 * @param s Gesture stroke
	 * @param p Index of initial point
	 * @param delta Array of distance changes between points
	 * @return Angle change between two points
	 */
	private double omegaP(TStroke s, int p, Point2D.Double[] delta) 
	{	
		double y = (delta[p].x * delta[p-1].y) - (delta[p-1].x * delta[p].y);
		double x = (delta[p].x * delta[p-1].x) + (delta[p].y * delta[p-1].y);
		
		if (x == 0)
		{
			if (y > 0)
				return Math.PI / 2.0;
			else
				return -Math.PI / 2.0;
		}
		
		double omegaP = Math.atan2(y, x);
		
		return omegaP;
	}
	
	
	/**
	 * Calculates the time change between a point
	 * and its following point
	 * 
	 * @param s Gesture stroke
	 * @param p Index of initial point
	 * @return Time change between two points
	 */
	private long timeP(TStroke s, int p)
	{	
		return s.getPoint(p+1).getTime() - s.getPoint(p).getTime();
	}
	
	
	/**
	 * Is the stroke standardized?
	 * @return	True if the stroke is standardized, false otherwise
	 */
	public boolean isStandardized()
	{
		return this.standardize;
	}
	
	
	/**
	 * Returns the feature set of our Features object
	 * @return	The feature set we use
	 */
	public FeatureSet getFeatureSet()
	{
		return this.featureSet;
	}
}
