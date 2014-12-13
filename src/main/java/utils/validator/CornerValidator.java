package utils.validator;

import com.google.common.collect.Maps;
import cornerfinders.core.shapes.TPoint;
import cornerfinders.impl.AbstractCornerFinder;
import cornerfinders.impl.rankfragmenter.rfutils.RFUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by jaideepray on 12/13/14.
 */
public class CornerValidator {

    public static List<TPoint> validateCorners(List<TPoint> corners) {

        // sort them
        corners.sort(new Comparator<TPoint>() {
            @Override
            public int compare(TPoint o1, TPoint o2) {
                return (o1.getX() < o2.getX()) ? -1 : 1;
            }
        });


        for (int i = 0; i < corners.size() - 1; i++) {
            if (RFUtils.euclidean(corners.get(i), corners.get(i + 1)) < 25) {
                corners.remove(i + 1);
            }
        }

        return filter(corners);
    }


    public static List<TPoint> filter(List<TPoint> corners) {
        Map<TPoint, Double> arcLengthMap = arcLengthMap(corners);
        return corners;
    }

    protected AbstractCornerFinder.SegType getSegmentType(TPoint p1, TPoint p2, double threshold, Map<TPoint, Double> arcLengthMap) {
        double eucDist = RFUtils.euclidean(p1, p2);
        double segDist = arcLengthMap.get(p1) - arcLengthMap.get(p2);
        double lengthRatio = eucDist / segDist;
        if (lengthRatio > threshold) {
            return AbstractCornerFinder.SegType.Line;
        }
        return AbstractCornerFinder.SegType.Arc;
    }

    public static Map<TPoint, Double> arcLengthMap(List<TPoint> corners) {
        Map<TPoint, Double> map = Maps.newHashMap();
        int numPts = corners.size();

        double[] arcLength = new double[numPts];

        arcLength[0] = 0.0;

        for (int i = 1; i < corners.size(); i++) {
            arcLength[i] = arcLength[i - 1] + RFUtils.euclidean(corners.get(i - 1), corners.get(i));
            map.put(corners.get(i), arcLength[i]);
        }
        return map;
    }


}
