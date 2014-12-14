package cornerfinders.impl.rankfragmenter.features.point;

import cornerfinders.core.shapes.TPoint;
import cornerfinders.impl.rankfragmenter.RFNode;
import cornerfinders.impl.rankfragmenter.rfutils.RFUtils;

import java.util.List;

/**
 * Created by jaideepray on 12/12/14.
 */
public class SpeedFeature {

    public static Integer strawWindow = 2;

    public static double getSpeed(RFNode node) {
        RFNode currMinusWindow = null;
        RFNode currPlusWindow = null;
        for (int i = 0; i < strawWindow; i++) {
            if (node.previous != null)
                currMinusWindow = node.previous;
        }
        for (int i = 0; i < strawWindow; i++) {
            if (node.next != null)
                currPlusWindow = node.next;
        }

        if (currMinusWindow != null && currPlusWindow != null) {
            return RFUtils.euclidean(currMinusWindow.corner, currPlusWindow.corner) / (currPlusWindow.corner.getTime() - currMinusWindow.corner.getTime());
        }
        return 0.0;
    }
}
