package cornerfinders.impl.rankfragmenter;

import com.google.common.collect.Lists;
import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.CornerFinder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jaideepray on 12/12/14.
 */
public class RFCornerFinder extends CornerFinder {

    private List<TPoint> pointList;
    private RFInitializer rfInitializer;
    private CornerPruner cornerPruner;
    private CornerClassifier classifier;
    private Integer notToBePruned;


    @Override
    public ArrayList<Integer> findCorners(TStroke stroke) {
        List<TPoint> strokePoints = stroke.getPoints();
        List<RFNode> rfNodes = rfInitializer.getInitialList(strokePoints);
        List<RFNode> prunedList = cornerPruner.pruneList(rfNodes);
        List<TPoint> corners = classifier.getCornerList(prunedList);
        return Lists.newArrayList();
    }

    RFCornerFinder(List<TPoint> ptList, Integer np) {
        classifier = new CornerClassifier();
        rfInitializer = new RFInitializer();
        cornerPruner = new CornerPruner();
        pointList = ptList;
        notToBePruned = np;
    }


}
