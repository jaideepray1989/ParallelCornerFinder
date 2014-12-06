package cornerfinders.impl;
import cornerfinders.core.shapes.TLine;
import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.core.shapes.helpers.Features;
import cornerfinders.recognizers.BStroke;
import cornerfinders.recognizers.Fit;

import java.util.*;


/**
 * 
 * @author Aaron Wolin
 */
public class MergeCornerFinder extends CornerFinder
{
	private double[] direction;
	
	private double[] curvature;
	
	private double[] speed;
	
	// Average curvature of the stroke
	private double avgCurvature;
	
	// Average speed of the stroke
	private double avgSpeed;
	
	// Maximum curvature of the stroke
	private double maxCurvature;
	
	// Maximum speed of the stroke
	private double maxSpeed;
	
	
	
	/**
	 * Finds the corners for a stroke
	 * 
	 * @param s	Stroke to find the corners for
	 * @return	Corners of a stroke
	 */
	public ArrayList<Integer> findCorners(TStroke s)
	{
		Features strokeCleaner = new Features();
		stroke = strokeCleaner.cleanStroke(s);
		
		pts = stroke.getPoints();
		
		arcLength = arcLength();
		direction = calcDirections(pts, true);
		curvature = calcCurvatures(arcLength, direction, true);
		speed = calcSpeed(pts, arcLength, true);
		
		// Get the corners from curvature and speed
		ArrayList<Integer> Fc = getCornersFromCurvature(curvature);
		ArrayList<Integer> Fs = getCornersFromSpeed(speed);
		
		// Calculate an initial fit for the corners
		ArrayList<Integer> corners = mergeFit(Fc, Fs);
		
		return corners;
	}
	
	
	
	/*
	 * Feature functions
	 */

	/**
	 * Calculates the speed at each point
	 * 
	 * @param pts Points of the stroke
	 * @param arcLength Arc length at each point
	 * @param smooth Should an average filter be applied?
	 * @return Speed values at each point
	 */
	private double[] calcSpeed(List<TPoint> pts, double[] arcLength, boolean smooth)
	{
		int numPts = pts.size();
		
		double[] d = arcLength;
		double[] s = new double[numPts];
		
		// First pt's speed
		s[0] = 0.0;
		
		// Get speed the pts in the stroke
		for (int i = 1; i < numPts - 1; i++)
		{
			double timeDiff = pts.get(i + 1).getTime() - pts.get(i - 1).getTime();
			
			if (timeDiff != 0.0)
				s[i] = (d[i + 1] - d[i - 1]) / timeDiff;
			else
				s[i] = s[i - 1];
		}
		
		// Last pt's speed
		s[numPts - 1] = 0.0;
	
		// Smooth speed
		if (smooth)
		{
			double[] smoothSpeed = new double[numPts];
			
			for (int i = 1; i < numPts - 1; i++)
			{
				smoothSpeed[i] = (s[i - 1] + s[i] + s[i + 1]) / 3.0;
			}
			
			s = smoothSpeed;
		}
		
		return s;
	}
	
	
	/**
	 * Calculates the direction at each point
	 * 
	 * @param pts Points of the stroke
	 * @param smooth Should an average filter be applied?
	 * @return Direction values at each point
	 */
	private double[] calcDirections(List<TPoint> pts, boolean smooth)
	{
		int numPts = pts.size();
		double[] direction = new double[numPts];
		
		// Set the initial direction point
		direction[0] = 0.0;
		
		// Calculate the direction value for each point
		for (int i = 1; i < numPts; i++)
		{
			double d = direction(pts, i);
		
			// Make sure there are no large jumps in direction - ensures graph continuity
			while (d - direction[i - 1] > Math.PI)
			{
				d -= (Math.PI * 2);
			}
			while (direction[i - 1] - d > Math.PI)
			{
				d += (Math.PI * 2);
			}
			
			direction[i] = d;
		}
		
		// Average filtering
		if (smooth)
		{
			double[] smoothDirection = new double[numPts];
			
			for (int i = 1; i < numPts - 1; i++)
			{
				smoothDirection[i] = (direction[i - 1] + direction[i] + direction[i + 1]) / 3.0;			
			}
			
			direction = smoothDirection;
		}
		
		return direction;
	}
	
	
	/**
	 * Calculates the direction at a point
	 * 
	 * @param pts Points of the stroke
	 * @param index Index of the point to check
	 * @return The direction at the index point
	 */
	private double direction(List<TPoint> pts, int index)
	{
		if (index - 1 >= 0)
		{
			double dy = pts.get(index).getY() - pts.get(index - 1).getY();
			double dx = pts.get(index).getX() - pts.get(index - 1).getX();
			
			return Math.atan2(dy, dx);
		}
		else
			return 0.0;
	}
	
	
	/**
	 * Calculates the curvature values at each point
	 *
	 * @param direction Direction (angles) of the points
	 * @param smooth Should an average filter be applied?
	 * @return The curvature value at each point
	 */
	private double[] calcCurvatures(double[] arclength, double[] direction, boolean smooth)
	{
		int numPts = direction.length;
		double[] curvature = new double[numPts];
		
		curvature[0] = 0.0;
		
		// Calculate the curvature value for each point
		for (int i = 1; i < numPts - 1; i++)
		{
			double curv = curvature(arclength, direction, i, 3);
			
			// Hack to check if we have a divide-by-0 error
			if (curv != -1.0)
				curvature[i] = curv;
			else
				curvature[i] = curvature[i - 1];
		}
		
		// Average filtering
		if (smooth)
		{
			double[] smoothCurvature = new double[numPts];
			
			for (int i = 1; i < numPts - 1; i++)
			{
				smoothCurvature[i] = (curvature[i - 1] + curvature[i] + curvature[i + 1]) / 3.0;			
			}
			
			curvature = smoothCurvature;
		}
		
		return curvature;
	}

	
	/**
	 * Finds the curvature at a given point index, given a point window, the arc lengths
	 * of the points, and the directions of the points
	 * 
	 * @param arcLength Arc lengths of the points
	 * @param direction Direction (angles) of the points
	 * @param index Corner index
	 * @return The curvature value for a point at the index
	 */
	private double curvature(double[] arcLength, double[] direction, int index, int k)
	{
		int start = index - k;
		if (index - k < 0)
			start = 0;
		
		int end = index + k;
		if (end + k > arcLength.length)
			end = arcLength.length - 1;
		
		double segmentLength = arcLength[end] - arcLength[start];
		
		if (segmentLength > 0.0)
		{
			double dirChanges = 0.0;
			
			for (int i = start + 1; i <= end; i++)
			{
				dirChanges += (direction[i] - direction[i - 1]);
			}
			
			return Math.abs(dirChanges) / segmentLength;
		}
		else 
		{		
			return 0.0;
		}
	}
	
	
	
	
	/*
	 * Get corner fits
	 */
	
	/**
	 * Finds possible corners from the curvature values of points.  Uses the 
	 * average curvature for a cutoff threshold.
	 * 
	 * @param curvature Curvature values for each point
	 * @return An ArrayList of indices indicating corners
	 */
	private ArrayList<Integer> getCornersFromCurvature(double[] curvature)
	{
		ArrayList<Integer> corners = new ArrayList<Integer>();
		
		double minCurvature = Double.POSITIVE_INFINITY;
		maxCurvature = Double.NEGATIVE_INFINITY;
		avgCurvature = 0.0;
		
		// Calculate the average, minimum, and maximum curvatures
		for (int i = 0; i < curvature.length; i++)
		{
			avgCurvature += curvature[i];
			
			if (curvature[i] < minCurvature)
				minCurvature = curvature[i];
			
			if (curvature[i] > maxCurvature)
				maxCurvature = curvature[i];
		}
		
		avgCurvature /= (double)curvature.length;
		
		// Curvature values above this threshold will be considered for corners
		double threshold = avgCurvature;
		
		// Find corners where our curvature is over the average curvature threshold
		for (int i = 0; i < curvature.length; i++)
		{
			// Find only the local maximum
			if (curvature[i] > threshold)
			{
				double localMaximum = Double.NEGATIVE_INFINITY;
				int localMaximumIndex = i;
				
				while (i < curvature.length && curvature[i] > threshold)
				{
					if (curvature[i] > localMaximum)
					{
						localMaximum = curvature[i];
						localMaximumIndex = i;
					}
					
					i++;
				}
				
				corners.add(new Integer(localMaximumIndex));
			}
		}
		
		// Add the endpoints
		if (!corners.contains(0))
			corners.add(0);
		if (!corners.contains(curvature.length - 1))
			corners.add(curvature.length - 1);
		
		// Sort the corners
		Collections.sort(corners);
		
		// Return the list of corners (indices)
		return corners;
	}
	
	
	/**
	 * Finds possible corners from the speed values of points.  Uses a 
	 * value of less than 90% of the mean speed for a cutoff threshold.
	 * 
	 * @param speed Speed values for each point
	 * @return An ArrayList of indices indicating corners
	 */
	private ArrayList<Integer> getCornersFromSpeed(double[] speed)
	{
		ArrayList<Integer> corners = new ArrayList<Integer>();
		
		double minSpeed = Double.POSITIVE_INFINITY;
		maxSpeed = Double.NEGATIVE_INFINITY;
		avgSpeed = 0.0;
		
		// Calculate the average, minimum, and maximum speed
		for (int i = 0; i < speed.length; i++)
		{
			avgSpeed += speed[i];
			
			if (speed[i] < minSpeed)
				minSpeed = speed[i];
			
			if (speed[i] > maxSpeed)
				maxSpeed = speed[i];
		}
		
		avgSpeed /= (double)speed.length;
		
		// Speed values below this threshold will be considered for corners
		double threshold = 0.90 * avgSpeed;
		
		// Find corners where our speed is under the average curvature threshold
		for (int i = 0; i < speed.length; i++)
		{
			// Find only the local minimum
			if (speed[i] < threshold)
			{
				double localMinimum = Double.POSITIVE_INFINITY;
				int localMinimumIndex = i;
				
				while (i < speed.length && speed[i] < threshold)
				{
					if (speed[i] < localMinimum)
					{
						localMinimum = speed[i];
						localMinimumIndex = i;
					}
					
					i++;
				}
				
				corners.add(new Integer(localMinimumIndex));
			}
		}
		
		// Add the endpoints
		if (!corners.contains(0))
			corners.add(0);
		if (!corners.contains(speed.length - 1))
			corners.add(speed.length - 1);
		
		// Sort the corners
		Collections.sort(corners);
		
		// Return the list of corners (indices)
		return corners;
	}
	
	
	/**
	 * Original merge fit algorithm
	 * 
	 * @param Fc
	 * @param Fs
	 * @return
	 */
	private ArrayList<Integer> mergeFit(ArrayList<Integer> Fc, ArrayList<Integer> Fs)
	{
		// Get all corners
		ArrayList<Integer> allCorners = new ArrayList<Integer>();
		allCorners.addAll(Fc);
		allCorners.addAll(Fs);
		
		Collections.sort(allCorners);
		
		// Filter out similar corners
		filterCorners(curvature, speed, arcLength, allCorners);
		
		ArrayList<Integer> aaronsCorners = new ArrayList<Integer>();
		aaronsCorners.addAll(allCorners);
		
		double error0 = getFitError(pts, aaronsCorners, arcLength);
		double thisFitError = error0;
		double prevError = error0;
		
		double run = 0.0;
		
		double smallEnoughError = 500.0;
		
		boolean everythingChecked = false;
		
		do
		{
			prevError = thisFitError;
			run++;
			
			TreeMap<Double, Integer> segmentRatios = getSegmentRatios(aaronsCorners, arcLength);
			
			double avgSegmentRatio = 1.0 / (double)aaronsCorners.size();
			
			double segmentRatioThreshold = (avgSegmentRatio / 2.0) * run;
			
			ArrayList<Integer> toRemove = new ArrayList<Integer>();
			
			double fitWindow = 1.5;
			
			while (!segmentRatios.isEmpty() && segmentRatios.firstKey() < segmentRatioThreshold)
			{
				Map.Entry<Double, Integer> smallestSegment = segmentRatios.pollFirstEntry();
				
				int segmentIndex = smallestSegment.getValue();
				
				int c1 = aaronsCorners.get(segmentIndex);
				int c2 = aaronsCorners.get(segmentIndex + 1);
				
				if (c1 > 0 && c2 < aaronsCorners.get(aaronsCorners.size() - 1))
				{
					int c0 = aaronsCorners.get(segmentIndex - 1);
					int c3 = aaronsCorners.get(segmentIndex + 2);
					
					double fitLeft = brandonFit(c0, c2);
					double fitRight = brandonFit(c1, c3);
					
					double currFitMiddle = brandonFit(c1, c2);
					double currFitLeft = brandonFit(c0, c1) + currFitMiddle;
					double currFitRight = currFitMiddle + brandonFit(c2, c3);
					
					if (fitLeft < fitRight && (fitLeft < currFitLeft * fitWindow || fitLeft < smallEnoughError))
					{
						toRemove.add(new Integer(c1));
					}
					else if (fitRight < currFitRight * fitWindow || fitRight < smallEnoughError)
					{
						toRemove.add(new Integer(c2));
					}
				}
				else if (c1 == 0 && c2 < aaronsCorners.get(aaronsCorners.size() - 1))
				{
					int c3 = aaronsCorners.get(segmentIndex + 2);
					
					double fitRight = brandonFit(c1, c3);
					double currFit = brandonFit(c1, c2) + brandonFit(c2, c3);
					
					if (fitRight < currFit * fitWindow || fitRight < smallEnoughError)
					{
						toRemove.add(new Integer(c2));
					}
				}
				else if (c1 > 0 && c2 == aaronsCorners.get(aaronsCorners.size() - 1))
				{
					int c0 = aaronsCorners.get(segmentIndex - 1);
					
					double fitLeft = brandonFit(c0, c2);
					double currFit = brandonFit(c0, c1) + brandonFit(c1, c2);
					
					if (fitLeft < currFit * fitWindow || fitLeft < smallEnoughError)
					{
						toRemove.add(new Integer(c1));
					}
				}
			}
			
			// Remove corners to be culled
			while (!toRemove.isEmpty())
			{
				Integer removeCorner = toRemove.remove(0);
				
				if (aaronsCorners.contains(removeCorner))
					aaronsCorners.remove(removeCorner);
			}
			
			thisFitError = getFitError(pts, aaronsCorners, arcLength);
			
			if (segmentRatios.isEmpty())
				everythingChecked = true;
		}
		while (thisFitError < prevError * 3.0 && !everythingChecked);
		
		// Check lines to remove them
		int i = 1;
		while (i < aaronsCorners.size() - 1)
		{
			int corner0 = aaronsCorners.get(i - 1);
			int corner1 = aaronsCorners.get(i);
			int corner2 = aaronsCorners.get(i + 1);
			
			if (getSegmentType(corner0, corner1) == SegType.Line && 
				getSegmentType(corner1, corner2) == SegType.Line)
			{
				TPoint p0 = pts.get(corner0);
				TPoint p1 = pts.get(corner1);
				TPoint p2 = pts.get(corner2);
				
				double dx1 = p1.getX() - p0.getX();
				double dy1 = p1.getY() - p0.getY();
				
				double angle1 = Math.atan2(dy1, dx1);
				
				double dx2 = p2.getX() - p1.getX();
				double dy2 = p2.getY() - p1.getY();
				
				double angle2 = Math.atan2(dy2, dx2);
			
				if (Math.abs(angle1 - angle2) < Math.PI / 9.0)
				{
					aaronsCorners.remove(new Integer(corner1));
				}
				else
				{
					i++;
				}
			}
			else
			{
				i++;
			}
		}
			
		return aaronsCorners;
	}
	

	/**
	 * Aaron's fit - under construction
	 */
	private ArrayList<Integer> mergeFitOld(ArrayList<Integer> corners, 
			ArrayList<Integer> Fc, ArrayList<Integer> Fs)
	{
		// Get all corners
		ArrayList<Integer> allCorners = new ArrayList<Integer>();
		allCorners.addAll(corners);
		allCorners.addAll(Fc);
		allCorners.addAll(Fs);
		
		Collections.sort(allCorners);
		
		// Filter out similar corners
		filterCorners(curvature, speed, arcLength, allCorners);
		
		ArrayList<Integer> aaronsCorners = new ArrayList<Integer>();
		//aaronsCorners.add(allCorners.get(0));
		aaronsCorners.addAll(allCorners);
		
		double error0 = getFitError(pts, aaronsCorners, arcLength);
		double thisFitError = error0;
		double prevError = error0;
		
		double run = 0.0;
		
		double smallEnoughError = 500.0;
		
		boolean everythingChecked = false;
		
		do
		{
			prevError = thisFitError;
			run++;
			
			TreeMap<Double, Integer> segmentRatios = getSegmentRatios(aaronsCorners, arcLength);
			
			double avgSegmentRatio = 1.0 / (double)aaronsCorners.size();
			
			double segmentRatioThreshold = (avgSegmentRatio / 2.0) * run;
			
			ArrayList<Integer> toRemove = new ArrayList<Integer>();
			
			double fitWindow = 1.5;
			
			while (!segmentRatios.isEmpty() && segmentRatios.firstKey() < segmentRatioThreshold)
			{
				Map.Entry<Double, Integer> smallestSegment = segmentRatios.pollFirstEntry();
				
				int segmentIndex = smallestSegment.getValue();
				
				int c1 = aaronsCorners.get(segmentIndex);
				int c2 = aaronsCorners.get(segmentIndex + 1);
				
				if (c1 > 0 && c2 < corners.get(corners.size() - 1))
				{
					int c0 = aaronsCorners.get(segmentIndex - 1);
					int c3 = aaronsCorners.get(segmentIndex + 2);
					
					double fitLeft = segmentError(pts, c0, c2, arcLength);
					double fitRight = segmentError(pts, c1, c3, arcLength);
					
					double currFitMiddle = segmentError(pts, c1, c2, arcLength);
					double currFitLeft = segmentError(pts, c0, c1, arcLength) + currFitMiddle;
					double currFitRight = currFitMiddle + segmentError(pts, c2, c3, arcLength);
					
					if (fitLeft < fitRight && (fitLeft < currFitLeft * fitWindow || fitLeft < smallEnoughError))
					{
						toRemove.add(new Integer(c1));
					}
					else if (fitRight < currFitRight * fitWindow || fitRight < smallEnoughError)
					{
						toRemove.add(new Integer(c2));
					}
				}
				else if (c1 == 0 && c2 < corners.get(corners.size() - 1))
				{
					int c3 = aaronsCorners.get(segmentIndex + 2);
					
					double fitRight = segmentError(pts, c1, c3, arcLength);
					double currFit = segmentError(pts, c1, c2, arcLength) + segmentError(pts, c2, c3, arcLength);
					
					if (fitRight < currFit * fitWindow || fitRight < smallEnoughError)
					{
						toRemove.add(new Integer(c2));
					}
				}
				else if (c1 > 0 && c2 == corners.get(corners.size() - 1))
				{
					int c0 = aaronsCorners.get(segmentIndex - 1);
					
					double fitLeft = segmentError(pts, c0, c2, arcLength);
					double currFit = segmentError(pts, c0, c1, arcLength) + segmentError(pts, c1, c2, arcLength);
					
					if (fitLeft < currFit * fitWindow || fitLeft < smallEnoughError)
					{
						toRemove.add(new Integer(c1));
					}
				}
			}
			
			
			while (!toRemove.isEmpty())
			{
				Integer removeCorner = toRemove.remove(0);
				
				if (aaronsCorners.contains(removeCorner))
					aaronsCorners.remove(removeCorner);
			}
			
			thisFitError = getFitError(pts, aaronsCorners, arcLength);
			
			if (segmentRatios.isEmpty())
				everythingChecked = true;
		}
		while (thisFitError < prevError * 3.0 && !everythingChecked);
		
		
		int i = 1;
		while (i < aaronsCorners.size() - 1)
		{
			int corner0 = aaronsCorners.get(i - 1);
			int corner1 = aaronsCorners.get(i);
			int corner2 = aaronsCorners.get(i + 1);
			
			if (getSegmentType(corner0, corner1) == SegType.Line && 
				getSegmentType(corner1, corner2) == SegType.Line)
			{
				TPoint p0 = pts.get(corner0);
				TPoint p1 = pts.get(corner1);
				TPoint p2 = pts.get(corner2);
				
				double dx1 = p1.getX() - p0.getX();
				double dy1 = p1.getY() - p0.getY();
				
				double angle1 = Math.atan2(dy1, dx1);
				
				double dx2 = p2.getX() - p1.getX();
				double dy2 = p2.getY() - p1.getY();
				
				double angle2 = Math.atan2(dy2, dx2);
			
				if (Math.abs(angle1 - angle2) < Math.PI / 9.0)
				{
					aaronsCorners.remove(new Integer(corner1));
				}
				else
				{
					i++;
				}
			}
			else
			{
				i++;
			}
		}
		
		
		/*SegType prevType = null;
		double prevFit = Double.POSITIVE_INFINITY;
		
		for (int i = 1; i < allCorners.size(); i++)
		{
			int c1 = aaronsCorners.get(aaronsCorners.size() - 1);
			int c2 = allCorners.get(i);
			
			SegType type = getSegmentType(c1, c2, pts, arcLength);
			
			if (prevType == null)
				prevType = type;
			
			double lineFit = orthogonalDistanceSquared(pts, c1, c2);
			double arcFit = arcOrthogonalDistanceSquared(pts, c1, c2);
			
			double fit = Math.min(lineFit, arcFit);
			
			double t = prevFit + (1.0 * (fit - prevFit));
			if (fit < t || Double.isNaN(t))
			{
				prevFit = fit;
			}
			else
			{
				prevType = null;
				aaronsCorners.add(allCorners.get(i - 1));
			}
		}
		
		aaronsCorners.add(allCorners.get(allCorners.size() - 1));*/
		
		return aaronsCorners;
	}
	
	
	/**
	 * Filters corners
	 * 
	 * @param curvature	Curvature array of the stroke
	 * @param speed		Speed array of the stroke
	 * @param arcLength	Arc length array of the stroke
	 * @param corners	Corners to filter corners
	 */
	private void filterCorners(double[] curvature, double[] speed, double[] arcLength, 
			ArrayList<Integer> corners)
	{
		int i = 1;
		
		// Remove similar corners
		while (i < corners.size())
		{
			int corner1 = corners.get(i - 1);
			int corner2 = corners.get(i);
			
			if (areCornersSimilar(arcLength, corner1, corner2))
			{
				if (corner1 == 0)
					corners.remove(i);
				else if (corner2 == arcLength.length - 1)
					corners.remove(i - 1);
				else if (curvature[corner1] < curvature[corner2])
					corners.remove(i - 1);
				else
					corners.remove(i);
			}
			else
			{
				i++;
			}
		}
		
		// Pixel distance threshold close to the endpoints
		double distThreshold = 10.0;
		
		// How many indices away from the endpoint we should be
		int endPtThreshold = 3;
		
		i = 1;
		
		// Remove corners too close to the stroke endpoints
		while (i < corners.size() - 1)
		{
			if (arcLength[corners.get(i)] < distThreshold ||
				corners.get(i) < endPtThreshold ||
				corners.get(i) > arcLength.length - endPtThreshold)
			{
				corners.remove(i);
			}
			else
			{
				i++;
			}
		}
		
		// Speed filter
		i = 1;
		while (i < corners.size() - 1)
		{
			if (speed[corners.get(i)] > avgSpeed * 0.75)
			{
				corners.remove(i);
			}
			else
			{
				i++;
			}
		}
	}
	
	
	/**
	 * Checks if two corners are similar to each other through a distance threshold
	 * 
	 * @param arcLength Array of arc length values for points
	 * @param index1 Index of corner 1
	 * @param index2 Index of corner 2
	 * @return True if the two corners are similar to one another
	 */
	private boolean areCornersSimilar(double[] arcLength, int index1, int index2)
	{
		// Pixel threshold to see if corners are too close
		double distThreshold = 15.0;
		
		// Index threshold to see if the corners are too close
		int pointIndexThreshold = 2;
		
		// Are the indices the same or too close?
		if (index1 == index2 || index2 - index1 <= pointIndexThreshold)
			return true;
		
		// Are the two corners close to each other?
		if (arcLength[index2] - arcLength[index1] < distThreshold)
			return true;
		
		return false;
	}
	
	
	
	
	/**
	 * Calculates the fit error for the stroke, given the corners
	 * 
	 * @param pts Points of the stroke
	 * @param corners Corner fit to calculate the error for
	 * @return The orthogonal distance squared error for the entire stroke
	 */
	private double getFitError(List<TPoint> pts, ArrayList<Integer> corners, double[] arcLength)
	{
		int numCorners = corners.size();

		double totalError = 0.0;
		double totalLength = arcLength[numCorners - 1];
		
		for (int i = 1; i < numCorners; i++)
		{
			int p1 = corners.get(i - 1);
			int p2 = corners.get(i);
			
			double error = segmentError(pts, p1, p2, arcLength);
			
			totalError += error;
		}
		
		if (totalLength > 0.0)
			return totalError / totalLength;
		else
			return totalError;
	}
	
	
	
	
	
	private double brandonFit(int p1, int p2)
	{
		BStroke bSegment = new BStroke(pts.subList(p1, p2));
		ArrayList<Fit> fits = bSegment.recognize(false);
		
		Fit bestFit = fits.get(0);
		
		if (bestFit.getName() == Fit.LINE || bestFit.getName() == Fit.ARC)
		{
			return bestFit.getError();
		}
		else
		{
			return Double.POSITIVE_INFINITY;
		}
	}
	
	
	
	
	
	
	private TreeMap<Double, Integer> getSegmentRatios(ArrayList<Integer> corners, double[] arcLength)
	{
		TreeMap<Double, Integer> segmentRatios = new TreeMap<Double, Integer>();
		
		double totalLength = arcLength[arcLength.length - 1];
		
		for (int i = 0; i < corners.size() - 1; i++)
		{
			double ratio = (arcLength[corners.get(i + 1)] - arcLength[corners.get(i)]) / totalLength;
			segmentRatios.put(ratio, i);
		}
		
		return segmentRatios;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Powerset test: test all the possible corner combinations
	 * 
	 * @param corners
	 * @param correctSegments
	 * @return
	 */
	public boolean powersetTest(ArrayList<Integer> corners, CornerFinder.SegType[] correctSegments)
	{
		ArrayList<ArrayList<Integer>> powerset = powersetCorners(corners);
		 
		for (ArrayList<Integer> subset : powerset)
		{
			CornerFinder.SegType[] segments = new CornerFinder.SegType[subset.size() - 1];
			 
			if (segments.length != correctSegments.length)
			{
				continue;
			}
			else
			{
				for (int i = 0; i < segments.length; i++)
				{
					segments[i] = getBSegType(subset.get(i), subset.get(i + 1));
				}
				 
				if (dataSegmentsMatch(segments, correctSegments))
				{
					return true;
				}
			}
		}
		 
		return false;
	}
	
	
	/**
	 * Check if two corner segment arrays match each other
	 * 
	 * @param data1	First segment array 
	 * @param data2 Second segment array
	 * @return		True if the segments match
	 */
	private boolean dataSegmentsMatch(CornerFinder.SegType[] data1, CornerFinder.SegType[] data2)
	{
		boolean match = false;
	
		if (data1.length == data2.length)
		{
			match = true;
			
			for (int i = 0; i < data1.length; i++)
			{
				if (data1[i] != data2[i])
				{
					match = false;
					i = data1.length;
				}
			}
		}
		
		return match;
	}
	
	
	/**
	 * Get the segment type from Brandon's segment
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	private CornerFinder.SegType getBSegType(int p1, int p2)
	{
		 BStroke bSegment = new BStroke(pts.subList(p1, p2));
		 ArrayList<Fit> fits = bSegment.recognize(false);
			
		 Fit bestFit = fits.get(0);
		 String fitName = bestFit.getName();
		 
		 if (fitName == Fit.LINE)
			 return CornerFinder.SegType.Line;
		 
		 else if (fitName == Fit.ARC)
			 return CornerFinder.SegType.Arc;
		 
		 else
			 return CornerFinder.SegType.Other;
	}
	
	
	private ArrayList<ArrayList<Integer>> powersetCorners(ArrayList<Integer> corners)
	{
		ArrayList<ArrayList<Integer>> powerset = new ArrayList<ArrayList<Integer>>();
		
		int start = corners.get(0);
		int end = corners.get(corners.size() - 1);
		
		if (corners.size() > 2)
		{
			ArrayList<Integer> cornersToPSet = new ArrayList<Integer>(corners.subList(1, corners.size() - 1));
			
			powerset = generatePSets(cornersToPSet);
		}
		
		appendEndpoints(start, end, powerset);
		
		return powerset;
	}
	
	
	private ArrayList<ArrayList<Integer>> generatePSets(ArrayList<Integer> corners)
	{
		ArrayList<ArrayList<Integer>> powerset = new ArrayList<ArrayList<Integer>>();
		
		if (corners.isEmpty())
		{
			powerset.add(corners);
			return powerset;
		}
		else
		{
			Integer removed = corners.remove(corners.size() - 1);
			
			ArrayList<ArrayList<Integer>> removedPSet = generatePSets(corners);
			ArrayList<ArrayList<Integer>> includedPSet = new ArrayList<ArrayList<Integer>>();//removedPSet);
			
			
			for (ArrayList<Integer> subset : removedPSet)
			{
				ArrayList<Integer> temp = new ArrayList<Integer>();
				temp.addAll(subset);
				temp.add(removed);
				
				includedPSet.add(temp);
			}
			
			powerset.addAll(removedPSet);
			powerset.addAll(includedPSet);
			
			return powerset;
		}	
	}
	
	
	private void appendEndpoints(int start, int end, ArrayList<ArrayList<Integer>> powerset)
	{
		for (ArrayList<Integer> subset : powerset)
		{
			subset.add(0, start);
			subset.add(end);
		}
	}
	
	
	
	
	
	

	/**
	 * Code to calculate a least squares fit between points
	 * 
	 * @param pts Points of the stroke
	 * @param index Midpoint of the LSQ fit
	 * @param window Window around the midpoint
	 * @return 0.0
	 */
	private double leastSquaresEstimate(List<TPoint> pts, int index, int window)
	{
		double Sx = 0.0;
		double Sy = 0.0;
		double Sxx = 0.0;
		double Sxy = 0.0;
	
		for (int i = index - window; i <= index + window; i++)
		{
			double x = pts.get(i).getX();
			double y = pts.get(i).getY();
			
			Sx += x;
			Sy += y;
			
			Sxx += x * x;
			Sxy += x * y;
		}
		
		double n = (window * 2) + 1;
		
		double dy = (n * Sxy) - (Sx * Sy);
		double dx = (n * Sxx) - (Sx * Sx);
		
		double beta = dy / dx;
		double alpha = (Sy - (beta * Sx)) / n;
	
		// Default return value
		return 0.0;
	}
	/**
	 * Segmentation error for a stroke
	 * 
	 * @param pts		Points of the stroke
	 * @param p1		Starting index point
	 * @param p2		Ending index point
	 * @param arcLength	Arc length array of the stroke
	 * @return			Segmentation error
	 */
	protected double segmentError(List<TPoint> pts, int p1, int p2, double[] arcLength)
	{
		double error = 0.0;
		
		if (getSegmentType(p1, p2) == SegType.Line)
		{
			error = orthogonalDistanceSquared(pts, p1, p2);
		}
		else
		{
			error = arcOrthogonalDistanceSquared(pts, p1, p2);
		}
		
		return error;
	}
	
	
	/**
	 * Compute the ODSQ using the original stroke and an optimal line between two points
	 * 
	 * @param pts Points of the stroke
	 * @param p1 End point one of the line (corner 1)
	 * @param p2 End point two of the line (corner 2)
	 * @return The ODSQ value for the segment
	 */
	protected double orthogonalDistanceSquared(List<TPoint> pts, int p1, int p2)
	{
		double error = 0.0;
		
		// Set the line between the corners
		TLine line = new TLine(pts.get(p1), pts.get(p2));
		
		// Checking for weird times when the point at p1 and p2 are equal
		if (pts.get(p1).getX() == pts.get(p2).getX() &&
			pts.get(p1).getY() == pts.get(p2).getY())
		{
			for (int i = p1 + 1; i < p2; i++)
			{
				double dist = euclidean(pts.get(p1), pts.get(i));
				
				error += (dist * dist);
			}
		}
			
		// Get the orthogonal distance between each point in the stroke and the line
		for (int i = p1 + 1; i < p2; i++)
		{
			// Running into an odd issue when a point is on the line, yet not part of the line segment:
			// p1------p2 * <- pt we're looking at is here
			try
			{
				double dist = line.distance(pts.get(i));
				error += (dist * dist);
			}
			catch (NullPointerException e)
			{
				// Do nothing then for this point
			}
		}
		
		return error;
	}
	
	
	/**
	 * Compute the ODSQ of an arc using the original stroke and an optimal line between 
	 * two points
	 * 
	 * @param pts Points of the stroke
	 * @param p1 End point one of the line (corner 1)
	 * @param p2 End point two of the line (corner 2)
	 * @return The ODSQ value for the segment
	 */
	protected double arcOrthogonalDistanceSquared(List<TPoint> pts, int p1, int p2)
	{
		double error = 0.0;
		
		// Set the line between the corners
		TLine line = new TLine(pts.get(p1), pts.get(p2));
		
		TPoint midPoint = line.getCenter();
		TLine perpBisector = line.getPerpendicularLine(midPoint);
		
		double slope = perpBisector.getSlope();
		double yInt = perpBisector.getYIntercept();
		
		TPoint pbPt1; 
		TPoint pbPt2;
		
		// Convoluted way to ensure that our perpendicular bisector line will definitely go through our stroke
		// It's long because there are many checks
		// TODO: Get rid of magic numbers
		if (Double.isInfinite(slope))
		{
			pbPt1 = new TPoint(midPoint.getX(), 0.0);
			pbPt2 = new TPoint(midPoint.getX(), 10000.0);
		}
		else if (slope == 0.0)
		{
			pbPt1 = new TPoint(0, yInt);
			pbPt2 = new TPoint(10000.0, yInt);
		}
		else
		{
			if (yInt < 0.0)
			{
				pbPt1 = new TPoint((-yInt / slope), 0.0);
				pbPt2 = new TPoint((10000.0 - yInt) / slope, 10000.0);
			}
			else
			{
				double xInt = -yInt / slope;
				
				if (xInt < 0.0)
				{
					pbPt2 = new TPoint(0, yInt);
					pbPt1 = new TPoint(10000.0, (10000.0 * slope) + yInt);
				}
				else
				{
					pbPt1 = new TPoint(xInt, 0.0);
					pbPt2 = new TPoint(0, yInt);
				}
			}
		}
		
		TPoint p3 = midPoint;
		
		// Get a third point that intersects the _stroke_ around its midpoint
		for (int i = p1; i < p2 - 1; i += 2)
		{
			if (isIntersection(pts.get(i), pts.get(i + 2), pbPt1, pbPt2))
			{
				double newX = (pts.get(i + 2).getX() + pts.get(i).getX()) / 2.0;
				double newY = (pts.get(i + 2).getY() + pts.get(i).getY()) / 2.0;
				p3 = new TPoint(newX, newY);
				
				i = p2;
			}
		}
		
		// http://mcraefamily.com/MathHelp/GeometryConicSectionCircleEquationGivenThreePoints.htm
		double a = pts.get(p1).getX();
		double b = pts.get(p1).getY();
		double c = p3.getX();
		double d = p3.getY();
		double e = pts.get(p2).getX();
		double f = pts.get(p2).getY();
		
		double k = (0.5 * ((((a*a) + (b*b)) * (e-c)) + (((c*c) + (d*d))*(a-e)) + (((e*e) + (f*f))*(c-a)))) 
			/ ((b * (e-c)) + (d * (a-e)) + (f * (c-a)));
		
		double h = (0.5 * ((((a*a) + (b*b)) * (f-d)) + (((c*c) + (d*d))*(b-f)) + (((e*e) + (f*f))*(d-b)))) 
			/ ((a * (f-d)) + (c * (b-f)) + (e * (d-b)));
	
		
		// If we're actually a line
		if (Double.isInfinite(k) || Double.isInfinite(h) || Double.isNaN(k) || Double.isNaN(h))
			return orthogonalDistanceSquared(pts, p1, p2);
		
		// Set the circle's center and radius
		TPoint center = new TPoint(h, k);
		double radius = Math.sqrt(((a-h) * (a-h)) + ((b-k) * (b-k)));
		
		// Get the orthogonal distance between each point in the stroke and the line
		for (int i = p1; i <= p2; i++)
		{
			double euc = euclidean(pts.get(i), center);
			double dist = radius - euc;
			
			error += (dist * dist);
		}
		
		return error;
	}
	
	
	/**
	 * http://www.mema.ucl.ac.be/~wu/FSA2716/Exercise1.htm
	 * 
	 * @param pt1
	 * @param pt2
	 * @param pt3
	 * @param pt4
	 * @return
	 */
	protected boolean isIntersection(TPoint pt1, TPoint pt2, TPoint pt3, TPoint pt4)
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

}
