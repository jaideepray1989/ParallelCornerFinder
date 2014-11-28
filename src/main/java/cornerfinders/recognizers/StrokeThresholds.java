/**
 * StrokeThresholds
 * tamu
 * StrokeThresholds.java
 * Created by hammond, Tracy Hammond
 * Created on Nov 18, 2006 2006 9:38:50 PM
 * Copyright Tracy Hammond, Texas A&M University, 2006
 */
package cornerfinders.recognizers;

/**
 * @author hammond
 *
 */
public interface StrokeThresholds {
  public static final int M_HOOK_MAXHOOKLENGTH = 10;
  public static final double M_HOOK_MAXHOOKPERCENT = .2;
  public static final int M_HOOK_MINSTROKELENGTH = 70;	// C
  public static final int M_HOOK_MINPOINTS = 5;	// B
  public static final double M_HOOK_MINHOOKCURVATURE = .5;	// A
  public static final double M_CORNER_MINANGLEDIFF = Math.PI/10;
  public static final double M_CORNER_MAXDISTANCETOSPEED = 15;
  public static final double M_CORNER_MAXDISTANCETOADJACENT = 15;
  public static final double M_CORNER_BRANDON_DIS_PERCENT = 0.04;
  public static final double M_CORNER_BRANDON_SIM_SLOPE = 1.0;

  // bpaulson thresholds
  public static final double M_REVS_TO_BE_OVERTRACED = 1.31;	// D
  public static final double M_PERCENT_DISTANCE_TO_BE_COMPLETE = 0.16;	// E
  public static final double M_NUM_REVS_TO_BE_COMPLETE = 0.75;	// F
  //public static final double M_LARGEST_DIRECTION_CHANGE = 0.5;
  //public static final double M_BEST_FIT_LINE_LS_ERROR = .01;
  // NOTE: change from 2.0 to 1.8
  public static final double M_LINE_LS_ERROR_FROM_ENDPTS = 1.8;	// G
  public static final double M_LINE_FEATURE_AREA = 10.25;	// H
  public static final double M_ELLIPSE_FEATURE_AREA = 0.33;	// M
  public static final double M_ELLIPSE_SMALL = 30.0;	// L
  public static final double M_CIRCLE_FEATURE_AREA = 0.35;	// P
  public static final double M_CIRCLE_SMALL = 16.0;	// N
  public static final double M_ARC_FEATURE_AREA = 0.4;	// Q
  //public static final double M_OVERTRACED_CIRCLE_CENTER_CLOSENESS = .05;
  //public static final double M_OVERTRACED_CIRCLE_RADIUS_DIFFERENCE = .05;
  //public static final double M_OVERTRACED_ELLIPSE_CENTER_CLOSENESS = .05;
  //public static final double M_OVERTRACED_ELLIPSE_AXIS_DIFFERENCE = .07;
  public static final double M_AXIS_RATIO_TO_BE_CIRCLE = 0.425;	// O
  public static final double M_NDDE_HIGH = 0.8;	// K
  //public static final double M_NDDE_LOW = 1.0-M_NDDE_HIGH;
  //public static final double M_SPIRAL_RADIUS_ERROR = 0.77;
  public static final double M_SPIRAL_CENTER_CLOSENESS = 0.25; // T
  //public static final double M_SPIRAL_AVG_CENTER_CLOSENESS = 0.85;
  public static final double M_SPIRAL_RADIUS_RATIO = 0.9; // S
  public static final double M_SPIRAL_DIAMETER_CLOSENESS = 0.2; // U
  //public static final double M_HELIX_CENTER_CLOSENESS = 0.5;
  public static final double M_POLYLINE_LS_ERROR = 0.0036;//0.55;	// I
  //public static final double M_POLYLINE_ERROR = 5.0;
  public static final double M_POLYLINE_SUBSTROKES_LOW = 10; // X
  public static final double M_DCR_TO_BE_POLYLINE = 6.0;	// J
  public static final double M_DCR_TO_BE_POLYLINE_STRICT = 9.0; // W
  //public static final double M_CURVE_FIT_ERROR = 0.3;
  public static final double M_CURVE_ERROR = 0.37;	// R
  //public static final double M_HIGH_CURVATURE = 0.5;
  public static final double M_RATIO_TO_REMOVE_TAIL = 0.1; // V

  public static final double M_ELLIPSE_CORNER_LENGTH_RATIO = 0.01;
  public static final double M_CIRCLE_CORNER_LENGTH_RATIO = 0.05;
  public static final double M_CURVE_CORNER_LENGTH_RATIO = 0.003;
  public static final double M_ARC_AREA_RATIO = 0.3;
  public static final double M_NON_ABS_CURV_RATIO = 0.6;
  public static final double M_SLOPE_DIFF = 6.5;
  public static final double M_REVS_TO_BE_CIRCULAR = 0.875;
  public static final double M_ENDPT_STROKE_LENGTH_RATIO = 0.99;
  public static final double M_NEIGHBORHOOD_PCT = .06;

  public static final double M_POLYGON_PCT = 0.09;
}
