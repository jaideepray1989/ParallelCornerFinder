package cornerfinders.impl.rankfragmenter.features.point;

import cornerfinders.core.shapes.TPoint;
import cornerfinders.impl.rankfragmenter.RFNode;
import cornerfinders.impl.rankfragmenter.rfutils.RFCost;
import cornerfinders.impl.rankfragmenter.rfutils.RFUtils;
import sun.awt.RequestFocusController;

/**
 * Created by jaideepray on 12/14/14.
 */
public class MSEFeature {

    public static double getMSEFeature(RFNode node) {

        RFNode prevNode = node.previous;
        RFNode next = node.next;

        double mse = 0.0;
        if (prevNode != null && next != null) {
            TPoint prevPoint = prevNode.corner;
            TPoint nextPoint = next.corner;
            TPoint point = node.corner;
            return RFCost.cost(prevPoint, nextPoint, point);
        }
        return mse;
    }


}
