import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by jaideepray on 12/6/14.
 */
public class AbstractCornerFinderTest {

    public void printCorners(ArrayList<Integer> pointIndices,TStroke stroke)
    {
        System.out.println("-----------------------printing corners -----------------------------");
        for(int i=0;i<pointIndices.size();i++)
        {
            stroke.getPoint(pointIndices.get(i)).printPoint();
        }
        System.out.println("--------------------done printing corners ---------------------------");
    }
}
