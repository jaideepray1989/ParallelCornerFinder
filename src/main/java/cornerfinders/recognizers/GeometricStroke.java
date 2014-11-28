package cornerfinders.recognizers;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.bpaulson.linclassifier.Classifiable;
import edu.tamu.hammond.sketch.shapes.TPoint;
import edu.tamu.hammond.sketch.shapes.TStroke;

public class GeometricStroke extends TStroke implements Classifiable {

	private ArrayList<Double> features = new ArrayList<Double>();

	public GeometricStroke() {
		super();
	}

	public GeometricStroke(TStroke ts) {
		super();
		for (TPoint p : ts.getPoints())
			addPoint(p);
	}

	public void calculateFeatures() {
		features = new ArrayList<Double>();
		BStroke stroke = new BStroke(this);
		if (stroke != null) {
			stroke.recognize(true);
			StrokeFeatures strf = stroke.getFeatures();
			LineFit lf = stroke.getLineFit();
			ArcFit af = stroke.getArcFit();
			CurveFit cf = stroke.getCurveFit();
			PolylineFit pf = stroke.getPolylineFit();
			EllipseFit ef = stroke.getEllipseFit();
			CircleFit cirf = stroke.getCircleFit();
			SpiralFit sf = stroke.getSpiralFit();
			ComplexFit comf = stroke.getComplexFit();
			features.add(strf.numRevolutions());
			features.add(strf.getEndptStrokeLengthRatio()*100);
			features.add(strf.getNDDE());
			features.add(strf.getDCR());
			features.add(strf.getSlopeDirGraph()*1000);
			features.add(strf.getMaxCurv());
			features.add(strf.getAvgCurv()*100);
			features.add(strf.getStrokeLength());
			features.add(lf.getLSQE());
			features.add(lf.getError());
			features.add(af.getRadius());
			features.add(af.getError());
			features.add(cf.getError());
			features.add((double)pf.getSubStrokes().size());
			features.add(pf.getPercentPassed());
			features.add(pf.getError());
			features.add(pf.getLSQE()*100);
			features.add(ef.getMajorAxisLength());
			features.add(ef.getMinorAxisLength());
			features.add(ef.getError()*100);
			features.add(cirf.getRadius());
			features.add(cirf.getAxisRatio());
			features.add(cirf.getError()*100);
			features.add(sf.getAvgRadBBRadRatio());
			features.add(sf.getError());
			features.add(sf.getMaxDistanceToCenter());
			features.add(sf.getAvgRadius());
			features.add(sf.radiusTestPassed());
			features.add((double)comf.getSubFits().size());
			features.add((double)comf.numPrimitives());
			features.add(comf.percentLines());
			features.add(stroke.getComplexScore());
		}
	}

	public double compareFeatures(Classifiable c) {
		double err = 0.0;
		GeometricStroke s = (GeometricStroke)c;
		for (int i = 0; i < features.size(); i++) {
			err += (Math.abs(s.getFeatures().get(i)-features.get(i)));
		}
		return err;
	}

	public List<Double> getFeatures() {
		return features;
	}

	public int numFeatures() {
		return features.size();
	}

}
