package cornerfinders.parallel;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.*;
import cornerfinders.impl.combination.SBFSCombinationSegmenter;
import cornerfinders.impl.combination.objectivefuncs.MSEObjectiveFunction;
import cornerfinders.parallel.callable.CornerFinderCallable;
import utils.TaskRunner;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        taskRunner = new TaskRunner<ArrayList<Integer>>();
    }

    @Override
    public ArrayList<Integer> findCorners(TStroke stroke) {
        long timeStart = System.currentTimeMillis();
        List<ListenableFuture<ArrayList<Integer>>> futures = Lists.newArrayList();
        CornerFinderCallable shortStrawCallable = new CornerFinderCallable(stroke.getCloned(), shortStrawCornerFinder);
        CornerFinderCallable sezginCallable = new CornerFinderCallable(stroke.getCloned(), sezginCornerFinder);
        CornerFinderCallable kimCallable = new CornerFinderCallable(stroke.getCloned(), kimCornerFinder);
        CornerFinderCallable angleCornerCallable = new CornerFinderCallable(stroke.getCloned(), angleCornerFinder);
        ListenableFuture<ArrayList<Integer>> shortStrawFuture = taskRunner.runTask(shortStrawCallable);
        ListenableFuture<ArrayList<Integer>> sezginFuture = taskRunner.runTask(sezginCallable);
        ListenableFuture<ArrayList<Integer>> kimFuture = taskRunner.runTask(kimCallable);
        ListenableFuture<ArrayList<Integer>> angleFuture = taskRunner.runTask(angleCornerCallable);
        futures.add(shortStrawFuture);
        futures.add(sezginFuture);
        futures.add(kimFuture);
        futures.add(angleFuture);
        ArrayList<Integer> finalIndices = mergeCornerFinder(futures, stroke);
        long timeEnd = System.currentTimeMillis();
        System.out.println(timeEnd - timeStart);
        return finalIndices;
    }

    public ArrayList<Integer> mergeCornerFinder(List<ListenableFuture<ArrayList<Integer>>> futures, final TStroke stroke) {
        final Set<Integer> cornerIndicesList = Sets.newHashSet(); // it has all the corners
        final SBFSCombinationSegmenter segmenter = new SBFSCombinationSegmenter();
        final MSEObjectiveFunction objectiveFunction = new MSEObjectiveFunction();
        final ListenableFuture<List<ArrayList<Integer>>> resultsFuture
                = Futures.allAsList(futures);
        try {
            List<ArrayList<Integer>> lists = resultsFuture.get();
            for (ArrayList<Integer> ptList : lists) {
                cornerIndicesList.addAll(ptList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (ArrayList) segmenter.sbfs(Lists.newArrayList(cornerIndicesList), stroke, objectiveFunction);

    }

}
