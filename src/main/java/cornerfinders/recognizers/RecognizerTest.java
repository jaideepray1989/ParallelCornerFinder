package cornerfinders.recognizers;
import java.io.File;
import java.util.ArrayList;

import edu.mit.sketch.geom.Approximation;
import edu.mit.sketch.language.shapes.Stroke;
import edu.mit.sketch.toolkit.Classifier;

/**
 * RecognizerTest class -
 * Used to recognize shapes based on geometric properties
 * @author bpaulson
 */
public class RecognizerTest {

	//public static String[] dirs = {"arc1", "circle", "ellipse", "helix", "line1", "line2", "line3",
	//	"line4", "line5", "line6", "spiral"};
	public static String[] dirs = {"arc1", "circle", "ellipse", "helix", "line1", "line2", "line3",
			"line4", "line5", "spiral"};
	//public static String[] dirs = {"test"};
	public static int numUsers = 20;
	public static int numExamples = 10;
	private static boolean runBPaulson = true;
	private static boolean runSezgin = false;

	/**
	 * Main program
	 * @param args currently not needed
	 */
	public static void main(String args[]) {

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
		if (runBPaulson){
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
						BStroke s = StrokeReader.readStrokes(filename).get(0);
						if (s != null) {
							System.out.print(shortFilename + " makeup: ");
							ArrayList<Fit> fits = s.recognize(true);
							if (fits.size() == 0)
								System.out.println(" - ");
							for (int n = 0; n < fits.size(); n++) {
								String name = fits.get(n).getName();
								String fn = files[i];
								System.out.println(name + " representation");
								if (n == 0) {
									if ((fn.compareToIgnoreCase("line1")==0 && name.compareToIgnoreCase(Fit.LINE)==0) ||
										(fn.compareToIgnoreCase("arc1")==0 && name.compareToIgnoreCase(Fit.ARC)==0) ||
										(fn.compareToIgnoreCase("circle")==0 && name.compareToIgnoreCase(Fit.CIRCLE)==0) ||
										(fn.compareToIgnoreCase("ellipse")==0 && name.compareToIgnoreCase(Fit.ELLIPSE)==0) ||
										(fn.compareToIgnoreCase("helix")==0 && name.compareToIgnoreCase(Fit.HELIX)==0) ||
										(fn.compareToIgnoreCase("spiral")==0 && name.compareToIgnoreCase(Fit.SPIRAL)==0) ||
										(fn.substring(0,4).compareToIgnoreCase("line")==0 && fn.charAt(4)!='1' &&
										name.compareToIgnoreCase(Fit.POLYLINE)==0)) {
										correct++;
										totalCorrect++;
									}
								}
							}
							num++;
							total++;
							/*Vector<String> makeup = s.strMakeup();
							for (int n = 0; n < makeup.size(); n++)
								System.out.println(makeup.elementAt(n));
							if ((makeup.size() == 1 && makeup.elementAt(0).substring(0, makeup.elementAt(0).indexOf(' '))
									.compareToIgnoreCase(files[i])==0) ||
									(makeup.elementAt(0).substring(0, makeup.elementAt(0).indexOf(' '))
											.compareToIgnoreCase("POLYLINE")==0
											&& files[i].substring(0, 4).compareToIgnoreCase("line")==0)) {
								correct++;
								totalCorrect++;
							}*/
							System.out.println("");
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
		}

		// Sezgin recognizer
		if (runSezgin) {
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
						BStroke s = StrokeReader.readStrokes(filename).get(0);
						if (s != null) {
							Stroke ss = s.getOldStroke();
							ss.compute();
							num++;
							total++;
							Approximation[] app = ss.getClassifier().classifyAndRank(true);
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
		}
	}
}
