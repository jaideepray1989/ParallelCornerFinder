package cornerfinders.impl.rankfragmenter;

import com.google.common.collect.Lists;
import cornerfinders.core.shapes.TPoint;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

import java.util.List;

/**
 * Created by jaideepray on 12/12/14.
 */
public class CornerClassifier {
    private RandomForest randomForestClassifier;

    CornerClassifier() {
        randomForestClassifier = new RandomForest();
        randomForestClassifier.setMaxDepth(10);
        randomForestClassifier.setNumTrees(200);
        randomForestClassifier.setSeed(13);
    }

    public boolean isPointACorner(Instances instances) throws Exception {
        double pred = randomForestClassifier.classifyInstance(instances.instance(0));
        instances.classAttribute().value((int) pred);
        return true;
    }

    public List<TPoint> getCornerList(List<RFNode> pointList) {
        return Lists.newArrayList();
    }

}
