package cornerfinders.recognizers;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.tamu.hammond.sketch.shapes.TStroke;

/**
 * StrokeReader class -
 * Used to read in stroke information from a file
 * that is in the format
 * [x0, y0, t0;
 * x1, y1, t1;
 * ...]
 * @author bpaulson
 */
public class StrokeReader {

	/**
	 * Static method used to return a stroke read in from a file
	 * @param filename name of the file with stroke information
	 * @param jitter determines whether jitter reduction should be on or off
	 * @return the stroke read in from the file
	 */
	public static ArrayList<BStroke> readStrokes(String filename) {
		/*BStroke s = new BStroke();
		String line, tmpStr;
		double x, y;
		long t;
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			while ((line = in.readLine()) != null && line.compareToIgnoreCase("")!=0) {
				// beginning of XML version
				// TODO parse XML versions of strokes
				if (line.charAt(0) == '<')
					return null;
				if (line.charAt(0) != ']') {
					if (line.charAt(0) == '[')
						line = line.substring(1);
					tmpStr = line.substring(0,line.indexOf(','));
					x = Double.parseDouble(tmpStr.trim());
					line = line.substring(line.indexOf(',')+1);
					tmpStr = line.substring(0,line.indexOf(','));
					y = Double.parseDouble(tmpStr.trim());
					line = line.substring(line.indexOf(',')+1);
					tmpStr = line.substring(0,line.indexOf(';'));
					t = Long.parseLong(tmpStr.trim());
					s.add(x,y,t);
				}
			}
		}
		catch (Exception e) {
			System.out.println("Error reading stroke from file: " + filename);
			System.out.println(e.getMessage());
		}
		return s;*/
		ArrayList<BStroke> bs = new ArrayList<BStroke>();
		List<TStroke> ts = TStroke.getTStrokesFromXML(new File(filename));
		for (TStroke s : ts)
			bs.add(new BStroke(s));
		return bs;
	}
}
