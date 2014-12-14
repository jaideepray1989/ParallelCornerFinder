package cornerfinders.impl.rankfragmenter.features.point;

import cornerfinders.core.shapes.TPoint;
import cornerfinders.impl.rankfragmenter.RFNode;
import cornerfinders.impl.rankfragmenter.rfutils.RFUtils;

import java.util.List;

/**
 * Created by jaideepray on 12/12/14.
 */
public class AngleFeature {

    public static double getAngleFeature(RFNode node) {

        RFNode prevNode = node.previous;
        RFNode next = node.next;

        double angle = 0.0;
        if (prevNode != null && next != null) {
            TPoint prevPoint = prevNode.corner;
            TPoint nextPoint = next.corner;
            TPoint point = node.corner;
            return computeAngle(prevPoint, point, nextPoint);
        }
        return angle;
    }

    public static double computeAngle(TPoint prev, TPoint point, TPoint next) {
        double p12 = RFUtils.euclidean(point, prev);
        double p23 = RFUtils.euclidean(point, next);
        double p13 = RFUtils.euclidean(prev, next);
        return Math.acos((p12 * p12 + p13 * p13 - p23 * p23) / (2 * p12 * p13));
    }

}
