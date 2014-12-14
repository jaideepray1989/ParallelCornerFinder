package cornerfinders.impl.rankfragmenter;

import com.google.common.collect.Lists;
import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.AbstractCornerFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jaideepray on 12/12/14.
 */
public class RFCornerFinder extends AbstractCornerFinder {

    private RFInitializer rfInitializer;
    private CornerPruner cornerPruner;
    private CornerClassifier classifier;
    private Integer notToBePruned;


    @Override
    public ArrayList<Integer> findCorners(TStroke stroke) {
        List<TPoint> strokePoints = stroke.getPoints();
        Map<Integer, RFNode> initialList = rfInitializer.getInitialList(strokePoints);
        Map<Integer, RFNode> prunedList = cornerPruner.pruneList(initialList,notToBePruned);
        return Lists.newArrayList(prunedList.keySet());
    }

    public RFCornerFinder(Integer np) {
        classifier = new CornerClassifier();
        rfInitializer = new RFInitializer();
        cornerPruner = new CornerPruner();
        notToBePruned = np;
    }


}
