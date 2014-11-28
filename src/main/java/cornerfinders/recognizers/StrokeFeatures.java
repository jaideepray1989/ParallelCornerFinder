/**
 * StrokeFeatures
 * tamu
 * StrokeFeatures.java
 * Created by hammond, Tracy Hammond
 * Created on Nov 18, 2006 2006 6:56:25 PM
 * Copyright Tracy Hammond, Texas A&M University, 2006
 */
package cornerfinders.recognizers;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import edu.mit.sketch.geom.Vertex;
import edu.mit.sketch.language.shapes.Stroke;
import edu.mit.sketch.toolkit.SimpleClassifier3;
import edu.tamu.awolin.cornerFinder.CornerFinder;
import edu.tamu.hammond.io.Plot;
import edu.tamu.hammond.sketch.shapes.TPoint;
import edu.tamu.hammond.sketch.shapes.TStroke;

/**
 * @author hammond
 *
 */
public class StrokeFeatures implements StrokeThresholds {
	public boolean m_isEmpty = true;
	public boolean m_isPoint = true;
	public int m_size;
	public double[] m_origX;
	public double[] m_origY;
	public long[] m_origT;
	public double[] m_x;
	public double[] m_y;
	public long[] m_time;
	public double[] m_speed;
	public double[] m_dtime;
	public double[] m_dx;
	public double[] m_dy;
	public double[] m_dir;
	public double[] m_segLength;
	public double[] m_2segLength;
	public double[] m_lengthSoFar;
	public double[] m_accumulatedTime;
	public double[] m_acceleration;
	public double[] m_curvature;
	public double[] m_curvNoAbs;
	public double m_max_curv;
	public int m_max_curv_index;
	public double[] m_totalCurvature;
	public double m_strokelength;
	public double m_stroketime;
	public double m_averageSpeed;
	public double m_maxSpeed;
	public double[] m_inverseSpeed;
	public List<Integer> m_curvatureCorners;
	public List<Integer> m_speedCorners;
	public List<Integer> m_brandonCorners;
	public List<Integer> m_tracyCorners;
	public List<Integer> m_sezginCorners;
	public double m_averageCurvature;
	public double[] m_2lengthSoFar;
	public double[] m_xcurvCorner;
	public double[] m_ycurvCorner;
	public double[] m_xspeedCorner;
	public double[] m_yspeedCorner;
	public double[] m_xbrandonCorner;
	public double[] m_ybrandonCorner;
	public double[] m_xTracyCorner;
	public double[] m_yTracyCorner;
	public List<Integer> m_finalCorner = new ArrayList<Integer>();
	public double[] m_xfinalCorner;
	public double[] m_yfinalCorner;

	public void interpolate() {
		double[] newx = new double[m_size];
		double[] newy = new double[m_size];
		long[] newt = new long[m_size];
		newx[0] = m_x[0];
		newy[0] = m_y[0];
		newt[0] = m_time[0];
		int count = 1;
		for (int i = 0; i < m_x.length - 1; i++) {
			newx[count] = (m_x[i] + m_x[i + 1]) / 2;
			newy[count] = (m_y[i] + m_y[i + 1]) / 2;
			newt[count] = (m_time[i] + m_time[i + 1]) / 2;
			count++;
		}
	}

	public StrokeFeatures(double[] x, double[] y, long[] time) {
		m_origX = x;
		m_origY = y;
		m_origT = time;
		m_x = x.clone();
		m_y = y.clone();
		m_time = time.clone();
		m_size = m_x.length;
		// debug(1, false, true);
		removeDuplicates();
		if (m_size == 0) {
			m_isEmpty = true;
			return;
		}
		m_isEmpty = false;
		if (m_size == 1) {
			m_isPoint = true;
			return;
		}
		m_isPoint = false;
		/*
		 * System.out.println("begin interpolate"); while(m_size <
		 * 4){interpolate();} System.out.println("stop interpolate");
		 */
		computeValues();
		removeHooks();
		computeValues();
		m_curvatureCorners = computePeakPoints(m_averageCurvature * 1.2,
				m_curvature);
		m_speedCorners = computePeakPoints(m_maxSpeed - m_averageSpeed,
				m_inverseSpeed);
		m_curvatureCorners.set(m_curvatureCorners.size() - 1, m_size - 1);
		m_speedCorners.set(m_speedCorners.size() - 1, m_size - 1);
		m_xcurvCorner = new double[m_curvatureCorners.size()];
		m_ycurvCorner = new double[m_curvatureCorners.size()];
		m_xspeedCorner = new double[m_speedCorners.size()];
		m_yspeedCorner = new double[m_speedCorners.size()];
		for (int i = 0; i < m_curvatureCorners.size(); i++) {
			m_xcurvCorner[i] = m_x[m_curvatureCorners.get(i)];
			m_ycurvCorner[i] = m_y[m_curvatureCorners.get(i)];
		}
		for (int i = 0; i < m_speedCorners.size(); i++) {
			m_xspeedCorner[i] = m_x[m_speedCorners.get(i)];
			m_yspeedCorner[i] = m_y[m_speedCorners.get(i)];
		}
		for (int i = 0; i < m_curvatureCorners.size(); i++) {
			double smallestdistance = 100;
			double x1 = m_xcurvCorner[i];
			double y1 = m_ycurvCorner[i];
			for (int j = 0; j < m_speedCorners.size(); j++) {
				double x2 = m_xspeedCorner[j];
				double y2 = m_yspeedCorner[j];
				double distance = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1)
						* (y2 - y1));
				smallestdistance = Math.min(smallestdistance, distance);
			}
			double lastdistance = 100;
			if (m_finalCorner.size() > 0) {
				int lastindex = m_finalCorner.get(m_finalCorner.size() - 1);
				double x2 = m_x[lastindex];
				double y2 = m_y[lastindex];
				lastdistance = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1)
						* (y2 - y1));
			}
			if ((i == m_curvatureCorners.size() - 1)
					|| (smallestdistance < M_CORNER_MAXDISTANCETOSPEED && lastdistance > M_CORNER_MAXDISTANCETOADJACENT)) {
				m_finalCorner.add(m_curvatureCorners.get(i));
			}
		}
		for (int index = m_finalCorner.size() - 2; index > 0; index--) {
			int i1 = m_finalCorner.get(index);
			int i0 = m_finalCorner.get(index - 1);
			int i2 = m_finalCorner.get(index + 1);
			double dir1 = Math.atan2(m_y[i1] - m_y[i0], m_x[i1] - m_x[i0]);
			double dir2 = Math.atan2(m_y[i2] - m_y[i1], m_x[i2] - m_x[i1]);
			double diff = Math.abs(dir1 - dir2);
			while (diff > 2 * Math.PI) {
				diff -= 2 * Math.PI;
			}
			if (diff < M_CORNER_MINANGLEDIFF
					|| diff > 2 * Math.PI - M_CORNER_MINANGLEDIFF) {
				m_finalCorner.remove(index);
			}
		}
		m_xfinalCorner = new double[m_finalCorner.size()];
		m_yfinalCorner = new double[m_finalCorner.size()];
		int count = 0;
		for (Integer i : m_finalCorner) {
			m_xfinalCorner[count] = m_x[i];
			m_yfinalCorner[count] = m_y[i];
			count++;
		}
		// debug(4, true, true);

		// New stuff added by bpaulson
		calcBounds();
		calcTotalRotation();
		calcNDDE();
		calcDCR();
		calcBestFitLineForDirGraph();
		calcBestFitLine();
		calcNonAbsCurvRatio();
		calcDirWindowPassed();
		// calcSlopeDiff();
		// calcMidSlopeDiff();
		// calcBrandonCorners();
		// calcDirectionChange();
	}

	private void computeValues() {
		m_dx = new double[m_size - 1];
		m_dy = new double[m_size - 1];
		m_dtime = new double[m_size - 1];
		m_dir = new double[m_size - 1];
		m_segLength = new double[m_size - 1];
		m_lengthSoFar = new double[m_size - 1];
		m_accumulatedTime = new double[m_size - 1];
		m_speed = new double[m_size - 1];
		m_maxSpeed = 0;

		for (int i = 0; i < m_size - 1; i++) {
			m_dx[i] = m_x[i + 1] - m_x[i];
			m_dy[i] = m_y[i + 1] - m_y[i];
			m_dtime[i] = m_time[i + 1] - m_time[i];
			if (i == 0) {
				m_accumulatedTime[i] = m_dtime[i];
			} else {
				m_accumulatedTime[i] = m_accumulatedTime[i - 1] + m_dtime[i];
			}
			m_dir[i] = Math.atan2(m_dy[i], m_dx[i]);
			while ((i > 0) && (m_dir[i] - m_dir[i - 1] > Math.PI)) {
				// SHOULD TRY A WINDOW OF 5 (2 on each side) - LIKE SEZGIN -
				// THEN TAKE BEST FIT LINE FOR DIR
				m_dir[i] = m_dir[i] - 2 * Math.PI;
			}
			while ((i > 0) && (m_dir[i - 1] - m_dir[i] > Math.PI)) {
				m_dir[i] = m_dir[i] + 2 * Math.PI;
			}
			m_segLength[i] = Math.sqrt(m_dy[i] * m_dy[i] + m_dx[i] * m_dx[i]);
			m_speed[i] = m_segLength[i] / m_dtime[i];
			if (i == 0) {
				m_lengthSoFar[i] = m_segLength[i];
			} else {
				m_lengthSoFar[i] = m_lengthSoFar[i - 1] + m_segLength[i];
			}
			m_maxSpeed = Math.max(m_maxSpeed, m_speed[i]);
		}
		m_inverseSpeed = new double[m_size - 1];
		for (int i = 0; i < m_size - 1; i++) {
			m_inverseSpeed[i] = m_maxSpeed - m_speed[i];
		}
		m_strokelength = m_lengthSoFar[m_lengthSoFar.length - 1];
		m_stroketime = m_accumulatedTime[m_accumulatedTime.length - 1];
		m_averageSpeed = m_strokelength / m_stroketime;
		m_acceleration = new double[m_size - 2];
		m_curvature = new double[m_size - 2];
		m_curvNoAbs = new double[m_size - 2];
		m_totalCurvature = new double[m_size - 2];
		m_2segLength = new double[m_size - 2];
		m_2lengthSoFar = new double[m_size - 2];
		m_max_curv = Double.MIN_VALUE;
		m_max_curv_index = 0;
		for (int i = 0; i < m_size - 2; i++) {
			m_2lengthSoFar[i] = m_lengthSoFar[i + 1];
			m_2segLength[i] = m_segLength[i] + m_segLength[i + 1];
			m_acceleration[i] = (m_speed[i + 1] - m_speed[i])
					/ (m_dtime[i] + m_dtime[i + 1]);
			m_curvature[i] = Math.abs(m_dir[i + 1] - m_dir[i])
					/ (m_2segLength[i]);
			m_curvNoAbs[i] = (m_dir[i + 1] - m_dir[i]) / (m_2segLength[i]);
			if (Math.abs(m_curvature[i]) > m_max_curv) {
				m_max_curv = Math.abs(m_curvature[i]);
				m_max_curv_index = i;
			}
			if (i == 0) {
				m_totalCurvature[i] = m_curvature[i];
			} else {
				m_totalCurvature[i] = m_totalCurvature[i - 1] + m_curvature[i];
			}
		}
		if (m_totalCurvature.length > 0)
			m_averageCurvature = m_totalCurvature[m_totalCurvature.length - 1]
					/ (m_totalCurvature.length);
	}

	/**
	 * Removes immediately subsequent points in the same location. (i.e., if the
	 * mouse hasn't moved, it doesn't create a new point) IIf two points have
	 * the same time stamp, the time on the first is changed to be interpolated
	 * between the two surrounding points.
	 */
	private void removeDuplicates() {
		double[] newx = new double[m_size];
		double[] newy = new double[m_size];
		long[] newt = new long[m_size];
		newx[0] = m_x[0];
		newy[0] = m_y[0];
		newt[0] = m_time[0];
		int count = 1;
		for (int i = 1; i < m_size; i++) {
			if (m_x[i - 1] == m_x[i] && m_y[i - 1] == m_y[i]) {
				// if same location or same time, delete second point.
			} else {
				newx[count] = m_x[i];
				newy[count] = m_y[i];
				newt[count] = m_time[i];
				if (newt[i - 1] == newt[i]) {
					// if same time, interpolate
					if (i == 1) {
						newt[count] = newt[count] + 1;
					} else {
						newt[count - 1] = (newt[i] + newt[i - 2]) / 2;
					}
				}
				count++;
			}
		}
		m_x = new double[count];
		m_y = new double[count];
		m_time = new long[count];
		for (int i = 0; i < count; i++) {
			m_x[i] = newx[i];
			m_y[i] = newy[i];
			m_time[i] = newt[i];
		}
		m_size = m_x.length;
	}

	private void removeHooks() {
		if (m_totalCurvature.length == 0)
			return;
		if (m_size < M_HOOK_MINPOINTS) {
			return;
		}
		if (m_lengthSoFar[m_size - 2] < M_HOOK_MINSTROKELENGTH) {
			return;
		}
		if (m_strokelength < M_HOOK_MINSTROKELENGTH) {
			return;
		}
		double hookcurvature = 0;
		int startindex = 0;
		for (int i = 1; i < m_size - 1; i++) {
			if (m_lengthSoFar[i] > M_HOOK_MAXHOOKLENGTH) {
				break;
			}
			if (m_lengthSoFar[i] / m_strokelength > M_HOOK_MAXHOOKPERCENT) {
				break;
			}
			if (Math.abs(m_totalCurvature[i]) > hookcurvature) {
				hookcurvature = Math.abs(m_totalCurvature[i]);
				startindex = i + 1;
			}
		}
		if (hookcurvature < M_HOOK_MINHOOKCURVATURE) {
			startindex = 0;
		}
		hookcurvature = 0;
		int endindex = m_size;
		for (int i = 1; i < m_size - 1; i++) {

			int startIndex = m_totalCurvature.length - 1 - i;
			if (startIndex < 0)
				startIndex = 0;

			double c = m_totalCurvature[m_totalCurvature.length - 1]
					- m_totalCurvature[startIndex];
			double l = m_lengthSoFar[m_lengthSoFar.length - 1]
					- m_lengthSoFar[m_lengthSoFar.length - 1 - i];
			if (l > M_HOOK_MAXHOOKLENGTH) {
				break;
			}
			if (l / m_strokelength > M_HOOK_MAXHOOKPERCENT) {
				break;
			}
			if (Math.abs(c) > hookcurvature) {
				hookcurvature = Math.abs(c);
				endindex = m_size - i;
			}
		}
		if (Math.abs(hookcurvature) < M_HOOK_MINHOOKCURVATURE) {
			endindex = m_size;
		}

		m_size = endindex - startindex;
		double[] newx = new double[m_size];
		double[] newy = new double[m_size];
		long[] newtime = new long[m_size];
		for (int i = startindex; i < endindex; i++) {
			newx[i - startindex] = m_x[i];
			newy[i - startindex] = m_y[i];
			newtime[i - startindex] = m_time[i];
		}
		m_x = newx;
		m_y = newy;
		m_time = newtime;
	}


	public void debug(int level, boolean plotit, boolean printit) {
		if (level == 0) {
			if (printit) {
				TracyPolyLineParse p = new TracyPolyLineParse(this, true);
			}
		}
		if (level == 1) {
			if (printit) {
				System.out.println("Points");
				for (int i = 0; i < m_size; i++) {
					System.out.println(i + ": x=" + m_x[i] + ",y=" + m_y[i]
							+ ",t=" + m_time[i]);
				}
			}
			if (plotit) {
				Plot plot = new Plot("Stroke");
				plot.addLine(m_x, m_y, Color.red, 10);
				plot.setKeepdim(true);
				plot.plot();
			}
		}
		if (level == 2) {
			if (printit) {
				System.out.println("First Derivative");
				for (int i = 0; i < m_size - 1; i++) {
					System.out.println(i + ": dx=" + m_dx[i] + ",dy=" + m_dy[i]
							+ ",dt=" + m_dtime[i] + ",dir=" + m_dir[i]
							+ ",l = " + m_segLength[i] + ",tl="
							+ m_lengthSoFar[i]);
				}
			}
			if (plotit) {
				Plot plot = new Plot("Direction");
				plot.addLine(m_lengthSoFar, m_dir, Color.green, 10);
				plot.plot();
				plot = new Plot("Speed");
				plot.addLine(m_lengthSoFar, m_speed, Color.blue, 10);
				plot.plot();
			}
		}
		if (level == 3) {
			if (printit) {
				System.out.println("Second Derivative");
				for (int i = 0; i < m_size - 2; i++) {
					System.out.println(i + ": acc=" + m_acceleration[i]
							+ ",curv=" + m_curvature[i] + ",tcurv="
							+ m_totalCurvature[i]);
				}
			}
			if (plotit) {
				Plot plot = new Plot("Curvature");
				plot.addLine(m_2lengthSoFar, m_curvature, Color.yellow, 10);
				plot.plot();
				plot = new Plot("Non-absolute Curvature");
				plot.addLine(m_2lengthSoFar, m_curvNoAbs, Color.magenta, 10);
				plot.plot();
				plot = new Plot("Acceleration");
				plot.addLine(m_2lengthSoFar, m_acceleration, Color.orange, 10);
				plot.plot();
			}
		}
		if (level == 4) {
			if (printit) {
				for (int i = 0; i < m_xcurvCorner.length; i++) {
					System.out.println(i + ": curve corner " + m_xcurvCorner[i]
							+ "," + m_ycurvCorner[i]);
				}
				for (int i = 0; i < m_xspeedCorner.length; i++) {
					System.out.println(i + ": speed corner "
							+ m_xspeedCorner[i] + "," + m_yspeedCorner[i]);
				}
				for (int i = 0; i < m_xfinalCorner.length; i++) {
					System.out.println(i + ": final corner "
							+ m_xfinalCorner[i] + "," + m_yfinalCorner[i]);
				}
			}
			if (plotit) {
				Plot plot = new Plot("Curvature Corners");
				plot.addLine(m_x, m_y, Color.yellow, 10);
				plot.addLine(m_xcurvCorner, m_ycurvCorner, Color.black, 10);
				plot.setKeepdim(true);
				plot.plot();
				plot = new Plot("Speed Corners");
				plot.addLine(m_x, m_y, Color.orange, 10);
				plot.addLine(m_xspeedCorner, m_yspeedCorner, Color.black, 10);
				plot.setKeepdim(true);
				plot.plot();
				plot = new Plot("Final Corners");
				plot.addLine(m_x, m_y, Color.red, 10);
				plot.addLine(m_xfinalCorner, m_yfinalCorner, Color.black, 10);
				plot.setKeepdim(true);
				plot.plot();
				calcBrandonCorners();
				plot = new Plot("Brandon Corners");
				plot.addLine(m_x, m_y, Color.red, 10);
				plot.addLine(m_xbrandonCorner, m_ybrandonCorner, Color.black,
						10);
				plot.setKeepdim(true);
				plot.plot();
				TracyPolyLineParse p = new TracyPolyLineParse(this, false);
				plot = new Plot("Tracy Corners");
				plot.addLine(m_x, m_y, Color.cyan, 10);
				plot.addLine(m_xTracyCorner, m_yTracyCorner, Color.black, 10);
				plot.setKeepdim(true);
				plot.plot();
			}
		}
	}

	public List<Integer> computePeakPoints(double threshold,
			double[] changearray) {
		List<Integer> corners = new ArrayList<Integer>();
		boolean wasabove = true;
		corners.add(0);
		for (int i = 0; i < changearray.length; i++) {
			if (changearray[i] > threshold) {
				if (wasabove) {
					if (changearray[i] > changearray[corners
							.get(corners.size() - 1)]) {
						corners.set(corners.size() - 1, i + 1);
					}
				} else {
					corners.add(i + 1);
					wasabove = true;
				}
			} else {
				wasabove = false;
			}
		}
		corners.set(0, 0);
		if (wasabove) {
			corners.set(corners.size() - 1, changearray.length - 1);
		} else {
			corners.add(changearray.length - 1);
		}
		return corners;
	}

	public List<Integer> getFinalCorners() {
		return m_finalCorner;
	}

	public double[] getXFinalCorners() {
		return m_xfinalCorner;
	}

	public double[] getYFinalCorners() {
		return m_yfinalCorner;
	}

	// getPolylineInterpretation
	// getComboInterpretation
	// getPolylineError
	// getCircleInterpretation
	// getCircleError

	/***************************************************************************
	 * bpaulson New features
	 **************************************************************************/

	private double m_endPtStrokeLengthRatio;
	private double m_totalRotation;
	private double m_numRevolutions; // number of 2PI revolutions
	private double m_NDDE; // normalized distance between direction extremes
	private double m_DCR; // direction change ratio (max direction change/avg
							// direction change)
	private boolean m_overtraced;
	private boolean m_complete; // is shape close to being complete (closed
								// figure)
	private double m_maxX;
	private double m_maxY;
	private double m_minX;
	private double m_minY;
	private double m_minDir;
	private double m_maxDir;
	private double m_slopeDirGraph;
	private Line2D m_bestFitDirGraph;
	// private double m_bestFitDirGraphErr;
	private Line2D m_bestFitLine;
	private Rectangle2D m_bounds; // bounds for the stroke
	private Rectangle2D m_invbounds; // inverse bounds for stroke
	private ArrayList<BStroke> m_2pi_substrokes; // substrokes (broken at 2PI
													// intervals)
	private ArrayList<BStroke> m_corner_substrokes; // substrokes (broken at
													// corners)
	private ArrayList<BStroke> m_curv_corner_substrokes; // substrokes
															// (broken at
															// curvature
															// corners)
	private ArrayList<BStroke> m_brandon_corner_substrokes;
	private ArrayList<BStroke> m_best_substrokes;
	private ArrayList<BStroke> m_tracy_corner_substrokes;
	private double m_nonAbsCurvRatio;
	private boolean m_dirWindowPassed;
	private double m_slopeDiff;
	private double m_midSlopeDiff;

	// private double m_largestDirectionChange;

	private void calcTotalRotation() {
		double sum = 0;
		double deltaX, deltaY, deltaX1, deltaY1;
		for (int i = 1; i < m_x.length - 1; i++) {
			deltaX = m_x[i + 1] - m_x[i];
			deltaY = m_y[i + 1] - m_y[i];
			deltaX1 = m_x[i] - m_x[i - 1];
			deltaY1 = m_y[i] - m_y[i - 1];
			if (deltaX * deltaX1 + deltaY * deltaY1 == 0) {
				if (deltaX * deltaY1 - deltaX1 * deltaY < 0) {
					sum += Math.PI / -2.0;
				} else if (deltaX * deltaY1 - deltaX1 * deltaY > 0) {
					sum += Math.PI / 2.0;
				}
			} else
				sum += Math.atan2((deltaX * deltaY1 - deltaX1 * deltaY),
						(deltaX * deltaX1 + deltaY * deltaY1));
		}
		m_totalRotation = sum;
		// System.out.println("TOTAL ROTATION: " + m_totalRotation);
		m_numRevolutions = Math.abs(m_totalRotation) / (Math.PI * 2.0);
		if (m_numRevolutions < .0000001)
			m_numRevolutions = 0.0;
		// System.out.println("REVS: " + m_numRevolutions);
		if (m_numRevolutions >= M_REVS_TO_BE_OVERTRACED) {
			m_overtraced = true;
		} else {
			m_overtraced = false;
		}
		m_endPtStrokeLengthRatio = Point2D.distance(m_x[0], m_y[0],
				m_x[m_x.length - 1], m_y[m_y.length - 1])
				/ m_strokelength;
		if (m_endPtStrokeLengthRatio <= M_PERCENT_DISTANCE_TO_BE_COMPLETE
				&& m_numRevolutions >= M_NUM_REVS_TO_BE_COMPLETE)
			m_complete = true;
		else
			m_complete = false;
	}

	public double getEndptStrokeLengthRatio() {
		return m_endPtStrokeLengthRatio;
	}

	private void calcNDDE() {
		int maxIndex = 0, minIndex = 0;
		if (m_dir.length == 0)
			return;
		m_maxDir = m_dir[0];
		m_minDir = m_dir[0];
		for (int i = 1; i < m_dir.length; i++) {
			if (m_dir[i] > m_maxDir) {
				m_maxDir = m_dir[i];
				maxIndex = i;
			}
			if (m_dir[i] < m_minDir) {
				m_minDir = m_dir[i];
				minIndex = i;
			}
		}
		if (m_lengthSoFar[maxIndex] > m_lengthSoFar[minIndex])
			m_NDDE = (m_lengthSoFar[maxIndex] - m_lengthSoFar[minIndex])
					/ m_strokelength;
		else
			m_NDDE = (m_lengthSoFar[minIndex] - m_lengthSoFar[maxIndex])
					/ m_strokelength;
	}

	private void calcDCR() {
		double maxDC = Double.MIN_VALUE;
		double sum = 0.0;
		int start = (int) (m_dir.length * .05); // ignore 5% at ends (to avoid
												// tails)
		int end = m_dir.length - start;
		int i;
		for (i = start; i < end - 1; i++) {
			double dc = Math.abs(m_dir[i] - m_dir[i + 1]);
			if (dc >= maxDC)
				maxDC = dc;
			sum += dc;
		}
		double avgDC = sum / i;
		m_DCR = maxDC / avgDC;
	}

	public void calcBounds() {
		if (m_x.length == 0 || m_y.length == 0)
			return;
		m_maxX = m_x[0];
		m_minX = m_x[0];
		m_maxY = m_y[0];
		m_minY = m_y[0];
		for (int i = 1; i < m_x.length; i++) {
			if (m_x[i] > m_maxX)
				m_maxX = m_x[i];
			if (m_x[i] < m_minX)
				m_minX = m_x[i];
			if (m_y[i] > m_maxY)
				m_maxY = m_y[i];
			if (m_y[i] < m_minY)
				m_minY = m_y[i];
		}
		m_bounds = new Rectangle2D.Double(m_minX, m_minY, m_maxX - m_minX,
				m_maxY - m_minY);
		m_invbounds = new Rectangle2D.Double(m_minY, m_minX, m_maxY - m_minY,
				m_maxX - m_minX);
	}

	private void calcBestFitLineForDirGraph() {
		double sx = 0, sx2 = 0, sy = 0, sxy = 0;
		double[] x = new double[m_dir.length];
		for (int i = 0; i < m_dir.length; i++) {
			sx += i;
			sx2 += (i * i);
			sy += m_dir[i];
			sxy += (i * m_dir[i]);
			x[i] = i;
		}
		Rectangle2D bounds = new Rectangle2D.Double(0, m_minDir, m_dir.length,
				m_maxDir);
		try {
			m_bestFitDirGraph = LeastSquares.bestFitLine(sx, sx2, sy, sxy,
					m_dir.length, bounds);
			// m_bestFitDirGraphErr = LeastSquares.error(x, m_dir,
			// m_bestFitDirGraph)/m_strokelength;
			m_slopeDirGraph = (m_bestFitDirGraph.getY2() - m_bestFitDirGraph
					.getY1())
					/ (m_bestFitDirGraph.getX2() - m_bestFitDirGraph.getX1());
		} catch (Exception e) {
		}
	}

	private void calcBestFitLine() {
		double sx = 0, sx2 = 0, sy = 0, sy2 = 0, sxy = 0;
		for (int i = 0; i < m_x.length; i++) {
			sx += m_x[i];
			sx2 += Math.pow(m_x[i], 2);
			sy += m_y[i];
			sy2 += Math.pow(m_y[i], 2);
			sxy += m_x[i] * m_y[i];
		}
		Line2D l1 = new Line2D.Double();
		Line2D l2 = new Line2D.Double();
		double err1 = Double.MAX_VALUE;
		double err2 = Double.MAX_VALUE;
		try {
			l1 = LeastSquares.bestFitLine(sx, sx2, sy, sxy, m_x.length,
					getBounds());
			err1 = LeastSquares.error(m_x, m_y, l1);
		} catch (Exception e) {
		}
		try {
			l2 = LeastSquares.bestFitLine(sy, sy2, sx, sxy, m_x.length,
					getBounds());
			err2 = LeastSquares.error(m_x, m_y, l2);
		} catch (Exception e) {
		}
		if (err1 < err2)
			m_bestFitLine = l1;
		else
			m_bestFitLine = l2;
	}

	private void calcSubStrokes() {
		// calc 2pi substrokes (stroke broken at every 2pi interval)
		boolean increasing = false;
		double startDir = m_dir[0];
		double midDir = m_dir[m_dir.length / 2];
		if (midDir > startDir)
			increasing = true;
		m_2pi_substrokes = new ArrayList<BStroke>();
		BStroke s = new BStroke();
		for (int i = 0; i < m_dir.length; i++) {
			if (increasing && m_dir[i] > startDir + (2 * Math.PI)) {
				s.calcFeatures();
				m_2pi_substrokes.add(s);
				s = new BStroke();
				startDir += Math.PI * 2;
			} else if (!increasing && m_dir[i] < startDir - (2 * Math.PI)) {
				s.calcFeatures();
				m_2pi_substrokes.add(s);
				s = new BStroke();
				startDir -= Math.PI * 2;
			} else
				s.add(m_x[i], m_y[i], m_time[i]);
		}
		if (s.size() > 2) {
			s.calcFeatures();
			m_2pi_substrokes.add(s);
		}
	}

	private void calcCornerSubStrokes(boolean useSezgin) {
		if (!useSezgin) {
			m_corner_substrokes = new ArrayList<BStroke>();
			if (m_finalCorner.size() <= 0)
				return;
			int start = m_finalCorner.get(0);
			int end;
			for (int i = 1; i < m_finalCorner.size(); i++) {
				end = m_finalCorner.get(i);
				BStroke s = getSubStroke(start, end);
				s.calcFeatures();
				m_corner_substrokes.add(s);
				start = end;
			}
		} else {
			m_corner_substrokes = new ArrayList<BStroke>();
			if (m_x.length <= 0)
				return;
			Stroke s = new Stroke(new TPoint(m_x[0], m_y[0], m_time[0]));
			for (int i = 1; i < m_x.length; i++)
				s.addPoint(new TPoint(m_x[i], m_y[i], m_time[i]));
			SimpleClassifier3 classifier = new SimpleClassifier3(s
					.getStrokeData());
			try {
				classifier.classify();
			} catch (Exception e) {
				return;
			}
			Vertex[] v = classifier.getFinalFit();
			if (v.length <= 0)
				return;
			int start = v[0].index;
			if (m_sezginCorners == null)
				m_sezginCorners = new ArrayList<Integer>();
			m_sezginCorners.clear();
			m_sezginCorners.add(v[0].index);
			int end;
			for (int i = 1; i < v.length; i++) {
				end = v[i].index;
				m_sezginCorners.add(v[i].index);
				BStroke bs = getSubStroke(start, end);
				bs.calcFeatures();
				m_corner_substrokes.add(bs);
				start = end;
			}
		}
	}

	private void calcCurvCornerSubStrokes() {
		m_curv_corner_substrokes = getSubStrokes(m_curvatureCorners);
	}

	private void calcBrandonCornerSubStrokes() {
		m_brandon_corner_substrokes = getSubStrokes(m_brandonCorners);
	}

	public ArrayList<BStroke> getSubStrokes(List<Integer> corners) {
		ArrayList<BStroke> ss = new ArrayList<BStroke>();
		if (corners == null || corners.size() <= 0)
			return ss;
		int start = corners.get(0);
		int end;
		for (int i = 1; i < corners.size(); i++) {
			end = corners.get(i);
			BStroke s = getSubStroke(start, end);
			s.calcFeatures();
			ss.add(s);
			start = end;
		}
		return ss;
	}

	private void calcTracyCornerSubStrokes() {
		TracyPolyLineParse p = new TracyPolyLineParse(this, false);
		m_tracy_corner_substrokes = getSubStrokes(m_tracyCorners);
	}

	private void calcBestSubStrokes() {
		// m_best_substrokes = new ArrayList<BStroke>();
		/*
		 * if (m_curvatureCorners.size()<=0) return; trySingle();
		 */
		/*
		 * if (m_best_substrokes.size()<=0) { tryDouble(); if
		 * (m_best_substrokes.size()<=0) { tryTriple(); } }
		 */
		m_best_substrokes = calcTheBestSubStrokes();
		if (m_best_substrokes.size() <= 2)
			return;
		for (int i = 0; i < m_best_substrokes.size() - 1; i++) {
			BStroke s1 = m_best_substrokes.get(i);

			BStroke s2 = m_best_substrokes.get(i + 1);
			BStroke s = combine(s1, s2);
			ArrayList<Fit> f = s.recognize(false);
			if (f.size() == 0)
				continue;
			if (f.get(0) instanceof PolylineFit || f.get(0) instanceof CurveFit)
				continue;
			m_best_substrokes.remove(i + 1);
			m_best_substrokes.remove(i);
			m_best_substrokes.add(i, s);
			i--;
		}
	}

	public ArrayList<BStroke> calcTheBestSubStrokes() {
		ArrayList<BStroke> best = new ArrayList<BStroke>();
		if (m_x.length == 2) {
			best.add(getSubStroke(0, m_x.length - 1));
			return best;
		} else if (m_x.length < 2)
			return best;
		BStroke s1 = getSubStroke(0, m_max_curv_index);
		BStroke s2 = getSubStroke(m_max_curv_index, m_x.length - 1);
		if (s1.size() <= 2 || s2.size() <= 2) {
			best.add(getSubStroke(0, m_x.length - 1));
			return best;
		}
		ArrayList<Fit> f1 = s1.recognize(false);
		ArrayList<Fit> f2 = s2.recognize(false);
		if (f1.size() == 0) {
			s1.calcFeatures();
			ArrayList<BStroke> tmp = s1.getFeatures().calcTheBestSubStrokes();
			for (BStroke b : tmp)
				best.add(b);
		} else if (f1.get(0) instanceof PolylineFit) {
			s1.calcFeatures();
			ArrayList<BStroke> tmp = s1.getFeatures().calcTheBestSubStrokes();
			for (BStroke b : tmp)
				best.add(b);
		} else {
			best.add(s1);
		}
		if (f2.size() == 0) {
			s2.calcFeatures();
			ArrayList<BStroke> tmp = s2.getFeatures().calcTheBestSubStrokes();
			for (BStroke b : tmp)
				best.add(b);
		} else if (f2.get(0) instanceof PolylineFit) {
			s2.calcFeatures();
			ArrayList<BStroke> tmp = s2.getFeatures().calcTheBestSubStrokes();
			for (BStroke b : tmp)
				best.add(b);
		} else {
			best.add(s2);
		}
		return best;
	}

	public BStroke getSubStroke(int start, int end) {
		BStroke s = new BStroke();
		if (start > end) {
			s.add(m_x[start], m_y[start], m_time[start]);
			s.add(m_x[end], m_y[end], m_time[end]);
		} else {
			for (int j = 0; j < m_x.length; j++) {
				if (j >= start && j <= end)
					s.add(m_x[j], m_y[j], m_time[j]);
			}
		}
		return s;
	}

	private BStroke combine(BStroke s1, BStroke s2) {
		BStroke newStroke = new BStroke();
		for (int i = 0; i < s1.size(); i++)
			newStroke.add(s1.get(i));
		for (int i = 1; i < s2.size(); i++)
			newStroke.add(s2.get(i));
		return newStroke;
	}

	/*
	 * private void calcDirectionChange() { double largestDiff =
	 * Double.MIN_VALUE; for (int i = 0; i < m_dir.length-1; i++) { if
	 * (Math.abs(m_dir[i]-m_dir[i+1]) > largestDiff) largestDiff =
	 * Math.abs(m_dir[i]-m_dir[i+1]); } m_largestDirectionChange =
	 * largestDiff/Math.abs(m_totalRotation); //System.out.println("CHANGE: " +
	 * m_largestDirectionChange); }
	 */

	/*
	 * public double getLargestDirectionChange() { return
	 * m_largestDirectionChange; }
	 */

	public double totalRotation() {
		return m_totalRotation;
	}

	public double numRevolutions() {
		return m_numRevolutions;
	}

	public double getNDDE() {
		return m_NDDE;
	}

	public double getDCR() {
		return m_DCR;
	}

	public Line2D getBestFitLine() {
		return m_bestFitLine;
	}

	/*
	 * public Line2D getBestFitDirGraphLine() { return m_bestFitDirGraph; }
	 *
	 * public double getBestFitDirGraphErr() { return m_bestFitDirGraphErr; }
	 */

	public boolean isOvertraced() {
		return m_overtraced;
	}

	public boolean isComplete() {
		return m_complete;
	}

	public Rectangle2D getBounds() {
		return m_bounds;
	}

	public Rectangle2D getInvBounds() {
		return m_invbounds;
	}

	public int numBrandonCorners() {
		return m_xbrandonCorner.length;
	}

	public int numFinalCorners() {
		return m_xfinalCorner.length;
	}

	public double getStrokeLength() {
		return m_strokelength;
	}

	public double getSlopeDirGraph() {
		return m_slopeDirGraph;
	}

	public ArrayList<BStroke> get2PIsubStrokes() {
		calcSubStrokes();
		return m_2pi_substrokes;
	}

	public ArrayList<BStroke> getCornerSubStrokes(boolean useSezgin) {
		calcCornerSubStrokes(useSezgin);
		return m_corner_substrokes;
	}

	public ArrayList<BStroke> getCurvCornerSubStrokes() {
		calcCurvCornerSubStrokes();
		return m_curv_corner_substrokes;
	}

	public ArrayList<BStroke> getBrandonCornerSubStrokes() {
		calcBrandonCornerSubStrokes();
		return m_brandon_corner_substrokes;
	}

	public ArrayList<BStroke> getTracyCornerSubStrokes() {
		calcTracyCornerSubStrokes();
		return m_tracy_corner_substrokes;
	}

	public ArrayList<BStroke> getBestSubStrokes() {
		calcBestSubStrokes();
		return m_best_substrokes;
	}

	public boolean isClockwise() {
		if (m_slopeDirGraph >= 0)
			return true;
		else
			return false;
	}

	public double[] getLengthSoFar() {
		return m_lengthSoFar;
	}

	public double getNumRevs() {
		return m_numRevolutions;
	}

	/*
	 * public void calcBrandonCorners() { m_brandonCorners =
	 * computePeakPoints(m_averageCurvature*.8, m_curvature);
	 * m_brandonCorners.addAll(m_speedCorners);
	 * Collections.sort(m_brandonCorners);
	 * m_brandonCorners.set(m_brandonCorners.size()-1, m_x.length-1); double
	 * thresh = getStrokeLength()*M_CORNER_BRANDON_DIS_PERCENT; boolean stop =
	 * false; int numCorners = m_brandonCorners.size(); while (!stop) { for (int
	 * i = m_brandonCorners.size()-2; i >= 0; i--) { if (m_brandonCorners.get(i) <
	 * 0 || m_brandonCorners.get(i+1) < 0) continue; double x1 =
	 * m_x[m_brandonCorners.get(i)]; double y1 = m_y[m_brandonCorners.get(i)];
	 * double x2 = m_x[m_brandonCorners.get(i+1)]; double y2 =
	 * m_y[m_brandonCorners.get(i+1)]; if (Point2D.distance(x1,y1,x2,y2) <
	 * thresh) { if (i == m_brandonCorners.size()-2) m_brandonCorners.remove(i);
	 * else { int avg = (m_brandonCorners.get(i)+m_brandonCorners.get(i+1))/2;
	 * m_brandonCorners.remove(i+1); m_brandonCorners.set(i, avg); } i--; } } if
	 * (m_brandonCorners.size()==numCorners) { stop = true; } numCorners =
	 * m_brandonCorners.size(); } // connecting lines with similar slopes should
	 * have middle point removed for (int i = m_brandonCorners.size()-2; i >= 1;
	 * i--) { double x0 = m_x[m_brandonCorners.get(i+1)]; double y0 =
	 * m_y[m_brandonCorners.get(i+1)]; double x1 = m_x[m_brandonCorners.get(i)];
	 * double y1 = m_y[m_brandonCorners.get(i)]; double x2 =
	 * m_x[m_brandonCorners.get(i-1)]; double y2 =
	 * m_y[m_brandonCorners.get(i-1)]; double slope1 = (y0-y1)/(x0-x1); double
	 * slope2 = (y1-y2)/(x1-x2); double diff = Math.abs(slope1-slope2); if (diff <
	 * M_CORNER_BRANDON_SIM_SLOPE || Double.isInfinite(diff))
	 * m_brandonCorners.remove(i); } //System.out.println("Final Slopes: ");
	 * /*for (int i = m_brandonCorners.size()-2; i >= 1; i--) { double x0 =
	 * m_x[m_brandonCorners.get(i+1)]; double y0 =
	 * m_y[m_brandonCorners.get(i+1)]; double x1 = m_x[m_brandonCorners.get(i)];
	 * double y1 = m_y[m_brandonCorners.get(i)]; double x2 =
	 * m_x[m_brandonCorners.get(i-1)]; double y2 =
	 * m_y[m_brandonCorners.get(i-1)]; //double slope1 = (y0-y1)/(x0-x1);
	 * //double slope2 = (y1-y2)/(x1-x2); //System.out.println("Slope: " +
	 * (Math.abs(slope1-slope2))); }
	 */
	/*
	 * uncomment this section m_xbrandonCorner = new
	 * double[m_brandonCorners.size()]; m_ybrandonCorner = new
	 * double[m_brandonCorners.size()]; for(int i = 0; i <
	 * m_brandonCorners.size(); i++){ m_xbrandonCorner[i] =
	 * m_x[m_brandonCorners.get(i)]; m_ybrandonCorner[i] =
	 * m_y[m_brandonCorners.get(i)]; }
	 */
	// plot
	/*
	 * Plot plot = new Plot("Brandon Corners"); plot.addLine(m_x, m_y,
	 * Color.yellow, 10); plot.addLine(m_xbrandonCorner, m_ybrandonCorner,
	 * Color.black, 10); plot.setKeepdim(true); plot.plot();
	 */

	// }
	public void calcBrandonCorners() {
		boolean debug = false;
		int neighborhood = (int) (m_x.length * M_NEIGHBORHOOD_PCT);
		if (neighborhood < 5)
			neighborhood = 5;
		m_brandonCorners = new ArrayList<Integer>();
		m_brandonCorners.add(0);

		// find preliminary corners
		BStroke currStroke = new BStroke();
		currStroke.add(m_x[0], m_y[0], m_time[0]);
		for (int i = 1; i < m_x.length; i++) {
			currStroke.add(m_x[i], m_y[i], m_time[i]);
			if (currStroke.size() > neighborhood && currStroke.size() > 5) {
				currStroke.calcFeatures();
				if (currStroke.getFeatures().getEndptStrokeLengthRatio() < M_ENDPT_STROKE_LENGTH_RATIO) {
					m_brandonCorners.add(i - 2);
					currStroke = new BStroke();
					currStroke.add(m_x[i - 1], m_y[i - 1], m_time[i - 1]);
				}
			}
		}
		m_brandonCorners.add(m_x.length - 1);

		if (debug) {
			System.out.println("neighborhood: " + neighborhood);
			for (int i = 0; i < m_brandonCorners.size(); i++) {
				System.out.println("old corner: " + m_brandonCorners.get(i));
			}
		}

		boolean done = false;

		while (!done) {
			int prevSize = m_brandonCorners.size();

			// observe neighborhood and find corner with highest curvature
			// within neighborhood
			for (int i = 0; i < m_brandonCorners.size(); i++) {
				int curr = m_brandonCorners.get(i);
				if (i > 0) {
					if (curr < m_brandonCorners.get(i - 1)) {
						m_brandonCorners.remove(i);
						continue;
					}
				}
				if (curr - neighborhood < 0
						|| curr + neighborhood > m_curvature.length - 1)
					continue;
				else {
					int maxIndex = curr - neighborhood;
					double maxCurv = m_curvature[maxIndex];
					boolean stop = false;
					for (int j = curr - neighborhood; j < curr + neighborhood
							&& !stop; j++) {
						if (j >= m_curvature.length)
							stop = true;
						else if (m_curvature[j] > maxCurv) {
							maxCurv = m_curvature[j];
							maxIndex = j;
						}
					}
					if (!stop
							&& !m_brandonCorners
									.contains((Integer) (maxIndex + 1))) {
						m_brandonCorners.add(i, maxIndex + 1);
						m_brandonCorners.remove(i + 1);
					}
				}
			}

			if (debug) {
				for (int i = 0; i < m_brandonCorners.size(); i++) {
					System.out
							.println("new corner: " + m_brandonCorners.get(i));
				}
			}

			// merge corners within a neighborhood of each other (leave end
			// points intact)
			doMerge(neighborhood + 1);

			if (debug) {
				for (int i = 0; i < m_brandonCorners.size(); i++) {
					System.out.println("1st merge corner: "
							+ m_brandonCorners.get(i));
				}
			}

			if (m_brandonCorners.size() == prevSize)
				done = true;
		}

		// perform merging until size converges
		/*
		 * boolean stop = false; while (!stop) { int prevSize =
		 * m_brandonCorners.size(); doMerge(neighborhood); if
		 * (m_brandonCorners.size()==prevSize) stop = true; }
		 */

		if (debug) {
			for (int i = 0; i < m_brandonCorners.size(); i++) {
				System.out.println("final corners: " + m_brandonCorners.get(i));
			}
		}

		m_xbrandonCorner = new double[m_brandonCorners.size()];
		m_ybrandonCorner = new double[m_brandonCorners.size()];
		for (int i = 0; i < m_brandonCorners.size(); i++) {
			m_xbrandonCorner[i] = m_x[m_brandonCorners.get(i)];
			m_ybrandonCorner[i] = m_y[m_brandonCorners.get(i)];
		}
	}

	private void doMerge(int neighborhood) {
		ArrayList<Integer> newCorners = new ArrayList<Integer>();
		newCorners.add(0);
		int prev = 0;
		int sum = 0;
		int num = 1;
		for (int i = 1; i < m_brandonCorners.size(); i++) {
			// no longer in the neighborhood
			if (m_brandonCorners.get(i) - prev >= neighborhood) {
				if (Math.round((float) sum / num) > neighborhood)
					newCorners.add(Math.round((float) sum / num));
				prev = m_brandonCorners.get(i);
				num = 1;
				sum = m_brandonCorners.get(i);
			}
			// still in the neighborhood
			else {
				num++;
				sum += m_brandonCorners.get(i);
			}
		}
		if (!newCorners.contains((Integer) (m_x.length - 1))) {
			if ((m_x.length - 1) - newCorners.get(newCorners.size() - 1) <= neighborhood)
				newCorners.remove(newCorners.size() - 1);
			newCorners.add(m_x.length - 1);
		}
		m_brandonCorners = newCorners;
	}

	public double getMaxCurv() {
		if (m_max_curv < .0000000001)
			m_max_curv = 0.0;
		return m_max_curv;
	}

	public int getMaxCurvIndex() {
		return m_max_curv_index;
	}

	public double getAvgCurv() {
		double sum = 0;
		double num = 0;
		for (int i = 0; i < m_curvature.length; i++) {
			sum += m_curvature[i];
			num++;
		}
		return sum / num;
	}

	public double getBBRotationRatio() {
		double bbArea = m_bounds.getWidth() * m_bounds.getHeight();
		// double width = m_bounds.getWidth();
		// if (m_bounds.getHeight() < m_bounds.getWidth())
		// width = m_bounds.getHeight();
		BStroke s = new BStroke(m_x, m_y, m_time);
		s.calcFeatures();
		EllipseFit f = new EllipseFit(s);
		// double minAxis = f.getMinorAxisLength();
		// return minAxis/width;
		BStroke rotStroke = rotatePoints(s, f.getMajorAxisAngle(), f
				.getCenter());
		rotStroke.calcFeatures();
		double bb2Area = rotStroke.getFeatures().getBounds().getWidth()
				* rotStroke.getFeatures().getBounds().getHeight();
		return bbArea / bb2Area;
	}

	/**
	 * Method used to rotate the points of a BStroke around a certain point
	 *
	 * @param s
	 *            stroke to rotate
	 * @param angle
	 *            angle to rotate by
	 * @param c
	 *            the point the rotate around (center point of rotation)
	 * @return new stroke of rotated points
	 */
	public static BStroke rotatePoints(BStroke s, double angle, Point2D c) {
		double newX, newY;
		BStroke newS = new BStroke();
		TPoint p;
		for (int i = 0; i < s.size(); i++) {
			TPoint sp = s.get(i);
			newX = c.getX() + Math.cos(angle) * (sp.getX() - c.getX())
					- Math.sin(angle) * (sp.getY() - c.getY());
			newY = c.getY() + Math.cos(angle) * (sp.getY() - c.getY())
					+ Math.sin(angle) * (sp.getX() - c.getX());
			p = new TPoint(newX, newY, sp.getTime());
			newS.add(p);
		}
		return newS;
	}

	public double numCorners() {
		if (m_tracyCorners == null)
			calcTracyCornerSubStrokes();
		return (double) m_tracyCorners.size();
	}

	public TPoint getOrigStartPoint() {
		return new TPoint(m_origX[0], m_origY[0], m_origT[0]);
	}

	public TPoint getOrigEndPoint() {
		return new TPoint(m_origX[m_origX.length - 1],
				m_origY[m_origY.length - 1], m_origT[m_origT.length - 1]);
	}

	public double getNonAbsCurvRatio() {
		return m_nonAbsCurvRatio;
	}

	public void calcNonAbsCurvRatio() {
		if (m_curvNoAbs.length == 0) {
			m_nonAbsCurvRatio = 1.0;
			return;
		}
		int start = (int) (m_curvNoAbs.length * .05); // ignore 5% at ends (to
														// avoid tails)
		int end = m_curvNoAbs.length - start;
		double min = m_curvNoAbs[start];
		double max = m_curvNoAbs[start];
		for (int i = start + 1; i < end; i++) {
			if (m_curvNoAbs[i] > max)
				max = m_curvNoAbs[i];
			if (m_curvNoAbs[i] < min)
				min = m_curvNoAbs[i];
		}
		double largest = Math.abs(max);
		double smallest = Math.abs(min);
		if (Math.abs(min) > Math.abs(max)) {
			largest = Math.abs(min);
			smallest = Math.abs(max);
		}
		m_nonAbsCurvRatio = (largest - smallest) / largest;
	}

	public boolean dirWindowPassed() {
		return m_dirWindowPassed;
	}

	public double getSlopeDiff() {
		return m_slopeDiff;
	}

	public double getMidSlopeDiff() {
		return m_midSlopeDiff;
	}

	public void calcSlopeDiff() {
		if (m_x.length < 10)
			m_slopeDiff = 0;
		else {
			Line2D.Double l1 = new Line2D.Double(m_x[0], m_y[0], m_x[4], m_y[4]);
			Line2D.Double l2 = new Line2D.Double(m_x[m_x.length - 5],
					m_y[m_y.length - 5], m_x[m_x.length - 1],
					m_y[m_y.length - 1]);
			double slope1 = (l1.y2 - l1.y1) / (l1.x2 - l1.x1);
			double slope2 = (l2.y2 - l2.y1) / (l2.x2 - l2.x1);
			int i1 = 4;
			int i2 = 5;
			while (Double.isInfinite(slope1)) {
				i1++;
				l1 = new Line2D.Double(m_x[0], m_y[0], m_x[i1], m_y[i1]);
				slope1 = (l1.y2 - l1.y1) / (l1.x2 - l1.x1);
			}
			while (Double.isInfinite(slope2)) {
				i2++;
				l2 = new Line2D.Double(m_x[m_x.length - i2], m_y[m_y.length
						- i2], m_x[m_x.length - 1], m_y[m_y.length - 1]);
				slope2 = (l2.y2 - l2.y1) / (l2.x2 - l2.x1);
			}
			m_slopeDiff = Math.abs(Math.abs(slope1) - Math.abs(slope2));
		}
	}

	private void calcMidSlopeDiff() {
		int p25 = 0;
		int p75 = 0;
		for (p25 = 0; m_lengthSoFar[p25] / m_strokelength < 0.25; p25++)
			;
		for (p75 = p25; m_lengthSoFar[p75] / m_strokelength < 0.75; p75++)
			;
		int p25next = p25 + 1;
		int p75next = p75 + 1;
		Line2D.Double l1 = new Line2D.Double(m_x[p25], m_y[p25], m_x[p25next],
				m_y[p25next]);
		Line2D.Double l2 = new Line2D.Double(m_x[p75], m_y[p75], m_x[p75next],
				m_y[p75next]);
		double slope1 = (l1.y2 - l1.y1) / (l1.x2 - l1.x1);
		double slope2 = (l2.y2 - l2.y1) / (l2.x2 - l2.x1);
		while (Double.isInfinite(slope1)) {
			p25next++;
			l1 = new Line2D.Double(m_x[p25], m_y[p25], m_x[p25next],
					m_y[p25next]);
			slope1 = (l1.y2 - l1.y1) / (l1.x2 - l1.x1);
		}
		while (Double.isInfinite(slope2)) {
			p75next++;
			l2 = new Line2D.Double(m_x[p75], m_y[p75], m_x[p75next],
					m_y[p75next]);
			slope2 = (l2.y2 - l2.y1) / (l2.x2 - l2.x1);
		}
		m_midSlopeDiff = Math.abs(Math.abs(slope1) - Math.abs(slope2));
	}

	private void calcDirWindowPassed() {
		double windowSize = 10;
		boolean result = true;
		ArrayList<Double> dirWindows = new ArrayList<Double>();
		int start = (int) (m_dir.length * .05); // ignore 5% at ends (to avoid
												// tails)
		int end = m_dir.length - start;
		int j = 0;
		double mean = 0;
		// cluster into windows
		for (int i = start; i < end; i++) {
			if (j >= windowSize) {
				dirWindows.add(mean / windowSize);
				j = 0;
				mean = 0;
			} else {
				mean += m_dir[i];
				j++;
			}
		}
		if (dirWindows.size() > 3) {
			boolean increasing = true;
			if (dirWindows.get(1) < dirWindows.get(0))
				increasing = false;
			for (int i = 0; i < dirWindows.size() - 1 && result; i++) {
				// System.out.println(dirWindows.get(i) + " " +
				// dirWindows.get(i+1));
				if (increasing && dirWindows.get(i + 1) < dirWindows.get(i)
						|| !increasing
						&& dirWindows.get(i + 1) > dirWindows.get(i))
					result = false;
			}
		}
		m_dirWindowPassed = result;
	}

	public ArrayList<TPoint> getBrandonCorners() {
		ArrayList<TPoint> c = new ArrayList<TPoint>();
		if (m_brandonCorners == null)
			calcBrandonCorners();
		for (Integer i : m_brandonCorners) {
			c.add(new TPoint(m_x[i], m_y[i], m_time[i]));
		}
		return c;
	}

	public ArrayList<Integer> getBrandonCornersInt() {
		if (m_brandonCorners == null)
			calcBrandonCorners();
		return (ArrayList<Integer>) m_brandonCorners;
	}

	public static double rectToPtdistance(Rectangle2D rect, double x, double y) {
		Line2D l1 = new Line2D.Double(rect.getX(), rect.getY(), rect.getX()
				+ rect.getWidth(), rect.getY());
		Line2D l2 = new Line2D.Double(rect.getX() + rect.getWidth(), rect
				.getY(), rect.getX() + rect.getWidth(), rect.getY()
				+ rect.getHeight());
		Line2D l3 = new Line2D.Double(rect.getX() + rect.getWidth(), rect
				.getY()
				+ rect.getHeight(), rect.getX(), rect.getY() + rect.getHeight());
		Line2D l4 = new Line2D.Double(rect.getX(), rect.getY()
				+ rect.getHeight(), rect.getX(), rect.getY());
		double minDistance = l1.ptSegDist(x, y);
		if (l2.ptSegDist(x, y) < minDistance)
			minDistance = l2.ptSegDist(x, y);
		if (l3.ptSegDist(x, y) < minDistance)
			minDistance = l3.ptSegDist(x, y);
		if (l4.ptSegDist(x, y) < minDistance)
			minDistance = l4.ptSegDist(x, y);
		return minDistance;
	}
}
