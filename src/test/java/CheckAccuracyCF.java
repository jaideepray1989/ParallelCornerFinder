import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.*;
import cornerfinders.impl.AngleCornerFinder;
import cornerfinders.impl.KimCornerFinder;
import cornerfinders.impl.SezginCornerFinder;
import cornerfinders.impl.ShortStrawCornerFinder;
import cornerfinders.impl.combination.SBFSCombinationSegmenter;
import cornerfinders.impl.combination.objectivefuncs.MSEObjectiveFunction;
import cornerfinders.impl.rankfragmenter.RFCornerFinder;
import cornerfinders.impl.combination.objectivefuncs.PolylineMSEObjectiveFunction;
import cornerfinders.parallel.ParallelMergedCornerFinder;
import cornerfinders.parallel.SerialMergedCornerFinder;
import cornerfinders.render.Figure;
import utils.validator.CornerValidator;
import utils.validator.SketchDataValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        Map<String, List<TStroke>> strokeMap = DBUtils.fetchStrokes(10);
        List<TStroke> trainingSet = Lists.newArrayList();
        for (Map.Entry<String, List<TStroke>> entry : strokeMap.entrySet()) {
            trainingSet.addAll(entry.getValue());
        }
        return new RFCornerFinder(10, trainingSet, cornerFinder);
    }

    public static void checkAccuracy() {
        ShortStrawCornerFinder strawCornerFinder = new ShortStrawCornerFinder();
        SezginCornerFinder sezginCornerFinder = new SezginCornerFinder();
        KimCornerFinder kimCornerFinder = new KimCornerFinder();
        AngleCornerFinder angleCornerFinder = new AngleCornerFinder();
        // RFCornerFinder rfCornerFinder = trainRFClassifier(strawCornerFinder);
        ParallelMergedCornerFinder mergedCornerFinder = new ParallelMergedCornerFinder();
        SerialMergedCornerFinder serialMergedCornerFinder = new SerialMergedCornerFinder();
        Map<String, List<TStroke>> strokeMap = DBUtils.fetchStrokes(100);
        List<TPoint> strawCorners = Lists.newArrayList();
        List<TPoint> sezginCorners = Lists.newArrayList();
        List<TPoint> kimCorners = Lists.newArrayList();
        List<TPoint> angleCorners = Lists.newArrayList();
        List<TPoint> rfCorners = Lists.newArrayList();
        List<TPoint> shapePoints = Lists.newArrayList();
        List<TPoint> allCorners = Lists.newArrayList();
        List<TPoint> finalCorners = Lists.newArrayList();
        Set<Integer> cornerIndicesSet = Sets.newHashSet();
        long serialTime = 0;
        long parallelTime = 0;
        SBFSCombinationSegmenter segmenter = new SBFSCombinationSegmenter();
        MSEObjectiveFunction objectiveFunction = new MSEObjectiveFunction();
        int numPoints = 0;
        for (List<TStroke> sList : strokeMap.values()) {
            List<TPoint> szC = Lists.newArrayList();
            szC = null;
            for (TStroke s : sList) {

                numPoints += s.getPoints().size();
                if (!SketchDataValidator.isValidStroke(s)) continue;
                shapePoints.addAll(s.getPoints());
//                ArrayList<Integer> c1 = strawCornerFinder.findCorners(s);
//                cornerIndicesSet.addAll(c1);
//                List<TPoint> stC = fetchCornerPoints(s, c1);
//                if (!stC.isEmpty())
//                    strawCorners.addAll(stC);
//
//                ArrayList<Integer> c2 = sezginCornerFinder.findCorners(s);
//                cornerIndicesSet.addAll(c2);
//                szC = fetchCornerPoints(s, c2);
//                if (!szC.isEmpty())
//                    sezginCorners.addAll(szC);
//
//                ArrayList<Integer> c3 = kimCornerFinder.findCorners(s);
//                cornerIndicesSet.addAll(c3);
//                List<TPoint> kimC = fetchCornerPoints(s, c3);
//                if (!kimC.isEmpty())
//                    kimCorners.addAll(kimC);
//
//                ArrayList<Integer> c4 = angleCornerFinder.findCorners(s);
//                List<TPoint> cornerAngles = fetchCornerPoints(s, c4);
//                cornerIndicesSet.addAll(c4);
//                if (!cornerAngles.isEmpty())
//                    angleCorners.addAll(cornerAngles);

//                ArrayList<Integer> c5 = rfCornerFinder.findCorners(s);
//                List<TPoint> rfC = fetchCornerPoints(s, c5);
//                cornerIndicesSet.addAll(c5);
//                if (!rfC.isEmpty())
//                    rfCorners.addAll(rfC);
//                allcorners.addAll(rfC);
                long ts1 = System.currentTimeMillis();
                ArrayList<Integer> serialFinalIndices = serialMergedCornerFinder.findCorners(s);
                long ts2 = System.currentTimeMillis();
                ArrayList<Integer> finalIndices = mergedCornerFinder.findCorners(s);
                long ts3 = System.currentTimeMillis();
                finalCorners.addAll(CornerValidator.validateCornersWithThreshold(fetchCornerPoints(s, finalIndices), 25));
                serialTime += (ts2 - ts1);
                parallelTime += (ts3 - ts2);
            }
            System.out.println("serial time :: " + serialTime);
            System.out.println("parallel time :: " + parallelTime);

            System.out.println("------------------------------------");
//            System.out.println(numPoints);
//            System.out.println("SHORT STRAW");
//            printCorners(CornerValidator.validateCorners(strawCorners));
//            System.out.println("ANGLE STRAW");
//            printCorners(CornerValidator.validateCorners(angleCorners));
//            System.out.println("KIM");
//            printCorners(CornerValidator.validateCorners(kimCorners));
//            System.out.println("SEZGIN");
//            printCorners(CornerValidator.validateCorners(sezginCorners));
            // System.out.println("PARALLEL");
            printCorners(finalCorners);
//            System.out.println("RFC");
//            printCorners(CornerValidator.validateCorners(rfCorners));
            System.out.println();
            System.out.println("------------------------------------");
        }
    }

    public static void printCorners(List<TPoint> corners) {
        System.out.println("Number of corners :: " + corners.size());
//        for (TPoint pt : corners) {
//            pt.printPoint();
//        }
    }

    public static void main(String[] args) {
        checkAccuracy();
    }
}
