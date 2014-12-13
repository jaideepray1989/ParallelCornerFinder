package cornerfinders.impl.rankfragmenter.features.point;

import com.google.common.collect.Maps;
import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.SezginCornerFinder;

import java.util.List;
import java.util.Map;

/**
 * Created by jaideepray on 12/12/14.
 */
public class CurvatureFeature {

    private Map<TPoint, Double> curvatureMap = Maps.newHashMap();

    CurvatureFeature(TStroke stroke) {
        SezginCornerFinder cornerFinder = new SezginCornerFinder();
        List<TPoint> pts = stroke.getPoints();
        double[] arcLength;
        double[] direction;
        // Get the arc length at each point
        arcLength = cornerFinder.arcLength();
        direction = cornerFinder.calcDirections(pts, true);
        double[] curvature = cornerFinder.calcCurvatures(arcLength, direction, true);

        for (int i = 0; i < curvature.length; i++) {
            curvatureMap.put(pts.get(i), curvature[i]);
        }
    }
}
