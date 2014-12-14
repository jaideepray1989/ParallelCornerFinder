package cornerfinders.impl.rankfragmenter.features.rank;

import cornerfinders.impl.rankfragmenter.RFNode;

/**
 * Created by jaideepray on 12/12/14.
 */
public class RankCostFeature {

    public static double getRankCostFeature(RFNode node) {
        return node.cost;
    }
}
