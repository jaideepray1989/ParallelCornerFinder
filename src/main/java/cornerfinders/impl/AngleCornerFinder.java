package cornerfinders.impl;

import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.core.shapes.helpers.Features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Corner finder based off Tracy's idea that the distance between resampled points
 * is small around corners
 *
 * @author Aaron Wolin
 */
public class AngleCornerFinder extends AbstractCornerFinder {
    private final boolean DEBUG = false;

    private final double PERCENTAGE = 0.95;

    private final double LINE_THRESHOLD = 0.95;

    private double resampleSpacing = 5.0;

    private int window = 3;


    /**
     * Finds the corners for a stroke
     *
     * @param s Stroke to find the corners for
     * @return Corners of a stroke
     */
    public ArrayList<Integer> findCorners(TStroke s) {
        Features strokeCleaner = new Features();
        this.stroke = strokeCleaner.cleanStroke(s);

        resampleSpacing = determineResampleSpacing(this.stroke.getPoints());

        this.pts = resamplePoints2(stroke, resampleSpacing);
        this.stroke = new TStroke(pts);
        arcLength = arcLength();
        if (pts.size() > (window * 2)) {
            ArrayList<Integer> corners = getCornersFromResampleLength(pts, window);
            return corners;
        } else
            return new ArrayList<Integer>();
        //return new ArrayList<Integer>();
    }


    /**
     * Resample a stroke's points to be roughly distApart euclidean distance
     * away from each other
     *
     * @param s         Stroke to resample the points for
     * @param distApart Distance each point should be away from the other
     * @return A list of resampled points
     */
    private List<TPoint> resamplePoints(TStroke s, double distApart) {
        ArrayList<TPoint> resampledPts = new ArrayList<TPoint>();
        List<TPoint> origPts = s.getPoints();

        TPoint prevResampled = origPts.get(0);
        resampledPts.add(origPts.get(0));

        int numPts = origPts.size();
        int prevIndex = 0;

        for (int i = 1; i < numPts; i++) {
            // Get the current euclidean distance between the two points
            double dist = distance(prevResampled, origPts.get(i));

            if (dist < distApart) {
                continue;
            }
            // If the distance is exactly equal to the threshold
            else if (dist == distApart) {
                prevResampled = origPts.get(i);
                resampledPts.add(prevResampled);
            }
            // If the distance is now greater than the threshold
            else {
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
                double distBetween = distance(prevPt, currPt);

                // Calculate the previous euclidean distance between the last resampled
                // point and the last point we examined (before the current one)
                double prevEuc = distance(prevResampled, prevPt);
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
                long newTime = prevPt.getTime() + (long) ((currPt.getTime() - prevPt.getTime()) * delta);

                // Create the new point
                TPoint newPt = new TPoint(newX, newY, newTime);

                // For debugging/breakpoint purposes
                //double newEuc = distance(prevResampled, newPt);

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


    private List<TPoint> resamplePoints2(TStroke s, double I) {
        List<TPoint> points = s.getPoints();

        ArrayList<TPoint> newPoints = new ArrayList<TPoint>();
        newPoints.add(points.get(0));

        double D = 0;

        for (int i = 1; i < points.size(); i++) {
            // Get the current distance distance between the two points
            double d = distance(points.get(i - 1), points.get(i));

            if (D + d >= I) {
                double q_x = points.get(i - 1).getX() +
                        (((I - D) / d) * (points.get(i).getX() - points.get(i - 1).getX()));

                double q_y = points.get(i - 1).getY() +
                        (((I - D) / d) * (points.get(i).getY() - points.get(i - 1).getY()));

                TPoint q = new TPoint(q_x, q_y);

                newPoints.add(q);
                points.add(i, q);

                D = 0;
            } else {
                D = D + d;
            }
        }

        //points = newPoints;

        return newPoints;
    }


    private double determineResampleSpacing(List<TPoint> pts) {
        double totalLength = 0.0;
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < pts.size(); i++) {
            double x = pts.get(i).getX();
            double y = pts.get(i).getY();

            if (x < minX)
                minX = x;
            if (x > maxX)
                maxX = x;
            if (y < minY)
                minY = y;
            if (y > maxY)
                maxY = y;
            //not being used - commenting
            //totalLength += distance(pts.get(i), pts.get(i + 1));
        }

        double diagonal = Math.sqrt(Math.pow(maxX - minX, 2) + Math.pow(maxY - minY, 2));
        //double density = totalLength / diagonal;

        double spacing = diagonal / 40.0;

        return spacing;
    }
	
	
	
	/*
	 * Corner fits
	 */

    /**
     * Gets the corners from the resampled points. Works by finding the shortest
     * local length around a corner
     *
     * @param pts Points of the stroke
     * @return Corners for the stroke
     */
    private ArrayList<Integer>  getCornersFromResampleLength(List<TPoint> pts, int window) {
        //double[] distApart = new double[pts.size()];
        double[] angleApart = new double[pts.size()];
        double[] sortedAngles = new double[pts.size() - (window * 2)];
        double avgAngleApart = 0.0;
        double distance1, distance2;
        for (int i = window; i < pts.size() - window; i++) {
            distance1 = distance(pts.get(i - window), pts.get(i));
            distance2 = distance(pts.get(i), pts.get(i + window));
            angleApart[i] = Math.atan(distance1 / distance2);
            sortedAngles[i - window] = angleApart[i];
            avgAngleApart += angleApart[i];
        }

        ArrayList<Integer> corners = new ArrayList<Integer>();
        corners.add(0);

        Arrays.sort(sortedAngles);
        double medianAngle = sortedAngles[sortedAngles.length / 2];

        avgAngleApart /= (pts.size() - (window * 2));
        double threshold = PERCENTAGE * medianAngle;

        //double oldThreshold = 0.80 * maxLengthBetween(resampleSpacing, window);
        //double d = 1.0;

        for (int i = window; i < angleApart.length - window; i++) {
            // Find only the local minimum
            if (angleApart[i] < threshold) {
                double localMinimum = Double.POSITIVE_INFINITY;
                int localMinimumIndex = i;

                while (i < angleApart.length - window && angleApart[i] < threshold) {
                    if (angleApart[i] < localMinimum) {
                        localMinimum = angleApart[i];
                        localMinimumIndex = i;
                    }

                    i++;
                }

                //corners.add(new Integer(localMinimumIndex - window));
                corners.add(new Integer(localMinimumIndex));
                //corners.add(new Integer(localMinimumIndex + window));
            }
        }

        corners.add(angleApart.length - 1);

        //Collections.sort(corners);

        return straightLinePostProcessing(corners, angleApart);

        //return corners;
    }


    private ArrayList<Integer> straightLinePostProcessing(ArrayList<Integer> corners, double[] distApart) {
        ArrayList<Integer> filteredCorners = new ArrayList<Integer>(corners);

        boolean allLines = false;

        while (!allLines) {
            allLines = true;

            for (int i = 1; i < filteredCorners.size(); i++) {
                int c1 = filteredCorners.get(i - 1);
                int c2 = filteredCorners.get(i);

                if (!isLine(c1, c2, LINE_THRESHOLD)) {
                    int newCorner = minDistBetweenIndices(distApart, c1, c2);
                    filteredCorners.add(i, newCorner);

                    allLines = false;
                }
            }
        }

        for (int i = 1; i < filteredCorners.size() - 1; i++) {
            int c1 = filteredCorners.get(i - 1);
            int c2 = filteredCorners.get(i);
            int c3 = filteredCorners.get(i + 1);

            if (isLine(c1, c3, LINE_THRESHOLD)) {
                filteredCorners.remove(i);
                i--;
            }
        }
        try {

            if (distance(pts.get(0), pts.get(filteredCorners.get(1))) < 15.0) {
                filteredCorners.remove(1);
            }

            if (distance(pts.get(pts.size() - 1), pts.get(filteredCorners.get(filteredCorners.size() - 2))) < 15.0) {
                filteredCorners.remove(filteredCorners.size() - 2);
            }
        } catch (Exception ex) {

        }

        return filteredCorners;
    }
	
	
	
	/*
	 * SIDE PROJECT
	 */

    private ArrayList<Integer> straightLineCornerFinder() {
        double[] distApart = new double[pts.size()];

        for (int i = window; i < pts.size() - window; i++) {
            distApart[i] = distance(pts.get(i - window), pts.get(i + window));
        }

        double medianDistApart = getMedianDist(distApart);
        distApart = refactorDistApart(distApart, medianDistApart * 0.95);

        ArrayList<Integer> corners = new ArrayList<Integer>();
        corners.add(0);
        corners.add(distApart.length - 1);

        boolean allLines = false;

        while (!allLines) {
            allLines = true;

            for (int i = 1; i < corners.size(); i++) {
                int c1 = corners.get(i - 1);
                int c2 = corners.get(i);

                if (!isLine(c1, c2, LINE_THRESHOLD)) {
                    int newCorner = minDistBetweenIndices(distApart, c1, c2);
                    corners.add(i, newCorner);

                    allLines = false;
                }
            }
        }

        //Collections.sort(corners);

        return corners;
    }


    private double[] refactorDistApart(double[] distApart, double threshold) {
        for (int i = 0; i < window; i++) {
            distApart[i] = threshold;
        }

        for (int i = distApart.length - window; i < distApart.length; i++) {
            distApart[i] = threshold;
        }

        for (int i = 0; i < distApart.length; i++) {
            // Find only the local minimum
            if (distApart[i] < threshold) {
                int startIndex = i;

                double localMinimum = Double.POSITIVE_INFINITY;
                int localMinimumIndex = i;

                while (i < distApart.length - window && distApart[i] < threshold) {
                    if (distApart[i] < localMinimum) {
                        localMinimum = distApart[i];
                        localMinimumIndex = i;
                    }

                    i++;
                }

                for (int k = startIndex; k < i; k++) {
                    if (k != localMinimumIndex)
                        distApart[k] = threshold;
                }
            }
        }

        return distApart;
    }


    private double getMedianDist(double[] distApart) {
        double[] sortedDists = Arrays.copyOfRange(distApart, window, distApart.length - window);
        Arrays.sort(sortedDists);

        return sortedDists[sortedDists.length / 2];
    }


    private int minDistBetweenIndices(double[] distApart, int p1, int p2) {
        int minIndex = Integer.MAX_VALUE;
        double minValue = Double.POSITIVE_INFINITY;
		
		/*for (int i = p1 + 1; i < p2; i++)
		{
			if (distApart[i] < minValue)
			{
				minValue = distApart[i];
				minIndex = i;
			}
		}*/

        int toMid = (p2 - p1) / 4;

        // search for min dist halfway between?
        for (int i = p1 + toMid; i < p2 - toMid; i++) {
            if (distApart[i] < minValue) {
                minValue = distApart[i];
                minIndex = i;
            }
        }

        return minIndex;
    }


    private ArrayList<Integer> allPoints(List<TPoint> pts, int window) {
        ArrayList<Integer> corners = new ArrayList<Integer>();

        for (int i = 0; i < pts.size(); i++) {
            corners.add(i);
        }

        return corners;
    }


    /**
     * Gets the corners from the resampled points. Works by finding the shortest
     * local length around a corner
     *
     * @return Corners for the stroke
     * <p/>
     * private ArrayList<Integer> getCornersFromResampleLength(List<TPoint> pts, int window)
     * {
     * double[] distApart = new double[pts.size()];
     * double[] stdDev = new double[pts.size()];
     * <p/>
     * for (int i = 7; i < pts.size() - 7; i++)
     * {
     * double[] distValues = new double[3];
     * <p/>
     * distValues[0] = euclidean(pts.get(i - 3), pts.get(i + 3)) / maxLengthBetween(resampleSpacing, 3);
     * distValues[1] = euclidean(pts.get(i - 5), pts.get(i + 5)) / maxLengthBetween(resampleSpacing, 5);
     * distValues[2] = euclidean(pts.get(i - 7), pts.get(i + 7)) / maxLengthBetween(resampleSpacing, 7);
     * <p/>
     * stdDev[i] = stdDev(distValues);
     * }
     * <p/>
     * ArrayList<Integer> corners = new ArrayList<Integer>();
     * corners.add(0);
     * <p/>
     * double avgStdDev = 0.0;
     * for (int i = 7; i < stdDev.length - 7; i++)
     * {
     * avgStdDev += stdDev[i];
     * }
     * avgStdDev /= (stdDev.length - 14);
     * <p/>
     * double threshold = avgStdDev;
     * <p/>
     * for (int i = 7; i < stdDev.length - 7; i++)
     * {
     * // Find only the local maximum
     * if (stdDev[i] > threshold)
     * {
     * double localMaximum = Double.NEGATIVE_INFINITY;
     * int localMaximumIndex = i;
     * <p/>
     * while (i < stdDev.length && stdDev[i] > threshold)
     * {
     * if (stdDev[i] > localMaximum)
     * {
     * localMaximum = stdDev[i];
     * localMaximumIndex = i;
     * }
     * <p/>
     * i++;
     * }
     * <p/>
     * corners.add(new Integer(localMaximumIndex));
     * }
     * }
     * <p/>
     * corners.add(stdDev.length - 1);
     * <p/>
     * return corners;
     * }
     */


    private double maxLengthBetween(double resampleSpacing, double window) {
        return resampleSpacing * ((window + 1) * 2);
    }


    /**
     * Calculates the standard deviation for an array of values
     *
     * @param values Values in an array
     * @return Std deviation of the values
     */
    private double stdDev(double[] values) {
        double sum = 0.0;
        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }
        double avg = sum / (double) values.length;

        double stdDev = 0.0;
        for (int i = 0; i < values.length; i++) {
            stdDev += Math.pow(values[i] - avg, 2.0);
        }

        stdDev = Math.sqrt(stdDev / (double) values.length);

        return stdDev;
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
    protected double distance(TPoint p0, TPoint p1) {
        return distance(p0.getX(), p0.getY(), p1.getX(), p1.getY());
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
    protected double distance(double x0, double y0, double x1, double y1) {
        double xSq = (x1 - x0) * (x1 - x0);
        double ySq = (y1 - y0) * (y1 - y0);

        //return Math.sqrt(xSq + ySq);
        return Math.sqrt(xSq + ySq);
    }
}
