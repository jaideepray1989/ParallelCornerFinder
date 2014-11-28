package cornerfinders.recognizers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import javax.swing.JFileChooser;


public class FeatureExtractor {

	public static File dir;
	public static BufferedWriter matrixFile;
	public static BufferedWriter labelFile;
	public static BufferedWriter featureFile;


	public static void main(String args[]) {
		JFileChooser c = new JFileChooser();
		c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int r = c.showOpenDialog(null);
		if (r == JFileChooser.APPROVE_OPTION) {
			dir = c.getSelectedFile();
		}
		else return;
		try {
			matrixFile = new BufferedWriter(new FileWriter(new File(dir + "_data.txt")));
			labelFile = new BufferedWriter(new FileWriter(new File(dir + "_label.txt")));
			featureFile = new BufferedWriter(new FileWriter(new File(dir + "_features.txt")));
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		File[] files = dir.listFiles();
		boolean wroteFeatures = false;
		for (int f = 0; f < files.length; f++) {
			if (!files[f].isDirectory())
				continue;
			File[] shapes = files[f].listFiles();
			String fn = files[f].getName();
			for (int s = 0; s < shapes.length; s++) {
				if (shapes[s].isDirectory() || !shapes[s].exists())
					continue;
				List<TStroke> strokeList = TStroke.getTStrokesFromXML(shapes[s]);
				if (strokeList.size() <= 0)
					continue;
				BStroke stroke = new BStroke(strokeList.get(0));
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

					try {

						System.out.println("writing for " + shapes[s].getName() + "...");

						// general features
						matrixFile.write(strf.numRevolutions()+",");
						if (!wroteFeatures)
							featureFile.write("NumRevolutions\n");
						matrixFile.write(strf.getEndptStrokeLengthRatio()*100+",");
						if (!wroteFeatures)
							featureFile.write("EndptStrokeLengthRatio\n");
						matrixFile.write(strf.getNDDE()+",");
						if (!wroteFeatures)
							featureFile.write("NDDE\n");
						matrixFile.write(strf.getDCR()+",");
						if (!wroteFeatures)
							featureFile.write("DCR\n");
						matrixFile.write(strf.getSlopeDirGraph()*1000+",");
						if (!wroteFeatures)
							featureFile.write("SlopeDirGraph\n");
						matrixFile.write(strf.getMaxCurv()+",");
						if (!wroteFeatures)
							featureFile.write("MaxCurvature\n");
						matrixFile.write(strf.getAvgCurv()*100+",");
						if (!wroteFeatures)
							featureFile.write("AvgCurvature\n");
						matrixFile.write(strf.numFinalCorners()+",");
						if (!wroteFeatures)
							featureFile.write("NumCorners\n");
						matrixFile.write(strf.getStrokeLength()+",");
						if (!wroteFeatures)
							featureFile.write("StrokeLength\n");

						// line features
						matrixFile.write(lf.getLSQE()+",");
						if (!wroteFeatures)
							featureFile.write("LineLSQE\n");
						matrixFile.write(lf.getError()+",");
						if (!wroteFeatures)
							featureFile.write("LineFA\n");

						// arc features
						if (af.getRadius() > 10000)
							matrixFile.write(10000+",");
						else
							matrixFile.write(af.getRadius()+",");
						if (!wroteFeatures)
							featureFile.write("ArcRadius\n");
						matrixFile.write(af.getError()+",");
						if (!wroteFeatures)
							featureFile.write("ArcFA\n");

						// curve features
						matrixFile.write(cf.getError()+",");
						if (!wroteFeatures)
							featureFile.write("CurveLSQE\n");

						// polyline features
						matrixFile.write(pf.getSubStrokes().size()+",");
						if (!wroteFeatures)
							featureFile.write("PolyNumSubStrokes\n");
						matrixFile.write(pf.getPercentPassed()+",");
						if (!wroteFeatures)
							featureFile.write("PolyPercentSubPassed\n");
						matrixFile.write(pf.getError()+",");
						if (!wroteFeatures)
							featureFile.write("PolyFA\n");
						matrixFile.write(pf.getLSQE()*100+",");
						if (!wroteFeatures)
							featureFile.write("PolyLSQE\n");

						// ellipse features
						matrixFile.write(ef.getMajorAxisLength()+",");
						if (!wroteFeatures)
							featureFile.write("EllipseMajAxisLength\n");
						matrixFile.write(ef.getMinorAxisLength()+",");
						if (!wroteFeatures)
							featureFile.write("EllipseMinAxisLength\n");
						matrixFile.write(ef.getError()*100+",");
						if (!wroteFeatures)
							featureFile.write("EllipseFA\n");

						// circle features
						matrixFile.write(cirf.getRadius()+",");
						if (!wroteFeatures)
							featureFile.write("CircleRadius\n");
						matrixFile.write(cirf.getAxisRatio()+",");
						if (!wroteFeatures)
							featureFile.write("CircleAxisRatio\n");
						if (cirf.getError()*100 > 200)
							matrixFile.write(200+",");
						else
							matrixFile.write(cirf.getError()*100+",");
						if (!wroteFeatures)
							featureFile.write("CircleFA\n");

						// spiral/helix features
						matrixFile.write(sf.getAvgRadBBRadRatio()+",");
						if (!wroteFeatures)
							featureFile.write("SpiralAvgRadBBRadRatio\n");
						matrixFile.write(sf.getError()+",");
						if (!wroteFeatures)
							featureFile.write("SpiralCenterError\n");
						matrixFile.write(sf.getMaxDistanceToCenter()+",");
						if (!wroteFeatures)
							featureFile.write("SpiralMaxDisBetweenCenters\n");
						matrixFile.write(sf.getAvgRadius()+",");
						if (!wroteFeatures)
							featureFile.write("SpiralAvgRadius\n");
						matrixFile.write(sf.radiusTestPassed()+",");
						if (!wroteFeatures)
							featureFile.write("SpiralRadiusTest\n");

						// complex featues
						matrixFile.write(comf.getSubFits().size()+",");
						if (!wroteFeatures)
							featureFile.write("ComplexNumSubFits\n");
						matrixFile.write(comf.numPrimitives()+",");
						if (!wroteFeatures)
							featureFile.write("ComplexNumPrimitives\n");
						matrixFile.write(comf.percentLines()+",");
						if (!wroteFeatures)
							featureFile.write("ComplexPctLines\n");
						matrixFile.write(stroke.getComplexScore()+",");
						if (!wroteFeatures)
							featureFile.write("ComplexScore\n");

						// rubine features
						TRubineStroke rs = new TRubineStroke(strokeList.get(0));
						rs.calculateFeatures();
						matrixFile.write(rs.getStartingCosAngle()+",");
						if (!wroteFeatures)
							featureFile.write("CosStartAngle\n");
						matrixFile.write(rs.getStartingSinAngle()+",");
						if (!wroteFeatures)
							featureFile.write("SinStartAngle\n");
						matrixFile.write(rs.getStartEndDistance()+",");
						if (!wroteFeatures)
							featureFile.write("EndptDist\n");
						matrixFile.write(rs.getBoundingBoxDiagonalLength()+",");
						if (!wroteFeatures)
							featureFile.write("BBDiagLength\n");
						matrixFile.write(rs.getBoundingBoxDiagonalAngle()+",");
						if (!wroteFeatures)
							featureFile.write("BBDiagAngle\n");
						matrixFile.write(rs.getCosEndingAngle()+",");
						if (!wroteFeatures)
							featureFile.write("CosEndAngle\n");
						matrixFile.write(rs.getSinEndingAngle()+",");
						if (!wroteFeatures)
							featureFile.write("SinEndAngle\n");
						matrixFile.write(rs.getAbsoluteRotation()+",");
						if (!wroteFeatures)
							featureFile.write("AbsoluteRotation\n");
						matrixFile.write(rs.getRotationSquared()+",");
						if (!wroteFeatures)
							featureFile.write("RotationSquared\n");
						matrixFile.write(rs.getMaximumSpeed()+",");
						if (!wroteFeatures)
							featureFile.write("MaxSpeed\n");
						matrixFile.write(rs.getTotalTime()+"");
						if (!wroteFeatures)
							featureFile.write("TotalTime\n");

						matrixFile.newLine();
						labelFile.write(fn + " " + shapes[s].getName());
						labelFile.newLine();
						wroteFeatures = true;
						featureFile.close();
					}
					catch (Exception e) {
						e.printStackTrace();
						System.exit(0);
					}
				}
			}
		}
		try {
			matrixFile.close();
			featureFile.close();
			labelFile.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
