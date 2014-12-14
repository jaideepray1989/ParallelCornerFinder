import com.google.common.collect.Lists;
import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.AngleCornerFinder;
import cornerfinders.impl.KimCornerFinder;
import cornerfinders.impl.SezginCornerFinder;
import cornerfinders.impl.ShortStrawCornerFinder;
import cornerfinders.impl.rankfragmenter.RFCornerFinder;
import cornerfinders.impl.combination.objectivefuncs.PolylineMSEObjectiveFunction;
import cornerfinders.render.Figure;
import utils.validator.CornerValidator;
import utils.validator.SketchDataValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public static void checkAccuracy() {
        ShortStrawCornerFinder strawCornerFinder = new ShortStrawCornerFinder();
        SezginCornerFinder sezginCornerFinder = new SezginCornerFinder();
        KimCornerFinder kimCornerFinder = new KimCornerFinder();
        AngleCornerFinder angleCornerFinder = new AngleCornerFinder();
        RFCornerFinder rfCornerFinder = new RFCornerFinder(10);
        Map<String, List<TStroke>> strokeMap = DBUtils.fetchStrokes(1);
        Figure render = new Figure();
        List<TPoint> strawCorners = Lists.newArrayList();
        List<TPoint> sezginCorners = Lists.newArrayList();
        List<TPoint> kimCorners = Lists.newArrayList();
        List<TPoint> angleCorners = Lists.newArrayList();
        List<TPoint> rfCorners = Lists.newArrayList();
        List<TPoint> shapePoints = Lists.newArrayList();
        int numPoints = 0;
        for (List<TStroke> sList : strokeMap.values()) {
            List<TPoint> szC = Lists.newArrayList();
            szC=null;
            render.renderFigure(sList,szC);
            for (TStroke s : sList) {

                numPoints += s.getPoints().size();
                if (!SketchDataValidator.isValidStroke(s)) continue;
                shapePoints.addAll(s.getPoints());
                ArrayList<Integer> c1 = strawCornerFinder.findCorners(s);
                List<TPoint> stC = fetchCornerPoints(s, c1);
                if (!stC.isEmpty())
                    strawCorners.addAll(stC);

                ArrayList<Integer> c2 = sezginCornerFinder.findCorners(s);
                szC = fetchCornerPoints(s, c2);
                if (!szC.isEmpty())
                    sezginCorners.addAll(szC);

                ArrayList<Integer> c3 = kimCornerFinder.findCorners(s);
                List<TPoint> kimC = fetchCornerPoints(s, c3);
                if (!kimC.isEmpty())
                    kimCorners.addAll(kimC);

                ArrayList<Integer> c4 = angleCornerFinder.findCorners(s);
                List<TPoint> cornerAngles = fetchCornerPoints(s, c4);
                if (!cornerAngles.isEmpty())
                    angleCorners.addAll(cornerAngles);

                ArrayList<Integer> c5 = rfCornerFinder.findCorners(s);
                List<TPoint> rfC = fetchCornerPoints(s, c5);
                if (!rfC.isEmpty())
                    rfCorners.addAll(rfC);
            }

            System.out.println("------------------------------------");
            System.out.println(numPoints);
            System.out.println("SHORT STRAW");
            printCorners(CornerValidator.validateCorners(strawCorners));
            System.out.println("ANGLE STRAW");
            printCorners(CornerValidator.validateCorners(angleCorners));
            System.out.println("KIM");
            printCorners(CornerValidator.validateCorners(kimCorners));
            System.out.println("SEZGIN");
            printCorners(CornerValidator.validateCorners(sezginCorners));
            System.out.println("RFC");
            printCorners(CornerValidator.validateCorners(rfCorners));
            System.out.println("------------------------------------");
            //System.out.println("Total Corners:" + allCorners.size());
        }
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
