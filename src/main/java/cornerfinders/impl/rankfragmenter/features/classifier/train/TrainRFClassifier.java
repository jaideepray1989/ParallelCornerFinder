package cornerfinders.impl.rankfragmenter.features.classifier.train;

import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.AbstractCornerFinder;
import cornerfinders.impl.rankfragmenter.features.classifier.FeatureAttributes;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.List;

/**
 * Created by jaideepray on 12/12/14.
 */
public class TrainRFClassifier {

    public Instances loadTrainingData(List<TStroke> strokes, AbstractCornerFinder cornerFinder) {
        FastVector fvAttributes = FeatureAttributes.getAttributeSet();
        Instances instances = new Instances("Training Data", fvAttributes, strokes.size());
        for (int i = 0; i < strokes.size(); i++) {
            cornerFinder.findCorners(strokes.get(i));
            for (int j = 0; j < strokes.get(i).getPoints().size(); j++) {
                Instance trainingSetInstance = new Instance(fvAttributes.size());
                for (int att = 0; att < fvAttributes.size(); att++) {
                    trainingSetInstance.setValue((Attribute) fvAttributes.elementAt(att), 1.0);
                }
                instances.add(trainingSetInstance);
            }
        }
        instances.setClassIndex(0);
        return instances;
    }


}
