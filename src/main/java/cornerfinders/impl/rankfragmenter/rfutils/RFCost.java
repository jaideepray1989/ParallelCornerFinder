package cornerfinders.impl.rankfragmenter.rfutils;

import cornerfinders.core.shapes.TPoint;

import java.util.List;

public class RFCost {

    public static long INF = 10000;


    public static double cost(TPoint prev, TPoint succ, TPoint p) {
        if (prev == null || succ == null)
            return INF;
        if (p == null)
            return 0;

        double mse_sqrt = Math.sqrt((RFUtils.euclidean(p, prev) + RFUtils.euclidean(p, succ)) / 2);
        return (mse_sqrt * dist(prev, succ, p));

    }

    public static double dist(TPoint l1, TPoint l2, TPoint p) {
        double x1 = l1.getX(), x2 = l2.getX(), x0 = p.getX();
        double y1 = l1.getY(), y2 = l2.getY(), y0 = p.getY();
        double den = Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
        double num = Math.abs((y2 - y1) * x0 - (x2 - x1) * y0 + x2 * y1 - y2 * x1);
        return num / den;
    }
}
