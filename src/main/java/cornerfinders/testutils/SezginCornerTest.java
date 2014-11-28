package cornerfinders.testutils;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.mit.sketch.geom.Approximation;
import edu.mit.sketch.geom.GeneralPath;
import edu.mit.sketch.geom.Point;
import edu.mit.sketch.geom.Polygon;
import edu.mit.sketch.geom.Range;
import edu.mit.sketch.geom.Vertex;
import edu.mit.sketch.language.shapes.Stroke;
import edu.mit.sketch.toolkit.Classifier;
import edu.tamu.bpaulson.newrecognizer.BStroke;
import edu.tamu.bpaulson.newrecognizer.ComplexFit;
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
public class SezginCornerTest {

	// public static String[] dirs = {"arc1", "circle", "ellipse", "helix",
	// "line1", "line2", "line3",
	// "line4", "line5", "line6", "spiral"};
	public static String[] dirs = { "arc1", "circle", "ellipse", "helix",
		"line1", "line2", "line3", "line4", "line5", "spiral" };

	// public static String[] dirs = {"test"};
	public static int numUsers = 20;

	public static int numExamples = 10;


	/**
	 * Main program
	 *
	 * @param args
	 *            currently not needed
	 * @throws java.io.IOException
	 */
	public static void main(String args[]) throws IOException {

		int total = 0;



		String datafilename = "../SRLData/data/singlestroke/cornertesting/";
		String jpgfilename = "../SRLData/interpretations/corner/cornertesting/";
		File gendir = new File(datafilename);

		for(File dir : gendir.listFiles()){
		//System.out.println(dir.exists() + " " + dir.getPath() + dir.getName() + dir.getAbsolutePath() + " " + dir.getCanonicalPath());
		for (File child : dir.listFiles()) {
			if (!child.getName().endsWith(".xml")) {
				continue;
			}
			total++;
			//BStroke s = StrokeReader.readStroke(child.getPath(), jitter);
			List<TStroke> strokesRead = new ArrayList<TStroke>();
			strokesRead = TStroke.getTStrokesFromXML(child);
			File file = new File(jpgfilename + "/" + dir.getName() );
			file.mkdirs();
			Plot plot;
			for (int i = 0; i < strokesRead.size(); i++) {
				BStroke s = new BStroke(strokesRead.get(i));
				if(s == null){continue;}
				if(s.getXValues() == null){System.out.println("null");}

				double[] x = s.getXValues();
				double[] y = s.getYValues();
				long[] time = s.getTValues();

				StrokeFeatures sf = new StrokeFeatures(x, y, time);




				Stroke ss = s.getOldStroke();
				ss.compute();

				for(Approximation app : ss.getClassifier().classifyAndRank(true)){
					int type = app.getGeometricObject().getIntType();
					switch (type) {
					case Classifier.COMPLEX:
						GeneralPath gp = (GeneralPath)app.getGeometricObject();
						double[] cornerx = new double[gp.numShapes() + 1];
						double[] cornery = new double[gp.numShapes() + 1];
						int count = 0;
						Point last_point = new Point();
						gp.numShapes();
						gp.isAllCurves();
						String interpretation = "";
						double      coefficients[] = new double[6];
						PathIterator path_iterator =
							gp.getPathIterator( new AffineTransform() );
						while ( !path_iterator.isDone() ) {
					        if ( path_iterator.currentSegment( coefficients ) ==
								 PathIterator.SEG_LINETO ) {
								interpretation+="L";
								last_point.setLocation( coefficients[0], coefficients[1] );
				            } else if (path_iterator.currentSegment( coefficients ) ==
			            		PathIterator.SEG_QUADTO){
				            	interpretation +="A";
				            	last_point.setLocation( coefficients[2], coefficients[3] );
				            }
				              else if (path_iterator.currentSegment( coefficients ) ==
				            	PathIterator.SEG_CUBICTO) {
				            	interpretation +="A";
				            	last_point.setLocation( coefficients[4], coefficients[5] );
				            }
				              else if (path_iterator.currentSegment( coefficients ) ==
				            	PathIterator.SEG_MOVETO) {
				            	  last_point.setLocation( coefficients[0], coefficients[1] );
				            }
				              else {
				            	  last_point = null;
				            }
					        if (last_point != null) {
					        	cornerx[count]=last_point.getX();
					        	cornery[count]=last_point.getY();
					        	count++;
					        }
							path_iterator.next();
						}

						plot = new Plot("Sezgin Corners");
						plot.addLine(sf.m_x, sf.m_y, Color.blue, 10);
						plot.addLine(cornerx, cornery, Color.black, 20);
						plot.setKeepdim(true);
						plot.plot();
						plot.saveJPG(jpgfilename + "/" + dir.getName() + "/SZ_C" + gp.numShapes() + child.getName() +  "_" + interpretation + ".jpg");
						break;
					case Classifier.POLYGON:
						//System.out.println("POLYGON");
						Polygon mp = (Polygon)app.getGeometricObject();
						plot = new Plot("Sezgin Corners");
						plot.addLine(sf.m_x, sf.m_y, Color.green, 10);
						plot.addLine(mp.xpoints, mp.ypoints, Color.black, 20);
						plot.setKeepdim(true);
						plot.plot();
						plot.saveJPG(jpgfilename + "/" + dir.getName() + "/SZ_P" + mp.getLines().length + "_" + child.getName() + "_" + i + ".jpg");
						break;
					}
					}
				}


		}
		}
	}
}
