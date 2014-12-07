package cornerfinders.parallel;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.SezginCornerFinder;
import cornerfinders.impl.ShortStrawCornerFinder;
import cornerfinders.parallel.callable.CornerFinderCallable;
import utils.TaskRunner;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Created by jaideepray on 12/6/14.
 */

public class ParallelMergedCornerFinder {
    private TStroke stroke;
    private ShortStrawCornerFinder shortStrawCornerFinder;
    private SezginCornerFinder sezginCornerFinder;
    private TaskRunner<ArrayList<Integer>> taskRunner;

    public ParallelMergedCornerFinder(TStroke s) {
        stroke = s;
        shortStrawCornerFinder = new ShortStrawCornerFinder();
        sezginCornerFinder = new SezginCornerFinder();
        // has to be injected
        taskRunner = new TaskRunner<ArrayList<Integer>>(10);
    }

    public ArrayList<Integer> findCorners() {
        Map<CornerFinderName, ListenableFuture<ArrayList<Integer>>> futuresMap = Maps.newHashMap();
        CornerFinderCallable shortStrawCallable = new CornerFinderCallable(this.stroke, shortStrawCornerFinder);
        CornerFinderCallable sezginCallable = new CornerFinderCallable(stroke, sezginCornerFinder);
        ListenableFuture<ArrayList<Integer>> shortStrawFuture = taskRunner.runTask(shortStrawCallable);
        ListenableFuture<ArrayList<Integer>> sezginFuture = taskRunner.runTask(sezginCallable);
        futuresMap.put(CornerFinderName.SEZGIN, sezginFuture);
        futuresMap.put(CornerFinderName.STRAW, shortStrawFuture);
        Set<Integer> corners = combine(futuresMap);
        return processCorners(corners);
    }

    public Set<Integer> combine(Map<CornerFinderName, ListenableFuture<ArrayList<Integer>>> map) {
        final Set<Integer> cornerList = Sets.newHashSet();
        for (Map.Entry<CornerFinderName, ListenableFuture<ArrayList<Integer>>> entry : map.entrySet()) {
            Futures.addCallback(entry.getValue(), new FutureCallback<ArrayList<Integer>>() {
                @Override
                public void onSuccess(@Nullable ArrayList<Integer> integers) {
                    for (Integer p : integers)
                        cornerList.add(p);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    System.out.println(throwable.getCause().getMessage());
                }
            });
        }
        return cornerList;
    }

    public ArrayList<Integer> processCorners(Set<Integer> set) {
        return Lists.newArrayList(set);
    }


}
