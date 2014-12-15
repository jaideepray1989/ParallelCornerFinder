package cornerfinders.impl.rankfragmenter.features.classifier.train;

import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.AbstractCornerFinder;
import cornerfinders.impl.rankfragmenter.RFInitializer;
import cornerfinders.impl.rankfragmenter.RFNode;
import cornerfinders.impl.rankfragmenter.features.classifier.FeatureAttributes;
import cornerfinders.impl.rankfragmenter.features.point.*;
import cornerfinders.impl.rankfragmenter.features.rank.RankCostFeature;
import cornerfinders.impl.rankfragmenter.features.rank.RankFeature;
import cornerfinders.impl.rankfragmenter.features.stroke.DiagonalFeature;
import cornerfinders.impl.rankfragmenter.features.stroke.LengthFeature;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrainRFClassifier {

    public double cornerProbability = 0.0;

    public Instances loadTrainingData(List<TStroke> strokes, List<AbstractCornerFinder> cornerFinders) {
        FastVector fvAttributes = FeatureAttributes.getAttributeSet();
        RFInitializer rfInitializer = new RFInitializer();
        Instances instances = new Instances("TRAIN", fvAttributes, strokes.size());
        instances.setClassIndex(0);
        Integer numPoints = 0;
        Integer corner = 0;
        for (int i = 0; i < strokes.size(); i++) {
            Map<Integer, RFNode> rfNodeMap = rfInitializer.getInitialList(strokes.get(i).getPoints(), true);
            for (AbstractCornerFinder cornerFinder : cornerFinders) {
                ArrayList<Integer> corners = cornerFinder.findCorners(strokes.get(i));
                for (Map.Entry<Integer, RFNode> entry : rfNodeMap.entrySet()) {
                    numPoints++;
                    RFNode value = entry.getValue();
                    boolean isPointACorner = corners.indexOf(entry.getKey()) != -1;
                    Instance trainingInstance = createInstance(strokes.get(i), value, instances);
                    String cornerInfo = (isPointACorner) ? FeatureAttributes.ClassSet.Corner.toString() : FeatureAttributes.ClassSet.NotACorner.toString();
                    trainingInstance.setClassValue(cornerInfo);
                    if (isPointACorner)
                        corner++;
                    instances.add(trainingInstance);
                }
            }
        }
        cornerProbability = (1.0 * corner) / numPoints;
        return instances;
    }

    public Instance createInstance(TStroke stroke, RFNode node, Instances dataSet) {
        FastVector attributeSet = FeatureAttributes.getAttributeSet();
        Instance instance = new Instance(attributeSet.size());
        instance.setDataset(dataSet);

        // angle
        instance.setValue(1, AngleFeature.getAngleFeature(node));
        // point curvature
        instance.setValue(2, CurvatureFeature.getCurvature(node));
        // point position
        instance.setValue(3, PositionFeature.getPosition(node));
        // point speed
        instance.setValue(4, SpeedFeature.getSpeed(node));
        // point straw
        instance.setValue(5, StrawFeature.calculateStraw(node));
        // rank cost feature
        instance.setValue(6, RankCostFeature.getRankCostFeature(node));
        // rank feature
        instance.setValue(7, RankFeature.getRank(node));
        // diagonal length feature
        instance.setValue(8, DiagonalFeature.getDiagonalLength(stroke));
        // length feature
        instance.setValue(9, LengthFeature.getLength(stroke));
        // angle with window 3
        instance.setValue(10, AngleWithWindowFeature.getAngle(node));
        // mse error
        instance.setValue(11, MSEFeature.getMSEFeature(node));

        return instance;
    }


}
