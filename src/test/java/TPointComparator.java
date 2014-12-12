import cornerfinders.core.shapes.TPoint;

import java.util.Comparator;

/**
 * Created by jaideepray on 12/12/14.
 */
public class TPointComparator implements Comparator<TPoint> {
    @Override
    public int compare(TPoint o1, TPoint o2) {
        return (o1.getX() < o2.getX()) ? 1 : 0;
    }
}
