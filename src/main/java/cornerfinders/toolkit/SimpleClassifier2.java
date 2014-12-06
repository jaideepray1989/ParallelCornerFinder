// (c) MIT 2003.  All rights reserved.

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


import edu.mit.sketch.geom.Arc;
import edu.mit.sketch.geom.GeometricObject;
import edu.mit.sketch.geom.Spiral;
import edu.mit.sketch.ui.Tablet;

/**
 *
 * This class extends SimpleClassifier in order to support Arcs.
 *
 */
public
class   SimpleClassifier2
extends SimpleClassifier
{ 

  /**
   * Arc fit
   **/
  protected Arc arc_fit;

  /**
   * The least square error between the original points and the arc fit
   **/
  protected double arc_LSQE        = Double.MAX_VALUE;


  protected Spiral spiral_fit;

  protected double spiral_LSQE = Double.MAX_VALUE;

  /**
   * Constructs a Classifier object.  You must pass it a StrokeData object.
   * Contains a default error calculator, which is just the squared error.
   **/
  public SimpleClassifier2( StrokeData stroke_data )
  {
    super( stroke_data );
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
                
    arc_fit  = Arc.fitArc( stroke_data );
    arc_fit.setOriginalVertices( stroke_data.vertices );

    if( arc_fit != null ) {
      arc_LSQE = arc_fit.leastSquaresError();
    }
        
    spiral_fit = Spiral.fitSpiral(stroke_data);
    if( spiral_fit != null ) {
      spiral_LSQE = spiral_fit.leastSquaresError();
    }
	
    boolean arc_conditions = false;

    if ( arc_fit != null ) {
      if ( points[0].distance( points[points.length-1] ) > 15 &&
	   arc_fit.extent > Math.PI/4 ) {
	double arc_length     = arc_fit.extent*arc_fit.getRadius();
	double polygon_length = arc_fit.getDataPoints().getPolygonLength();
	
	if ( !Tablet.very_quiet ) {
	  System.out.println( "arc_length " +  arc_length );
	  System.out.println( "polygon_length " + polygon_length );
	  System.out.println( "arc_extent " + arc_fit.extent*180/Math.PI );
	}
	if ( arc_length > polygon_length*0.9 &&
	     arc_length < polygon_length*1.1 ) {
	  arc_conditions = true;
	} else {
	  if ( !Tablet.very_quiet ) {
	    System.out.println( "Rejected shape to be classified as ARC" );
	  }
	  arc_conditions = false;
	}
      }
    }
 
    if(m_classification == COMPLEX && spiral_LSQE < 10){
      m_classification = SPIRAL;
      return SPIRAL;
    }

    if ( !Tablet.very_quiet ) {
      System.out.println( "arc_LSQE       = " + arc_LSQE );
    }
    if ( arc_LSQE < 50 && arc_conditions ) {
      m_classification = ARC;
    }
        
    if ( arc_LSQE < getPolygonError() &&
         arc_LSQE < getLineError()*.8 &&
         arc_LSQE < getEllipseError() &&
         arc_LSQE < getComplexError() &&
         arc_conditions               ) {
      m_classification = ARC;
    }

    if ( m_classification == COMPLEX && 
         getComplexApproximation().isAllCurves() && 
         getEllipseError() < getComplexError()*1.1) {
      if ( arc_conditions ) {
        m_classification = ARC;
      }
    }
        
    if ( m_classification == ELLIPSE ) {
      if ( arc_conditions ) {
        m_classification = ARC;
      }
    }
        
    m_isClassified = true;

    return m_classification;
  }

  public int 
    reclassify()
  {
    m_isClassified = false;
    m_classification = 0;
    return classify();
  }


  /**
   *
   * Returns true if the best fit for strokeData is an arc
   *
   **/
  public boolean 
    isArc()
  {
    return ( classify() == ARC );
  }
  
  public boolean 
    isSpiral()
  {
    return ( classify() == SPIRAL );
  }

  /**
   *
   * Returns the best fit Acr for the StrokeData.   Java defined shapes.
   *
   **/
  public Arc 
    getArcApproximation()
  {
    return arc_fit;
  }

  /**
   *
   * Returns the error between the best fit arc and the strokeData.
   *
   **/
  public double getArcError()
  {
    return arc_LSQE;
  }

  
  public Spiral 
    getSpiralApproximation()
  {
    return spiral_fit;
  }


  public GeometricObject getApproximation() 
  {
    switch(classify()) {
    case ARC:
      return getArcApproximation();
    case SPIRAL:
      return getSpiralApproximation();
    default:
      return super.getApproximation();
    }
  }
  
    
}
