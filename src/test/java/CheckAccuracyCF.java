import com.google.common.collect.Lists;
import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.SezginCornerFinder;
import cornerfinders.impl.ShortStrawCornerFinder;
import utils.dbconnector.ConnectDB;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jaideepray on 12/12/14.
 */
public class CheckAccuracyCF {


    public static List<TPoint> fetchCornerPoints(TStroke s, ArrayList<Integer> indices) {
        List<TPoint> corners = Lists.newArrayList();
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
        ConnectDB connectDB = new ConnectDB();
        connectDB.startConnection();
        List<TStroke> strokeList = DBUtils.fetchStrokes(1);
        List<TPoint> strawCorners = Lists.newArrayList();
        List<TPoint> sezginCorners = Lists.newArrayList();
        for (TStroke s : strokeList) {
            ArrayList<Integer> c1 = strawCornerFinder.findCorners(s);
            ArrayList<Integer> c2 = sezginCornerFinder.findCorners(s);
            strawCorners.addAll(fetchCornerPoints(s, c1));
            sezginCorners.addAll(fetchCornerPoints(s, c2));
        }
        System.out.println("SHORT STRAW");
        printCorners(strawCorners);
        System.out.println("SEZGIN");
        printCorners(sezginCorners);
    }

    public static void printCorners(List<TPoint> corners) {
        System.out.println("Number of corners" + corners.size());
        for (TPoint pt : corners) {
            pt.printPoint();
        }
    }


    public double similarCorners(List<TPoint> c1, List<TPoint> c2) {
        return 0.0;
    }

    public static void main(String[] args) {
        checkAccuracy();
    }
}
