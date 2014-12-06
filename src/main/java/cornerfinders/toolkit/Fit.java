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
  * $Id: Fit.java,v 1.1 2006-11-22 22:54:35 hammond Exp $     
  * $Name:  $   
  * $Locker:  $
  * $Source: /cvsroot/tamudrg/code/src/edu/mit/sketch/toolkit/Fit.java,v $
  *  
  **/


import edu.mit.sketch.geom.Point;
import edu.mit.sketch.geom.Vertex;
import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.Comparator;


/**
  *
  * This class represents a fit to a set of points. It has methods
  * for finding the LSQ error.
  *
  **/

public
class      Fit
implements Serializable,
           Comparator
{
    /**
    *
    * The original points
    *
    **/    
    Point original_points[];


    /**
    *
    * The fit
    *
    **/    
    public Vertex vertices[];


    /**
    *
    * The least squares error of the fit wrt points
    *
    **/    
    private double lsq_error;
    

    /**
    *
    * The constructor.
    *
    **/
    public
    Fit( Point original_points[], Vertex vertices[], double lsq_error )
    {
        this.original_points = original_points;
        this.vertices        = vertices;
        this.lsq_error       = lsq_error;
    }
    

    /**
    *
    * The constructor.
    *
    **/    
    Fit( Point original_points[], Vertex vertices[] )
    {
        this.original_points = original_points;
        this.vertices        = vertices;
        this.lsq_error       = Double.MAX_VALUE;
    }


    /**
    *
    * Get the LSQ error
    *
    **/    
    public double
    getLSQError()
    {
        if ( lsq_error == Double.MAX_VALUE ) {
            lsq_error = Blackboard.leastSquaresForPolygon( vertices, 
                                                           original_points );
        }
        
        return lsq_error;
    }
    
    
    /**
    *
    * Implement Comparator
    *
    **/    
    public int
    compare( Object o1, Object o2 )
    {
        if ( (((Fit)o1).getLSQError()-((Fit)o2).getLSQError()) < 0 )
            return -1;
        if ( (((Fit)o1).getLSQError()-((Fit)o2).getLSQError()) > 0 )
            return 1;
            return 0;
    }
    
    
    /**
    *
    * Implement Comparator
    *
    **/    
    public boolean
    equals( Object o1, Object o2 )
    {
        return ( ((Fit)o1).getLSQError() == ((Fit)o2).getLSQError() );
    }
    
    
    /**
    *
    * Implement toString()
    *
    **/    
    public String
    toString()
    {
        return "Fit with "           + 
                vertices.length      + 
                " vertices, LSQE = " + 
                getLSQError();
    }
    
    
    /**
    *
    * paint on g
    *
    **/    
    public void
    paint( Graphics g )
    {
        g.setColor( Color.blue );        
        for ( int i=0; i<vertices.length-1; i++ ) {
            g.drawLine( (int)vertices[i].x,
                        (int)vertices[i].y,
                        (int)vertices[i+1].x,
                        (int)vertices[i+1].y );
        }
    }
    
    
    /**
    *
    * paint on g
    *
    **/    
    public void
    paintWithCertainties( Graphics g )
    {
        g.setColor( Color.red );
        for ( int i=0; i<vertices.length; i++ ) {
            g.fillOval( 
                (int)vertices[i].x - 
                    (int)(vertices[i].certainty*20)/2,
                (int)vertices[i].y - 
                    (int)(vertices[i].certainty*20)/2,
                (int)(vertices[i].certainty*20),
                (int)(vertices[i].certainty*20) );
        }

        g.setColor( Color.blue );        
        for ( int i=0; i<vertices.length-1; i++ ) {
            g.drawLine( (int)vertices[i].x,
                        (int)vertices[i].y,
                        (int)vertices[i+1].x,
                        (int)vertices[i+1].y );
        }
    }
}
/** 
  * 
  * $Log: Fit.java,v $
  * Revision 1.1  2006-11-22 22:54:35  hammond
  * mit's drg code, will extract and refactor to get ladder code
  *
  * Revision 1.6  2005/07/25 17:12:10  dpitman
  * sketch.geom package now uses doubles for coordinate values instead of integers. Code in other packages was adjusted because it contained int-based Graphic calls, and thus, the coordinate values had to be type-casted.
  *
  * Revision 1.5  2005/01/27 22:15:14  hammond
  * organized imports
  *
  * Revision 1.4  2003/10/13 19:46:37  moltmans
  * Removed bad line endings.
  *
  * Revision 1.3  2003/03/06 01:08:52  moltmans
  * Added copyright to all the files.
  *
  * Revision 1.2  2001/11/23 03:22:56  mtsezgin
  * Major reorganization
  *
  * Revision 1.1.1.1  2001/03/29 16:25:00  moltmans
  * Initial directories for DRG
  *
  * Revision 1.3  2000/09/06 22:40:35  mtsezgin
  * Combinations of curves and polygons are successfully approximated
  * by straight lines and Bezier curves as appropriate. System works
  * quite reliably.
  *
  * Revision 1.12  2000/06/08 03:14:30  mtsezgin
  *
  * Made the class Serializable for supporting saving and loading
  * designs. Both the object attributes, and the original data points
  * are stored and restored.
  *
  * Revision 1.11  2000/06/03 01:52:34  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.10  2000/05/03 23:26:46  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.9  2000/04/28 04:45:04  mtsezgin
  *
  * Now each GeometricObject keeps the mouse input that was previously
  * discarded. User can switch between seeing the recognized mode and
  * the raw mode. setDataPoints( Polygon points ) and getDataPoints()
  * are added to GeometricObject, and all the implementors are modified
  * accordingly.
  *
  * Revision 1.8  2000/04/25 22:18:57  mtsezgin
  *
  * The getBounds changed to getRectangularBounds which returns a Rectangle.
  *
  * Revision 1.7  2000/04/17 07:02:30  mtsezgin
  *
  * Finally made the Rectangle really rotatable.
  *
  * Revision 1.6  2000/04/13 06:24:08  mtsezgin
  *
  * The current version of the program recognized Crosses, and Shades.
  * Implementors of Terminal and their descendants were modified to
  * implement the changes in GeometricObject.
  *
  * Revision 1.5  2000/04/11 01:41:46  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.4  2000/04/11 00:41:47  mtsezgin
  *
  * Now the whole package succesfully parses a motor.
  *
  * Revision 1.3  2000/04/07 04:28:54  mtsezgin
  *
  * Added Rotatable interface. Rectangle and Line are Rotatable for now, but
  * Rectangle should be modified to have an angle field. Also other rotatable
  * classes should also implement Rotatable interface.
  *
  *
  **/
