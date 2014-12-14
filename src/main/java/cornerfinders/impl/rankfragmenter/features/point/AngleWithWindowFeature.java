package cornerfinders.impl.rankfragmenter.features.point;

import cornerfinders.impl.rankfragmenter.RFNode;
import cornerfinders.impl.rankfragmenter.rfutils.RFUtils;

/**
 * Created by jaideepray on 12/14/14.
 */
public class AngleWithWindowFeature {
    public static Integer strawWindow = 3;

    public static double getAngle(RFNode node) {
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
            return AngleFeature.computeAngle(currMinusWindow.corner, node.corner, currPlusWindow.corner);
        }
        return 0.0;
    }
}
