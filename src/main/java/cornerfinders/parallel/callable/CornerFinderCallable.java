package cornerfinders.parallel.callable;

import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.CornerFinder;
import utils.ITask;

import java.util.ArrayList;

public class CornerFinderCallable implements ITask<ArrayList<Integer>> {

    private TStroke stroke;
    private CornerFinder cornerFinder;

    public CornerFinderCallable(TStroke stroke, CornerFinder finder) {
        this.stroke = stroke;
        this.cornerFinder = finder;
    }

    @Override
    public ArrayList<Integer> call() throws Exception {
        return runTask();
    }

    @Override
    public ArrayList<Integer> runTask() {
        return cornerFinder.findCorners(stroke);
    }
}
