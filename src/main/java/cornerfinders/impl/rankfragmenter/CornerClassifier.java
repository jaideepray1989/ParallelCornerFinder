package cornerfinders.impl.rankfragmenter;

import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.AbstractCornerFinder;
import cornerfinders.impl.rankfragmenter.features.classifier.FeatureAttributes;
import cornerfinders.impl.rankfragmenter.features.classifier.train.TrainRFClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.List;

public class CornerClassifier {
    private RandomForest randomForestClassifier;
    private TrainRFClassifier trainRFClassifier;

    CornerClassifier(AbstractCornerFinder trainer, List<TStroke> trainingSet) {
        randomForestClassifier = new RandomForest();
        randomForestClassifier.setMaxDepth(10);
        randomForestClassifier.setNumTrees(200);
        randomForestClassifier.setSeed(13);
        trainRFClassifier = new TrainRFClassifier();
        try {
            trainClassifier(randomForestClassifier, trainingSet, trainer);
        } catch (Exception ex) {
            System.out.println("could not train classifier");
        }
    }

    public boolean isPointACorner(TPoint point) {
        FastVector fvAttributes = FeatureAttributes.getAttributeSet();
        Instance testInstance = new Instance(fvAttributes.size() + 1);
        testInstance.setValue(new Attribute("Hell"), 0.1);
        testInstance.setValue(new Attribute("Hello"), 0.1);
        double pred = 0.0;
        try {
            pred = randomForestClassifier.classifyInstance(testInstance);
        } catch (Exception ex) {
            return false;
        }
        return (pred > 0.5);
    }

    public void trainClassifier(RandomForest randomForestClassifier, List<TStroke> strokes, AbstractCornerFinder cornerFinder) throws Exception {
        Instances train = trainRFClassifier.loadTrainingData(strokes, cornerFinder);
        randomForestClassifier.buildClassifier(train);
    }


}
