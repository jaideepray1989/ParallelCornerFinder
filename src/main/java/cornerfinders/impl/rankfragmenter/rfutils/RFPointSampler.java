package cornerfinders.impl.rankfragmenter.rfutils;

import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jaideepray on 12/12/14.
 */
public class RFPointSampler {

    protected double ptDistance(double x0, double y0, double x1, double y1) {
        double xSq = (x1 - x0) * (x1 - x0);
        double ySq = (y1 - y0) * (y1 - y0);
        return Math.sqrt(xSq + ySq);
    }

    public double distance(TPoint p0, TPoint p1) {
        return ptDistance(p0.getX(), p0.getY(), p1.getX(), p1.getY());
    }

    public List<TPoint> resamplePoints(TStroke s, double distApart) {
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
        return resampledPts;
    }


}
