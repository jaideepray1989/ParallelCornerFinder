package cornerfinders.impl.rankfragmenter.features.rank;

import cornerfinders.core.shapes.TPoint;
import cornerfinders.impl.rankfragmenter.RFNode;
import cornerfinders.impl.rankfragmenter.rfutils.RFCost;
import cornerfinders.impl.rankfragmenter.rfutils.RFUtils;

/**
 * Created by jaideepray on 12/12/14.
 */
public class RankFeature {

    public static double getRank(RFNode node) {
        RFNode prevNode = node.previous;
        RFNode next = node.next;
        if (prevNode != null && next != null) {
            TPoint prevPoint = prevNode.corner;
            TPoint nextPoint = next.corner;
            TPoint point = node.corner;
            return Math.min(RFUtils.euclidean(point,prevPoint),RFUtils.euclidean(point,nextPoint));
        }
        return 0.0;
    }
}
