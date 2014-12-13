package utils.validator;

import cornerfinders.core.shapes.TPoint;
import cornerfinders.impl.rankfragmenter.rfutils.RFUtils;

import java.util.Comparator;
import java.util.List;

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
        return corners;
    }


}
