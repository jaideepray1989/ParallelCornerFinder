package cornerfinders.impl.rankfragmenter;

import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.AbstractCornerFinder;
import cornerfinders.impl.rankfragmenter.features.classifier.train.TrainRFClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.BayesianLogisticRegression;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;

import java.util.List;
import java.util.Random;

public class CornerClassifier {
    private RandomForest randomForestClassifier;
    private BayesianLogisticRegression logisticRegClassifier;
    private Vote combiner;
    private TrainRFClassifier trainRFClassifier;
    private NaiveBayes naiveBayesClassifier;
    private Instances train;


    private RandomForest createRandomForestClassifier() {
        randomForestClassifier = new RandomForest();
        randomForestClassifier.setMaxDepth(10);
        randomForestClassifier.setNumTrees(20);
        randomForestClassifier.setNumFeatures(9);
        return randomForestClassifier;
    }


    private Vote getCombiner(Classifier[] classifiers) {
        combiner = new Vote();
        combiner.setClassifiers(classifiers);
        combiner.setSeed(new Random(System.currentTimeMillis()).nextInt());
        return combiner;
    }


    CornerClassifier(List<AbstractCornerFinder> trainers, List<TStroke> trainingSet) {
        Classifier[] classifiers = new Classifier[3];
        classifiers[0] = createRandomForestClassifier();
        trainRFClassifier = new TrainRFClassifier();
        try {
            Instances dataSet = getTrainingDataSet(trainingSet, trainers);
            randomForestClassifier.buildClassifier(dataSet);
//            Vote combiner = getCombiner(classifiers);
//            combiner.buildClassifier(dataSet);
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

    public Instances getTrainingDataSet(List<TStroke> strokes, List<AbstractCornerFinder> cornerFinders) {
        train = trainRFClassifier.loadTrainingData(strokes, cornerFinders);
        return train;
    }


}
