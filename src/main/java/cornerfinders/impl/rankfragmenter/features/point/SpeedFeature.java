package cornerfinders.impl.rankfragmenter.features.point;

import cornerfinders.core.shapes.TPoint;
import cornerfinders.impl.rankfragmenter.rfutils.RFUtils;

import java.util.List;

/**
 * Created by jaideepray on 12/12/14.
 */
public class SpeedFeature {
    public static double getSpeed(int idx, List<TPoint> pointList) {
        TPoint pt1 = pointList.get(idx);
        TPoint pt2 = pointList.get(idx - 1);
        return 1.0 * RFUtils.euclidean(pt1, pt2) / (pt1.getTime() - pt2.getTime());
    }
}
