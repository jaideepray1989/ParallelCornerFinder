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
 * This class extends SimpleClassifier2 in order to support Arcs with the
 * TabletPC. For some reason, sometimes the tablet pc records a tiny tail
 * at the ends of strokes. This class is written to handle this glicth and
 * some problems raised with higher sampling rate of the tablet.
 *
 */
public
class   SimpleClassifier3
extends SimpleClassifier2
{
    /**
    * Constructs a Classifier object.  You must pass it a StrokeData object.
    * Contains a default error calculator, which is just the squared error.
    **/
    public SimpleClassifier3( StrokeData stroke_data )
    {
        super( stroke_data );
// 	dd_dt_average_scale = 0.5;
// 	speed_average_scale = 0.3;
    }

    /**
    *
    * Returns the best fit type for the strokeData
    *
    **/
    public int classify()
    {
        if( m_isClassified ) {
            return m_classification;
        }

        super.classify();


        speed_fit     = AWTUtil.simplifyPolygon2(
                            stroke_data.getSpeedFit(
                                speed_average_scale ), Math.PI/6 );
	if ( !Tablet.very_quiet ) {
	    System.out.println("Speed fit before " + speed_fit.length );
	}
	if ( speed_fit != null ) {
	  speed_fit     = stroke_data.filterVerticesByLSQE( speed_fit, 1.2 );
	}
	if ( !Tablet.very_quiet ) {
	    System.out.println("Speed fit after " + speed_fit.length );
	}


	  direction_fit =
	    AWTUtil.simplifyPolygon2( stroke_data.getDirectionFit(
	     dd_dt_average_scale ), Math.PI/6 );
	if ( !Tablet.very_quiet ) {
	    System.out.println("Curvature fit before " + direction_fit.length );
	}
	if ( direction_fit != null ) {
	  direction_fit = stroke_data.filterVerticesByLSQE( direction_fit, 1.2 );
	}
	if ( !Tablet.very_quiet ) {
	    System.out.println("Curvature fit after " + direction_fit.length );
	    System.out.println("Final fit after " + final_fit.length );
	}

	if ( speed_fit == null && direction_fit == null ) {
	  return m_classification;
	}

	if ( speed_fit == null ) {
	  final_fit = direction_fit;
	}

	else if ( direction_fit == null ) {
	  final_fit = speed_fit;
	}
	else {

	  final_fit     = Blackboard.decide( speed_fit,
					     direction_fit,
					     points,
					     stroke_data );
	}
	if ( !Tablet.very_quiet ) {
	    System.out.println("Final fit after " + final_fit.length );
	}

	// Sometimes the Tablet PC captures a tail at the end of strokes.
	// Here try to see if we have a very short segment at the end. This
	// test can eventually be made through pressure information.

	if ( !Tablet.very_quiet ) {
	    System.out.println( "Segment length at the end:" +
				final_fit[final_fit.length-1].distance(
			        final_fit[final_fit.length-2] ) );
	}
	if ( final_fit[final_fit.length-1].distance(
	     final_fit[final_fit.length-2] ) <= 10 ) {
	    if ( !Tablet.very_quiet ) {
		System.out.println( "Caught a tail at the end of the stroke." );
	    }
	    Vertex new_final_fit[] = new Vertex[final_fit.length-1];
	    for ( int i=0; i<new_final_fit.length; i++ ) {
		new_final_fit[i] = final_fit[i];
	    }
	    new_final_fit[new_final_fit.length-1] =
	        final_fit[final_fit.length-1];
	    final_fit = new_final_fit;

	}
	if ( final_fit.length > 2 &&
	     final_fit[0].distance( final_fit[1] ) <= 10 ) {
	  if ( !Tablet.very_quiet ) {
	    System.out.println( "Caught a tail at the beginning of the stroke." );
          }
	  Vertex new_final_fit[] = new Vertex[final_fit.length-1];
	  new_final_fit[0] = final_fit[0];
	  for ( int i=2; i<final_fit.length; i++ ) {
	    new_final_fit[i-1] = final_fit[i];
	  }
	  final_fit = new_final_fit;
	}

	if ( m_classification == POLYGON && final_fit.length <= 2 ) {
	  m_classification = LINE;
	}

	polygon_fit = makePolyFromFit( final_fit );

	line_fit.setOriginalVertices( stroke_data.vertices );
	ellipse_fit.setOriginalVertices( stroke_data.vertices );
	polygon_fit.setOriginalVertices( stroke_data.vertices );
	general_path_fit.setOriginalVertices( stroke_data.vertices );

	updateLSQEs();

        if ( m_classification == COMPLEX ) {
	    if (general_path_LSQE< polyline_vs_general_path_bias*final_fit_LSQE){
		general_path_result = true;
		polygon_result      = false;
		m_classification    = COMPLEX;
	    } else {
		polygon_result      = true;
		general_path_result = false;
		m_classification = POLYGON;
	    }
	}
        return m_classification;
    }

    /**
     * Reclassify the stroke.
     **/
  public int
  reclassify()
  {
    m_isClassified = false;
    m_classification = 0;
    return classify();
  }

  public Vertex[] getFinalFit() {
	  return final_fit;
  }
}
