package cornerfinders.impl.rankfragmenter.features.point;

import com.google.common.collect.Maps;
import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.SezginCornerFinder;
import cornerfinders.impl.rankfragmenter.RFNode;
import cornerfinders.impl.rankfragmenter.rfutils.RFCost;
import cornerfinders.impl.rankfragmenter.rfutils.RFUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by jaideepray on 12/12/14.
 */
public class CurvatureFeature {


    public static double getCurvature(RFNode node) {

        RFNode prevNode = node.previous;
        RFNode next = node.next;

        double curvature = 0.0;
        if (prevNode != null && next != null) {
            TPoint prevPoint = prevNode.corner;
            TPoint nextPoint = next.corner;
            TPoint point = node.corner;
            double d1 = RFUtils.euclidean(point, prevPoint);
            double d2 = RFUtils.euclidean(point, nextPoint);
            double d3 = RFUtils.euclidean(prevPoint, nextPoint);
            return (d1 + d2) / d3;
        }
        return curvature;
    }
}
