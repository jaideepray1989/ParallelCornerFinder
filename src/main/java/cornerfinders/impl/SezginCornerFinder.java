package cornerfinders.impl;

import cornerfinders.core.shapes.TLine;
import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;


public class SezginCornerFinder extends CornerFinder
{
	private final boolean DEBUG = false;

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
	 * @param s Stroke to find the corners for
	 * @return Corners of a stroke
	 */
	public ArrayList<Integer> findCorners(TStroke s)
	{
//		Features strokeCleaner = new Features();
//		this.stroke = strokeCleaner.cleanStroke(s);
		
		this.pts = stroke.getPoints();
		
		// Get the arc length at each point
		arcLength = arcLength();
		direction = calcDirections(pts, true);
		curvature = calcCurvatures(arcLength, direction, true);
		speed = calcSpeed(pts, arcLength, true);
		
		// Get the corners from curvature and speed
		ArrayList<Integer> Fc = getCornersFromCurvature(curvature);
		ArrayList<Integer> Fs = getCornersFromSpeed(speed);
		
		// Calculate an initial fit for the corners
		ArrayList<Integer> corners = initialFit(Fc, Fs);
		
		// Find the best corners from a hybrid fit
		corners = hybridFit(pts, curvature,  speed, arcLength, corners, Fc, Fs);
		
		// Graph output (for debugging)
		if (DEBUG)
		{
			outputDirectionGraph(direction, arcLength);
			outputSpeedGraph(speed, arcLength);			
			outputCurvatureGraph(curvature, arcLength);
			outputCornerGraph(curvature, speed, arcLength, corners);
		}
		
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
	 * Generate corner fits
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
	 * Finds the initial corner fit by taking the intersection of the curvature and speed
	 * corners
	 * 
	 * @param Fc Curvature corners
	 * @param Fs Speed corners
	 * @return intersection(Fc, Fs)
	 */
	private ArrayList<Integer> initialFit(ArrayList<Integer> Fc, ArrayList<Integer> Fs)
	{
		ArrayList<Integer> corners = new ArrayList<Integer>();
		
		// Initial fit is comprised of the intersection of
		// curvature and speed corners
		int i = 0;
		
		while (i < Fc.size())
		{
			int value = Fc.get(i).intValue();
			
			if (Fs.contains(value))
			{
				corners.add(value);
				
				Integer corner = Fc.get(i);
				
				Fs.remove(corner);
				Fc.remove(corner);
			}
			else
			{
				i++;
			}
		}
		
		// Sort the collections, just to be safe
		Collections.sort(corners);
		Collections.sort(Fc);
		Collections.sort(Fs);
		
		return corners;
	}
	
	
	/**
	 * Generate hybrid fits from the corners, and choose the "best" fit based on the fit that
	 * is below a threshold with the least number of corners
	 * 
	 * @param pts Points of the stroke
	 * @param curvature Curvature values in an array
	 * @param speed Speed values in an array
	 * @param corners Initial fit
	 * @param Fc Remaining curvature corners
	 * @param Fs Remaining speed corners
	 * @return Corners representing the best hybrid fit for the stroke
	 */
	private ArrayList<Integer> hybridFit(List<TPoint> pts, double[] curvature, double[] speed, 
		double[] arcLength, ArrayList<Integer> corners, ArrayList<Integer> Fc, ArrayList<Integer> Fs)
	{
		// Filter out similar corners
		filterCorners(curvature, speed, arcLength, Fc, Fs);
		
		// Calculate the curvature and speed metrics
		TreeMap<Double, Integer> curvatureMetrics = calculateCurvatureMetrics(curvature, arcLength, Fc);
		TreeMap<Double, Integer> speedMetrics = calculateSpeedMetrics(speed, maxSpeed, Fs);
		
		// Get a fit for all of the corners
		ArrayList<Integer> allCorners = new ArrayList<Integer>();
		allCorners.addAll(corners);
		allCorners.addAll(Fc);
		allCorners.addAll(Fs);
		Collections.sort(allCorners);
		
		// Get the initial fit
		double error0 = getFitError(pts, corners, arcLength);
		
		// Get the fit for all of the corners
		double errorAll = getFitError(pts, allCorners, arcLength);
		
		// Create an error threshold
		double errorThreshold = (0.1 * (error0 - errorAll)) + errorAll; 
		
		// Check to see if the two endpoints are a decent enough approximation
		double singleShapeRatio = 0.70;
		if (errorAll / errorThreshold > singleShapeRatio || error0 < 100.0)
			return corners;
		
		// Storage for the hybrid fits
		TreeMap<Double, ArrayList<Integer>> hybridFits = new TreeMap<Double, ArrayList<Integer>>();
		
		// Generate the Hybrid fits
		ArrayList<Integer> Hi = new ArrayList<Integer>();
		Hi.addAll(corners);
		
		// Create hybrid fits
		while (!curvatureMetrics.isEmpty() || !speedMetrics.isEmpty())
		{
			// Create a hybrid fit for curvature
			ArrayList<Integer> HiP = new ArrayList<Integer>();
			HiP.addAll(Hi);
			
			double HiPFit = Double.POSITIVE_INFINITY;
			Double HiPKey = 0.0;
			
			if (!curvatureMetrics.isEmpty())
			{
				HiP.add(curvatureMetrics.lastEntry().getValue());
				HiPKey = curvatureMetrics.lastEntry().getKey();
				Collections.sort(HiP);
				
				HiPFit = getFitError(pts, HiP, arcLength);
			}
			
			// Create a hybrid fit for speed
			ArrayList<Integer> HiPP = new ArrayList<Integer>();
			HiPP.addAll(Hi);
			
			double HiPPFit = Double.POSITIVE_INFINITY;
			Double HiPPKey = 0.0;
			
			if (!speedMetrics.isEmpty())
			{
				HiPP.add(speedMetrics.lastEntry().getValue());
				HiPPKey = speedMetrics.lastEntry().getKey();
				Collections.sort(HiPP);
				
				HiPPFit = getFitError(pts, HiPP, arcLength);
			}
			
			// Set the next fit in the series
			if (HiPFit <= HiPPFit)
			{
				hybridFits.put(HiPFit, HiP);
				Hi = HiP;
				
				curvatureMetrics.remove(HiPKey);
			}
			else
			{
				hybridFits.put(HiPPFit, HiPP);
				Hi = HiPP;
				
				speedMetrics.remove(HiPPKey);
			}
		}
		
		// Find the best fit by corners
		ArrayList<Integer> bestFit = corners;
		int fewestCorners = Integer.MAX_VALUE;
		
		while (!hybridFits.isEmpty())
		{
			Map.Entry<Double, ArrayList<Integer>> entry = hybridFits.pollFirstEntry();
			
			double fitError = entry.getKey();
			int numCorners = entry.getValue().size();
			
			if (numCorners < fewestCorners && fitError < errorThreshold)
			{
				fewestCorners = numCorners;
				bestFit = entry.getValue();
			}
		}
		
		return bestFit;
	}
	
	
	
	
	/*
	 * Metrics for curvature and speed 
	 */
	
	/**
	 * Calculates the CCM metric for all corners given
	 * 
	 * @param curvature Curvature values in an array
	 * @param arcLength Arc length values in an array
	 * @param corners Corners to calculate the metric for
	 * @return A sorted (ascending) TreeMap mapping the metric value with the corner
	 */
	private TreeMap<Double, Integer> calculateCurvatureMetrics(double[] curvature,
		double[] arcLength, ArrayList<Integer> corners)
	{
		TreeMap<Double, Integer> curvatureMetrics = new TreeMap<Double, Integer>();
		
		for (Integer c : corners)
		{
			curvatureMetrics.put(CCM(curvature, arcLength, c), c);
		}
		
		return curvatureMetrics;
	}
	
	
	/**
	 * Calculates the SCM metric for all corners given
	 * 
	 * @param speed Speed values in an array
	 * @param maxSpeed Maximum speed value of the stroke
	 * @param corners Corners to calculate the metric for
	 * @return A sorted (ascending) TreeMap mapping the metric value with the corner
	 */
	private TreeMap<Double, Integer> calculateSpeedMetrics(double[] speed,
		double maxSpeed, ArrayList<Integer> corners)
	{
		TreeMap<Double, Integer> speedMetrics = new TreeMap<Double, Integer>();
		
		for (Integer c : corners)
		{
			speedMetrics.put(SCM(speed, maxSpeed, c), c);
		}
		
		return speedMetrics;
	}
	
	
	/**
	 * CCM metric from Sezgin's paper
	 * 
	 * @param curvature Curvature values in an array
	 * @param arclength Arc length values in an array
	 * @param index Index of the point (corner)
	 * @return The computed metric
	 */
	private double CCM(double[] curvature, double[] arclength, int index)
	{
		int k = 3;
		
		int start = index - k;
		if (start < 0)
			start = 0;
		
		int end = index + k;
		if (end > curvature.length - 1)
			end = curvature.length - 1;
	
		double segmentLength = arclength[end] - arclength[start];
		
		if (segmentLength > 0.0)
			return Math.abs(curvature[start] - curvature[end]) / segmentLength;
		else
			return 0.0;
	}
	
	
	/**
	 * SCM metric from Sezgin's paper
	 * 
	 * @param speed Curvature values in an array
	 * @param maxSpeed Maximum speed value of the stroke
	 * @param index Index of the point (corner)
	 * @return The computed metric
	 */
	private double SCM(double[] speed, double maxSpeed, int index)
	{
		if (maxSpeed > 0.0)
			return 1.0 - (speed[index] / maxSpeed);
		else
			return 1.0;
	}
	
	
	/**
	 * Removes corners based on similarity.  This is my work, not Sezgin's
	 * 
	 * @param speed Speed values for points
	 * @param curvature Curvature values for points
	 * @param arcLength Arc length values for points
	 * @return An ArrayList of filtered corners
	 */
	private void filterCorners(double[] curvature, double[] speed, double[] arcLength, 
			ArrayList<Integer> Fc, ArrayList<Integer> Fs)
	{
		ArrayList<Integer> corners = new ArrayList<Integer>();
		corners.addAll(Fc);
		corners.addAll(Fs);
		Collections.sort(corners);
		
		int i = 1;
		
		// Remove similar corners
		while (i < corners.size())
		{
			int corner1 = corners.get(i - 1);
			int corner2 = corners.get(i);
			
			if (areCornersSimilar(arcLength, corner1, corner2))
			{
				if (CCM(curvature, arcLength, corner1) < CCM(curvature, arcLength, corner2))
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
		
		i = 0;
		
		// Remove corners too close to the stroke endpoints
		while (i < corners.size())
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
		
		i = 0;
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
		
		// Resort the corners into their respective Fc and Fs holders
		i = 0;
		while (i < Fc.size())
		{
			if (!corners.contains(Fc.get(i)))
			{
				Fc.remove(i);
			}
			else
			{
				i++;
			}
		}
		
		i = 0;
		while (i < Fs.size())
		{
			if (!corners.contains(Fs.get(i)))
			{
				Fs.remove(i);
			}
			else
			{
				i++;
			}
		}
		
		return;
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
	 * Checks to see if the segment is a line
	 * 
	 * @param p1 Index for point 1
	 * @param p2 Index for point 2
	 * @param pts Points of the stroke
	 * @param arcLength Arc length value at each point
	 * @return True if the segment is a line, False if the segment is an arc
	 */
	private boolean isLine(int p1, int p2, List<TPoint> pts, double[] arcLength)
	{
		if (getSegmentType(p1, p2) == SegType.Line)
			return true;
		else
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
	
	
	/**
	 * Segmentation error for a stroke
	 * 
	 * @param pts		Points of the stroke
	 * @param p1		Starting index point
	 * @param p2		Ending index point
	 * @param arcLength	Arc length array of the stroke
	 * @return			Segmentation error
	 */
	private double segmentError(List<TPoint> pts, int p1, int p2, double[] arcLength)
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
	private double orthogonalDistanceSquared(List<TPoint> pts, int p1, int p2)
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
	private double arcOrthogonalDistanceSquared(List<TPoint> pts, int p1, int p2)
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
	
	
	
	
	/*
	 * Getter methods
	 */
	
	public double getAvgCurvature()
	{
		return this.avgCurvature;
	}
	
	
	public double getMaxCurvature()
	{
		return this.getMaxCurvature();
	}
	

	public double getAvgSpeed()
	{
		return this.avgSpeed;
	}
	
	
	
	
	/*
	 * Output information to create graphs in Excel
	 */
	
	/**
	 * Outputs a direction graph in a .txt file that can be imported to Excel
	 * 
	 * @param direction	Directions of a stroke
	 * @param arcLength	Arclength values of a stroke
	 */
	private void outputDirectionGraph(double[] direction, double[] arcLength)
	{
		try
		{
			FileOutputStream fout = new FileOutputStream("dirGraph.txt");
			PrintStream p = new PrintStream(fout);
		
			for (int i = 0; i < direction.length; i++)
			{
				p.print(direction[i] + "\t");
				p.print(arcLength[i] + "\n");
			}
			
			fout.close();
		}
		catch (IOException e)
		{
			System.err.println ("Error: could not print to file");
			System.exit(-1);
		}
	}
	
	
	/**
	 * Outputs a curvature graph in a .txt file that can be imported to Excel
	 * 
	 * @param curvature	Curvatures of a stroke
	 * @param arcLength	Arclength values of a stroke
	 */
	private void outputCurvatureGraph(double[] curvature, double[] arcLength)
	{
		try
		{
			FileOutputStream fout = new FileOutputStream("curvGraph.txt");
			PrintStream p = new PrintStream(fout);
		
			for (int i = 0; i < curvature.length; i++)
			{
				p.print(curvature[i] + "\t");
				p.print(arcLength[i] + "\n");
			}
			
			fout.close();
		}
		catch (IOException e)
		{
			System.err.println ("Error: could not print to file");
			System.exit(-1);
		}
	}
	
	
	/**
	 * Outputs a speed graph in a .txt file that can be imported to Excel
	 * 
	 * @param speed		Speeds of a stroke 
	 * @param arcLength	Arclength values of a stroke
	 */
	private void outputSpeedGraph(double[] speed, double[] arcLength)
	{
		try
		{
			FileOutputStream fout = new FileOutputStream("speedGraph.txt");
			PrintStream p = new PrintStream(fout);
		
			for (int i = 0; i < speed.length; i++)
			{
				p.print(speed[i] + "\t");
				p.print(arcLength[i] + "\n");
			}
			
			fout.close();
		}
		catch (IOException e)
		{
			System.err.println ("Error: could not print to file");
			System.exit(-1);
		}
	}
	
	
	/**
	 * Outputs a corner graph in a .txt file that can be imported to Excel
	 * 
	 * @param curvature	Curvatures of a stroke
	 * @param speed		Speeds of a stroke 
	 * @param arcLength	Arclength values of a stroke
	 * @param corners	Corners of a stroke
	 */
	private void outputCornerGraph(double[] curvature, double[] speed, double[] arcLength, 
			ArrayList<Integer> corners)
	{
		try
		{
			FileOutputStream fout = new FileOutputStream("cornerGraph.txt");
			PrintStream p = new PrintStream(fout);
		
			for (int i = 0; i < speed.length; i++)
			{
				p.print(curvature[i] + "\t");
				p.print(speed[i] + "\t");
				p.print(arcLength[i] + "\n");
			}
			
			p.println();
			for (Integer corner : corners)
			{
				p.println(corner);
			}
			
			fout.close();
		}
		catch (IOException e)
		{
			System.err.println ("Error: could not print to file");
			System.exit(-1);
		}
	}
}
