////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// AUTHOR:      Tevfik Metin Sezgin                                           //
//              Massachusetts Institute of Technology                         //
//              Department of Electrical Engineering and Computer Science     //
//              Artificial Intelligence Laboratory                            //
//                                                                            //
// E-MAIL:        mtsezgin@ai.mit.edu, mtsezgin@mit.edu                       //
//                                                                            //
// COPYRIGHT:   Tevfik Metin Sezgin                                           //
//              All rights reserved. This code can not be copied, modified,   //
//              or distributed in whole or partially without the written      //
//              permission of the author. Also see the COPYRIGHT file.        //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////
// Cleaned -Metin

package cornerfinders.toolkit;

import edu.mit.sketch.geom.Approximation;
import edu.mit.sketch.geom.Arc;
import edu.mit.sketch.geom.Ellipse;
import edu.mit.sketch.geom.GeneralPath;
import edu.mit.sketch.geom.GeometricObject;
import edu.mit.sketch.geom.Line;
import edu.mit.sketch.geom.Polygon;
import edu.mit.sketch.geom.Spiral;
import edu.mit.sketch.geom.Vertex;
import edu.mit.sketch.ui.Tablet;
import edu.mit.sketch.util.AWTUtil;
import edu.mit.sketch.util.LinearFit;
import java.util.Arrays;

/**
 *
 * This class implements Classifier. The
 * SimpleClassifier will deal with single stroke input only.  There is a
 * SimpleClassifier object associated with each stroke.
 *
 * In conjunction with this class is the StrokeData class.  The users
 * must creare a StrokeData object and pass it to this class.
 *
 */
public class StrokeClassifier extends Classifier {
  /**
   * This is the number of points to be included for deriving direction.
   **/
  public int direction_window_width = 5;

  /**
   * This is the fit method to be used when deriving the direction.
   **/
  public LinearFit.Method fit_method = LinearFit.Method.ROTATION;
    
  
  /**
   * The angle threshold for simplifying vertices in polygons.
   **/
  public double turn_angle_threshold = Math.PI/6;
  
  /**
   * Determines whether they system filters vertices by least squared loss.
   **/
  public boolean filter_vertices = true;

  /**
   * Determines whether the system removes tails in beginning and end of strokes.
   **/
  public boolean remove_tail = true;
  
  /**
   * Determines whether the system will classify arcs.
   **/
  public boolean fit_arcs = true;
  
  /**
   * Determines whether the system will use the greedy polygon fit method.
   **/
  public boolean greedy_polygon_fit = false;
  
  /**
   * Determines the error threshold for the greedy polygon fit
   **/
  public double polygon_error_threshold = 2;
  
  /**
   * Determines the minimum segment size for the greedy polygon fit
   **/
  public int polygon_min_segment = 5;
  
  /**
   * This is how much we scale the average curvature to get the threshold
   * for curvature fit. Making this too high or too low results in fewer
   * points in the output fit.
   **/
  public static double dd_dt_average_scale = 1.0;

  /**
   * This is how much we scale the average speed to get the threshold
   * for the speed fit. Making this too high or too low results in fewer
   * points in the output fit.
   **/
  public static double speed_average_scale = 0.6;

  /**
   * This controls the strictness of line classification. Higher threshold
   * means less strict.
   **/
  public double test_line_scale = 1.1;

  /**
   * Final fit LSQE is scaled by this, and compared against the general path
   * LSQE. Making this larger creates a bias towards complex fits.
   **/
  public double polyline_vs_general_path_bias = 0.5; //Multiply by polyline LE

  /**
   * Best hybrid fit
   **/
  public Vertex hybrid_fit[];

  /**
   * Speed fit
   **/
  public Vertex speed_fit[];

  /**
   * Greedy fit
   **/
  public Vertex greedy_fit[];

  /**
   * Final fit
   **/
  public Vertex final_fit[];
  
  
  /**
   * Fit based on curvature
   **/
  public Vertex direction_fit[];

  /**
   * Speed original points in the stroke.
   **/
  public Vertex points[];

  /**
   * Line fit
   **/
  protected Line line_fit;

  /**
   * Oval fit
   **/
  protected Ellipse ellipse_fit;

  /**
   * Polyline=polygon fit
   **/
  protected Polygon polygon_fit;

  /**
   * Arc fit
   **/
  private Arc arc_fit;

  /**
   * Spiral fit
   **/
  private Spiral spiral_fit;

  /**
   * Complex fit
   **/
  protected GeneralPath general_path_fit;

  /**
   * True if the fit of this kind.
   **/
  protected boolean line_result;

  /**
   * True if the fit of this kind.
   **/
  protected boolean circle_result;

  /**
   * True if the fit of this kind.
   **/
  protected boolean polygon_result;

  /**
   * True if the fit of this kind.
   **/
  protected boolean general_path_result;

  /**
   * The least square error between the original points and the line fit
   **/
  protected double line_LSQE = Double.MAX_VALUE;

  /**
   * The least square error between the original points and the speed fit
   **/
  protected double speed_LSQE = Double.MAX_VALUE;

  /**
   * The least square error between the original points and the direction
   * (i.e. curvature)fit
   **/
  protected double direction_LSQE = Double.MAX_VALUE;

  /**
   * The least square error between the original points and the oval fit
   **/
  protected double circle_LSQE = Double.MAX_VALUE;

  /**
   * The least square error between the original points and the arc fit
   **/
  private double arc_LSQE = Double.MAX_VALUE;

  /**
   * The least square error between the original points and the spiral fit
   **/
  private double spiral_LSQE = Double.MAX_VALUE;

  /**
   * The least square error between the original points and the best hybrid
   * polyline fit
   **/
  protected double hybrid_fit_LSQE = Double.MAX_VALUE;

  /**
   * The least square error between the original points and the best hybrid
   * polyline fit
   **/
  protected double final_fit_LSQE = Double.MAX_VALUE;

  
  /**
   * The least square error between the original points and the complex fit
   **/
  protected double general_path_LSQE = Double.MAX_VALUE;

  /**
   * The least square error between the original points and the greedy polygon fit
   **/  
  protected double greedy_fit_LSQE = Double.MAX_VALUE;
  
  
  /**
   * Has the classifier done its thing?
   **/
  protected boolean m_isClassified = false;

  /**
   * What was the result of the classification.  It is -1 if there has
   * not been a result yet.
   **/
  protected int m_classification = -1;

  /**
   * Constructs a Classifier object.  You must pass it a StrokeData object.
   * Contains a default error calculator, which is just the squared error.
   **/
  public StrokeClassifier(StrokeData stroke_data) {
    this.stroke_data = stroke_data;
  }

  
  public void setVersion(int version)
  {
    if (version == 1)
    {
      turn_angle_threshold = Math.PI/10;
      filter_vertices = false;
      remove_tail = false;
      fit_arcs = false;
    }
    else if (version == 2)
    {
      turn_angle_threshold = Math.PI/10;
      filter_vertices = false;
      remove_tail = false;
      fit_arcs = true;    
    }
    else if (version == 3)
    {
      turn_angle_threshold = Math.PI/6;
      filter_vertices = true;
      remove_tail = true;
      fit_arcs = true;    
    }  
  }
  
  /**
   *
   * Returns the best fit type for the strokeData 
   *
   **/
  public int classify() {
    if (m_isClassified) {
      return m_classification;
    }

    if (!Tablet.very_quiet) {
      System.out.println("\tdirection_window_width        = "
          + direction_window_width);
      System.out.println("\tfit_method                    = " + fit_method);
      System.out
          .println("\ttest_line_scale               = " + test_line_scale);
      System.out.println("\tdd_dt_average_scale           = "
          + dd_dt_average_scale);
      System.out.println("\tspeed_average_scale           = "
          + speed_average_scale);
      System.out.println("\tpolyline_vs_general_path_bias = "
          + polyline_vs_general_path_bias);
    }
    
    stroke_data.direction_window_width = direction_window_width;
    stroke_data.test_line_scale = test_line_scale;
    stroke_data.fit_method = fit_method;

    stroke_data.deriveProperties();

    double actual_scale = test_line_scale;
    int point_count = stroke_data.vertices.length;

    points = stroke_data.vertices;
    actual_scale = (point_count < 20) ? test_line_scale + .05 : actual_scale;
    actual_scale = (point_count < 12) ? test_line_scale * 4 : actual_scale;

    line_result = stroke_data.testLine(actual_scale);

    speed_fit = AWTUtil.simplifyPolygon2(stroke_data.getSpeedFit(speed_average_scale), turn_angle_threshold);
    direction_fit = AWTUtil.simplifyPolygon2(stroke_data.getDirectionFit(dd_dt_average_scale), turn_angle_threshold);
    greedy_fit = stroke_data.getPolygonFit(polygon_error_threshold, polygon_min_segment);    
    
    if (filter_vertices == true)
    {
      speed_fit = stroke_data.filterVerticesByLSQE( speed_fit, 1.2 );
      direction_fit = stroke_data.filterVerticesByLSQE( direction_fit, 1.2 );
    }
    
    
    if (speed_fit == null && direction_fit == null) {
      System.err.println("Error: SimpleClassifier- both Speed and Direction fit are null");
      hybrid_fit = null;
    } else if (speed_fit == null) {
      hybrid_fit = direction_fit;
    } else if (direction_fit == null) {
      hybrid_fit = speed_fit;
    } else {
      hybrid_fit = Blackboard.decide(speed_fit, direction_fit, points,
          stroke_data);
    }

    
    // Sometimes the Tablet PC captures a tail at the end of strokes.
    // Here try to see if we have a very short segment at the end. This
    // test can eventually be made through pressure information. 
    
    if (remove_tail == true) {
      if (hybrid_fit[hybrid_fit.length - 1].distance(hybrid_fit[hybrid_fit.length - 2]) <= 10) {
        if (!Tablet.very_quiet) {
          System.out.println("Caught a tail at the end of the stroke.");
        }
        Vertex new_hybrid_fit[] = new Vertex[hybrid_fit.length - 1];
        for (int i = 0; i < new_hybrid_fit.length; i++) {
          new_hybrid_fit[i] = hybrid_fit[i];
        }
        new_hybrid_fit[new_hybrid_fit.length - 1] = hybrid_fit[hybrid_fit.length - 1];
        hybrid_fit = new_hybrid_fit;
      }
      if (hybrid_fit.length > 2 && hybrid_fit[0].distance(hybrid_fit[1]) <= 10) {
        if (!Tablet.very_quiet) {
          System.out.println("Caught a tail at the beginning of the stroke.");
        }
        Vertex new_hybrid_fit[] = new Vertex[hybrid_fit.length - 1];
        new_hybrid_fit[0] = hybrid_fit[0];
        for (int i = 2; i < hybrid_fit.length; i++) {
          new_hybrid_fit[i - 1] = hybrid_fit[i];
        }
        hybrid_fit = new_hybrid_fit;
      }
    }
    
    //Util.printArray( stroke_data.vertices, "stroke_data.vertices" );
    //Util.printArray( speed_fit, "speed_fit" );
    //Util.printArray( direction_fit, "direction_fit" );
    //Util.printArray( hybrid_fit, "hybrid_fit" );

        
    line_fit = new Line(points[0], points[points.length - 1]);
    ellipse_fit = stroke_data.getEllipse();
    arc_fit = Arc.fitArc( stroke_data );
    spiral_fit = Spiral.fitSpiral(stroke_data);
    
    updateLSQEs();
    
    boolean arc_conditions = false;
    if (arc_fit != null) {
      if (points[0].distance(points[points.length - 1]) > 15
          && arc_fit.extent > Math.PI / 4) {
        double arc_length = arc_fit.extent * arc_fit.getRadius();
        double polygon_length = arc_fit.getDataPoints().getPolygonLength();

        if (!Tablet.very_quiet) {
          System.out.println("arc_length " + arc_length);
          System.out.println("polygon_length " + polygon_length);
          System.out.println("arc_extent " + arc_fit.extent * 180 / Math.PI);
        }
        if (arc_length > polygon_length * 0.9
            && arc_length < polygon_length * 1.1) {
          arc_conditions = true;
        } else {
          if (!Tablet.very_quiet) {
            System.out.println("Rejected shape to be classified as ARC");
          }
          arc_conditions = false;
        }
      }
    }

    if (!Tablet.very_quiet) {
      System.out.println("circle_LSQE       = " + circle_LSQE);
      System.out.println("speed_LSQE        = " + speed_LSQE);
      System.out.println("direction_LSQE    = " + direction_LSQE);
      System.out.println("hybrid_fit_LSQE    = " + hybrid_fit_LSQE);
      System.out.println("line_LSQE         = " + line_LSQE);
      System.out.println("general_path_LSQE = " + general_path_LSQE);
    }

    if (circle_LSQE < final_fit_LSQE
        && (circle_LSQE < general_path_LSQE * 2 | circle_LSQE < 50)) {
      circle_result = true;
    }

    if (!line_result && !circle_result) {
      if (general_path_LSQE < polyline_vs_general_path_bias * final_fit_LSQE) {
        general_path_result = true;
      } else {
        polygon_result = true;
      }
    }

    general_path_fit = new GeneralPath(Blackboard.general_path);
    general_path_fit.setRanges(Blackboard.getRanges());

    polygon_fit = makePolyFromFit(final_fit);
    general_path_fit.setRanges(Blackboard.getRanges());

    line_fit.setOriginalVertices(stroke_data.vertices);
    ellipse_fit.setOriginalVertices(stroke_data.vertices);
    polygon_fit.setOriginalVertices(stroke_data.vertices);
    general_path_fit.setOriginalVertices(stroke_data.vertices);
    arc_fit.setOriginalVertices( stroke_data.vertices );

    int result = -1;

    if (line_result) {
      result = LINE;
      //System.out.println("line_result true");
    }

    if (polygon_result) {
      result = POLYGON;
      if (polygon_fit.npoints == 2) {
        polygon_result = false;
        line_result = true;
        result = LINE;
        //System.out.println("Recognized polygon w 1 edge");
      }
    }

    if (circle_result) {
      result = ELLIPSE;
    }

    if (general_path_result) {
      result = COMPLEX;
    }
    
    if (fit_arcs) {
      if (result == COMPLEX && spiral_LSQE < 10) {
        result = SPIRAL;
        return SPIRAL;
      }
      if (arc_LSQE < 50 && arc_conditions) {
        result = ARC;
      }
      if (arc_LSQE < getPolygonError() && arc_LSQE < getLineError() * .8
          && arc_LSQE < getEllipseError() && arc_LSQE < getComplexError()
          && arc_conditions) {
        result = ARC;
      }
      if (result == COMPLEX && getComplexApproximation().isAllCurves()
          && getEllipseError() < getComplexError() * 1.1) {
        if (arc_conditions) {
          result = ARC;
        }
      }

      if (result == ELLIPSE) {
        if (arc_conditions) {
          result = ARC;
        }
      }
    }
    
    
    m_classification = result;
    m_isClassified = true;
    
    return result;
  }

  public int reclassify() {
    m_isClassified = false;
    m_classification = 0;
    return classify();
  }

  protected Polygon makePolyFromFit(Vertex fit[]) {
    Polygon ret = new Polygon(fit);

    /**
     * Computing the indices from the original data points so they
     * can be accessed from the polygon object.
     **/
    int indices[] = new int[fit.length];
    for (int i = 0; i < indices.length; i++) {
      indices[i] = fit[i].index;
    }
    ret.setIndices(indices);

    return ret;
  }

  /**
   *
   * Updates LSQEs
   *
   **/
  public void updateLSQEs() {
    circle_LSQE = stroke_data.leastSquaresForCircle();
    speed_LSQE = AWTUtil.leastSquaresForPolygon(speed_fit, points);
    direction_LSQE = AWTUtil.leastSquaresForPolygon(direction_fit, points);
    hybrid_fit_LSQE = AWTUtil.leastSquaresForPolygon(hybrid_fit, points);
    line_LSQE = AWTUtil.leastSquaresForLine(line_fit, points);
    general_path_LSQE = Blackboard.general_path.getLSQError(stroke_data);
    greedy_fit_LSQE = AWTUtil.leastSquaresForPolygon(greedy_fit, points);
    
    if( arc_fit != null ) {
      arc_LSQE = arc_fit.leastSquaresError();
    }
    if( spiral_fit != null ) {
      spiral_LSQE = spiral_fit.leastSquaresError();
    }
    
    //System.out.println(hybrid_fit_LSQE + "\t" + greedy_fit_LSQE);
    if (greedy_fit_LSQE < hybrid_fit_LSQE && greedy_polygon_fit)
    {
      final_fit_LSQE = greedy_fit_LSQE;
      final_fit = greedy_fit;
    }else
    {
      final_fit_LSQE = hybrid_fit_LSQE;
      final_fit = hybrid_fit;
    }
    
  }

  /**
   *
   * Returns the set of types that fit the stroke with error value less
   * than the bound specified, (not necessarily sorted by goodness).
   *
   **/
  public int[] classify(double error_bound) {
    // make sure to run the classification and find the errors first.
    classify();
    int length = 0;

    length += (final_fit_LSQE < error_bound) ? 1 : 0;
    length += (line_LSQE < error_bound) ? 1 : 0;
    length += (general_path_LSQE < error_bound) ? 1 : 0;
    length += (circle_LSQE < error_bound) ? 1 : 0;

    int result[] = new int[length];
    length = 0;
    if (final_fit_LSQE < error_bound) {
      result[length] = POLYGON;
      length++;
    }
    if (line_LSQE < error_bound) {
      result[length] = LINE;
      //System.out.println("Recognized line BC error bound is high");
      length++;
    }
    if (general_path_LSQE < error_bound) {
      result[length] = COMPLEX;
      length++;
    }
    if (circle_LSQE < error_bound) {
      result[length] = ELLIPSE;
      length++;
    }

    return result;
  }

  /**
   *
   * Returns an array of approximations to the input stroke
   * sorted by their goodness.
   *
   **/
  public Approximation[] classifyAndRank() {
    classify();
    Approximation approximations[] = new Approximation[4];

    approximations[0] = new Approximation(line_fit, line_LSQE);
    approximations[1] = new Approximation(ellipse_fit, circle_LSQE);
    approximations[2] = new Approximation(general_path_fit, general_path_LSQE);
    approximations[3] = new Approximation(polygon_fit, final_fit_LSQE);

    Arrays.sort(approximations, approximations[0]);

    approximations[0].setError(approximations[0].getError()
        / approximations[3].getError() * 10);
    approximations[1].setError(approximations[1].getError()
        / approximations[3].getError() * 10);
    approximations[2].setError(approximations[2].getError()
        / approximations[3].getError() * 10);
    approximations[3].setError(10);

    return approximations;
  }

  /**
   *
   * Returns true if the best fit for strokeData is a polygon.
   *
   **/
  public boolean isPolygon() {
    return (classify() == POLYGON);
  }

  /**
   *
   * Returns true if the best fit polygon has an error of less than
   * bound.  The error is  squared error 
   *
   **/
  public boolean isPolygon(double error_bound) {
    return (final_fit_LSQE < error_bound);
  }

  /**
   *
   * Returns true if the best fit for the strokeData is an Ellipse.
   *
   **/
  public boolean isEllipse() {
    return (classify() == ELLIPSE);
  }

  /**
   *
   * Returns true if the best fit ellipse for the strokeData is less than
   * error_bound.
   *
   **/
  public boolean isEllipse(double error_bound) {
    return (circle_LSQE < error_bound);
  }

  /**
   *
   * Returns true if the best fit for the strokeData is a complex shape.
   *
   **/
  public boolean isComplex() {
    return (classify() == COMPLEX);
  }

  /**
   *
   * Returns true if the best fit complex shape has error measure less than
   * error_bound.
   *
   **/
  public boolean isComplex(double error_bound) {
    return (general_path_LSQE < error_bound);
  }

  /**
   *
   * Returns true if the best fit shape for the strokeData is a Line.
   *
   **/
  public boolean isLine() {
    return (classify() == LINE);
  }

  /**
   *
   * Returns true if the best fit line has an error of less than error_bound.
   *
   **/
  public boolean isLine(double error_bound) {
    return (line_LSQE < error_bound);
  }
  
  /**
   * Returns true if the best fit for strokeData is an arc
   */
  public boolean isArc() {
    return (classify() == ARC);
  }
  
  /**
   * Returns true if the best fit for strokeData is an spiral
   */
  public boolean isSpiral() {
    return (classify() == SPIRAL);
  }
  

  /**
   * 
   * Returns the error between the best fit polygon and the strokeData. The
   * error is calculated according to the ErrorCalculator, which uses squared
   * error if not set by the user.
   * 
   */
  public double getPolygonError() {
    return final_fit_LSQE;
  }

  /**
   *
   * Returns the error between the best fit ellipse and the strokeData.
   * The error is calculated according to the ErrorCalculator, which uses
   * squared error if not set by the user.
   *
   **/
  public double getEllipseError() {
    return circle_LSQE;
  }

  /**
   *
   * Returns the error between the best fit GeneralPath and the strokeData.
   * The error is calculated according to the ErrorCalculator, which uses
   * squared error if not set by the user.
   *
   **/
  public double getComplexError() {
    return general_path_LSQE;
  }

  /**
   *
   * Returns the error between the best fit line and the strokeData.
   * The error is calculated according to the ErrorCalculator, which uses
   * squared error if not set by the user.
   *
   **/
  public double getLineError() {
    return line_LSQE;
  }

  /**
   * Returns the error between the best fit arc and the strokeData.
   */
  public double getArcError() {
    return arc_LSQE;
  }
  
  /**
   * 
   * Returns the error between the best fit spiral and the strokeData.
   * 
   */
  public double getSpiralError() {
    return spiral_LSQE;
  }

  
  /**
   *
   * Returns the best fit Polygon for the StrokeData.  I have used the
   * class Polygon here because we are using a standard library of
   * shapes which may or may not be the same as the Java defined shapes.
   *
   **/
  public Polygon getPolygonApproximation() {
    return polygon_fit;
  }

  /**
   * This method returns a polygon made of a subset of the vertices of
   * the hybrid_fit polygon.  If removing a vertex does not increase
   * the error by more than tolerance, then that vertex is removed.
   **/
  public Polygon getPolygonApproximation(double tolerance) {
    // get the indecies from the best fit.
    int[] vertIndex = new int[final_fit.length];
    for (int i = 0; i < final_fit.length; i++) {
      vertIndex[i] = final_fit[i].index;
    }

    // filter the verticies that aren't very helpful
    int[] newVertIndex;
    newVertIndex = stroke_data.filterVerticesByLSQE(vertIndex, tolerance);
    newVertIndex = stroke_data.filterCollinearVertices(newVertIndex);

    // now convert the indecies back to vertex objects
    Vertex[] verts = new Vertex[newVertIndex.length];
    for (int i = 0; i < newVertIndex.length; i++) {
      verts[i] = points[newVertIndex[i]];
    }

    return new Polygon(verts);
  }

  /**
   *
   * Returns the best fit Ellipse for the StrokeData.  I have used the
   * class Ellipse here because we are using a standard library of
   * shapes which may or may not be the same as the Java defined shapes.
   *
   **/
  public Ellipse getEllipseApproximation() {
    return ellipse_fit;
  }

  /**
   *
   * Returns the best fit GeneralPath for the StrokeData.  I have used the
   * class GeneralPath here because we are using a standard library of
   * shapes which may or may not be the same as the Java defined shapes.
   *
   **/
  public GeneralPath getComplexApproximation() {
    return general_path_fit;
  }

  /**
   *
   * Returns the best fit Line for the StrokeData.  I have used the
   * class Line here because we are using a standard library of
   * shapes which may or may not be the same as the Java defined shapes.
   *
   **/
  public Line getLineApproximation() {
    return line_fit;
  }

  /**
   * Returns the best fit Acr for the StrokeData. Java defined shapes.
   */
  public Arc getArcApproximation() {
    return arc_fit;
  }
  
  /**
   * Returns the best fit Spiral for the StrokeData. Java defined shapes.
   */
  public Spiral getSpiralApproximation() {
    return spiral_fit;
  }
 
  public GeometricObject getApproximation() {
    switch (classify()) {
    case POLYGON:
      return getPolygonApproximation();
    case ELLIPSE:
      return getEllipseApproximation();
    case COMPLEX:
      return getComplexApproximation();
    case LINE:
      return getLineApproximation();
    case ARC:
      return getArcApproximation();
    case SPIRAL:
      return getSpiralApproximation();
    default:
      return null;
    }
  }

  public StrokeData getStrokeData() {
    return stroke_data;
  }

}
