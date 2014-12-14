package cornerfinders.parallel;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.*;
import cornerfinders.impl.combination.SBFSCombinationSegmenter;
import cornerfinders.impl.combination.objectivefuncs.MSEObjectiveFunction;
import cornerfinders.parallel.callable.CornerFinderCallable;
import utils.TaskRunner;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        taskRunner = new TaskRunner<ArrayList<Integer>>(10);
    }


    @Override
    public ArrayList<Integer> findCorners(TStroke stroke) {
        Map<CornerFinderName, ListenableFuture<ArrayList<Integer>>> futuresMap = Maps.newHashMap();
        CornerFinderCallable shortStrawCallable = new CornerFinderCallable(stroke, shortStrawCornerFinder);
        CornerFinderCallable sezginCallable = new CornerFinderCallable(stroke, sezginCornerFinder);
        CornerFinderCallable kimCallable = new CornerFinderCallable(stroke, kimCornerFinder);
        CornerFinderCallable angleCornerCallable = new CornerFinderCallable(stroke, angleCornerFinder);
        ListenableFuture<ArrayList<Integer>> shortStrawFuture = taskRunner.runTask(shortStrawCallable);
        ListenableFuture<ArrayList<Integer>> sezginFuture = taskRunner.runTask(sezginCallable);
        ListenableFuture<ArrayList<Integer>> kimFuture = taskRunner.runTask(kimCallable);
        ListenableFuture<ArrayList<Integer>> angleFuture = taskRunner.runTask(angleCornerCallable);
        futuresMap.put(CornerFinderName.SEZGIN, sezginFuture);
        futuresMap.put(CornerFinderName.STRAW, shortStrawFuture);
        futuresMap.put(CornerFinderName.KIM, kimFuture);
        futuresMap.put(CornerFinderName.ANGLE, angleFuture);
        return combine(futuresMap,stroke);
    }

    public ArrayList<Integer> combine(Map<CornerFinderName, ListenableFuture<ArrayList<Integer>>> map, TStroke stroke) {
        final Set<Integer> cornerIndicesList = Sets.newHashSet(); // it has all the corners
        SBFSCombinationSegmenter segmenter = new SBFSCombinationSegmenter();
        MSEObjectiveFunction objectiveFunction = new MSEObjectiveFunction();
        for (Map.Entry<CornerFinderName, ListenableFuture<ArrayList<Integer>>> entry : map.entrySet()) {
            Futures.addCallback(entry.getValue(), new FutureCallback<ArrayList<Integer>>() {
                @Override
                public void onSuccess(@Nullable ArrayList<Integer> integers) {
                    cornerIndicesList.addAll(integers);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    System.out.println(throwable.getCause().getMessage());
                }
            });
        }
        return (ArrayList) segmenter.sbfs(Lists.newArrayList(cornerIndicesList), stroke, objectiveFunction);
    }

}
