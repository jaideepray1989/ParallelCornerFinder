package cornerfinders.impl.rankfragmenter.rfutils;

import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.rankfragmenter.features.classifier.FeatureAttributes;
import weka.core.Instance;
import weka.core.Instances;

import java.util.List;

/**
 * Created by jaideepray on 12/12/14.
 */
public class RFUtils {

    public static double euclidean(TPoint p0, TPoint p1) {
        double x2 = (p1.getX() - p0.getX()) * (p1.getX() - p0.getX());
        double y2 = (p1.getY() - p0.getY()) * (p1.getY() - p0.getY());

        return Math.sqrt(x2 + y2);
    }

    private static double euclidean(double x0, double y0, double x1, double y1) {
        double xSq = (x1 - x0) * (x1 - x0);
        double ySq = (y1 - y0) * (y1 - y0);

        return Math.sqrt(xSq + ySq);
    }

    public static double firstLastPtDist(TStroke s) {
        return euclidean(s.getFirstPoint(), s.getLastPoint());
    }


    public static double boundingBoxSize(TStroke s) {
        return euclidean(s.getMinX(), s.getMinY(), s.getMaxX(), s.getMaxY());
    }

    public static Integer INF = 10000;


    public void populateInstanceWithData(List<TPoint> pointList, TStroke stroke) {
        Instances trainingDataInstance = new Instances("TRAIN", FeatureAttributes.getAttributeSet(), 1);
        trainingDataInstance.setClassIndex(0);
        int numAttributes = FeatureAttributes.getAttributeSet().size();

        for (int num = 0; num < pointList.size(); num++) {
            Instance instance = new Instance(numAttributes);
            for (int i = 1; i < numAttributes; i++) {

            }
        }

    }


}
