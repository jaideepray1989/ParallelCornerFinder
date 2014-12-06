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
package cornerfinders.toolkit;

import edu.mit.sketch.geom.Point;
import java.util.ArrayList;
/** 
  * 
  * See the end of the file for the log of changes.
  * 
  * $Author: hammond $
  * $Date: 2006-11-22 22:54:34 $   
  * $Revision: 1.1 $
  * $Headers$
  * $Id: StrokeDataReader.java,v 1.1 2006-11-22 22:54:34 hammond Exp $     
  * $Name:  $   
  * $Locker:  $
  * $Source: /cvsroot/tamudrg/code/src/edu/mit/sketch/toolkit/StrokeDataReader.java,v $
  *  
  **/


/**
  *
  * This object implements StrokeDataFactory
  *
  **/

public
class      StrokeDataReader
implements StrokeDataFactory
{
    private static final int MAX_DATA_POINTS = 10000;
  
    /**
     * Points are put into this buffer when addPoint method is 
     * called.
     **/
    private ArrayList points_buffer;


    /**
    *
    * The constructor.
    *
    **/
    public StrokeDataReader()
    {
      points_buffer = new ArrayList();
    }

    public void translate(double dx, double dy){
	for (int i = 0; i < points_buffer.size(); i++){
	    ((Point) points_buffer.get(i)).translate(dx,dy);
	}
    }
    
    
   /**
    *
    * Add a point.
    * Throws an array index out of bound exception if more points
    * than the max_data_points are added.
    *
    **/
    public void
    addPoint( Point point )
    {
      int previous_position = points_buffer.size() - 1;
      if ( previous_position > 0 ) {
    Point previousPoint = (Point)points_buffer.get( previous_position );

    if ( point.time_stamp == previousPoint.time_stamp ) {
      return;
    }
    if ( ( point.x == previousPoint.x ) &&
         ( point.y == previousPoint.y ) )
    {
      return;
    }
      }
      points_buffer.add(point);
    }

  
    /**
    *
    * Return the StrokeData collected by this reader
    *
    **/
    public StrokeData
    getStrokeData()
    {
      
        Point data[] = new Point[points_buffer.size()];
    data = (Point[])points_buffer.toArray( data );
      
//          System.out.println( "available_position = " + available_position );
//          System.out.println( "data.length        = " + data.length        );
                
//          long time_difference = data[data.length-1].getTimeStamp() - 
//                                 data[0].getTimeStamp();
//          System.out.println( "Read " + data.length + " points in " +
//                              time_difference + " milliseconds" );
//          System.out.println( "This is " + ((1000*data.length)/time_difference ) +
//                              " points per second" );
    //        Tablet.setStatusMessage( "Read " + ((1000*data.length)/time_difference ) +
    //                            " points per second" );

        return new StrokeData( data );
    }

  
    /**
    *
    * Reset
    *
    **/
    public void
    reset()
    {
      points_buffer.clear();
    }


    /**
    *
    * Return the StrokeData collected by this reader
    *
    **/
    public boolean
    hasSufficientData()
    {
        return ( points_buffer.size() > 2 );
    }
}

/**
  *
  * $Log: StrokeDataReader.java,v $
  * Revision 1.1  2006-11-22 22:54:34  hammond
  * mit's drg code, will extract and refactor to get ladder code
  *
  * Revision 1.7  2005/01/27 22:15:17  hammond
  * organized imports
  *
  * Revision 1.6  2003/07/28 18:46:02  moltmans
  * removed a peskey '^M' in the file
  *
  * Revision 1.5  2003/03/06 01:08:52  moltmans
  * Added copyright to all the files.
  *
  * Revision 1.4  2003/02/28 20:20:12  rebecca8
  * added translation functionality
  *
  * Revision 1.3  2001/11/23 03:22:56  mtsezgin
  * Major reorganization
  *
  * Revision 1.2  2001/10/12 22:25:52  mtsezgin
  * This is a commit of all files.
  * Shoapid
  * vi sux:q
  *
  * Revision 1.1.1.1  2001/03/29 16:25:01  moltmans
  * Initial directories for DRG
  *
  * Revision 1.4  2000/09/20 20:07:35  mtsezgin
  * This is a working version with curve recognition and curve
  * refinement. The GeneralPath approximation is refined if needed
  * to result in a better fit.
  *
  * Revision 1.3  2000/09/06 22:40:58  mtsezgin
  * Combinations of curves and polygons are successfully approximated
  * by straight lines and Bezier curves as appropriate. System works
  * quite reliably.
  *
  * Revision 1.4  2000/06/03 01:52:34  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.3  2000/04/28 04:45:04  mtsezgin
  *
  * Now each GeometricObject keeps the mouse input that was previously
  * discarded. User can switch between seeing the recognized mode and
  * the raw mode. setDataPoints( Polygon points ) and getDataPoints()
  * are added to GeometricObject, and all the implementors are modified
  * accordingly.
  *
  * Revision 1.2  2000/04/11 01:41:45  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.1  2000/04/11 00:41:47  mtsezgin
  *
  * Now the whole package succesfully parses a motor.
  *
  *
  **/
