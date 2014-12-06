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

/** 
  * 
  * See the end of the file for the log of changes.
  * 
  * $Author: hammond $
  * $Date: 2006-11-22 22:54:35 $   
  * $Revision: 1.1 $
  * $Headers$
  * $Id: StrokeDataFactory.java,v 1.1 2006-11-22 22:54:35 hammond Exp $     
  * $Name:  $   
  * $Locker:  $
  * $Source: /cvsroot/tamudrg/code/src/edu/mit/sketch/toolkit/StrokeDataFactory.java,v $
  *  
  **/


import edu.mit.sketch.geom.Point;

/**
  *
  * Objects which take a sequence of input vertices and instantiates
  * a StrokeData object should implement this interface.
  *
  **/

public interface StrokeDataFactory
{
    /**
    *
    * Add a point
    *
    **/
    public abstract void
    addPoint(Point point);

  
    /**
    *
    * Throws an array index out of bound exception if more points
    * than the max_data_points are added.
    *
    **/
    public abstract StrokeData
    getStrokeData();
}

/**
  *
  * $Log: StrokeDataFactory.java,v $
  * Revision 1.1  2006-11-22 22:54:35  hammond
  * mit's drg code, will extract and refactor to get ladder code
  *
  * Revision 1.3  2005/01/27 22:15:17  hammond
  * organized imports
  *
  * Revision 1.2  2003/03/06 01:08:52  moltmans
  * Added copyright to all the files.
  *
  * Revision 1.1.1.1  2001/03/29 16:25:01  moltmans
  * Initial directories for DRG
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
