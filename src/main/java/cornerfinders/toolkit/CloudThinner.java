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
package cornerfinders.toolkit;

  /**
  *
  * See the end of the file for the log of changes.
  *
  * $Author: hammond $
  * $Date: 2006-11-22 22:54:35 $
  * $Revision: 1.1 $
  * $Headers$
  * $Id: CloudThinner.java,v 1.1 2006-11-22 22:54:35 hammond Exp $
  * $Name:  $
  * $Locker:  $
  * $Source: /cvsroot/tamudrg/code/src/edu/mit/sketch/toolkit/CloudThinner.java,v $
  *
  **/



import edu.mit.sketch.geom.GeometryUtil;
import edu.mit.sketch.geom.Point;
import edu.mit.sketch.util.LinearFit;
import java.util.ArrayList;

/**
  * Acronyms: sdr -> StrokeDataReader, sd -> StrokeData
  *
  * This class handles the updates to various frames.
  *
  **/
public
class CloudThinner
{
    /**
    *
    * 
    *
    **/
    private static JMCommunicator communicator = new JMCommunicator();
    
    
    /**
    *
    * The constructor.
    *
    **/
    public CloudThinner()
    {
    }


    /**
    *
    * Thin input cloud
    *
    **/
  public static Point[]
    thinCloud( Point points[] )
  {
    double distance_tolerance = 40;
    Point result[] = new Point[points.length];
		
    //    Tablet.debug_graphics.setColor( Color.red );
    for ( int i=0; i<points.length; i++ ) {
      ArrayList neighbor_buffer   = new ArrayList();
      Point     neighbor_points[];
      for ( int j=0; j<points.length; j++ ) {
				
        if ( ( points[i].distance( points[j] ) < distance_tolerance ) &&
             ( i != j ) ) {
          neighbor_buffer.add( points[j] );
        }
      }
      neighbor_points = new Point[ neighbor_buffer.size() ];
      for ( int j=0; j<neighbor_points.length; j++ ) {
        neighbor_points[j] = new Point();
        neighbor_points[j].x = ((Point) neighbor_buffer.get(j)).x; 
        neighbor_points[j].y = ((Point) neighbor_buffer.get(j)).y; 
      }
			
			
      double angle = LinearFit.findAngleRotationMethod( neighbor_points );
      //double angle = communicator.getAngle( neighbor_points );
			
      System.out.println( "Doing point " + i ); 
      System.out.println( "angle = in deg " + GeometryUtil.radian2degree(angle));
				
      for ( int j=0; j<neighbor_points.length; j++ ) {
        neighbor_points[j].x -= points[i].x;
        neighbor_points[j].y -= points[i].y;
        neighbor_points[j].rotate( -angle );
      }
      System.out.println( neighbor_points.length + " neighbors " );
			 
      //double c = communicator.getYintersectionForQuadraticApproximation( neighbor_points );
      double c = JMCommunicator.getYintersectionForLinearApproximation( neighbor_points );
      result[i]   = new Point();
      result[i].x = (int)(-Math.sin( angle )*c);
      result[i].y = (int)(Math.cos( angle )*c);
      System.out.println( " c = "        + c + 
                          " dx = "       + result[i].x +
                          " dy = "       + result[i].y ); 

      result[i].x += points[i].x;
      result[i].y += points[i].y;
      System.out.println( "in  -> " + ((Point)points[i]) );
      System.out.println( "out -> " + result[i] );
//       Tablet.debug_graphics.drawLine( result[i].x, 
//                                       result[i].y, 
//                                       result[i].x, 
//                                       result[i].y );
    }
    return result;    
  }

}

  
