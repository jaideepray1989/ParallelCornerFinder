package cornerfinders.recognizers;

import java.awt.Shape;
import java.util.ArrayList;

import edu.mit.sketch.language.shapes.DrawnShape;
import edu.tamu.hammond.sketch.shapes.TArc;
import edu.tamu.hammond.sketch.shapes.TComplex;
import edu.tamu.hammond.sketch.shapes.TPrimitiveShape;

/**
 * ComplexFit class - fit a stroke to a complex fit
 * @author bpaulson
 */
public class ComplexFit implements Fit, StrokeThresholds, Cloneable {

	private double err;
	private boolean passed;
	private boolean debug = false;
	private ArrayList<Fit> subFits;
	private ArrayList<BStroke> strokes;
	private boolean bestFit = false;
	private Shape beautified;

	/**
	 * Constructor for complex fit
	 * @param ss arraylist of substroke
	 */
	public ComplexFit(ArrayList<BStroke> ss) {
		bestFit = true;
		passed = true;
		err = 0.0;
		subFits = new ArrayList<Fit>();
		strokes = new ArrayList<BStroke>();
		for (BStroke s : ss) {
			ArrayList<Fit> fits = s.recognize(false);
			Fit f = fits.get(0);
			subFits.add(f);
			strokes.add(s);
			err += f.getError();
		}
		beautified = ((TComplex)getShape()).get2DShape();
		if (debug) {
			System.out.print("ComplexFit: passed = " + passed + " error = " + err + " num subs: " + subFits.size() + " subFits: ");
			for (int i = 0; i < subFits.size()-1; i++) {
				System.out.print(subFits.get(i).getName() + " + ");
			}
			System.out.println(subFits.get(subFits.size()-1).getName());
		}
	}

	public ComplexFit() {
		bestFit = false;
		err = 0.0;
		passed = true;
		subFits = new ArrayList<Fit>();
	}

	public double getError() {
		return err;
	}

	public String getName() {
		return Fit.COMPLEX;
	}

	public DrawnShape getShape() {
		ArrayList<Shape> s = new ArrayList<Shape>();
		for (Fit f : subFits) {
			if (f.getShape() instanceof TPrimitiveShape)
				s.add(((TPrimitiveShape)f.getShape()).get2DShape());
		}
		TComplex tc = new TComplex(s);
		return tc;
	}

	public Shape get2DShape() {
		return beautified;
	}

	public boolean passed() {
		return passed;
	}

	public ArrayList<Fit> getSubFits() {
		return subFits;
	}

	public ArrayList<BStroke> getStrokes() {
		return strokes;
	}

	public int numPrimitives() {
		if (subFits.size()==0)
			return 0;
		int num = 0;
		for (int i = 0; i < subFits.size(); i++) {
			if (subFits.get(i) instanceof PolylineFit) {
				PolylineFit pf = (PolylineFit)subFits.get(i);
				num += pf.getSubStrokes().size();
			}
			else
				num++;
		}
		return num;
	}

	public double percentLines() {
		if (subFits.size()==0)
			return 0;
		double num = 0;
		double numPrims = 0;
		for (int i = 0; i < subFits.size(); i++) {
			if (subFits.get(i) instanceof PolylineFit) {
				PolylineFit pf = (PolylineFit)subFits.get(i);
				num += pf.getSubStrokes().size();
				numPrims += pf.getSubStrokes().size();
			}
			else if (subFits.get(i) instanceof LineFit) {
				num++;
				numPrims++;
			}
			else
				numPrims++;
		}
		return num/numPrims;
	}

	public boolean didBest() {
		return bestFit;
	}

	public int getID() {
		return Fit.COMPLEX_INT;
	}
}
