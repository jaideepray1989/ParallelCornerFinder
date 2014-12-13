package cornerfinders.impl.rankfragmenter.features.classifier;

import weka.core.Attribute;
import weka.core.FastVector;

public class FeatureAttributes {


    public enum FeatureSet {
        PointAngle, PointCurvature, PointPosition, PointSpeed, PointStraw, RankCostFeature, RankFeature, DiagonalLengthFeature, LengthFeature;
    }

    public enum ClassSet {
        Corner, NotACorner;
    }

    public static FastVector getAttributeSet() {
        FastVector atts = new FastVector();
        atts.addElement(getClassAttribute());
        atts.addElement(new Attribute(FeatureSet.PointAngle.toString()));
        atts.addElement(new Attribute(FeatureSet.PointCurvature.toString()));
        atts.addElement(new Attribute(FeatureSet.PointPosition.toString()));
        atts.addElement(new Attribute(FeatureSet.PointSpeed.toString()));
        atts.addElement(new Attribute(FeatureSet.PointStraw.toString()));
        atts.addElement(new Attribute(FeatureSet.RankCostFeature.toString()));
        atts.addElement(new Attribute(FeatureSet.RankFeature.toString()));
        atts.addElement(new Attribute(FeatureSet.DiagonalLengthFeature.toString()));
        atts.addElement(new Attribute(FeatureSet.LengthFeature.toString()));
        return atts;
    }

    public static Attribute getClassAttribute() {
        FastVector fvNominalVal = new FastVector(2);
        fvNominalVal.addElement(ClassSet.Corner);
        fvNominalVal.addElement(ClassSet.NotACorner);
        return new Attribute("class", fvNominalVal);
    }


}
