package cornerfinders.impl.rankfragmenter.features.point;

import cornerfinders.impl.rankfragmenter.RFNode;

/**
 * Created by jaideepray on 12/14/14.
 */
public class SharpnessFeature {

    public static double Sharpness(RFNode node) {
        double x = AngleFeature.getAngleFeature(node);
        return x * x;
    }
}
