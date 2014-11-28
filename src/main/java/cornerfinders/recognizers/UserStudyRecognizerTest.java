package cornerfinders.recognizers;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import edu.mit.sketch.geom.Approximation;
import edu.mit.sketch.geom.GeneralPath;
import edu.mit.sketch.geom.Point;
import edu.mit.sketch.grammar.me.Spring;
import edu.mit.sketch.language.shapes.Stroke;
import edu.mit.sketch.toolkit.Classifier;
import edu.tamu.hammond.sketch.shapes.TStroke;

/**
 * UserStudyRecognizerTest class -
 * Used to recognize shapes based on geometric properties
 * @author bpaulson
 */
public class UserStudyRecognizerTest {

	public static File dir;
	private static boolean runBPaulson = true;
	private static boolean runSezgin = false;
	private static ArrayList<String> bad = new ArrayList<String>();

	/**
	 * Main program
	 * @param args currently not needed
	 */
	public static void main(String args[]) {
		long startTime = 0, endTime = 0, sum = 0;
		JFileChooser c = new JFileChooser();
		c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int r = c.showOpenDialog(null);
		if (r == JFileChooser.APPROVE_OPTION) {
			dir = c.getSelectedFile();
		}
		else return;

		// bpaulson recognizer
		double total = 0;
		double correct = 0;
		double alt = 0;
		double numInts = 0;
		sum = 0;
		File[] files = dir.listFiles();
		System.out.println("Running BPaulson...\n");
		if (runBPaulson) {
			for (int f = 0; f < files.length; f++) {
				if (!files[f].isDirectory())
					continue;
				File[] shapes = files[f].listFiles();
				double num = 0;
				double top = 0;
				double sub = 0;
				String fn = files[f].getName();
				for (int s = 0; s < shapes.length; s++) {
					if (shapes[s].isDirectory() || !shapes[s].exists())
						continue;
					List<TStroke> strokeList = TStroke.getTStrokesFromXML(shapes[s]);
					if (strokeList.size() <= 0)
						continue;
					BStroke stroke = new BStroke(strokeList.get(0));
					if (stroke != null) {
						System.out.println(shapes[s].getName() + " makeup: ");
						startTime = System.currentTimeMillis();
						ArrayList<Fit> fits = stroke.recognize(true);
						endTime = System.currentTimeMillis();
						sum += (endTime-startTime);
						if (fits.size() == 0)
							System.out.println(" - ");
						boolean found = false;
						double oldTop = top;
						for (int n = 0; n < fits.size(); n++) {
							String name = fits.get(n).getName();
							if (name.compareToIgnoreCase(Fit.COMPLEX)==0) {
								ComplexFit cf = (ComplexFit)fits.get(n);
								ArrayList<Fit> l = cf.getSubFits();
								System.out.print(name + "(");
								for (int i = 0; i < l.size(); i++)
									System.out.print(l.get(i).getName() + ", ");
								System.out.println(") representation");
							}
							else
								System.out.println(name + " representation");
							if ((fn.compareToIgnoreCase("line")==0 && name.compareToIgnoreCase(Fit.LINE)==0) ||
								(fn.compareToIgnoreCase("arc")==0 && name.compareToIgnoreCase(Fit.ARC)==0) ||
								(fn.compareToIgnoreCase("circle")==0 && name.compareToIgnoreCase(Fit.CIRCLE)==0) ||
								(fn.compareToIgnoreCase("ellipse")==0 && name.compareToIgnoreCase(Fit.ELLIPSE)==0) ||
								(fn.compareToIgnoreCase("helix")==0 && name.compareToIgnoreCase(Fit.HELIX)==0) ||
								(fn.compareToIgnoreCase("spiral")==0 && name.compareToIgnoreCase(Fit.SPIRAL)==0) ||
								(fn.compareToIgnoreCase("polyline")==0 && name.compareToIgnoreCase(Fit.POLYLINE)==0) ||
								(fn.compareToIgnoreCase("polyline")==0 && name.compareToIgnoreCase(Fit.POLYGON)==0) ||
								(fn.compareToIgnoreCase("complex")==0 && name.compareToIgnoreCase(Fit.COMPLEX)==0) ||
								(fn.compareToIgnoreCase("curve")==0 && name.compareToIgnoreCase(Fit.CURVE)==0) ||
								(fn.compareToIgnoreCase("rectangle")==0 && name.compareToIgnoreCase(Fit.RECTANGLE)==0) ||
								(fn.compareToIgnoreCase("polygon")==0 && name.compareToIgnoreCase(Fit.POLYGON)==0) ||
								(fn.compareToIgnoreCase("square")==0 && name.compareToIgnoreCase(Fit.RECTANGLE)==0) ||
								(fn.compareToIgnoreCase("arrow")==0 && name.compareToIgnoreCase(Fit.ARROW)==0)) {
									found = true;
									if (n == 0) {
										correct++;
										top++;
									}
							}
						}
						numInts += fits.size();
						if (found) {
							sub++;
							alt++;
						}
						else {
							System.out.println("LOOK AT ME: " + shapes[s].getName());
						}
						if (top == oldTop)
							bad.add(shapes[s].getName());
						total++;
						num++;
						System.out.println("");
					}
				}

				System.out.println("Total " + fn + "s: " + num);
				System.out.println("Correctness Accuracy: " + (top/num));
				System.out.println("Approx Accuracy: " + (sub/num));
				System.out.println("");
			}
			System.out.println("Total Shapes: " + total);
			System.out.println("Correctness Accuracy: " + (correct/total));
			System.out.println("Approx Accuracy: " + (alt/total));
			System.out.println("Recognition Time: " + sum + "ms");
			System.out.println("Missed Examples:");
			System.out.println("Avg Num Test Passed: " + (numInts/total));
			for (int i = 0; i < bad.size(); i++) {
				System.out.println(bad.get(i));
			}
		}
		System.out.println("\n");

		// Sezgin recognizer
		total = 0;
		correct = 0;
		alt = 0;
		sum = 0;
		files = dir.listFiles();
		System.out.println("Running Sezgin...\n");
		if (runSezgin) {
			for (int f = 0; f < files.length; f++) {
				if (!files[f].isDirectory())
					continue;
				File[] shapes = files[f].listFiles();
				double num = 0;
				double top = 0;
				double sub = 0;
				String fn = files[f].getName();
				for (int s = 0; s < shapes.length; s++) {
					if (shapes[s].isDirectory() || !shapes[s].exists())
						continue;
					List<TStroke> strokeList = TStroke.getTStrokesFromXML(shapes[s]);
					if (strokeList.size() <= 0)
						continue;
					BStroke stroke = new BStroke(strokeList.get(0));
					if (stroke != null) {
						Stroke ss = stroke.getOldStroke();
						ss.compute();
						total++;
						num++;
						boolean found = false;
						startTime = System.currentTimeMillis();
						try {
						int chosen = ss.getClassifier().classify();
						endTime = System.currentTimeMillis();
						sum += (endTime-startTime);
						Approximation[] app = ss.getClassifier().classifyAndRank(false);
						System.out.println(shapes[s].getName() + " makeup: ");
						switch(chosen) {
						case Classifier.ARC:
							System.out.println("ARC");
							if (fn.compareToIgnoreCase("arc")==0 ||
								fn.compareToIgnoreCase("curve")==0) {
								found = true;
								top++;
								correct++;
							}
							break;
						case Classifier.COMPLEX:
							System.out.print("COMPLEX");
							if (fn.compareToIgnoreCase("complex")==0 ||
								(fn.compareToIgnoreCase("curve")==0 && ss.isCurve()) ||
							    (fn.compareToIgnoreCase("arc")==0 && (ss.isArc() || ss.isCurve()))) {
								found = true;
								top++;
								correct++;
							}
							int index = 0;
							while (!(app[index].getGeometricObject() instanceof GeneralPath)) index++;
							GeneralPath gp = (GeneralPath)app[0].getGeometricObject();
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
							System.out.println(" " + interpretation);
							break;
						case Classifier.ELLIPSE:
							System.out.println("ELLIPSE");
							if (fn.compareToIgnoreCase("ellipse")==0 ||
								fn.compareToIgnoreCase("circle")==0) {
								found = true;
								top++;
								correct++;
							}
							break;
						case Classifier.LINE:
							System.out.println("LINE");
							if (fn.compareToIgnoreCase("line")==0) {
								found = true;
								top++;
								correct++;
							}
							break;
						case Classifier.POINT:
							System.out.println("POINT");
							break;
						case Classifier.POLYGON:
							System.out.println("POLYGON");
							if (fn.compareToIgnoreCase("polyline")==0 ||
								fn.compareToIgnoreCase("square")==0 ||
								fn.compareToIgnoreCase("polygon")==0 ||
								fn.compareToIgnoreCase("rect")==0) {
								found = true;
								top++;
								correct++;
							}
							break;
						case Classifier.SPIRAL:
							System.out.println("SPIRAL");
							if (fn.compareToIgnoreCase("spiral")==0 ||
								fn.compareToIgnoreCase("helix")==0) {
								found = true;
								top++;
								correct++;
							}
							break;
						}
						for (int i = 0; i < app.length && !found; i++) {
							int type = app[i].getGeometricObject().getIntType();
							if (type == chosen)
								continue;
							switch(type) {
							case Classifier.ARC:
								System.out.println("ARC");
								if (fn.compareToIgnoreCase("arc")==0 ||
									fn.compareToIgnoreCase("curve")==0) {
									found = true;
								}
								break;
							case Classifier.COMPLEX:
								System.out.println("COMPLEX");
								if (fn.compareToIgnoreCase("complex")==0 ||
									(fn.compareToIgnoreCase("curve")==0 && ss.isCurve()) ||
									(fn.compareToIgnoreCase("arc")==0 && (ss.isArc() || ss.isCurve()))) {
									found = true;
								}
								break;
							case Classifier.ELLIPSE:
								System.out.println("ELLIPSE");
								if (fn.compareToIgnoreCase("ellipse")==0 ||
									fn.compareToIgnoreCase("circle")==0) {
									found = true;
								}
								break;
							case Classifier.LINE:
								System.out.println("LINE");
								if (fn.compareToIgnoreCase("line")==0) {
									found = true;
								}
								break;
							case Classifier.POINT:
								System.out.println("POINT");
								break;
							case Classifier.POLYGON:
								System.out.println("POLYGON");
								if (fn.compareToIgnoreCase("polyline")==0) {
									found = true;
								}
								break;
							case Classifier.SPIRAL:
								System.out.println("SPIRAL");
								if (fn.compareToIgnoreCase("spiral")==0 ||
									fn.compareToIgnoreCase("helix")==0) {
									found = true;
								}
								break;
							}
						}
						if (found) {
							sub++;
							alt++;
						}
						if (ss.isArc())
							System.out.println("Arc passed");
						if (ss.isCurve())
							System.out.println("Curve passed");
						if (ss.isEllipse())
							System.out.println("Ellipse passed");
						if (ss.isGeneralPath())
							System.out.println("GeneralPath passed");
						if (ss.isLine())
							System.out.println("Line passed");
						if (ss.isPolyline())
							System.out.println("Polyline passed");
						if (ss.isSpiral())
							System.out.println("Spiral passed");
						if (Spring.isSpring(ss.getPoly()))
							System.out.println("Spring passed");
						System.out.println("");
						}
						catch (Exception e) {}
					}
				}
				System.out.println("Total " + fn + "s: " + num);
				System.out.println("Correctness Accuracy: " + (top/num));
				System.out.println("Approx Accuracy: " + (sub/num));
				System.out.println("");
			}
			System.out.println("Total Shapes: " + total);
			System.out.println("Correctness Accuracy: " + (correct/total));
			System.out.println("Approx Accuracy: " + (alt/total));
			System.out.println("Recognition Time: " + sum + "ms");
		}

		/*if (runSezgin) {
		System.out.println("\nStart Sezgin recognizer...");
		total = 0;
		totalCorrect = 0;
		for (int i = 0; i < files.length; i++) {
			int num = 0;
			int correct = 0;
			filename = "../data/singlestroke/" + files[i] + "/" + files[i] + "-";
			shortFilename = files[i] + "-";
			tmpFilename = filename;
			tmpShortFilename = shortFilename;
			for (int j = 0; j <= numUsers; j++) {
				filename = tmpFilename;
				shortFilename = tmpShortFilename;
				filename += j + "-";
				shortFilename += j + "-";
				tmptmpFilename = filename;
				tmptmpShortFilename = shortFilename;
				for (int k = 0; k <= numExamples; k++) {
					filename = tmptmpFilename;
					shortFilename = tmptmpShortFilename;
					filename += k + ".txt";
					shortFilename += k + ".txt";
					File f = new File(filename);
					if (f.exists()) {
						BStroke s = StrokeReader.readStroke(filename, jitter);
						if (s != null) {
							Stroke ss = s.getOldStroke();
							ss.compute();
							num++;
							total++;
							Approximation[] app = ss.getClassifier().classifyAndRank();
							int type = app[0].getGeometricObject().getIntType();
							System.out.print(shortFilename + " makeup: ");
							switch(type) {
								case Classifier.ARC:
									System.out.println("ARC");
									if (files[i].compareToIgnoreCase("arc1")==0) {
										correct++;
										totalCorrect++;
									}
									break;
								case Classifier.COMPLEX:
									System.out.println("COMPLEX");
									if (files[i].compareToIgnoreCase("line2")==0 ||
										files[i].compareToIgnoreCase("line3")==0 ||
										files[i].compareToIgnoreCase("line4")==0 ||
										files[i].compareToIgnoreCase("line5")==0 ||
										files[i].compareToIgnoreCase("line6")==0) {
										correct++;
										totalCorrect++;
									}
									break;
								case Classifier.ELLIPSE:
									System.out.println("ELLIPSE");
									if (files[i].compareToIgnoreCase("ellipse")==0 ||
										files[i].compareToIgnoreCase("circle")==0) {
										correct++;
										totalCorrect++;
									}
									break;
								case Classifier.LINE:
									System.out.println("LINE");
									if (files[i].compareToIgnoreCase("line1")==0) {
										correct++;
										totalCorrect++;
									}
									break;
								case Classifier.POINT:
									System.out.println("POINT");
									break;
								case Classifier.POLYGON:
									System.out.println("POLYGON");
									if (files[i].compareToIgnoreCase("line2")==0 ||
										files[i].compareToIgnoreCase("line3")==0 ||
										files[i].compareToIgnoreCase("line4")==0 ||
										files[i].compareToIgnoreCase("line5")==0 ||
										files[i].compareToIgnoreCase("line6")==0) {
										correct++;
										totalCorrect++;
									}
									break;
								case Classifier.SPIRAL:
									System.out.println("SPIRAL");
									if (files[i].compareToIgnoreCase("spiral")==0 ||
										files[i].compareToIgnoreCase("helix")==0) {
										correct++;
										totalCorrect++;
									}
									break;
							}
						}
					}
				}
			}
			System.out.println("num of " + files[i] + " = " + num + "  num correct = " + correct);
			System.out.println("accuracy = " + (double)correct/num);
			System.out.println("");
		}
		System.out.println("total shapes = " + total + "  total correct = " + totalCorrect);
		System.out.println("total accuracy = " + (double)totalCorrect/total);
		}*/
	}
}
