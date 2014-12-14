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
    private Set<Integer> combinedCorners = Sets.newHashSet();


    public SerialMergedCornerFinder() {
        shortStrawCornerFinder = new ShortStrawCornerFinder();
        sezginCornerFinder = new SezginCornerFinder();
        kimCornerFinder = new KimCornerFinder();
        angleCornerFinder = new AngleCornerFinder();
    }

    @Override
    public ArrayList<Integer> findCorners(TStroke stroke) {
        ArrayList<Integer> allCorners = Lists.newArrayList();
        ArrayList<Integer> c1 = shortStrawCornerFinder.findCorners(stroke);
        combinedCorners.addAll(c1);
        ArrayList<Integer> c2 = sezginCornerFinder.findCorners(stroke);
        combinedCorners.addAll(c2);
        ArrayList<Integer> c3 = kimCornerFinder.findCorners(stroke);
        combinedCorners.addAll(c3);
        ArrayList<Integer> c4 = angleCornerFinder.findCorners(stroke);
        combinedCorners.addAll(c4);
        allCorners.addAll(combinedCorners);
        ArrayList<Integer> combine = combine(allCorners, stroke);
        return combine;
    }

    public ArrayList<Integer> combine(ArrayList<Integer> allCorners, TStroke s) {
        SBFSCombinationSegmenter segmenter = new SBFSCombinationSegmenter();
        MSEObjectiveFunction objectiveFunction = new MSEObjectiveFunction();
        return (ArrayList) segmenter.sbfs(Lists.newArrayList(allCorners), s, objectiveFunction);
    }


}
