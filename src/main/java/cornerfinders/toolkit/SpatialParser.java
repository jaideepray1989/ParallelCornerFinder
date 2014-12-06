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
import edu.mit.sketch.geom.Ellipse;
import edu.mit.sketch.geom.GeometricObject;
import edu.mit.sketch.geom.GeometryUtil;
import edu.mit.sketch.geom.Line;
import edu.mit.sketch.geom.Rectangle;
import edu.mit.sketch.grammar.me.Cross;
import edu.mit.sketch.grammar.me.Ground;
import edu.mit.sketch.grammar.me.Motor;
import edu.mit.sketch.grammar.me.Pin;
import edu.mit.sketch.grammar.me.Shade;

/** 
  * 
  * See the end of the file for the log of changes.
  * 
  * $Author: hammond $
  * $Date: 2006-11-22 22:54:35 $   
  * $Revision: 1.1 $
  * $Headers$
  * $Id: SpatialParser.java,v 1.1 2006-11-22 22:54:35 hammond Exp $     
  * $Name:  $   
  * $Locker:  $
  * $Source: /cvsroot/tamudrg/code/src/edu/mit/sketch/toolkit/SpatialParser.java,v $
  *  
  **/



/**
  *
  * This class parses the objects drawn by the user.
  *
  **/


public
class SpatialParser
{

    /**
    *
    * The constructor.
    *
    **/
    SpatialParser()
    {
    }

     
    /**
    *
    * Handle key stroke events
    *
    **/
    public void
    parse( ObjectManager object_manager )
    {    
        parseMotor( object_manager );
        parseCross( object_manager );
        parseGround( object_manager );
        parsePin( object_manager );
    }

     
    /**
    *
    * Parse Cross
    *
    **/
    public void
    parseCross( ObjectManager object_manager )
    {    
        Object objects[] = object_manager.objects.toArray();
        
        GeometricObject line1;
        GeometricObject line2;

        for ( int i=0; i<objects.length; i++ ) {
            if ( objects[i]  instanceof Line ) {
                System.out.println( "parser got a line" );
                for ( int j=0; j<objects.length; j++ ) {
                    if ( objects[j] instanceof Line ) {
                        if ( i == j )
                            continue;
                        line1 = (Line)objects[i];
                        line2 = (Line)objects[j];
                        System.out.println( "parser got another line" );
                        System.out.println( "parseLine ( " + 
                                             i + ", " + j + " )" );
                        if ( ( line1.touches( line2 ) ) &&
                              ( GeometryUtil.equalDoubles(
                                                 ((Line)line1).getAngle() -
                                                ((Line)line2).getAngle(),
                                              Math.PI/2,
                                            Math.PI/6 ))) {
                            object_manager.addObject( new
                                Cross( ((Line)line1), ((Line)line2) ) );
                            object_manager.objects.remove( line1 );
                            object_manager.objects.remove( line2 );
                            System.out.println( "parseCross parsed a cross" );
                        }
                    }
                }
            }
        }
        System.out.println( "parseCross returned" );
    }

     
    /**
    *
    * Parse Motor
    *
    **/
    public void
    parseMotor( ObjectManager object_manager )
    {    
        Object objects[] = object_manager.objects.toArray();
        
        GeometricObject body;
        GeometricObject rotor;
    
        System.out.println( "parseMotor called" );
        
        for ( int i=0; i<objects.length; i++ ) {
            if ( ( objects[i]  instanceof Rectangle ) &&
                 ( ( ((Rectangle)objects[i]).getType() == "square"    ) ||
                   ( ((Rectangle)objects[i]).getType() == "rectangle" ) ) ){
                System.out.println( "parser got a rectangle" );
                for ( int j=0; j<objects.length; j++ ) {
                    if ( objects[j] instanceof Line ) {
                        body  = (Rectangle) objects[i];
                        rotor = (Line)      objects[j];
                        System.out.println( "parser got a line" );
                        System.out.println( "parseMotor ( " + 
                                           i + ", " + j + " )" );
                        System.out.println( "Rectangle MajorAxisAngle : " + 
                            GeometryUtil.radian2degree( 
                                ((Rectangle)body).getMajorAxisAngle()) );
                        System.out.println( "Line angle      : " + 
                            GeometryUtil.radian2degree( 
                                ((Line)rotor).getAngle()) );
                     
                                             
                        if ( ( ((Rectangle)body).hasOnMajorAxisDirection( 
                                          rotor ) ) &&
                           ( body.touches( rotor ) ) &&
                              ( GeometryUtil.parallelAngles(
                                    ((Rectangle)body).getMajorAxisAngle(),
                                   ((Line)rotor).getCartesianAngle(),
                                   Math.PI/6 ))) {
                            object_manager.addObject( new
                                Motor( ((Rectangle)body), ((Line)rotor) ) );
                            object_manager.objects.remove( rotor );
                            object_manager.objects.remove( body );
                            System.out.println( "parseMotor parsed a motor" );
                        }
                    }
                }
            }
        }
        System.out.println( "parseMotor returned" );
    }

     
    /**
    *
    * Parse Ground
    *
    **/
    public void
    parseGround( ObjectManager object_manager )
    {    
        Object objects[] = object_manager.objects.toArray();
        
        Line  line;
        Shade shade;
    
        System.out.println( "parseGround called" );
        
        for ( int i=0; i<objects.length; i++ ) {
            if ( objects[i]  instanceof Shade ) {
                System.out.println( "parser got a shade" );
                for ( int j=0; j<objects.length; j++ ) {
                    if ( objects[j] instanceof Line ) {
                        shade = (Shade) objects[i];
                        line  = (Line)  objects[j];
                        System.out.println( "parser got a line" );
                        System.out.println( "parseGround ( " + 
                                           i + ", " + j + " )" );
                                           
                        if ( ( shade.touches( line ) ) &&
                             ( GeometryUtil.parallelAngles(
                                   shade.getMajorAxisAngle(),
                                   line.getCartesianAngle(),
                                   Math.PI/6 ))) {
                          
                            object_manager.addObject( new
                                Ground( shade, line.toPolygon() ) );
                            object_manager.objects.remove( shade );
                            object_manager.objects.remove( line );
                            System.out.println( "parseGround parsed a ground" );
                        }
                    }
                }
            }
        }
        
        System.out.println( "parseGround returned" );
    }

     
    /**
    *
    * Parse Pin
    *
    **/
    public void
    parsePin( ObjectManager object_manager )
    {    
        Object objects[] = object_manager.objects.toArray();
        
        Ellipse ellipse;
        Cross   cross;
    
        System.out.println( "parsePin called" );
        
        for ( int i=0; i<objects.length; i++ ) {
            if ( objects[i]  instanceof Cross ) {
                System.out.println( "parser got a cross" );
                for ( int j=0; j<objects.length; j++ ) {
                    if ( objects[j] instanceof Ellipse ) {
                        cross   = (Cross)   objects[i];
                        ellipse = (Ellipse) objects[j];
                        System.out.println( "parser got an ellipse" );
                        System.out.println( "parsePin ( " + 
                                            i + ", " + j + " )" );
                                           
                        if ( ( ellipse.containsGeometricObjects( 
                              cross.getPolygonalBoundsArray() ) ) &&
                              ( ellipse.getTypeWithTolerance( 0.2 ) == 
                                "circle" ) ) {

                            object_manager.addObject( 
                              new Pin( ellipse, cross ) );
                            object_manager.objects.remove( ellipse );
                            object_manager.objects.remove( cross );

                            System.out.println( "parsePin parsed a pin" );
                        }
                    }
                }
            }
        }
        System.out.println( "parsePin returned" );
    }
}


/** 
  * 
  * $Log: SpatialParser.java,v $
  * Revision 1.1  2006-11-22 22:54:35  hammond
  * mit's drg code, will extract and refactor to get ladder code
  *
  * Revision 1.5  2005/01/27 22:15:16  hammond
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
  * Revision 1.15  2000/09/06 22:40:55  mtsezgin
  * Combinations of curves and polygons are successfully approximated
  * by straight lines and Bezier curves as appropriate. System works
  * quite reliably.
  *
  * Revision 1.9  2000/06/03 01:52:35  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.8  2000/05/26 20:37:49  mtsezgin
  *
  * Fixed the bug in parseMotor which caused shades to be taken as
  * rectangles. Now parseGround works properly.
  *
  * Revision 1.7  2000/05/07 17:27:58  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.6  2000/05/04 01:36:28  mtsezgin
  *
  * Fixed minor bugs.
  * The current version successfuly parses Motor, Cross, Pin and Ground.
  * In addition the ParseSupervisor is introduced here.
  *
  * Revision 1.5  2000/05/03 23:26:47  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.4  2000/04/28 04:45:05  mtsezgin
  *
  * Now each GeometricObject keeps the mouse input that was previously
  * discarded. User can switch between seeing the recognized mode and
  * the raw mode. setDataPoints( Polygon points ) and getDataPoints()
  * are added to GeometricObject, and all the implementors are modified
  * accordingly.
  *
  * Revision 1.3  2000/04/25 22:29:38  mtsezgin
  *
  * Modified to work with the new Rectangle implementation. The current
  * version parses crosses as well as motors in arbitrary orientations.
  *
  * Revision 1.2  2000/04/13 06:24:09  mtsezgin
  *
  * The current version of the program recognized Crosses, and Shades.
  * Implementors of Terminal and their descendants were modified to
  * implement the changes in GeometricObject.
  *
  * Revision 1.1  2000/04/11 00:42:55  mtsezgin
  *
  * The parser should be changed to a "real" parser to make things right.
  *
  * Revision 1.2  2000/04/06 19:16:23  mtsezgin
  *
  * Modified all the classes to use my Point class which extends java.awt.Point
  * instead of directly using java.awt.Point
  *
  * Revision 1.1.1.1  2000/04/01 03:07:07  mtsezgin
  * Imported sources
  *
  * Revision 1.2  2000/03/31 22:41:03  mtsezgin
  *
  * Started Log tracking.
  *
  *  
  **/
