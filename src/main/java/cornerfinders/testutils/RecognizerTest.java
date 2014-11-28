package cornerfinders.testutils;

import java.awt.Color;
import java.awt.Shape;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.mit.sketch.geom.Approximation;
import edu.mit.sketch.geom.Polygon;
import edu.mit.sketch.language.shapes.Stroke;
import edu.mit.sketch.toolkit.Classifier;
import edu.tamu.bpaulson.newrecognizer.BStroke;
import edu.tamu.bpaulson.newrecognizer.Fit;
import edu.tamu.bpaulson.newrecognizer.PolylineFit;
import edu.tamu.bpaulson.newrecognizer.StrokeFeatures;
import edu.tamu.bpaulson.newrecognizer.StrokeReader;
import edu.tamu.bpaulson.newrecognizer.TracyPolyLineParse;
import edu.tamu.hammond.io.FileHelper;
import edu.tamu.hammond.io.Plot;
import edu.tamu.hammond.sketch.shapes.TPoint;
import edu.tamu.hammond.sketch.shapes.TPolyline;
import edu.tamu.hammond.sketch.shapes.TStroke;

/**
 * RecognizerTest class - Used to recognize shapes based on geometric properties
 *
 * @author bpaulson
 */
public class RecognizerTest {

	// public static String[] dirs = {"arc1", "circle", "ellipse", "helix",
	// "line1", "line2", "line3",
	// "line4", "line5", "line6", "spiral"};
	public static String[] dirs = { "arc1", "circle", "ellipse", "helix",
		"line1", "line2", "line3", "line4", "line5", "spiral" };

	// public static String[] dirs = {"test"};
	public static int numUsers = 20;

	public static int numExamples = 10;

	private static boolean runBPaulson = true;

	private static boolean runSezgin = false;

	/**
	 * Main program
	 *
	 * @param args
	 *            currently not needed
	 * @throws java.io.IOException
	 */
	public static void main(String args[]) throws IOException {

		// bpaulson recognizer
		boolean jitter = false;
		String[] files = dirs;
		String filename;
		String shortFilename;
		String tmpFilename;
		String tmpShortFilename;
		String tmptmpFilename;
		String tmptmpShortFilename;
		int total = 0;
		int totalCorrect = 0;
		filename = "../SRLData/low_level_training/polyline";
		File dir = new File(filename);
		int correctMTS = 0;
		int correctBP = 0;
		int correctTH = 0;
		System.out.println(dir.exists() + " " + dir.getPath() + dir.getName() + dir.getAbsolutePath() + " " + dir.getCanonicalPath());
		for (File child : dir.listFiles()) {
			if (!child.getName().endsWith(".xml")) {
				continue;
			}
			//BStroke s = StrokeReader.readStroke(child.getPath(), jitter);
			List<TStroke> strokesRead = new ArrayList<TStroke>();
			strokesRead = TStroke.getTStrokesFromXML(child);
			for (int i = 0; i < strokesRead.size(); i++) {
				BStroke s = new BStroke(strokesRead.get(i));
				if(s == null){continue;}
				System.out.println(s);
				System.out.println(s.getXValues().length);
				if(s.getXValues() == null){System.out.println("null");}

				for(double d : s.getXValues()){
					System.out.println("d = " + d);
				}
				System.out.println(s.getXValues() + " : " + (s.getXValues() == null));

				double[] x = s.getXValues();
				double[] y = s.getYValues();
				long[] time = s.getTValues();

				StrokeFeatures sf = new StrokeFeatures(x, y, time);

				ArrayList<Fit> fits = s.recognize(true);
				for (Fit fit : fits) {
					String name = fit.getName();
					if (name.compareToIgnoreCase(Fit.POLYLINE) == 0) {
						Plot plot = new Plot("brandon corners");
						plot.addLine(s.getXValues(), s.getYValues(),
								Color.cyan, 10);
						PolylineFit pfit = s.getPolylineFit();
						Shape pline = pfit.get2DShape();
						double[] cornerx = new double[pfit.getSubStrokes().size() + 1];
						double[] cornery = new double[pfit.getSubStrokes().size() + 1];
						int count = 0;
						for (BStroke bs : pfit.getSubStrokes()) {
							cornerx[count] = bs.get(0).getX();
							cornery[count] = bs.get(0).getY();
							count++;
							if(bs == pfit.getSubStrokes().get(pfit.getSubStrokes().size()-1)){
								cornerx[count] = bs.get(bs.size() -1).getX();
								cornery[count] = bs.get(bs.size() - 1).getY();
							}
						}
						plot.addLine(cornerx, cornery,
									Color.black, 20);

						plot.setKeepdim(true);
						plot.plot();
					}
				}
				Stroke ss = s.getOldStroke();
				ss.compute();
				total++;
				for(Approximation app : ss.getClassifier().classifyAndRank(true)){
					int type = app.getGeometricObject().getIntType();
					switch (type) {
					case Classifier.POLYGON:
						System.out.println("POLYGON");
						Polygon mp = (Polygon)app.getGeometricObject();
						Plot plot = new Plot("Sezgin Corners");
						plot.addLine(sf.m_x, sf.m_y, Color.green, 10);
						plot.addLine(mp.xpoints, mp.ypoints, Color.black, 20);
					}
				}

				TracyPolyLineParse p = new TracyPolyLineParse(sf, false);
				Plot plot = new Plot("Tracy Corners");
				plot.addLine(sf.m_x, sf.m_y, Color.red, 10);
				plot.addLine(sf.m_xTracyCorner, sf.m_yTracyCorner, Color.black, 20);
				plot.setKeepdim(true);
				plot.plot();

			}
		}
	}
}
