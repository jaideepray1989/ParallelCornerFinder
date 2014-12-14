package cornerfinders.impl.rankfragmenter;

import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.AbstractCornerFinder;
import cornerfinders.impl.rankfragmenter.features.classifier.train.TrainRFClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;

import java.util.List;

public class CornerClassifier {
    private RandomForest randomForestClassifier;
    private TrainRFClassifier trainRFClassifier;
    private Instances train;

    CornerClassifier(AbstractCornerFinder trainer, List<TStroke> trainingSet) {
        randomForestClassifier = new RandomForest();
        randomForestClassifier.setMaxDepth(10);
        randomForestClassifier.setNumTrees(20);
        randomForestClassifier.setNumFeatures(9);
        trainRFClassifier = new TrainRFClassifier();
        try {
            trainClassifier(randomForestClassifier, trainingSet, trainer);
        } catch (Exception ex) {
            System.out.println("could not train classifier");
        }
    }

    public boolean isPointACorner(TStroke stroke, RFNode node) {
        Instance testInstance = trainRFClassifier.createInstance(stroke, node, train);
        double[] pred;
        try {
            pred = randomForestClassifier.distributionForInstance(testInstance);

        } catch (Exception ex) {
            return false;
        }
        return (pred[0] > 0.1);
    }

    public void trainClassifier(RandomForest randomForestClassifier, List<TStroke> strokes, AbstractCornerFinder cornerFinder) throws Exception {
        train = trainRFClassifier.loadTrainingData(strokes, cornerFinder);
        randomForestClassifier.buildClassifier(train);
    }


}
