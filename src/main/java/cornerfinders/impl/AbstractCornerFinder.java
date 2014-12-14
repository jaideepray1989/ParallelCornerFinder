package cornerfinders.impl;

import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCornerFinder
{
	/**
	 * Segmentation types
	 */
	public enum SegType { Line, Arc, Curve, Other };

	/**
	 * Stroke last associated with the corner finder
	 */
	protected TStroke stroke;

	/**
	 * Points last associated with the corner finder
	 */
	protected List<TPoint> pts;

	/**
	 * Arc length array, holding the total path distance to a point
	 */
	protected double[] arcLength;


	/**
	 * Finds corners for the stroke given (it returns the indices)
	 *
	 * @return	Corners for the stroke
	 */
	public abstract ArrayList<Integer> findCorners(TStroke stroke);



	/*
	 * Feature functions
	 */

	/**
	 * Calculates the arc length for each point
	 *
	 * @return Arc length values at each point
	 */
	public double[] arcLength()
	{
		int numPts = pts.size();

		double[] arcLength = new double[numPts];

		arcLength[0] = 0.0;

		for (int i = 1; i < pts.size(); i++)
		{
			arcLength[i] = arcLength[i - 1] + euclidean(pts.get(i - 1), pts.get(i));
		}

		return arcLength;
	}



	/*
	 * Segment handling
	 */

	/**
	 * Return the stroke's segments
	 *
	 * @param corners	Corners of the stroke
	 * @return			Segmentation type arrays
	 */
	public SegType[] strokeSegments(ArrayList<Integer> corners)
	{
		SegType[] segments = new SegType[corners.size() - 1];

		for (int i = 0; i < corners.size() - 1; i++)
		{
			int c1 = corners.get(i);
			int c2 = corners.get(i + 1);

			segments[i] = getSegmentType(c1, c2);
		}

		return segments;
	}


	/**
	 * Check to see if the stroke segment between two points is a line or arc
	 *
	 * @param p1 Index for point 1
	 * @param p2 Index for point 2
	 * @return True if the segment is a line, False if the segment is an arc
	 */
	protected boolean isLine(int p1, int p2, double threshold)
	{
		if (getSegmentType(p1, p2, threshold) == SegType.Line)
			return true;
		else
			return false;
	}


	/**
	 * Check to see if the stroke segment between two points is a line or arc
	 *
	 * @param p1 Index for point 1
	 * @param p2 Index for point 2
	 * @return Segment type of the substroke between point indices p1 and p2
	 */
	protected SegType getSegmentType(int p1, int p2)
	{
		double threshold = 0.90;

		return getSegmentType(p1, p2, threshold);
	}


	/**
	 * Check to see if the stroke segment between two points is a line or arc
	 *
	 * @param p1 Index for point 1
	 * @param p2 Index for point 2
	 * @return Segment type of the substroke between point indices p1 and p2
	 */
	protected SegType getSegmentType(int p1, int p2, double threshold)
	{
        if (Math.max(p1, p2) >= arcLength.length || Math.max(p1, p2) >= pts.size())
            return SegType.Arc;
		double sizeThreshold = 4;

		TPoint pt1 = pts.get(p1);
		TPoint pt2 = pts.get(p2);

		double eucDist = euclidean(pt1, pt2);
		double segDist = arcLength[p2] - arcLength[p1];

		double lengthRatio = eucDist / segDist;

		if (lengthRatio > threshold || p2 - p1 < sizeThreshold)
		{
			return SegType.Line;
		}

		return SegType.Arc;
	}



	/*
	 * Euclidean distance functions
	 */

	/**
	 * Finds and returns the Euclidean distance between two points
	 *
	 * @param p0 First point
	 * @param p1 Second point
	 * @return Euclidean distance between p0 and p1
	 */
	protected double euclidean(TPoint p0, TPoint p1)
	{
		return euclidean(p0.getX(), p0.getY(), p1.getX(), p1.getY());
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
	protected double euclidean(double x0, double y0, double x1, double y1)
	{
		double xSq = (x1 - x0) * (x1 - x0);
		double ySq = (y1 - y0) * (y1 - y0);

		return Math.sqrt(xSq + ySq);
	}



	/*
	 * Getters
	 */

	/**
	 * Returns the last associated stroke with the corner finder
	 *
	 * @return	Last stroke associated with the corner finder
	 */
	public TStroke getStroke()
	{
		return this.stroke;
	}


	/**
	 * Returns the points last associated with the corner finder
	 *
	 * @return	Points last associated with the corner finder
	 */
	public List<TPoint> getPoints()
	{
		return this.pts;
	}
}
