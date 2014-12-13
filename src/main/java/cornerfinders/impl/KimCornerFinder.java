package cornerfinders.impl;


import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.core.shapes.helpers.Features;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * My Kim-Kim corner finder implementation
 * 
 * @author Aaron Wolin
 */
public class KimCornerFinder extends AbstractCornerFinder
{
	private final boolean DEBUG = false;

	private final double RESAMPLE_SPACING = 5.0;
	
	private double[] direction;
	
	private double[] curvature;
	
	
	/**
	 * Finds the corners for a stroke
	 * 
	 * @param s		Stroke to find the corners for
	 * @return 		Corners of a stroke
	 */
	public ArrayList<Integer> findCorners(TStroke s)
	{
		long startTime = System.nanoTime();
		Features strokeCleaner = new Features();
		this.stroke = strokeCleaner.cleanStroke(s);
		
		// Resample the points
		this.pts = resamplePoints(stroke, RESAMPLE_SPACING);
		stroke = new TStroke(pts);
		
		// Get the arc length at each point
		arcLength = arcLength();
		direction = calcKimDirections(pts);
		curvature = calcKimCurvatures(direction, 3);
		
		ArrayList<Integer> corners = getKimFit(curvature);
		filterCorners(curvature, arcLength, corners);
		long elapsed = System.nanoTime() - startTime;
		System.out.println("Time taken by Kim & kim Corner finder:"+ elapsed);
		return corners;
	}
	
	
	/**
	 * Resample a stroke's points to be roughly distApart euclidean distance
	 * away from each other
	 *
	 * @param s			Stroke to resample the points for
	 * @param distApart Distance each point should be away from the other
	 * @return			A list of resampled points
	 */
	private List<TPoint> resamplePoints(TStroke s, double distApart)
	{
		ArrayList<TPoint> resampledPts = new ArrayList<TPoint>();
		List<TPoint> origPts = s.getPoints();
		
		TPoint prevResampled = origPts.get(0);
		resampledPts.add(origPts.get(0));
		
		int numPts = origPts.size();
		int prevIndex = 0;
		
		for (int i = 1; i < numPts; i++)
		{
			// Get the current euclidean distance between the two points
			double dist = euclidean(prevResampled, origPts.get(i));
			
			if (dist < distApart)
			{
				continue;
			}
			// If the distance is exactly equal to the threshold
			else if (dist == distApart)
			{
				prevResampled = origPts.get(i);
				resampledPts.add(prevResampled);
			}
			// If the distance is now greater than the threshold
			else
			{
				// Get the previous point we have seen
				TPoint prevPt;
				if (prevIndex == i - 1)
					prevPt = prevResampled;
				else
					prevPt = origPts.get(i - 1);
				
				// Get the current point we are looking at
				TPoint currPt = origPts.get(i);
				
				// Get the x and y differences between the current point and previous
				double xDiff = currPt.getX() - prevPt.getX();
				double yDiff = currPt.getY() - prevPt.getY();
				
				// Calculate the distance between the two
				double distBetween = euclidean(prevPt, currPt);
				
				// Calculate the previous euclidean distance between the last resampled
				// point and the last point we examined (before the current one)
				double prevEuc = euclidean(prevResampled, prevPt);
				double distDiff = distApart - prevEuc;
				
				double delta = distDiff / distBetween;
				
				// Create a new x and y value for the new point
				double newX = prevPt.getX();
				if (xDiff > 0)
					newX += Math.ceil(delta * xDiff);
				else
					newX += Math.floor(delta * xDiff);
				
				double newY = prevPt.getY();
				if (yDiff > 0)
					newY += Math.ceil(delta * yDiff);
				else
					newY += Math.floor(delta * yDiff);
				
				// Create a new time value for the new point
				long newTime = prevPt.getTime() + (long)((currPt.getTime() - prevPt.getTime()) * delta);
				
				// Create the new point
				TPoint newPt = new TPoint(newX, newY, newTime);
				
				// For debugging/breakpoint purposes
				//double newEuc = euclidean(prevResampled, newPt);
				
				// Set the new point to be the previous resampled, and then relook at the
				// distance between this new point and the current point we're on
				// This is incase the distance between the two points was greater than 2 * distApart
				prevResampled = newPt;
				resampledPts.add(prevResampled);
				prevIndex = i - 1;
				i--;
			}	
		}
		
		origPts = resampledPts;
		
		return resampledPts;
	}
	
	
	
	/*
	 * Feature functions
	 */

	/**
	 * Calculates the directions - Kim style
	 * 
	 * @param pts	Points of the stroke
	 * @return		Direction array of the stroke
	 */
	private double[] calcKimDirections(List<TPoint> pts)
	{
		int numPts = pts.size();
		double[] deltaXY = new double[numPts];
		
		// Set the initial direction point
		deltaXY[0] = 0.0;
		
		// Calculate the direction value for each point
		for (int i = 1; i < numPts; i++)
		{
			double d = direction(pts, i);
		
			// Make sure there are no large jumps in direction - ensures graph continuity
			if (Math.abs(d - deltaXY[i - 1]) > (0.95 * Math.PI * 2))
			{
				if (deltaXY[i - 1] < 0)
					d -= (Math.PI * 2);
				else
					d += (Math.PI * 2);
			}
			
			deltaXY[i] = d;
		}
		
		double[] direction = new double[numPts];
		
		for (int i = 1; i < numPts; i++)
		{
			direction[i] = deltaXY[i - 1] - deltaXY[i];
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
	 * Calculates the curvatures for the stroke - Kim style
	 * 
	 * @param direction	Direction array for the stroke
	 * @param window	Window of points around an index
	 * @return			Curvature array for the stroke
	 */
	private double[] calcKimCurvatures(double[] direction, int window)
	{
		double[] curvature = new double[direction.length];
		
		for (int i = 0; i < direction.length; i++)
		{
			curvature[i] = localMonotonicity(direction, i, window);
		}
		
		return curvature;
	}
	
	
	/**
	 * Calculates the local convexity
	 * 
	 * @param curvature	Curvature array of the stroke
	 * @param index		Index of the point to get the convexity for
	 * @param window	Window around the point
	 * @return			The local convexity
	 */
	private double localConvexity(double[] curvature, int index, int window)
	{
		/* WTF is u?
		if (Math.abs(curvature[index]) < u)
			return 0.0;
		*/
		
		int start = index - window;
		if (start < 0)
			start = 0;
		
		int end = index + window;
		if (end > curvature.length)
			end = curvature.length - 1;
		
		double c = curvature[index];
		
		for (int i = index + 1; i <= end; i++)
		{
			if (areSignsEqual(curvature[i - 1], curvature[i]))
				c += curvature[i];
		}
		
		
		for (int i = index - 1; i >= start; i--)
		{
			if (areSignsEqual(curvature[i + 1], curvature[i]))
				c += curvature[i];
		}
		
		return c;
	}
	
	
	/**
	 * Calculates the local monotonicity
	 * 
	 * @param curvature	Curvature array of the stroke
	 * @param index		Index of the point to get the convexity for
	 * @param window	Window around the point
	 * @return			Local monotonicity
	 */
	private double localMonotonicity(double[] curvature, int index, int window)
	{
		/* WTF is u?
		if (Math.abs(curvature[index]) < u)
			return 0.0;
		*/
		
		int start = index - window;
		if (start < 0)
			start = 0;
		
		int end = index + window;
		if (end > curvature.length - 1)
			end = curvature.length - 1;
		
		double c = curvature[index];
		double min = Math.abs(c);
		
		for (int i = index + 1; i <= end; i++)
		{
			if (areSignsEqual(curvature[i - 1], curvature[i]) && Math.abs(curvature[i]) < min)
			{
				c += curvature[i];
				min = Math.abs(curvature[i]);
			}
			else
				break;
		}
		
		
		for (int i = index - 1; i >= start; i--)
		{
			if (areSignsEqual(curvature[i + 1], curvature[i]) && Math.abs(curvature[i]) < min)
			{
				c += curvature[i];
				min = Math.abs(curvature[i]);
			}
			else
				break;
		}
		
		return c;
	}
	
	
	/**
	 * Checks to see if two doubles have equal signs
	 * 
	 * @param a	First double
	 * @param b	Second double
	 * @return	True if the signs are equal
	 */
	private boolean areSignsEqual(double a, double b)
	{
		if (a <= 0.0 && b <= 0.0)
			return true;
		else if (a >= 0.0 && b >= 0.0)
			return true;
		
		return false;
	}
	
	
	/*
	 * Get the fit for the corner
	 */

	/**
	 * Gets the corners of the stroke, based on Kim's curvature
	 * 
	 * @param curvature	Curvature array of the stroke
	 * @return			Corners for the stroke
	 */
	private ArrayList<Integer> getKimFit(double[] curvature)
	{
		double avgCurvMag = 0.0;
		double maxCurvature = Double.NEGATIVE_INFINITY;
		double minCurvature = Double.POSITIVE_INFINITY;
		
		for (int i = 0; i < curvature.length; i++)
		{
			double currCurv = curvature[i];
			
			if (currCurv > maxCurvature)
				maxCurvature = currCurv;
			
			if (currCurv < minCurvature)
				minCurvature = currCurv;
				
			avgCurvMag += Math.abs(curvature[i]);
		}
		
		avgCurvMag /= (double)curvature.length;
		
		
		ArrayList<Integer> corners = new ArrayList<Integer>();
		
		// Curvature values above this threshold will be considered for corners
		double threshold = Math.max(Math.abs(minCurvature), maxCurvature) * 0.25;
		
		// Find corners where our curvature is over the average curvature threshold
		for (int i = 0; i < curvature.length; i++)
		{
			// Find only the local maximum
			if (curvature[i] > 0 && curvature[i] > threshold)
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
			
			// Find only the local minimum
			else if (curvature[i] < 0 && curvature[i] < -threshold)
			{
				double localMinimum = Double.POSITIVE_INFINITY;
				int localMinimumIndex = i;
				
				while (i < curvature.length && curvature[i] < -threshold)
				{
					if (curvature[i] < localMinimum)
					{
						localMinimum = curvature[i];
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
		if (!corners.contains(curvature.length - 1))
			corners.add(curvature.length - 1);
		
		
		// Sort the corners
		Collections.sort(corners);
		
		// Return the list of corners (indices)
		return corners;
	}
	
	
	/**
	 * Filters corners
	 * 
	 * @param curvature	Curvature array for the stroke
	 * @param arcLength	Arc length array for the stroke
	 * @param corners	Corners to filter
	 */
	private void filterCorners(double[] curvature, double[] arcLength, ArrayList<Integer> corners)
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
}
