import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jaideepray on 12/6/14.
 */
public class TestFigure {

    public static List<TPoint> getPointsFromTestFigure()
    {
        return new ArrayList<TPoint>();
    }

    public static TStroke getStroke()
    {
        return new TStroke(getPointsFromTestFigure());
    }


}
