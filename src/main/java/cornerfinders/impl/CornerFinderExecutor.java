package cornerfinders.impl;

import java.util.ArrayList;
import java.util.List;

import cornerfinders.core.shapes.TStroke;
import utils.dbconnector.StrokeFetcher;

public class CornerFinderExecutor {

	private static CornerFinder[] allCornerFinders = new CornerFinder[] {
			new SezginCornerFinder(), new KimCornerFinder(),
			new ShortStrawCornerFinder() };

	public static void executeAllCornerFinders() {

		List<TStroke> strokes = StrokeFetcher.fetchAllStrokes();

		for (CornerFinder cornerFinder : allCornerFinders) {
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
	}

	public static void main(String[] args) {
		executeAllCornerFinders();
	}

}
