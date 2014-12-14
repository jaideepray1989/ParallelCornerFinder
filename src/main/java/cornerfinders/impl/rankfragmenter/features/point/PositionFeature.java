package cornerfinders.impl.rankfragmenter.features.point;


import cornerfinders.impl.rankfragmenter.RFNode;

public class PositionFeature {

    public static Integer getPosition(RFNode node) {
        return node.id;
    }
}
