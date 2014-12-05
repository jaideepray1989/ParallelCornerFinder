package cornerfinders.testutils;

import java.io.File;
import java.io.IOException;


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

					}
					}
				}


		}

