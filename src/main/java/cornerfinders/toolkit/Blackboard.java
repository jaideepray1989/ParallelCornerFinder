// (c) MIT 2003-4.  All rights reserved.

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


  /**
  *
  * See the end of the file for the log of changes.
  *
  * $Author: hammond $
  * $Date: 2006-11-22 22:54:36 $
  * $Revision: 1.1 $
  * $Headers$
  * $Id: Blackboard.java,v 1.1 2006-11-22 22:54:36 hammond Exp $
  * $Name:  $
  * $Locker:  $
  * $Source: /cvsroot/tamudrg/code/src/edu/mit/sketch/toolkit/Blackboard.java,v $
  *
  **/
import edu.mit.sketch.geom.GeneralPath;
import edu.mit.sketch.geom.GeometryUtil;
import edu.mit.sketch.geom.Point;
import edu.mit.sketch.geom.Range;
import edu.mit.sketch.geom.Vertex;
import edu.mit.sketch.ui.Tablet;
import edu.mit.sketch.util.AWTUtil;
import edu.mit.sketch.util.Util;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.QuadCurve2D;
import java.util.Arrays;
import java.util.Vector;

/**
  *
  * Polygons recognized by the tablet are sent here. This class 
  * decides between candidate polygons and returns it.
  *
  **/

public
class Blackboard
{
    /**
    *
    * The array of hybrid fits obtained from speed and direction 
    * fits.
    *
    **/
    public static Fit hybrid_fits[];


    /**
    *
    * A general path object obtained from the hybrid fits.
    *
    **/
    public static GeneralPath general_path;


    /**
    *
    * The ranges for the segments in the general_path
    *
    **/
    public static Range ranges[];


    /**
    *
    * Index difference tolerance. 
	* index_difference and 
    * distance_difference are used during computation
	* of intersection etc.
	*
    **/
    public static int index_difference = 3;

    
    /**
    *
    * Pixel distance tolerance
	* index_difference and 
    * distance_difference are used during computation
	* of intersection etc.
    *
    **/
    public static double distance_difference = 5.0;

    /**
    *
    * Curvature vertices
    *
    **/
    private static Vertex curvature_vertices[];

    /**
    *
    * Best index (index of the best fit).
    *
    **/
     public static int   best_index   = 0;

    /**
     *
     * Returns the ranges for the segments
     *
    **/
    public static Range[] getRanges()
    {  
      return Range.cloneRanges( ranges );
    }

    /**
    *
    * a and b are two possible polygon matches for points. Return
    * a fit by combining info from a and b.
    *
    **/
    public static Vertex[]
    decide( Vertex     speed_fit[], 
            Vertex     direction_fit[], 
            Point      points[],
            StrokeData data )
    {        
        hybrid_fits = getHybridFits( speed_fit, direction_fit, points );

        double speed_LSQE      = leastSquaresForPolygon( speed_fit, points );
        double direction_LSQE  = leastSquaresForPolygon( direction_fit,points );

		if ( !Tablet.very_quiet ) {
	        System.out.println( "LSQE for speed      : " + speed_LSQE       );
    	    System.out.println( "LSQE for direction  : " + direction_LSQE  );
		}
		
        Arrays.sort( hybrid_fits, hybrid_fits[0] );

        int    min_vertices = Integer.MAX_VALUE;
        best_index   = 0; 
        for ( int i=0; i<hybrid_fits.length; i++ ) {
            if ( ( hybrid_fits[i].getLSQError()   < Tablet.LSQE_treshold ) &&
                 ( hybrid_fits[i].vertices.length < min_vertices         ) ) {
                min_vertices = hybrid_fits[i].vertices.length;
                best_index   = i; 
            }
        }
        
        curvature_vertices = 
            getCurvatureFit( points, speed_fit, 1.05 );
        
        general_path = computeGeneralPath( speed_fit,
                                           curvature_vertices,
                                           hybrid_fits,
                                           data );
                                           
        general_path = adjustGeneralPath( general_path, data );
        
        return hybrid_fits[best_index].vertices;
    }
    
        
    /**
	*
    * Returns a curvature fit. 	This is done by adding points
	* between vertices to indicate that this portion should be
	* approximated by curves. (an old version).
    *
    **/
    public static Vertex[]
    getCurvatureFitOld( Point points[], Vertex vertices[], double scale )
    {
        Vector result = new Vector();
        
        double scaled_distance = 0.0;
        double length   = 0.0;
        for ( int i=0; i<vertices.length-1; i++ ) {
            scaled_distance = vertices[i].distance( vertices[i+1] ) * scale;
            length          = 0.0;
            result.addElement( new Vertex( vertices[i] ) );
            for ( int j=vertices[i].index; j<vertices[i+1].index; j++ ) {
                length += points[j].distance( points[j+1] );
                System.out.println( j + "scaled distance = " + scaled_distance + 
                                    " length  = " + length );
                if ( length > scaled_distance ) {
                    System.out.println( j + "the treshold exceeded for " +
                                        "scaled distance = " + scaled_distance + 
                                        " length  = " + length );

                    result.addElement( new Vertex( points[j+1] ) );
                    scaled_distance = points[j+1].distance(vertices[i+1])*scale;
                    length          = 0.0;
                }
            }
            result.addElement( new Vertex( vertices[i+1] ) );
        }
        
        return Vertex.vectorToArray( result );
    }
    
        
    /**
    *
    * Returns a curvature fit. 	This is done by adding points
	* between vertices to indicate that this portion should be
	* approximated by curves. 
    *
    **/
    public static Vertex[]
    getCurvatureFit( Point points[], Vertex vertices[], double scale )
    {
        Vector result = new Vector();
        
        double  scaled_distance = 0.0;
        double  length          = 0.0;
        boolean modified        = false;
        for ( int i=0; i<vertices.length-1; i++ ) {
            scaled_distance = vertices[i].distance( vertices[i+1] ) * scale;
            length          = 0.0;
            result.addElement( new Vertex( vertices[i] ) );
            for ( int j=vertices[i].index; j<vertices[i+1].index; j++ ) {
                length += points[j].distance( points[j+1] );
            }
            if ( ( length > scaled_distance                        ) && 
                 ( ( vertices[i+1].index - vertices[i].index ) > 4 ) ) {
                int    new_index  = (vertices[i].index + vertices[i+1].index)/2;
                Vertex new_vertex = new Vertex( points[new_index] );
                new_vertex.setIndex( new_index );
                result.addElement( new Vertex( points[new_index] ) );
                modified      = true;
            }
        }
        result.addElement( new Vertex( vertices[vertices.length-1] ) );
        
        Vertex result_vertices[] = Vertex.vectorToArray( result );
        
        if ( modified ) {
			if ( !Tablet.very_quiet ) {
	            System.out.println( "recursive call " + vertices.length + 
    	                            ", "              + result_vertices.length );
			}
            return getCurvatureFit( points, result_vertices, scale );
        } else {
            return result_vertices;
        }
    }


    /**
    *
    * Computes the hybrid fits from speed_fit and direction_fit
    *
    **/
    public static Fit[]
    getHybridFits( Vertex speed_fit[], Vertex direction_fit[], Point points[] ) 
    {
        double speed_LSQE     = leastSquaresForPolygon( speed_fit,     points );
        double direction_LSQE = leastSquaresForPolygon( direction_fit, points );

        int bias = ( speed_LSQE < direction_LSQE ) ? 0 : 1;
        
        Vertex intersection[] = 
            getIntersection( speed_fit, 
                             direction_fit,  
                             bias );
            
        Vertex points_left_out_in_speed_fit[] = 
            getSymmetricDifference( speed_fit, 
                                    intersection, 
                                    intersection, 
                                    bias );
            
        Vertex points_left_out_in_direction_fit[] = 
            getSymmetricDifference( direction_fit, 
                                    intersection, 
                                    intersection, 
                                    bias );
        Vertex comparator = new Vertex();
        
        Arrays.sort( points_left_out_in_speed_fit,     comparator );
        Arrays.sort( points_left_out_in_direction_fit, comparator );

        if ( Tablet.debug ) {
            printPoints( speed_fit,                        "speed" );
            printPoints( direction_fit,                    "direction" );
            printPoints( intersection,                     "intersection" );
            printPoints( points_left_out_in_speed_fit,     "speed_prime" );
            printPoints( points_left_out_in_direction_fit, "direction_prime" );        
        }
        
        double augmented_speed_LSQE;
        double augmented_direction_LSQE;
        double augmented_intersection_LSQE;
        Vector fits                         = new Vector();        
        Vertex augmented_by_speed_fit[]     = null;
        Vertex augmented_by_direction_fit[] = null;
        Vertex augmented_intersection[] = Vertex.cloneVertices( intersection );
        int    speed_pointer     = points_left_out_in_speed_fit.length;
        int    direction_pointer = points_left_out_in_direction_fit.length;
        int    max_iterations    = speed_pointer + direction_pointer;
        fits.addElement( new Fit( 
                        points, 
                        Vertex.cloneVertices( direction_fit ) ) );
        fits.addElement( new Fit( 
                        points, 
                        Vertex.cloneVertices( speed_fit ) ) );
        fits.addElement( new Fit( 
                        points, 
                        Vertex.cloneVertices( intersection ) ) );
        for ( int i=0; i<max_iterations; i++ ) {
            augmented_speed_LSQE        = Double.MAX_VALUE;
            augmented_direction_LSQE    = Double.MAX_VALUE;
            augmented_intersection_LSQE = Double.MAX_VALUE;
            if ( speed_pointer > 0 ) {
                augmented_by_speed_fit = insertVertex( 
                    augmented_intersection,
                    points_left_out_in_speed_fit[speed_pointer-1] );
                augmented_speed_LSQE   = leastSquaresForPolygon(
                                             augmented_by_speed_fit,
                                             points );
				if ( !Tablet.very_quiet ) {
                	System.out.println( "LSQE when augmented by speed     = " +
                	                    augmented_speed_LSQE );
				}
            }
            if ( direction_pointer > 0 ) {
                augmented_by_direction_fit = insertVertex( 
                    augmented_intersection,
                    points_left_out_in_direction_fit[direction_pointer-1] );
                augmented_direction_LSQE   = leastSquaresForPolygon(
                                             augmented_by_direction_fit,
                                             points );
				if ( !Tablet.very_quiet ) {
                	System.out.println( "LSQE when augmented by direction = " +
                	                    augmented_direction_LSQE );
				}
            }
            
            if ( augmented_speed_LSQE < augmented_direction_LSQE ) {
				if ( !Tablet.very_quiet ) {
                	System.out.println( "Including vertex from speed fit" );
				}
                augmented_intersection = augmented_by_speed_fit;
                augmented_intersection_LSQE = augmented_speed_LSQE;
                speed_pointer--;
            } else {
				if ( !Tablet.very_quiet ) {
                	System.out.println( "Including vertex from direction fit" );
				}
                augmented_intersection = augmented_by_direction_fit;
                augmented_intersection_LSQE = augmented_direction_LSQE;
                direction_pointer--;
            }
            if ( augmented_direction_LSQE == Double.MAX_VALUE )
                break;
            fits.addElement( new Fit( 
                            points, 
                            Vertex.cloneVertices( augmented_intersection ),
                            augmented_intersection_LSQE ) );
        }
        
        Fit result[] = new Fit[fits.size()];
        for ( int i=0; i<fits.size(); i++ ) {
            result[i] = ((Fit)(fits.elementAt(i)));
        }
                
        return result;
    }

    
    /**
    *
    * Improve the fit by reducing the least square error.
    *
    **/
    public static GeneralPath
    adjustGeneralPath( GeneralPath general_path,
                       StrokeData  data )
    {
        int          type;
        int          segment;
        PathIterator path_iterator;
        Shape        shape            = null;
        Point        last_point       = new Point();
        double       coefficients[]   = new double[6];
        boolean      path_modified    = false;
        GeneralPath  updated_path     = new GeneralPath();
        Range        updated_ranges[] = Range.cloneRanges( ranges );
        updated_path.setRanges(Range.cloneRanges(updated_ranges));
        
        path_iterator  = general_path.getPathIterator( new AffineTransform() );

        segment = 0;        
        while ( !path_iterator.isDone() ) {
            type = path_iterator.currentSegment( coefficients );

            switch ( type ) {
                case PathIterator.SEG_CUBICTO :
                    shape = new CubicCurve2D.Double( last_point.x,
                                                     last_point.y,
                                                     coefficients[0],
                                                     coefficients[1],
                                                     coefficients[2],
                                                     coefficients[3],
                                                     coefficients[4],
                                                     coefficients[5] );
                    last_point.setLocation( coefficients[4], coefficients[5] );
                    segment++;
                    break;

                case PathIterator.SEG_QUADTO :
                    shape = new QuadCurve2D.Double( last_point.x,
                                                    last_point.y,
                                                    coefficients[0],
                                                    coefficients[1],
                                                    coefficients[2],
                                                    coefficients[3] );
                    last_point.setLocation( coefficients[2], coefficients[3] );
                    segment++;
                    break;

                case PathIterator.SEG_LINETO :
                    shape = new Line2D.Double( last_point.x,
                                               last_point.y,
                                               coefficients[0],
                                               coefficients[1] );
                    last_point.setLocation( coefficients[0], coefficients[1] );
                    segment++;
                    updated_path.lineTo( (float)coefficients[0],
                                         (float)coefficients[1] );
                    break;

                case PathIterator.SEG_MOVETO :
                    shape = null;
                    last_point.setLocation( coefficients[0], coefficients[1] );
                    updated_path.moveTo( (float)coefficients[0], 
                                         (float)coefficients[1] );
                    break;

                case PathIterator.SEG_CLOSE :
                    shape = null;
                    updated_path.closePath();
                    break;
                    
                default:
                    System.out.println( "Error: No matching case" );
            }
            
            if ( ( type == PathIterator.SEG_CUBICTO ) ||
                 ( type == PathIterator.SEG_QUADTO  ) ) {
                double lsq_error = Double.MAX_VALUE;
                
                lsq_error = AWTUtil.getCurveLSQEror( shape,
                                                     data, 
                                                     ranges[segment-1] );
                
                if ( Tablet.debug ) {
                    System.out.println( "LSQ Error for segment " + (segment-1) + 
                                        " = " + lsq_error );
                }
                int difference = ranges[segment-1].max - ranges[segment-1].min;
                if ( ( lsq_error  > 10 ) && 
                     ( !path_modified  ) && 
                     (  difference > 8 ) ) {
				if ( !Tablet.very_quiet ) {
                    System.out.println( "Breaking up segment " + (segment-1) );
                }   
                    Range segment_range = ranges[segment-1];
                    int mid_index = ( segment_range.min + segment_range.max )/2;

                    Range left_range    = new Range( segment_range.min,
                                                     mid_index );

                    Range right_range   = new Range( mid_index,
                                                     segment_range.max );
                    updated_ranges[segment-1] = left_range;
                    updated_ranges = Range.appendRanges( updated_ranges, 
                                                         right_range );
                                                         
					if ( !Tablet.very_quiet ) {
                    	System.out.println( "left_range  " + left_range );
                	    System.out.println( "right_range " + right_range );
					}

                    Point  control_points[];
                    Vertex vertices[] = data.vertices;
                    control_points = getControlPoints( vertices[left_range.min],
                                                       vertices[left_range.max],
                                                       data,
                                                       0,
                                                       0 );
                                                       
                    updated_path.curveTo( (float)control_points[0].x,
                                          (float)control_points[0].y,
                                          (float)control_points[1].x,
                                          (float)control_points[1].y,
                                          (float)vertices[left_range.max].x,
                                          (float)vertices[left_range.max].y );

                    control_points= getControlPoints( vertices[right_range.min],
                                                      vertices[right_range.max],
                                                       data,
                                                       0,
                                                       0 );
                                                       
                    updated_path.curveTo( (float)control_points[0].x,
                                          (float)control_points[0].y,
                                          (float)control_points[1].x,
                                          (float)control_points[1].y,
                                          (float)vertices[right_range.max].x,
                                          (float)vertices[right_range.max].y );
                    path_modified = true;
                } else {
                    if ( type == PathIterator.SEG_CUBICTO ) {
                        updated_path.curveTo( (float)coefficients[0],
                                              (float)coefficients[1],
                                              (float)coefficients[2],
                                              (float)coefficients[3],
                                              (float)coefficients[4],
                                              (float)coefficients[5] );
                    }
                    if ( type == PathIterator.SEG_QUADTO ) {
                        updated_path.quadTo( (float)coefficients[0],
                                             (float)coefficients[1],
                                             (float)coefficients[2],
                                             (float)coefficients[3] );
                    }
                }
            }
            
			if ( !Tablet.very_quiet ) {
            	System.out.println( "updated_path segment count = " + 
            	                    updated_path.getSegmentCount() );
			}
			
            path_iterator.next();
        }
        
        if ( path_modified ) {
			if ( !Tablet.very_quiet ) {
            	System.out.println( "Making a recursive call :" );
            	System.out.println( "Old ranges.length = "  + 
                	                ranges.length );
            	System.out.println( "Old general_path segment count = " +
                	                general_path.getSegmentCount() + "\n");
			}						

            Blackboard.ranges       = updated_ranges;
            Blackboard.general_path = updated_path;
            ranges       = updated_ranges;
            general_path = updated_path;

			if ( !Tablet.very_quiet ) {
            System.out.println( "New ranges.length = "  + 
                                ranges.length );
            System.out.println( "New general_path segment count = " +
                                general_path.getSegmentCount() + "\n");
			}						
            
            return adjustGeneralPath( updated_path, data );
        } else {
            return general_path;
        }
    }


    /**
    *
    * Compute a GeneralPath given the information in the arguments.
    *
    **/
    public static GeneralPath
    computeGeneralPath( Vertex     intersection[],
                        Vertex     superset[],
                        Fit        fits[],
                        StrokeData data )
    {
        if ( Tablet.debug ) {
            Util.printArray( intersection, "Before redundant point elimination subset" );
            Util.printArray( superset,     "Before redundant point elimination superset" );
        }
        intersection = removeCloseVertices( intersection, 
                                            index_difference, 
                                            distance_difference );
        superset     = removeCloseVertices( superset, 
                                            index_difference,
                                            distance_difference );
        
        if ( Tablet.debug ) {
            Util.printArray( intersection, "Before overlay subset" );
            Util.printArray( superset,     "Before overlay superset" );
        }
		
		if ( !Tablet.very_quiet ) {
       		System.out.println( "Doing overlay..." );
		}
        
		superset = overlay( intersection, superset, 
                            index_difference, 
                            distance_difference );
        
        if ( Tablet.debug ) {
            Util.printArray( intersection, "After overlay subset" );
            Util.printArray( superset,     "After overlay superset" );
        }        
        Vertex excluded_points[];

        Vector      range_vector = new Vector();
        GeneralPath general_path = new GeneralPath();
        general_path.moveTo( (float)intersection[0].x, (float)intersection[0].y );
        
        Vertex last_added_vertex = intersection[0]; 
        Vertex next_vertex;
        
        for ( int i=0; i<intersection.length-1; i++ ) {
            excluded_points = getContiguosExcludedPointSequence( 
                                i,
                                intersection, 
                                superset );
                  
            if ( Tablet.debug ) {
                System.out.println( "\n# # # # # # # # # # # # # # # # # # #" );
                Util.printArray( excluded_points,
                    "excluded points after intersection[" + i + "]" );
            }
            if ( excluded_points.length == 0 ) {
                if ( Tablet.debug ) {
                    System.out.println( "No points excluded between indices " + 
                                        i + ", " + (i+1) + " in subset" );
                    System.out.println( "Adding line from: " + last_added_vertex + 
                                        " to: "              + intersection[i+1] );
                }
                general_path.lineTo( (float)intersection[i+1].x, (float)intersection[i+1].y );
                range_vector.addElement( new Range( last_added_vertex.index,
                                                    intersection[i+1].index ) );
                last_added_vertex = intersection[i+1];
            } else {
                excluded_points = Vertex.appendVertices( excluded_points, 
                                                         intersection[i+1] );
                int points_left         = excluded_points.length;
                int unadded_point_index = 0;
                int left_offset;
                int right_offset;
                Point control_points[];

                while ( points_left > 0 ) {
                    left_offset  = ( ( excluded_points.length > 1 ) && 
                                     ( unadded_point_index   != 0 ) ) ? 0 : 3;
                    right_offset = ( ( excluded_points.length > 1 ) && 
                                     ( unadded_point_index   !=
                                       excluded_points.length - 1 ) ) ? 0 : 3;
                    next_vertex    = excluded_points[unadded_point_index];
                    control_points = getControlPoints( last_added_vertex,
                                                       next_vertex,
                                                       data,
                                                       left_offset,
                                                       right_offset );
                      if ( Tablet.debug ) {
                        System.out.println( "Adding curve\n\t" + last_added_vertex +
                                            "\n\t"             + next_vertex );
                    }
                    general_path.curveTo( (float)control_points[0].x,
                                          (float)control_points[0].y,
                                          (float)control_points[1].x,
                                          (float)control_points[1].y,
                                          (float)next_vertex.x,
                                          (float)next_vertex.y );
                    range_vector.addElement( new Range( last_added_vertex.index,
                                                        next_vertex.index ) );
                    last_added_vertex = excluded_points[unadded_point_index];
                    points_left         -=1;
                    unadded_point_index +=1;
                      if ( Tablet.debug ) {
                        System.out.println( "points_left = " + points_left );
                    }
                }
            }
        }
  
        ranges = Range.vectorToArray( range_vector );
        general_path.setRanges(Range.cloneRanges(ranges));
        return general_path;
    }
    
    
    /**
    *
    * Return the sequence of contiguos point sequence that 
    * is included in superset, but is excluded in subset. We
    * start the search from start_index in subset. So the indices
    * of the vertices returned are strictly between the indices of
    * subset[start_pointer] and subset[start_pointer+1]. If 
    * start_pointer points to the last in the subset, then all 
    * vertices in superset whose indices are larger than the 
    * index of the last element in subset are returned. This 
    * method assumes that the input vertices are sorted by index.
    *
    **/
    public static Vertex[]
    getContiguosExcludedPointSequence( int    start_pointer, 
                                       Vertex subset[], 
                                       Vertex superset[] )
    {
        Vector excluded_points = new Vector();
        
        int beginning_pointer_in_superset = Integer.MAX_VALUE;
        for ( int i=0; i<superset.length; i++ ) {
            if ( superset[i].index > subset[start_pointer].index ) {
                beginning_pointer_in_superset = i;
                break;
            }
        }
        
        if ( beginning_pointer_in_superset == Integer.MAX_VALUE ) {
            if ( Tablet.debug ) {
                System.out.println( "NO EXCLUDED POINT SEQUENCE" );
            }
            return Vertex.vectorToArray( excluded_points );
        }
        
        if ( start_pointer+1 == subset.length ) {
            for ( int i = beginning_pointer_in_superset; 
                  i<superset.length; 
                  i++ ) { 
                excluded_points.addElement( superset[i] );
            }
            
            return Vertex.vectorToArray( excluded_points );
        }
        
        for ( int i = beginning_pointer_in_superset;
              ( ( i<superset.length ) &&
                ( superset[i].index < subset[start_pointer+1].index ) );
              i++ ) {
            excluded_points.addElement( superset[i] );
        }
        
        return Vertex.vectorToArray( excluded_points );
    }
        

    /**
    *
    * calculate the lsq error. points is the actual data points,
    * polgon is the match 
    *
    **/
    public static double
    leastSquaresForPolygon( Vertex vertices[], Point points[] )
    {
        return AWTUtil.leastSquaresForPolygon( vertices, points );
    }
    
    
    /**
    *
    * get the indices of output_polygon in the array points.
    *
    **/
    public static int[]
    getPolygonIndices( Point output_polygon[], Point points[] )
    {
        int last_index = points.length-1;
        
        if ( (output_polygon == null) || (output_polygon.length<2) ) {
            System.err.println("Error: getPolgonIndices no polygon detected.");
        }
        
        if ( Tablet.debug ) {
            for ( int i=0; i<output_polygon.length; i++ ) {
                System.out.println( "@ " + i + " " + output_polygon[i] );
            }
        }
        
        int index     = 0;
        int indices[] = new int[output_polygon.length];

        for ( int i=0; (i<=last_index) && (index<output_polygon.length); i++ ) {
            if ( points[i].equals( output_polygon[index] ) ){
                indices[index] = i;
                if ( Tablet.debug ) {
                    System.out.println( "# " + i + " " + index +
                                        " " + output_polygon[index] + 
                                        " " + points[indices[index]] + " equal");
                }
                index++;
            } else {
                if ( Tablet.debug ) {
                    System.out.println( "# " + i + " " + index +
                                        " " + output_polygon[index] + 
                                        " " + points[i] + " not equal");
                }
            }
        }
        
        if ( Tablet.debug ) {
            for ( int i=0; i<indices.length; i++ ) {
                 System.out.println( "* " + i + " " + indices[i] +
                                     " " + output_polygon[i] +
                                     " " + points[indices[i]] );
             }
        System.out.println( index + " " + indices.length );
        }
        
        if ( index != indices.length ) {
            System.err.println( "Error: getPolygonIndices missed a point" );
            for ( int i=0; i<last_index; i++ ) {
                System.err.println( "Error: getPolgonIndices " + 
                                     i + " " + points[indices[i]] );
            }
        }
        return indices;
    }
    
    
    /**
    *
    * Print the points array.
    *
    **/
    public static void
    printPoints( Point points[], String name )
    {
        Util.printArray( points, name );
    }
    
    
    /**
    *
    * Return true if the array contains a Vertex that is sufficiently
    * close to the argument vertex given the tolerances. 
    *
    **/
    public static boolean
    containsWithTolerance( Vertex vertices[],
                           Vertex vertex,
                           int    index_difference_tolerance, 
                           double distance_tolerance )
    {
        for ( int i=0; i<vertices.length; i++ ) {
            if ( ( Math.abs( vertex.index - vertices[i].index ) < 
                   index_difference_tolerance                      ) ||
                 ( vertex.distance( vertices[i] )                < 
                   distance_tolerance                              ) ) {
                return true;
            }
        }
        
        return false;
    }
    
    
    /**
    *
    * Get the symmetric difference of a and b. Use a reasonable 
    * measure of "closeness" for the points in a and b so that 
    * points are considered to be equal even if they do not have 
    * the same index. Intersection can be null or it may contain
    * the correct intersection of a and b.
    *
    **/
    public static Vertex[]
    getSymmetricDifference( Vertex a[], 
                            Vertex b[], 
                            Vertex intersection[], 
                            int bias )
    {
        if ( intersection == null ) {
            intersection = getIntersection( a, b, bias );
        }
    
        Vector difference = new Vector();
        
        for ( int i=0; i<a.length; i++ ) {
            if ( !containsWithTolerance( intersection, 
                                         a[i], 
                                         index_difference, 
                                         distance_difference ) )
            difference.addElement( new Vertex( a[i] ) );
        }
        for ( int i=0; i<b.length; i++ ) {
            if ( !containsWithTolerance( intersection, 
                                         b[i], 
                                         index_difference,
                                         distance_difference ) )
            difference.addElement( new Vertex( b[i] ) );
        }
        
        Vertex result[] = new Vertex[difference.size()];
        for ( int i=0; i<result.length; i++ ) {
            result[i] = (Vertex)difference.elementAt( i );
        }
        
        return result;
    }
    
    
    /**
    *
    * Overlay subset and superset so the vertices very close to
    * one another are collapsed into one.
    *
    **/
    public static Vertex[]
    overlay( Vertex subset[], 
             Vertex superset[], 
             int    index_tolerance, 
             double distance_tolerance )
    {
        Vector superset_result = new Vector();
        
        if ( ( subset        == null ) || ( superset        == null ) ||
             ( subset.length == 0    ) || ( superset.length == 0    ) ) {
             return superset;
        }

        int index = 0;
        for ( int i=0; i<superset.length; i++ ) {
            index = getIndexInVertices( subset, 
                                        superset[i], 
                                        index_tolerance, 
                                        distance_tolerance );
            if ( index != -1 ) {
                superset_result.addElement( subset[index] );
                if ( Tablet.debug ) {
                    System.out.println( "Replacing item superset[" + i + "] with " +
                                        "subset[" + index + "]" );
                }
            } else {
                superset_result.addElement( superset[i]   );
            }
        }
        
        superset = Vertex.vectorToArray( superset_result );
        
        return superset;
    }
    
    
    /**
    *
    * Return the index in subset if the array contains a Vertex that
    * is sufficiently close to the argument vertex given the 
    * tolerances. Return -1 if no such vertex exists.
    *
    **/
    public static int
    getIndexInVertices( Vertex vertices[],
                        Vertex vertex,
                        int    index_difference_tolerance, 
                        double distance_tolerance )
    {
        for ( int i=0; i<vertices.length; i++ ) {
            if ( ( Math.abs( vertex.index - vertices[i].index ) < 
                   index_difference_tolerance                      ) ||
                 ( vertex.distance( vertices[i] )                < 
                   distance_tolerance                              ) ) {
                return i;
            }
        }
        
        return -1;
    }
    
    
    /**
    *
    * Remove vertices closer than index_tolerance in indices or,
	* distance_tolerance spatially.
    *
    **/
    public static Vertex[]
    removeCloseVertices( Vertex vertices[],
                          int    index_tolerance, 
                          double distance_tolerance )
    {
        Vector result = new Vector();
        
        // We go upto vertices.length-1 because last point always included
        for ( int i=0; i<vertices.length-1; i++ ) { 
            if ( !containsWithTolerance( Vertex.vectorToArray( result ),
                                         vertices[i],
                                         index_tolerance, 
                                         distance_tolerance ) ) {
                result.addElement( vertices[i] );
            }
        }
        // Add the last element seperately
        result.addElement( vertices[vertices.length-1] );
        
        return Vertex.vectorToArray( result );
    }
    
    
    /**
    *
    * Get the common points in a and b. Use a reasonable measure of
    * "closeness" for the points in a and b so that points are 
    * considered to be equal even if they do not have the same 
    * index.
    *
    **/
    public static Vertex[]
    getIntersection( Vertex a[], Vertex b[], int bias )
    {
      if( a == null || b == null ) {
        return new Vertex[0];
      }
        Vector intersection = new Vector();
        
        for ( int i=0; i<a.length; i++ ) {
            for ( int j=0; j<b.length; j++ ) {
		// ACS: Magic number! Upped index offset from 3 to 4
                if ( ( Math.abs( a[i].index - b[j].index ) < 4 ) &&
                     ( a[i].distance( b[j] )               < 5 ) ) {
                    if ( bias == 0 ) {
                        intersection.addElement( new Vertex( a[i] ) );
                    } else {
                        intersection.addElement( new Vertex( b[j] ) );
                    }
                    break;
                }
            }
        }
        
        Vertex result[] = new Vertex[intersection.size()];
        for ( int i=0; i<result.length; i++ ) {
            result[i] = (Vertex)intersection.elementAt( i );
        }
        
        return result;
    }
    
    
    /**
    *
    * Get all the points in a and b. Use a reasonable measure of
    * "closeness" for the points in a and b so that points are 
    * considered to be equal even if they do not have the same 
    * index.
    *
    **/
    public static Vertex[]
    getUnion( Vertex a[], Vertex b[] )
    {
        Vertex union[]     = new Vertex[a.length+b.length];        
        int a_pointer      = 0;
        int b_pointer      = 0;
        int union_pointer  = 0;
        
        while ( ( a_pointer < a.length ) && ( b_pointer < b.length ) ) {
            if ( a[a_pointer].index < b[b_pointer].index ) {
                union[union_pointer] = a[a_pointer];
                union_pointer++;
                a_pointer++;
            } else {
                union[union_pointer] = b[b_pointer];
                union_pointer++;
                b_pointer++;
            }
        }
        
        if ( a_pointer < a.length ) {
            for ( ; union_pointer<union.length; a_pointer++, union_pointer++ ) {
                union[union_pointer] = a[a_pointer];
            }
        }

        if ( b_pointer < b.length ) {
            for ( ; union_pointer<union.length; b_pointer++, union_pointer++ ) {
                union[union_pointer] = b[b_pointer];
            }
        }
        
        return union;
    }
    
    
    /**
    *
    * Print the points assuming with their corresponding indices
    * in real_points. Assume that original points contains all
    * points[i].
    *
    **/
    public static void
    printPointsWithIndices( Point points[], String name, Point real_points[] )
    {
        int indices[] = getPolygonIndices( points, real_points );
        System.out.println( name + ".length is " + points.length );
        for ( int i=0; i<points.length; i++ ) {
            System.out.println( name + "[" + i + "] = " + 
                                points[i] +
                                "\t index = " + indices[i] );
        }
    }
    
    
    /**
    *
    * See if all the certainty values are between 0 and 1.
    *
    **/
    public static void
    checkCertaintyBounds( Vertex vertices[] )
    {
        double certainty = 0.0;
        for ( int i=0; i<vertices.length; i++ ) {
            certainty = vertices[i].certainty;
            if ( ( certainty > 1.0 ) || ( certainty < 0.0 ) ) {
                System.err.println( "ERROR : certainty bound exceeded " + 
                                    certainty );
            }
        }
    }
    
    
    /**
    *
    * Paint the general path
    *
    **/
    public static void
    paintGeneralPath( GeneralPath general_path )
    {
        Graphics2D g2D = (Graphics2D)(Tablet.debug_graphics);
        
        g2D.setColor( Color.magenta );
        g2D.setStroke( new BasicStroke( 1.0f ) );
        general_path.paint( g2D );
    }
    
    
    /**
    *
    * Paint the general path in segments
    *
    **/
    public static void
    paintGeneralPathInSegments( GeneralPath general_path )
    {
        general_path.paintInSegments( (Graphics2D)Tablet.debug_graphics );
    }
    
    
    /**
    *
    * Show convex hulls for the curves
    *
    **/
    public static void
    paintGeneralPathConvexHulls( GeneralPath general_path )
    {
        general_path.paintConvexHulls( (Graphics2D)Tablet.debug_graphics );
    }
    
    
    /**
    *
    * Return the vertices obtained by inserting the input vertex to 
    * the appropriate slot in the input vertices.
    *
    **/
    public static Vertex[]
    insertVertex( Vertex vertices[], Vertex vertex )
    {
        int insertion_place = 0;
        
        Vertex result[] = new Vertex[vertices.length+1];
        
        for ( int i=0; i<vertices.length; i++ ) {
            if ( vertices[i].index > vertex.index ) {
                insertion_place = i;
                break;
            }
        }
        
        if ( insertion_place == 0 ) {
            if ( ( vertices.length == 0 ) || 
                 ( vertices[0].index > vertex.index ) ) {
                insertion_place = 0;  // Redundant, I know...
            } else {
                insertion_place = vertices.length - 1;
            }
        }
        
        for ( int i=0; i<insertion_place; i++ ) {
            result[i] = vertices[i];
        }
        result[insertion_place] = vertex;
        for ( int i=insertion_place+1; i<result.length; i++ ) {
            result[i] = vertices[i-1];
        }
        
        return result;        
    }
    
    
    /**
    *
    * Return the control points to be used for drawing a curve 
    * from v1 to v2.
    *
    **/
    public static Point[]
    getControlPoints( Vertex v1, Vertex v2, StrokeData data )
    {
        Vertex vertices[]       = data.vertices;
        Point  control_points[] = new Point[2];
        double segment_length   = 
            GeometryUtil.segmentLength( vertices, v1.index, v2.index )/3;
        
        Vertex first  = ( v1.index < v2.index ) ? v1 : v2;
        Vertex second = ( v1.index < v2.index ) ? v2 : v1;
        
        int offset = 3;
        int i      = offset;
        int j      = offset;
        
        if ( !( first.index + offset < vertices.length ) ) {
            i = vertices.length - first.index - 1;
        }

        if ( second.index - offset < 0 ) {
            j = 0;
        }
        
        control_points[0]   = new Point();
        control_points[1]   = new Point();
        control_points[0].x = vertices[first.index +i].x - first.x;
        control_points[0].y = vertices[first.index +i].y - first.y;
        control_points[1].x = vertices[second.index-j].x - second.x;
        control_points[1].y = vertices[second.index-j].y - second.y;
        
        control_points[0].scale( segment_length/control_points[0].magnitude() );
        control_points[1].scale( segment_length/control_points[1].magnitude() );
        
        control_points[0].translate( (double)first.x,  (double)first.y  );
        control_points[1].translate( (double)second.x, (double)second.y );
        
        Point result[] = new Point[2];
        
        if ( v1.index >= v2.index ) {
            result[0] = control_points[1];
            result[1] = control_points[0];
        } else {
            result = control_points;
        }

        return result;
    }
    
    
    /**
    *
    * Return the control points to be used for drawing a curve 
    * from v1 to v2.
    *
    **/
    public static Point[]
    getControlPoints( Vertex v1, 
                      Vertex v2, 
                      StrokeData data, 
                      int left_offset,
                      int right_offset )
    {
        Vertex vertices[]         = data.vertices;
        Point  control_points[]   = new Point[2];
        Point  reference_points[] = new Point[2];
        Point  candidates[][]     = new Point[2][2];
        double magnitude = 
            GeometryUtil.segmentLength( vertices, v1.index, v2.index )/3;
        
        Vertex first  = ( v1.index < v2.index ) ? v1 : v2;
        Vertex second = ( v1.index < v2.index ) ? v2 : v1;
        
        int i                    = first.index  + left_offset; 
        int j                    = second.index - right_offset;
        int left_neighbor_index  = first.index  + 3; // For
        int right_neighbor_index = second.index - 3; // comparison
        
        if ( i >= vertices.length ) {
            i = vertices.length-1;
        }
        
        if ( j < 0 ) {
            j = 0;
        }

        if ( left_neighbor_index >= vertices.length ) {
            left_neighbor_index = vertices.length-1;
        }
        
        if ( right_neighbor_index < 0 ) {
            right_neighbor_index = 0;
        }

        candidates[0][0]    = new Point( vertices[i] );
        candidates[0][1]    = new Point( vertices[j] );
        candidates[1][0]    = new Point( vertices[i] );
        candidates[1][1]    = new Point( vertices[j] );
        control_points[0]   = new Point();
        control_points[1]   = new Point();
        reference_points[0] = new Point();
        reference_points[1] = new Point();
        
        reference_points[0].x = vertices[left_neighbor_index ].x - first.x;
        reference_points[0].y = vertices[left_neighbor_index ].y - first.y;
        reference_points[1].x = vertices[right_neighbor_index].x - second.x;
        reference_points[1].y = vertices[right_neighbor_index].y - second.y;
        reference_points[0].scale( magnitude/reference_points[0].magnitude() );
        reference_points[1].scale( magnitude/reference_points[1].magnitude() );
        reference_points[0].translate( (double)first.x,  (double)first.y  );
        reference_points[1].translate( (double)second.x, (double)second.y );
        
        candidates[0][0].x += Math.cos( data.d[i]           ) * magnitude;
        candidates[0][0].y += Math.sin( data.d[i]           ) * magnitude;
        candidates[0][1].x += Math.cos( data.d[j]           ) * magnitude;
        candidates[0][1].y += Math.sin( data.d[j]           ) * magnitude;
        candidates[1][0].x += Math.cos( data.d[i] + Math.PI ) * magnitude;
        candidates[1][0].y += Math.sin( data.d[i] + Math.PI ) * magnitude;
        candidates[1][1].x += Math.cos( data.d[j] + Math.PI ) * magnitude;
        candidates[1][1].y += Math.sin( data.d[j] + Math.PI ) * magnitude;


        for ( int k=0; k<2; k++ ) {
            if ( ( candidates[0][k].distance( reference_points[0] ) ) <
                 ( candidates[1][k].distance( reference_points[0] ) ) ) {
                control_points[k] = candidates[0][k]; 
            } else {
                control_points[k] = candidates[1][k]; 
            }
        }
        
        
        Point result[] = new Point[2];
        
        if ( v1.index >= v2.index ) {
            result[0] = control_points[1];
            result[1] = control_points[0];
        } else {
            result = control_points;
        }

        return result;
    }
}

/**
  *
  * $Log: Blackboard.java,v $
  * Revision 1.1  2006-11-22 22:54:36  hammond
  * mit's drg code, will extract and refactor to get ladder code
  *
  * Revision 1.15  2005/07/25 17:12:10  dpitman
  * sketch.geom package now uses doubles for coordinate values instead of integers. Code in other packages was adjusted because it contained int-based Graphic calls, and thus, the coordinate values had to be type-casted.
  *
  * Revision 1.14  2005/01/27 22:15:10  hammond
  * organized imports
  *
  * Revision 1.13  2004/07/07 06:26:47  snoeren
  * Changed the magic-number definition of index closeness to allow the
  * quadrilateral at the bottom right of Gary7 to be properly recognized as
  * containing four strokes.  Of course, this may or may not have other
  * (negative) repercusions.
  *
  * Revision 1.12  2003/08/08 19:25:53  moltmans
  * Added an error check in the vertex calculation to avoid null pointers.
  *
  * Revision 1.11  2003/05/07 20:58:54  mtsezgin
  *
  * Fixed some problems with the arc recognition in general, and recognition
  * on the acer tablet in particular.
  *
  * Revision 1.10  2003/03/06 01:08:51  moltmans
  * Added copyright to all the files.
  *
  * Revision 1.9  2003/01/06 18:28:25  hammond
  * Polygon can now access ranges for original data points for particular lines
  *
  * Revision 1.8  2002/11/25 02:45:07  hammond
  * made ranges accessible and increased ellipse tolerance to have fewer arcs - now based on size of ellipse
  *
  * Revision 1.6  2002/04/01 23:51:06  moltmans
  * Updated some small bugs in Polygon,  having to do with accessing null
  * original_points, and added some thresholded polygon parsing to filter
  * out small edges.
  *
  * Revision 1.5  2001/11/23 03:22:56  mtsezgin
  * Major reorganization
  *
  * Revision 1.4  2001/11/13 17:54:41  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.3  2001/10/12 23:32:29  mtsezgin
  * Turned off printing...
  *
  * Revision 1.2  2001/10/12 22:25:52  mtsezgin
  * This is a commit of all files.
  * Sho
ap
id
  * vi sux:q
  *
  * Revision 1.1.1.1  2001/03/29 16:25:00  moltmans
  * Initial directories for DRG
  *
  * Revision 1.7  2000/09/06 22:40:32  mtsezgin
  * Combinations of curves and polygons are successfully approximated
  * by straight lines and Bezier curves as appropriate. System works
  * quite reliably.
  *
  * Revision 1.1  2000/06/29 02:09:33  mtsezgin
  * This is the blackboard.
  *
  **/
