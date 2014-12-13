package cornerfinders.impl;

import java.util.ArrayList;
import java.util.List;

import cornerfinders.core.shapes.TStroke;
import utils.dbconnector.StrokeFetcher;

public class CornerFinderExecutor {

	private static AbstractCornerFinder[] allCornerFinders = new AbstractCornerFinder[] {
			new SezginCornerFinder(), new KimCornerFinder(),
			new ShortStrawCornerFinder() };

	public static void executeAllCornerFinders() {

		List<TStroke> strokes = StrokeFetcher.fetchAllStrokes();

		
		for (AbstractCornerFinder cornerFinder : allCornerFinders) {
			for (TStroke s : strokes) {
				ArrayList<Integer> corners = cornerFinder.findCorners(s);
				s = cornerFinder.getStroke();
				if (null == corners)
					continue;
				for (Integer index : corners) {
					System.out.println("printing corner :: ");
					s.getPoint(index).printPoint();
				}
			}
		}
		System.out.println("Size: " + strokes.size());
	}

	public static void main(String[] args) {
		executeAllCornerFinders();
	}

}
