import com.google.common.collect.Lists;
import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.SezginCornerFinder;
import cornerfinders.impl.ShortStrawCornerFinder;
import utils.validator.SketchDataValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jaideepray on 12/12/14.
 */
public class CheckAccuracyCF {

    public static List<TPoint> fetchCornerPoints(TStroke s, ArrayList<Integer> indices) {
        List<TPoint> corners = Lists.newArrayList();
        if (indices == null || indices.isEmpty())
            return corners;
        for (Integer index : indices) {
            if (0 <= index && index <= s.getSize()) {
                corners.add(s.getPoint(index));
            }
        }
        return corners;
    }

    public static void checkAccuracy() {
        ShortStrawCornerFinder strawCornerFinder = new ShortStrawCornerFinder();
        SezginCornerFinder sezginCornerFinder = new SezginCornerFinder();
        Map<String, List<TStroke>> strokeMap = DBUtils.fetchStrokes(10);
        List<TPoint> strawCorners = Lists.newArrayList();
        List<TPoint> sezginCorners = Lists.newArrayList();
        for (List<TStroke> sList : strokeMap.values()) {
            for (TStroke s : sList) {
                if (!SketchDataValidator.isValidStroke(s)) continue;
                ArrayList<Integer> c1 = strawCornerFinder.findCorners(s);
                ArrayList<Integer> c2 = sezginCornerFinder.findCorners(s);
                List<TPoint> stC = fetchCornerPoints(s, c1);
                if (!stC.isEmpty())
                    strawCorners.addAll(stC);
                List<TPoint> szC = fetchCornerPoints(s, c2);
                if (!szC.isEmpty())
                    sezginCorners.addAll(szC);
            }
            System.out.println("------------------------------------");
            System.out.println("SHORT STRAW");
            printCorners(strawCorners);
            System.out.println("SEZGIN");
            printCorners(sezginCorners);
            System.out.println("------------------------------------");
        }

    }

    public static void printCorners(List<TPoint> corners) {
        System.out.println("Number of corners" + corners.size());
        for (TPoint pt : corners) {
            pt.printPoint();
        }
    }

    public static void main(String[] args) {
        checkAccuracy();
    }
}
