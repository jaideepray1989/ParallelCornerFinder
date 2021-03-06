package cornerfinders.impl.rankfragmenter.features.point;

import cornerfinders.impl.rankfragmenter.RFNode;
import cornerfinders.impl.rankfragmenter.rfutils.RFUtils;

/**
 * Created by jaideepray on 12/12/14.
 */
public class StrawFeature {

    public static Integer strawWindow = 3;

    public static double calculateStraw(RFNode node) {
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
            return RFUtils.euclidean(currMinusWindow.corner, currPlusWindow.corner);
        }
        return 0.0;
    }
}
