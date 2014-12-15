package cornerfinders.parallel;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.*;
import cornerfinders.impl.combination.SBFSCombinationSegmenter;
import cornerfinders.impl.combination.objectivefuncs.MSEObjectiveFunction;
import cornerfinders.parallel.callable.CornerFinderCallable;
import utils.ITask;
import utils.TaskRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

public class ParallelMergedCornerFinder extends AbstractCornerFinder {
    private ShortStrawCornerFinder shortStrawCornerFinder;
    private SezginCornerFinder sezginCornerFinder;
    private KimCornerFinder kimCornerFinder;
    private AngleCornerFinder angleCornerFinder;
    private TaskRunner<ArrayList<Integer>> taskRunner;

    public ParallelMergedCornerFinder() {
        shortStrawCornerFinder = new ShortStrawCornerFinder();
        sezginCornerFinder = new SezginCornerFinder();
        kimCornerFinder = new KimCornerFinder();
        angleCornerFinder = new AngleCornerFinder();
        // has to be injected
        taskRunner = new TaskRunner<ArrayList<Integer>>(4);
    }

    @Override
    public ArrayList<Integer> findCorners(TStroke stroke) {
        List<ITask<ArrayList<Integer>>> cornerFinderCallables = Lists.newArrayList();
        CornerFinderCallable shortStrawCallable = new CornerFinderCallable(stroke.clone(), shortStrawCornerFinder);
        CornerFinderCallable sezginCallable = new CornerFinderCallable(stroke.clone(), sezginCornerFinder);
        CornerFinderCallable kimCallable = new CornerFinderCallable(stroke.clone(), kimCornerFinder);
        CornerFinderCallable angleCornerCallable = new CornerFinderCallable(stroke.clone(), angleCornerFinder);
        cornerFinderCallables.add(shortStrawCallable);
        cornerFinderCallables.add(sezginCallable);
        cornerFinderCallables.add(kimCallable);
        cornerFinderCallables.add(angleCornerCallable);
        try {
            return mergeCornerFinder(taskRunner.invokeAll(cornerFinderCallables), stroke);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("merging failed");
        }
        return Lists.newArrayList();
    }

    public ArrayList<Integer> mergeCornerFinder(List<Future<ArrayList<Integer>>> futures, final TStroke stroke) {
        final Set<Integer> cornerIndicesList = Sets.newHashSet(); // it has all the corners
        final SBFSCombinationSegmenter segmenter = new SBFSCombinationSegmenter();
        final MSEObjectiveFunction objectiveFunction = new MSEObjectiveFunction();
        try {
            for (Future<ArrayList<Integer>> future : futures) {
                cornerIndicesList.addAll(future.get());
            }
        } catch (Exception e) {
        }
        return (ArrayList) segmenter.sbfs(Lists.newArrayList(cornerIndicesList), stroke, objectiveFunction);
        //return Lists.newArrayList(cornerIndicesList);

    }

}
