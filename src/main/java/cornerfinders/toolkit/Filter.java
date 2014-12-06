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
  * $Date: 2006-11-22 22:54:36 $   
  * $Revision: 1.1 $
  * $Headers$
  * $Id: Filter.java,v 1.1 2006-11-22 22:54:36 hammond Exp $     
  * $Name:  $   
  * $Locker:  $
  * $Source: /cvsroot/tamudrg/code/src/edu/mit/sketch/toolkit/Filter.java,v $
  *  
  **/


import edu.mit.sketch.geom.GeometryUtil;
import edu.mit.sketch.geom.Line;
import edu.mit.sketch.grammar.me.Shade;
import edu.mit.sketch.ui.Tablet;
import java.util.Vector;


/**
  *
  * This class manages the objects recognized by the Tablet.
  * When Tablet adds the recognized objects using the addObject 
  * to the ObjectManager, the ObjectManager passes the input throug
  * this filter first, and then adds the object. Later depending
  * on the rest of the input, the Filter may remove the previously
  * added objects from the ObjectManager.
  *
  **/

class Filter
{
    /**
    *
    * The vector for holding the line sequences.
    *
    **/    
    private Vector line_sequences;

    /**
    *
    * The object manager associated with this object.
    *
    **/    
    private ObjectManager object_manager;

    /**
    *
    * The constructor.
    *
    **/    
    Filter( ObjectManager object_manager )
    {
        line_sequences = new Vector( 10, 0 );
        this.object_manager = object_manager;
    }


    /**
    *
    * Filters the object.
    *
    **/
    public Vector
    filter( Object object )
    {
        Vector return_vector = new Vector();
        // There is some redundancy in the code for clarity. -Metin
        if ( line_sequences.size() < 3 ) {
            if ( line_sequences.size() > 0 ) {
                if ( object instanceof Line ) {
                    if ( ( (((Line)object).getTimeStamp() - 
                           ((Line)line_sequences.lastElement()).getTimeStamp())<
                            Tablet.sequence_delay ) &&
                          ( GeometryUtil.linesParallel( 
                           ((Line)line_sequences.lastElement()), 
                           ((Line)object), 
                            Math.PI/6 ) ) ) {
                        line_sequences.addElement( object );
                    } else {
                        line_sequences.clear();
                        line_sequences.addElement( object );
                    }
                }
                return_vector.addElement( object );
            } else {
                if ( object instanceof Line ) {
                    line_sequences.addElement( object );
                    return_vector.addElement( object );
                } else {
                    return_vector.addElement( object );
                }
            }
        } else {
            if ( ( object instanceof Line ) && 
                 ( (((Line)object).getTimeStamp() - 
                    ((Line)line_sequences.lastElement()).getTimeStamp()) <
                    Tablet.sequence_delay ) &&
                 ( GeometryUtil.linesParallel( 
                     ((Line)line_sequences.lastElement()), 
                    ((Line)object), 
                    Math.PI/6 ) ) ) {
                line_sequences.addElement( object );
                return_vector.addElement( object );
            } else {
                Shade shade = new Shade( line_sequences );
                
                for ( int i=0; i<line_sequences.size(); i++ ) {
                    object_manager.removeObject( line_sequences.elementAt(i) );
                }
                
                line_sequences.clear();
                if ( object instanceof Line )
                    line_sequences.addElement( object );
                return_vector.addElement( shade  );
                return_vector.addElement( object );
            }
        }
        
        return return_vector;
    }


    /**
    *
    * Reset the filter.
    *
    **/
    public void
    reset()
    {
        System.out.println( "Resetting the filter" );
        line_sequences.clear();
    }
}


/** 
  * 
  * $Log: Filter.java,v $
  * Revision 1.1  2006-11-22 22:54:36  hammond
  * mit's drg code, will extract and refactor to get ladder code
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
  * Revision 1.1.1.1  2001/03/29 16:25:01  moltmans
  * Initial directories for DRG
  *
  * Revision 1.10  2000/09/06 22:40:35  mtsezgin
  * Combinations of curves and polygons are successfully approximated
  * by straight lines and Bezier curves as appropriate. System works
  * quite reliably.
  *
  * Revision 1.4  2000/06/03 01:52:32  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.3  2000/05/26 20:41:27  mtsezgin
  *
  * Fixed the bug in shade recognition. Now if the user draws a line
  * that is not parallel to the previous ones, or that is drawn
  * significantly later than the last line, we stop buffering and
  * process the buffered lines.
  *
  * Revision 1.2  2000/04/20 03:59:52  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.1  2000/04/13 06:16:02  mtsezgin
  *
  * Just started, and for now it filters out Shade objects using the timing data.
  *
  *  
  **/
