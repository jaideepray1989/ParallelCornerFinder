package cornerfinders.parallel;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.parallel.callable.CornerFinderCallable;
import utils.ITask;
import utils.TaskRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created by jaideepray on 12/15/14.
 */
public class ShapeProcessor {

    private TaskRunner<ArrayList<Integer>> taskRunner;

    public ShapeProcessor() {
        taskRunner = new TaskRunner<ArrayList<Integer>>(10);
    }

    public Map<TStroke, ArrayList<Integer>> processStrokesInParallel(ParallelMergedCornerFinder cornerFinder, List<TStroke> strokes) {
        List<ITask<ArrayList<Integer>>> strokeCallables = Lists.newArrayList();
        for (TStroke stroke : strokes) {
            CornerFinderCallable strokeCallable = new CornerFinderCallable(stroke, cornerFinder);
            strokeCallables.add(strokeCallable);
        }
        try {
            return getCornerIndices(taskRunner.invokeAll(strokeCallables), strokes);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("merging failed");
        }
        return Maps.newHashMap();
    }

    private Map<TStroke, ArrayList<Integer>> getCornerIndices(List<Future<ArrayList<Integer>>> futures, List<TStroke> strokes) throws Exception {
        Map<TStroke, ArrayList<Integer>> strokeMap = Maps.newHashMap();
        int c = 0;
        for (TStroke stroke : strokes) {
            strokeMap.put(stroke, futures.get(c).get());
        }
        return strokeMap;
    }
}
