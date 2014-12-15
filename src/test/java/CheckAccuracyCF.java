import com.google.common.collect.Lists;
import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.*;
import cornerfinders.impl.AngleCornerFinder;
import cornerfinders.impl.KimCornerFinder;
import cornerfinders.impl.SezginCornerFinder;
import cornerfinders.impl.ShortStrawCornerFinder;
import cornerfinders.impl.rankfragmenter.RFCornerFinder;
import cornerfinders.impl.rankfragmenter.rfutils.RFUtils;
import cornerfinders.parallel.ParallelMergedCornerFinder;
import cornerfinders.parallel.SerialMergedCornerFinder;
import cornerfinders.parallel.ShapeProcessor;
import utils.validator.CornerValidator;
import utils.validator.SketchDataValidator;

import java.util.*;

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

    public static RFCornerFinder trainRFClassifier(List<AbstractCornerFinder> cornerFinders) {

        Map<String, List<TStroke>> strokeMap = DBUtils.fetchShapes(40);
        List<TStroke> trainingSet = Lists.newArrayList();
        for (Map.Entry<String, List<TStroke>> entry : strokeMap.entrySet()) {
            trainingSet.addAll(entry.getValue());
        }
        return new RFCornerFinder(30, trainingSet, cornerFinders);
    }

    public static void checkAccuracy() {
        ShortStrawCornerFinder strawCornerFinder = new ShortStrawCornerFinder();
        SezginCornerFinder sezginCornerFinder = new SezginCornerFinder();
        KimCornerFinder kimCornerFinder = new KimCornerFinder();
        AngleCornerFinder angleCornerFinder = new AngleCornerFinder();
        List<AbstractCornerFinder> cornerFinders = Lists.newArrayList();
        cornerFinders.add(sezginCornerFinder);
        cornerFinders.add(kimCornerFinder);
        cornerFinders.add(angleCornerFinder);
        cornerFinders.add(strawCornerFinder);
        RFCornerFinder rfCornerFinder = trainRFClassifier(cornerFinders);
        ParallelMergedCornerFinder parallelMergedCornerFinder = new ParallelMergedCornerFinder();
        SerialMergedCornerFinder serialMergedCornerFinder = new SerialMergedCornerFinder();
        ShapeProcessor shapeProcessor = new ShapeProcessor();
        Map<String, List<TStroke>> strokeMap = DBUtils.fetchShapes(80);
        long serialTime = 0;
        long parallelTime = 0;
        long parallelShapeTime = 0;
        int numPoints = 0;
        List<TStroke> shape = Lists.newArrayList();
        int numRFCorners = 0;
        int numCorners = 0;
        double error = 0.0;
        long initialTss = serialTime;
        int shapeSize = 0;
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
                ArrayList<Integer> rfCorners = rfCornerFinder.findCorners(s);
                numCorners += serialFinalIndices.size();
                numRFCorners += rfCorners.size();
                parallelTime += (tp2 - tp1);
                shapeSize += s.getPoints().size();
                computeError(fetchCornerPoints(s, serialFinalIndices), fetchCornerPoints(s, rfCorners));
                //System.out.println("stroke size :: " + s.getPoints().size());
                //System.out.println("corners detected ::" + serialFinalIndices.size());
                //System.out.println("rfCorners detected ::" + rfCorners.size());
                //System.out.println("serial time :: " + (ts2 - ts1));
                //System.out.println("parallel time :: " + (tp2 - tp1));
            }

            
            /*running entire shape in parallel*/
            long tsp1 = System.nanoTime();
            shapeProcessor.processStrokesInParallel(parallelMergedCornerFinder, shape);
            long tsp2 = System.nanoTime();
            parallelShapeTime += (tsp2 - tsp1);
            System.out.println(shapeSize + "," + (serialTime - initialTss) + "," + (tsp2 - tsp1));
            //System.out.println("Shape size :: " + shapeSize);
            //System.out.println("Shape in Serial time :: " + (serialTime - initialTss));
            //System.out.println("Shape in parallel time :: " + (tsp2 - tsp1));
            //System.out.println("Corners :: " + numCorners + "rf Corners :: " + numRFCorners);
            //System.out.println("------------------------------------");
        }

        System.out.println("Mean squared distance error :: " + error);
        System.out.println("------------------------------------");
        System.out.println(" Average stroke speed up ::" + (1.0 * serialTime) / parallelTime);
        System.out.println("Average speed up :: " + (1.0 * serialTime) / parallelShapeTime);
    }

    public static void printCorners(List<TPoint> corners) {
        //System.out.println("Number of corners :: " + corners.size());
        for (TPoint pt : corners) {
            //pt.printPoint();
        }
    }

    public static void computeError(List<TPoint> actualCorners, List<TPoint> rfCorners) {
        printCorners(actualCorners);
        printCorners(rfCorners);
    }

    public static void main(String[] args) {
        checkAccuracy();
    }
}
