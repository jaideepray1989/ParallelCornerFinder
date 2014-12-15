package cornerfinders.parallel;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.*;
import cornerfinders.impl.combination.SBFSCombinationSegmenter;
import cornerfinders.impl.combination.objectivefuncs.MSEObjectiveFunction;
import utils.TaskRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Anurag Garg on 12/14/2014.
 */

public class SerialMergedCornerFinder extends AbstractCornerFinder {
    private ShortStrawCornerFinder shortStrawCornerFinder;
    private SezginCornerFinder sezginCornerFinder;
    private KimCornerFinder kimCornerFinder;
    private AngleCornerFinder angleCornerFinder;

    public SerialMergedCornerFinder() {
        shortStrawCornerFinder = new ShortStrawCornerFinder();
        sezginCornerFinder = new SezginCornerFinder();
        kimCornerFinder = new KimCornerFinder();
        angleCornerFinder = new AngleCornerFinder();
    }

    @Override
    public ArrayList<Integer> findCorners(TStroke stroke) {
        Set<Integer> combinedCorners = Sets.newHashSet();
        ArrayList<Integer> c1 = shortStrawCornerFinder.findCorners(stroke.clone());
        combinedCorners.addAll(c1);
        ArrayList<Integer> c2 = sezginCornerFinder.findCorners(stroke.clone());
        combinedCorners.addAll(c2);
        ArrayList<Integer> c3 = kimCornerFinder.findCorners(stroke.clone());
        combinedCorners.addAll(c3);
        ArrayList<Integer> c4 = angleCornerFinder.findCorners(stroke.clone());
        combinedCorners.addAll(c4);
        return combine(Lists.newArrayList(combinedCorners), stroke);
    }

    public ArrayList<Integer> combine(ArrayList<Integer> allCorners, TStroke s) {
        return getCornersAfterSBFS(allCorners,s);
    }

    public ArrayList<Integer> getCornersAfterSBFS(ArrayList<Integer> cornerList, TStroke stroke) {
        final SBFSCombinationSegmenter segmenter = new SBFSCombinationSegmenter();
        final MSEObjectiveFunction objectiveFunction = new MSEObjectiveFunction();
        return (ArrayList) segmenter.sbfs(cornerList, stroke, objectiveFunction);
    }

}
