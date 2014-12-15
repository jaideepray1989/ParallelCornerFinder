import com.google.common.collect.Lists;
import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.*;
import cornerfinders.impl.AngleCornerFinder;
import cornerfinders.impl.KimCornerFinder;
import cornerfinders.impl.SezginCornerFinder;
import cornerfinders.impl.ShortStrawCornerFinder;
import cornerfinders.impl.rankfragmenter.RFCornerFinder;
import cornerfinders.parallel.ParallelMergedCornerFinder;
import cornerfinders.parallel.SerialMergedCornerFinder;
import cornerfinders.parallel.ShapeProcessor;
import utils.validator.SketchDataValidator;

import java.util.*;

import cornerfinders.impl.combination.*;

/**
 * Created by jaideepray on 12/12/14.
 */
public class CheckAccuracyCF {

    public static List<TPoint> fetchCornerPoints(TStroke s, ArrayList<Integer> indices) {
        List<TPoint> corners = Lists.newLinkedList();
        if (indices == null || indices.isEmpty())
            return corners;
        for (Integer index : indices) {
            if (0 <= index && index < s.getSize()) {
                corners.add(s.getPoint(index));
            }
        }
        return corners;
    }

    public static RFCornerFinder trainRFClassifier(AbstractCornerFinder cornerFinder) {

        Map<String, List<TStroke>> strokeMap = DBUtils.fetchShapes(40);
        List<TStroke> trainingSet = Lists.newArrayList();
        for (Map.Entry<String, List<TStroke>> entry : strokeMap.entrySet()) {
            trainingSet.addAll(entry.getValue());
        }
        return new RFCornerFinder(30, trainingSet, cornerFinder);
    }

    public static void checkAccuracy() {
        ShortStrawCornerFinder strawCornerFinder = new ShortStrawCornerFinder();
        SezginCornerFinder sezginCornerFinder = new SezginCornerFinder();
        KimCornerFinder kimCornerFinder = new KimCornerFinder();
        AngleCornerFinder angleCornerFinder = new AngleCornerFinder();
        // RFCornerFinder rfCornerFinder = trainRFClassifier(strawCornerFinder);

        ParallelMergedCornerFinder parallelMergedCornerFinder = new ParallelMergedCornerFinder();
        SerialMergedCornerFinder serialMergedCornerFinder = new SerialMergedCornerFinder();
        ShapeProcessor shapeProcessor = new ShapeProcessor();
        Map<String, List<TStroke>> strokeMap = DBUtils.fetchShapes(5);
        long serialTime = 0;
        long parallelTime = 0;
        long parallelShapeTime = 0;
        int numPoints = 0;
        List<TStroke> shape = Lists.newArrayList();
        for (List<TStroke> sList : strokeMap.values()) {
            if (sList.size() < 5) continue;
            shape.clear();
            for (TStroke s : sList) {
                numPoints += s.getPoints().size();
                if (!SketchDataValidator.isValidStroke(s)) continue;
                shape.add(s);
                long ts1 = System.nanoTime();
                ArrayList<Integer> serialFinalIndices = serialMergedCornerFinder.findCorners(s);
                long ts2 = System.nanoTime();
                serialTime += (ts2 - ts1);
                long tp1 = System.nanoTime();
                ArrayList<Integer> parallelFinalIndices = parallelMergedCornerFinder.findCorners(s);
                long tp2 = System.nanoTime();
                parallelTime += (tp2 - tp1);
                System.out.println("stroke size :: " + s.getPoints().size());
                System.out.println("serial time :: " + (ts2 - ts1));
                System.out.println("parallel time :: " + (tp2 - tp1));
            }

            /*running entire shape in parallel*/
            long tsp1 = System.nanoTime();
            shapeProcessor.processStrokesInParallel(parallelMergedCornerFinder, shape);
            long tsp2 = System.nanoTime();
            parallelShapeTime += (tsp2 - tsp1);
            System.out.println("shape in parallel time :: " + (tsp2 - tsp1));

            System.out.println();
            System.out.println("------------------------------------");
        }

        System.out.println("------------------------------------");
        System.out.println("Average speed up :: " + (1.0 * serialTime) / parallelShapeTime);
    }

    public static void printCorners(List<TPoint> corners) {
        System.out.println("Number of corners :: " + corners.size());
        for (TPoint pt : corners) {
            pt.printPoint();
        }
    }

    public static void main(String[] args) {
        checkAccuracy();
    }
}
