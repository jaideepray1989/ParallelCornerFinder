package cornerfinders.recognizers;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import Jama.Matrix;

/**
 * LeastSquares class - contains static methods used to conduct
 * least squares tests
 * @author bpaulson
 */
public class LeastSquares {

	/**
	 * Perform a least squares fit with the input values
	 * @param sumX sum of the x values
	 * @param sumX2 sum of the x values squared
	 * @param sumY sum of the y values
	 * @param sumXY sum of the x*y values
	 * @param n number of values
	 * @return 2x1 matrix containing the least squares fit y = a + bx; first value in matrix will
	 * contain the y-intercept, the second will contain the slope
	 */
	public static Matrix fit(double sumX, double sumX2, double sumY, double sumXY, int n) {
		Matrix A = new Matrix(2,2);
		Matrix b = new Matrix(2,1);
		A.set(0,0,n);
		A.set(1,0,sumX);
		A.set(0,1,sumX);
		A.set(1,1,sumX2);
		b.set(0,0,sumY);
		b.set(1,0,sumXY);
		return A.solve(b);
	}
	
	/**
	 * Perform a least squares fit with the input values and return a line within the given bounds
	 * @param sumX sum of the x values
	 * @param sumX2 sum of the x values squared
	 * @param sumY sum of the y values
	 * @param sumXY sum of the x*y values
	 * @param n number of values
	 * @param bounds rectanglular bounds of best fit line
	 * @return best fit line of the least squares fit y = a + bx
	 */
	public static Line2D bestFitLine(double sumX, double sumX2, double sumY, double sumXY, 
			int n, Rectangle2D bounds) {
		Matrix result = new Matrix(1,0);
		result = fit(sumX, sumX2, sumY, sumXY, n);
		double a = result.get(0,0);
		double b = result.get(1,0);
		double minX = bounds.getMinX();
		double maxX = bounds.getMaxX();
		double minY = a+b*minX;
		double maxY = a+b*maxX;
		return new Line2D.Double(minX,minY,maxX,maxY);
	}
	
	/**
	 * Return the total least squares error between the array of points and the input line
	 * @param x x values
	 * @param y y values
	 * @param line line to find the LSE to
	 * @return total least squares error between the input points and line
	 */
	public static double error(double[] x, double[] y, Line2D line) {
		double err = 0.0;
		for (int i = 0; i < x.length; i++)
			err += line.ptSegDist(x[i], y[i]);
		return err;
	}
	
	/**
	 * Return the total least squares error squared between the array of points and the input line
	 * @param x x values
	 * @param y y values
	 * @param line line to find the LSE to
	 * @return total least squares error squared between the input points and line
	 */
	public static double squaredError(double[] x, double[] y, Line2D line) {
		double err = 0.0;
		for (int i = 0; i < x.length; i++)
			err += line.ptSegDistSq(x[i], y[i]);
		return err;
	}
}
