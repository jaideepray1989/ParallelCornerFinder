package cornerfinders.impl;

import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;

import java.util.ArrayList;



/**
 * Brandon's corner finder
 * @author bpaulson
 */
public class BrandonCornerFinder extends CornerFinder {

	private boolean debug = false;

	@Override
	public ArrayList<Integer> findCorners(TStroke s) {
		stroke = s;
		pts = s.getPoints();
		ArrayList<Integer> c;
        BStroke bs = new BStroke(stroke);
		bs.calcFeatures();
		c = bs.getFeatures().getBrandonCornersInt();
		if (debug) {
			for (Integer i : c)
				System.out.println("orig corner: " + i);
		}
		ArrayList<TPoint> crns = bs.getFeatures().getBrandonCorners();
		if (debug) {
			System.out.println("c size = " + crns.size());
		}
		c = new ArrayList<Integer>();
		c.add(0);
		for (int i = 1; i < crns.size()-1; i++) {
			boolean found = false;
			TPoint curr = crns.get(i);
			for (int j = 0; j < stroke.numPoints() && !found; j++) {
				TPoint p = stroke.getPoint(j);
				if (curr.getX()==p.getX() && curr.getY()==p.getY() &&
						curr.getTime()==p.getTime()) {
					c.add(j);
					found = true;
				}
			}
		}
		c.add(stroke.numPoints()-1);
		if (debug) {
			System.out.println("new size = " + c.size());
		}
		return c;
	}
}
