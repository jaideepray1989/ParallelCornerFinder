package cornerfinders.impl.combination.objectivefuncs;

import java.util.List;
import cornerfinders.core.shapes.TStroke;

/**
 * Objective function for Feature Subset Selection
 * 
 * @author awolin
 */
public interface IObjectiveFunction {
	
	/**
	 * Solve the objective function
	 * 
	 * @param corners
	 *            Corner indices of the stroke
	 * @param stroke
	 *            Stroke to segment
	 * @return Value of the objective function
	 */
	public double solve(List<Integer> corners, TStroke stroke);
}
