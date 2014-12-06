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
  * $Id: ObjectManager.java,v 1.1 2006-11-22 22:54:35 hammond Exp $     
  * $Name:  $   
  * $Locker:  $
  * $Source: /cvsroot/tamudrg/code/src/edu/mit/sketch/toolkit/ObjectManager.java,v $
  *  
  **/


import edu.mit.sketch.ddlcompiler.Segmentation;
import edu.mit.sketch.geom.Ellipse;
import edu.mit.sketch.geom.GeneralPath;
import edu.mit.sketch.geom.GeneralPathProcessor;
import edu.mit.sketch.geom.GeometricObject;
import edu.mit.sketch.geom.GeometryUtil;
import edu.mit.sketch.geom.Line;
import edu.mit.sketch.geom.Point;
import edu.mit.sketch.geom.Polygon;
import edu.mit.sketch.geom.Rectangle;
import edu.mit.sketch.geom.Translatable;
import edu.mit.sketch.geom.Vertex;
import edu.mit.sketch.grammar.me.MechanicalDesignFilenameFilter;
import edu.mit.sketch.grammar.me.Spring;
import edu.mit.sketch.ui.Paintable;
import edu.mit.sketch.ui.Tablet;
import edu.mit.sketch.ui.TabletDataProcessor;
import edu.mit.sketch.ui.TabletGUI;
import edu.mit.sketch.util.AWTUtil;
import edu.mit.sketch.util.GIFEncoder;
import edu.mit.sketch.util.LinearFit;
import edu.mit.sketch.util.LoadStoreModule;
import edu.mit.sketch.util.Util;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

/**
  *
  * This class manages the objects recognized by the Tablet.
  * Tablet adds the recognized using the addObject method.
  * The objects are kept in a Vector.
  *
  **/

public
class      ObjectManager
implements MouseMotionListener,
           MouseListener,
           KeyListener
{
    public Vector       stroke_vertices;
    public Vector       paths;
    public Tablet       tablet;
    public Vector       objects;

    ParseSupervisor     parse_supervisor;
    SpatialParser       spatial_parser;
    Object              selected_objects[];
    Vector              all_paths;
    Filter              filter;
    Point               last_position;

    String          current_file_name      = "Untitled";
    String          current_file_directory = null;

    public static final int RECOGNITION_MODE = 0;
    public static final int SELECTION_MODE   = 1;


    public int mode = RECOGNITION_MODE;

     public static String hmm_classes[] = { "butterfly",
                                             "motorizedunit",
                                             "rectangle",
                                             "stickfigure",
                                             "pulley",
                                             "wheel",
                                             "motor",
                                             "gravity",
                                             "anchor",
                                             "damper"
                                              };

   /**
    *
    * This is true if the parsed stuff is to be painted.
    * Otherwise the original data points are displayed.
    *
    **/    
    public boolean show_parsed_objects = false;


    /**
    *
    * The constructor.
    *
    **/
    public
    ObjectManager( TabletDataProcessor tablet )
    {
        this.tablet      = (Tablet)tablet;
        spatial_parser   = new SpatialParser();
        parse_supervisor = new ParseSupervisor( this );
        stroke_vertices  = new Vector( 10, 0 );
        objects          = new Vector( 10, 0 );
        paths            = new Vector( 10, 0 );
        all_paths        = new Vector( 10, 0 );
        filter           = new Filter( this );
    }


    /**
    *
    * Adds an object to the objects vector after filtering.
    *
    **/
    public void
    addObject( Object o )
    {        
        if ( mode == RECOGNITION_MODE ) {
            /*
            Vector filtered_output = filter.filter( o );
            for ( int i=0; i<filtered_output.size(); i++ ) {
                addObjectInternal( filtered_output.elementAt(i) );
            }
            */
            addObjectInternal( o );
            
            all_paths.add( new
               GeneralPath( new 
                  Polygon(
                  Blackboard.hybrid_fits[Blackboard.best_index].vertices ) ) );
            paint( Tablet.debug_graphics );
        }
        
        if ( mode == SELECTION_MODE && selected_objects == null ) {
            if ( !( o instanceof Line ) ) {
                selectInside( ((GeometricObject)o) );
            }
        }
    }



    /**
    *
    * Adds an object to the objects vector after filtering.
    *
    **/
    public void
    addVertices( Vertex vertices[] ) {
        stroke_vertices.addElement( vertices );
    }

    /**
    *
    * Adds an object to the objects vector after checking if the polygonal
    * fit is a spring or not.
    *
    **/
    public void
    addObject( Object o, Polygon polygonal_fit )
    {
        if ( mode == RECOGNITION_MODE ) {
            if ( Spring.isSpring( polygonal_fit ) ) {
                objects.addElement( new Spring( polygonal_fit ) );
                return;
            } else {
                addObject( o );
            }
            paint( Tablet.debug_graphics );
        }
        
        if ( mode == SELECTION_MODE ) {
            if ( !( o instanceof Line ) ) {
                selectInside( polygonal_fit );
            }
        }
    }


    /**
    *
    * Adds given objects to the objects vector after filtering.
    *
    **/
    public void
    addObjects( Vector input_objects )
    {
        for ( int i=0; i<input_objects.size(); i++ ) {
            addObjectInternal( input_objects.elementAt(i) );
        }
    }


    /**
    *
    * Adds given objects to the paths vector
    *
    **/
    public void
    addPath( GeneralPath path )
    {
        paths.add( path );
    }


    /**
    *
    * Removes the argument object from the objects Vector.
    *
    **/
    public boolean
    removePath( Object object )
    {
        return paths.removeElement( object );
    }


    /**
    *
    * Selects all the objects inside the given object.
    *
    **/
    public void
    selectInside( GeometricObject object )
    {
        Vector objects_inside = new Vector();
        
        for ( int i=0; i<objects.size(); i++ ) {
            if ( objects.elementAt( i ) instanceof GeometricObject ) {
                if ( ((GeometricObject)object).containsGeometricObject( 
                    ((GeometricObject)objects.elementAt( i ) ) ) ) {
                    objects_inside.addElement( objects.elementAt( i ) );
                }
            }
        }
        
        selectObjects( objects_inside );
        
        System.out.println( "selectInside called" );
    }


    /**
    *
    * Removes the argument object from the objects Vector.
    *
    **/
    public boolean
    removeObject( Object object )
    {
        return objects.removeElement( object );
    }


    /**
    *
    * Adds an object to the objects vector.
    *
    **/
    private void
    addObjectInternal( Object o )
    {
        Paintable p = (Paintable)o;
        
        if ( !Tablet.very_quiet ) {
            System.out.println( "addObject " + ((Paintable)o).getType() );
        }
            
        if ( p instanceof Polygon ) {
            /* kill higher level recognition
            if ( Rectangle.isRectangle( (Polygon)o ) ) {
                Rectangle rectangle = ((Polygon)o).getRectangularBounds();
                rectangle.setDataPoints( ((Polygon)o).getDataPoints() );
                addObject( rectangle ); 
                return;
            }
            if ( Spring.isSpring( ((Polygon)o) ) ) {
                objects.addElement( new Spring( ((Polygon)o) ) );
                return;
            } */
            if ( Line.isLine( ((Polygon)o) ) ) {
                addObject( ((Polygon)o).getEdge(0)  ); 
                return;
            }
        }
        /* kill polygon processing 
        if ( tryCombining( p ) ) {
            System.out.println( "Combined two Polygons" );
            return;
        }
        normalizeObject( ((Object)p) );
        */
        
        objects.addElement( o );

        if ( !Tablet.very_quiet ) {
            System.out.println( "Added " + o );
        }
    }

    /**
    *
    * Look at the statistical information and modify the polygons
    * to look like as they were intended to be.
    *
    **/
    private void
    normalizeObject( Object o )
    {
        if ( true )
            return;
        if ( Tablet.debug ) {
            System.out.println( "Normalize Object" );
        }
        if ( o instanceof Polygon ) {
            Polygon polygon     = ((Polygon)o);
            double  angle_set[] = polygon.getMajorAngles();
            if ( Tablet.debug ) {
                for ( int i=0; i<angle_set.length; i++ ) {
                    System.out.println( "Angle " + i + " = " + 
                                         angle_set[i]*180/Math.PI );
                }
            }
            
            angle_set = GeometryUtil.roundAngles( angle_set, Math.PI/36 );

            double tmp_angle_set[] = new double[angle_set.length*3];
            for ( int i=0; i<angle_set.length; i++ ) {
                tmp_angle_set[i                   ] = angle_set[i];
                tmp_angle_set[i+angle_set.length  ] = angle_set[i] + Math.PI;
                tmp_angle_set[i+angle_set.length*2] = angle_set[i] - Math.PI;
            }
            angle_set = tmp_angle_set;

            if ( Tablet.debug ) {
                for ( int i=0; i<angle_set.length; i++ ) {
                    System.out.println( "Angle " + i + " = " + 
                                         angle_set[i]*180/Math.PI );
                }
            }
            
            polygon.normalize( angle_set );
        }
    }


    /**
    *
    * Try combining the input object with whatever is present in 
    * the objects vector.
    *
    **/
    private boolean
    tryCombining( Object o )
    {
        Paintable p = (Paintable)o;
        
        if ( ( p instanceof Polygon ) || ( p instanceof Line ) ) {
            for ( int i=0; i<objects.size(); i++ ) {
                if ( objects.elementAt( i ) instanceof Polygon ) {
                    if ( ((Polygon)objects.elementAt(i)).tryCombining(
                        (Object)p, 5 ) ) {
                        normalizeObject( objects.elementAt(i) );
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    
    /**
    *
    * return selected objets
    * 
    **/
    public Object[]
    getSelectedObjects()
    {
        Util.printArray( selected_objects, "selected_objects" );
        return selected_objects;
    }
    
    
    /**
    *
    * Load the segmentation from file, change it using the parameters,
    * save it back.
    * 
    **/
    public void
    modifySegmentationForObject( int beginning_index,
                              int ending_index,
                              String file_name,
                              int object_class )
    {
        Segmentation data_segmentation = new Segmentation( file_name );
        
        int object_ids[]     = data_segmentation.getObjectIds();
        int ending_indices[] = data_segmentation.getEndingIndices();
    
        ArrayList new_object_ids     = new ArrayList();
        ArrayList new_ending_indices = new ArrayList();
    
        int i = 0;
        for ( i=0; i<object_ids.length; i++ ) {
            if ( ending_indices[i] >= beginning_index ) {
                new_ending_indices.add( new Integer( beginning_index ) );
                new_ending_indices.add( new Integer( ending_index    ) );
                new_object_ids.add(     new Integer( object_ids[i]   ) );
                new_object_ids.add(     new Integer( object_class    ) );
                break;
            } else {
                new_ending_indices.add( new Integer( ending_indices[i] ) );
                new_object_ids.add(     new Integer( object_ids[i]     ) );
            }
        }
        for ( ; i<object_ids.length; i++ ) {
            if ( ending_indices[i] > ending_index ) {
                new_object_ids.add(     new Integer( object_ids[i] ) );
                new_ending_indices.add( new Integer( ending_indices[i] ) );
            }
        }
        
        
        data_segmentation.setObjectIds( Util.arrayListToIntArray( new_object_ids ) );
        data_segmentation.setEndingIndices( Util.arrayListToIntArray( new_ending_indices ) );
        data_segmentation.save( file_name );
    }
    
    
    /**
    *
    * return the first index for the input_set in the objects
    * arraylist. Assume input_set is ordered by time. Return -1
    * if there is no match.
    * 
    **/
    public int
    getBeginningIndex( Object input_set[] )
    {
        Util.printArray( input_set, "selected_objects" );
    
        for ( int i=0; i<objects.size(); i++ ) {
            if ( objects.get( i ) == input_set[0] )
                return i;
        }
        
        System.err.println( "No index found in getBeginningIndex" );
        return -1;
    }
    
    
    /**
    *
    * return the last index + 1 for the input_set in the objects
    * arraylist. Assume input_set is ordered by time. (to be used
    * with < operator as usual.
    *
    **/
    public int
    getEndingIndex( Object input_set[] )
    {
        for ( int i=0; i<objects.size(); i++ ) {
            if ( objects.get( i ) == input_set[input_set.length-1] )
                return i+1;
        }
        
        System.err.println( "No index found in getEndingIndex" );
        return -1;
    }
    
    
    /**
    *
    * Draw the objects
    *
    **/
    public void
    paint( Graphics graphics )
    {
        Paintable     object;

        if ( Tablet.doing_batch_processing ) {
            ((Paintable)objects.elementAt(objects.size()-1)).paint( tablet.getDrawComponent().getGraphics());
            return;
        }

        if ( !tablet.buffer_ready ) {
            tablet.initializeBuffer();
        }

        Graphics2D    g = (Graphics2D)tablet.buffer_graphics;
        
        g.setColor( Color.white );
        g.clearRect( 0, 0, 2000, 2000 );

        g.setColor( Color.black );
        g.setStroke( new BasicStroke( Tablet.brush_thickness ) );
        
        for ( int i=0; i<objects.size(); i++ ) {
            object = ((Paintable)objects.elementAt(i));
            object.setGraphicsContext( g );
            if ( show_parsed_objects ) {
                object.paint( g );
            } else {
                object.paintOriginal( g );
            }
        }
        
        if ( selected_objects != null ) {
            g.setColor( Color.green );
            for ( int i=0; i<selected_objects.length; i++ ) {
                if ( selected_objects[i] instanceof Paintable ) {
                    if ( show_parsed_objects ) {
                        ((Paintable)selected_objects[i]).paint( g );
                    } else {
                        ((Paintable)selected_objects[i]).paintOriginal( g );
                    }
                }
            }
        }
        
        tablet.getDrawComponent().getGraphics().drawImage( tablet.buffer, 0, 0, tablet );
    }
    
    
    /**
    *
    * Draw the objects with indices from [begin,end)
    *
    **/
    public void
    paint( int begin, int end, int hmm_number )
    {
        Paintable     object;

        if ( !tablet.buffer_ready ) {
            tablet.initializeBuffer();
        }

        Graphics2D    g = (Graphics2D)tablet.buffer_graphics;
        
        g.setColor( Color.white );
        g.clearRect(0, 0, 2000, 2000 );

        g.setColor( Color.black );
        g.setStroke( new BasicStroke( Tablet.brush_thickness*2 ) );
        
        for ( int i=0; i<objects.size(); i++ ) {
            object = ((Paintable)objects.elementAt( i ) );
            object.setGraphicsContext( g );

            if ( i >= begin && i < end ) {
                g.setColor( AWTUtil.colors[hmm_number%AWTUtil.colors.length] );
            } else {
                g.setColor( Color.black );
            }
            if ( show_parsed_objects ) {
                object.paint( g );
            } else {
                object.paintOriginal( g );
            }
        }
        
        if ( selected_objects != null ) {
            g.setColor( Color.green );
            for ( int i=0; i<selected_objects.length; i++ ) {
                if ( selected_objects[i] instanceof Paintable ) {
                    if ( show_parsed_objects ) {
                        ((Paintable)selected_objects[i]).paint( g );
                    } else {
                        ((Paintable)selected_objects[i]).paintOriginal( g );
                    }
                }
            }
        }
        
        tablet.getDrawComponent().getGraphics().drawImage( tablet.buffer, 0, 0, tablet );
    }
    
    
    /**
    *
    * Draw the objects with the given segmentation
    *
    **/
    public void
    paint( Graphics graphics, int segmentation[], String input_names[] )
    {
        Paintable     object;
        graphics.setColor( Color.white );
        graphics.clearRect(0, 0, 2000, 2000 );

        graphics.setColor( Color.black );
        
        for ( int i=0; i<objects.size(); i++ ) {
            object = ((Paintable)objects.elementAt( i ) );
            object.setGraphicsContext( graphics );
            
            /*
            System.out.println( "\nobjects.elementAt( "+ i + ") " + objects.elementAt( i ) );
            System.out.println( "objects.size() " + objects.size() );
            System.out.println( "segmentation.length " + segmentation.length );
            System.out.println( "AWTUtil.colors.length " + AWTUtil.colors.length );
            */
            ((Graphics2D)graphics).setStroke( new BasicStroke( Tablet.brush_thickness*2 ) );
            graphics.setColor( AWTUtil.colors[segmentation[i]%AWTUtil.colors.length] );
            Point starting_point = (((GeometricObject)object).getDataPoints()).pointAt(0);
            int offset = (int)((Math.random()-.5)*6);
            graphics.drawString( input_names[segmentation[i]] + " s" + i, 
                          (int)starting_point.x + offset, 
                          (int)starting_point.y + offset );
            if ( show_parsed_objects ) {
                object.paint( graphics );
            } else {
                object.paintOriginal( graphics );
            }
        }
    }
    
    
    /**
    *
    * Draw the objects with the given segmentation
    *
    **/
    public void
    paint( Graphics graphics, int ending_indices[], int object_ids[], String object_names[] )
    {
        Paintable     object;
        graphics.setColor( Color.white );
        graphics.clearRect(0, 0, 2000, 2000 );

        graphics.setColor( Color.black );

        ((Graphics2D)graphics).setStroke( new BasicStroke( Tablet.brush_thickness*2 ) );
        
        for ( int i=0; i<ending_indices.length; i++ ) {
            for ( int j = ( i==0 ) ? 0 : ending_indices[i-1]; j<ending_indices[i]; j++ ) {
                object = ((Paintable)objects.elementAt( j ) );
                object.setGraphicsContext( graphics );
            
                Point starting_point = (((GeometricObject)object).getDataPoints()).pointAt(0);
                int offset = (int)((Math.random()-.5)*6);
                graphics.setColor( Color.black );
                graphics.drawString( " s" + i + object_names[object_ids[i]], 
                              (int)starting_point.x + offset, 
                              (int)starting_point.y + offset );
                graphics.setColor( AWTUtil.colors[object_ids[i]%AWTUtil.colors.length] );
                if ( show_parsed_objects ) {
                    object.paint( graphics );
                } else {
                    object.paintOriginal( graphics );
                }
            }
        }
    }
    

    /**
    *
    * Clear the screen
    *
    **/    
    public void
    clear()
    {
        tablet.clearScreen();
    }
    
    
    /**
    *
    * Removes all the objects.
    *
    **/    
    public void
    reset()
    {
        objects.removeAllElements();
        all_paths.removeAllElements();
        paths.removeAllElements();
        filter.reset();
        Blackboard.general_path = null;
        stroke_vertices.removeAllElements();
    }
    
    
    /**
    *
    * A very primitive undo
    *
    **/    
    public void
    undo()
    {
        objects.remove( objects.size() - 1 );
        stroke_vertices.remove( stroke_vertices.size() - 1 );
        if ( paths.size() > 0 ) {
            paths.remove( paths.size() - 1 );
        }
        paint( Tablet.debug_graphics );
    }
    
    
    /**
    *
    * Returns a vector of all the geometric objects "sufficiently 
    * close to the point passes, determined by the search radius r.
    * For the moment, the only Terminals are searched.
    *
    **/    
    public Vector
    getTranslatableNeighborObjects( Point p, int r )
    {
        Object       object;
        Translatable translatable;
        Vector       translatable_neighbors = new Vector();
        
        for ( Enumeration e = objects.elements(); e.hasMoreElements(); ) {
            object = e.nextElement();
            if ( object instanceof Translatable ) {
                   translatable = ((Translatable)object);
                if ( translatable.pointIsOn( p, r ) ) {
                    translatable_neighbors.addElement( translatable );
                }
            }
        }
        
        return translatable_neighbors;
    }
    
    
    /**
    *
    * Return a string representation for the objects
    *
    **/    
    public String
    getObservations()
    {
        Object object;
        String observations = "";
        
        for ( int i = 0; i<objects.size(); i++ ) {
            object = objects.elementAt( i );
            /*
            System.out.println( "\nobject(" + i + ") "  + object );
            System.out.print( "observation for stroke " + i + " is " );
            */
            if ( object instanceof Line ) {
                Line line = (Line)object;
                double x = line.x2 - line.x1;
                double y = line.y2 - line.y1;
                
                double angle = Math.atan2( y, x );
                
                if ( ( angle >  7*Math.PI/18 ) &&
                     ( angle <  11*Math.PI/18 ) ) {
                    observations += " 3";
                    //System.out.println( "3" );
                    continue;
                }
                if ( ( angle <  -7*Math.PI/18 ) &&
                     ( angle >  -11*Math.PI/18 ) ) {
                    observations += " 3";
                    //System.out.println( "3" );
                    continue;
                }
                if ( ( angle <  2*Math.PI/18 ) &&
                     ( angle > -2*Math.PI/18 ) ) {
                    observations += " 4";
                    //System.out.println( "4" );
                    continue;
                }
                if ( ( angle >  16*Math.PI/18 ) ||
                     ( angle < -16*Math.PI/18 ) ) {
                    observations += " 4";
                    //System.out.println( "4" );
                    continue;
                }
                if ( x*y >= 0 ) {
                    observations += " 2";  // 1 and 2 swapped due to the flip in y axis
                    //System.out.println( "2" );
                    continue;
                } else {
                    observations += " 1"; // 1 and 2 swapped due to the flip in y axis
                    //System.out.println( "1" );
                    continue;
                }
            }
            if ( object instanceof Ellipse ) {
                Rectangle bounds = ((Ellipse)object).getRectangularBounds();
                if ( bounds.getWidth()/bounds.getHeight() > 1.33 ) {
                    observations += " 6";
                    //System.out.println( "6" );
                    continue;
                }
                if ( bounds.getWidth()/bounds.getHeight() < 1.0/1.33 ) {
                    observations += " 7";
                    //System.out.println( "7" );
                    continue;
                }
                observations += " 5";
                //System.out.println( "5" );
                continue;
            }
            if ( object instanceof GeneralPath ) {
                observations += " 8";
                //System.out.println( "8" );
                continue;
            }
            if ( object instanceof Polygon ) {
                Polygon polygon = (Polygon)object;
                if ( polygon.npoints == 3 ) {
                    observations += " 9";
                    //System.out.println( "9" );
                }
                if ( polygon.npoints == 4 ) {
                    observations += " 10";
                    //System.out.println( "10" );
                }
                if ( polygon.npoints == 5 ) {
                    observations += " 11";
                    //System.out.println( "11" );
                }
                if ( polygon.npoints == 6 ) {
                    observations += " 12";
                    //System.out.println( "12" );
                }
                if ( polygon.npoints >= 7 ) {
                    observations += " 13";
                    //System.out.println( "13" );
                }
                //System.out.println( "npoints in the polygon = " + polygon.npoints );
                continue;
            }
        }
        
        //System.out.println( "Observations " + observations );
        //System.out.println( "There are " + objects.size() + " objects" );
        
        return observations;
    }
    
    
    /**
    *
    * Return a string representation for the objects
    * An alternate method.
    *
    **/    
    public String
    getObservations( ArrayList input_objects )
    {
        Object object;
        String observations = "";
        
        for ( int i = 0; i<input_objects.size(); i++ ) {
            object = input_objects.get( i );
            /*
            System.out.println( "\nobject(" + i + ") "  + object );
            System.out.print( "observation for stroke " + i + " is " );
            */
            if ( object instanceof Line ) {
                Line line = (Line)object;
                double x = line.x2 - line.x1;
                double y = line.y2 - line.y1;
                
                double angle = Math.atan2( y, x );
                
                if ( ( angle >  7*Math.PI/18 ) &&
                     ( angle <  11*Math.PI/18 ) ) {
                    observations += " 3";
                    //System.out.println( "3" );
                    continue;
                }
                if ( ( angle <  -7*Math.PI/18 ) &&
                     ( angle >  -11*Math.PI/18 ) ) {
                    observations += " 3";
                    //System.out.println( "3" );
                    continue;
                }
                if ( ( angle <  2*Math.PI/18 ) &&
                     ( angle > -2*Math.PI/18 ) ) {
                    observations += " 4";
                    //System.out.println( "4" );
                    continue;
                }
                if ( ( angle >  16*Math.PI/18 ) ||
                     ( angle < -16*Math.PI/18 ) ) {
                    observations += " 4";
                    //System.out.println( "4" );
                    continue;
                }
                if ( x*y >= 0 ) {
                    observations += " 2";  // 1 and 2 swapped due to the flip in y axis
                    //System.out.println( "2" );
                    continue;
                } else {
                    observations += " 1"; // 1 and 2 swapped due to the flip in y axis
                    //System.out.println( "1" );
                    continue;
                }
            }
            if ( object instanceof Ellipse ) {
                Rectangle bounds = ((Ellipse)object).getRectangularBounds();
                if ( bounds.getWidth()/bounds.getHeight() > 1.33 ) {
                    observations += " 6";
                    //System.out.println( "6" );
                    continue;
                }
                if ( bounds.getWidth()/bounds.getHeight() < 1.0/1.33 ) {
                    observations += " 7";
                    //System.out.println( "7" );
                    continue;
                }
                observations += " 5";
                //System.out.println( "5" );
                continue;
            }
            if ( object instanceof GeneralPath ) {
                observations += " 8";
                //System.out.println( "8" );
                continue;
            }
            if ( object instanceof Polygon ) {
                Polygon polygon = (Polygon)object;
                if ( polygon.npoints == 3 ) {
                    observations += " 9";
                    //System.out.println( "9" );
                }
                if ( polygon.npoints == 4 ) {
                    observations += " 10";
                    //System.out.println( "10" );
                }
                if ( polygon.npoints == 5 ) {
                    observations += " 11";
                    //System.out.println( "11" );
                }
                if ( polygon.npoints == 6 ) {
                    observations += " 12";
                    //System.out.println( "12" );
                }
                if ( polygon.npoints >= 7 ) {
                    observations += " 13";
                    //System.out.println( "13" );
                }
                //System.out.println( "npoints in the polygon = " + polygon.npoints );
                continue;
            }
        }
        
        //System.out.println( "Observations " + observations );
        //System.out.println( "There are " + input_objects.size() + " input_objects" );
        
        return observations;
    }    


    /**
    *
    * Returns a vector of all the geometric objects "sufficiently 
    * close to the point passes, determined by the search radius r.
    * For the moment, the only Terminals are searched. This method
    * looks at the original data points.
    *
    **/    
    public Vector
    getTranslatableNeighborObjectsOriginal( Point p, int r )
    {
        Object       object;
        Translatable translatable;
        Vector       translatable_neighbors = new Vector();
        
        for ( Enumeration e = objects.elements(); e.hasMoreElements(); ) {
            object = e.nextElement();
            if ( object instanceof Translatable ) {
                   translatable = ((Translatable)object);
                if ( translatable.pointIsOnOriginal( p, r ) ) {
                    translatable_neighbors.addElement( translatable );
                }
            }
        }
        
        return translatable_neighbors;
    }
    
    
    /**
    *
    * Returns a vector of all the geometric objects "sufficiently 
    * close to the point passes, determined by the search radius r.
    * For the moment, the only Terminals are searched.
    *
    **/    
    public Vector
    getNeighborObjects( Point p, int r )
    {
        Object          object;
        GeometricObject geometric_object;
        Vector          neighbors = new Vector();
        
        for ( Enumeration e = objects.elements(); e.hasMoreElements(); ) {
            object = e.nextElement();
            if ( object instanceof GeometricObject ) {
                   geometric_object = ((GeometricObject)object);
                if ( geometric_object.pointIsOn( p, r ) ) {
                    neighbors.addElement( geometric_object );
                }
            }
        }
        
        return neighbors;
    }
    
    
    /**
    *
    * Returns a vector of all the geometric objects "sufficiently 
    * close to the point passes, determined by the search radius r.
    * For the moment, the only Terminals are searched. This method
    * looks at the original data points.
    *
    **/    
    public Vector
    getNeighborObjectsOriginal( Point p, int r )
    {
        Object          object;
        GeometricObject geometric_object;
        Vector          neighbors = new Vector();
        
        for ( Enumeration e = objects.elements(); e.hasMoreElements(); ) {
            object = e.nextElement();
            if ( object instanceof GeometricObject ) {
                   geometric_object = ((GeometricObject)object);
                if ( geometric_object.pointIsOnOriginal( p, r ) ) {
                    neighbors.addElement( geometric_object );
                }
            }
        }
        
        return neighbors;
    }

    /**
    *
    * Handle mouse events
    *
    **/
    public void
    mouseClicked( MouseEvent e ) 
    {
        int    index;
        Object object;
        
        Vector neighbors;
        Point  point = new Point( e.getPoint() );

        
        if ( e.getClickCount() == 1 ) {
            switch ( mode ) 
            {
                case SELECTION_MODE :
                     changeModeTo( RECOGNITION_MODE );
                     deselectObjects();
                     paint( Tablet.debug_graphics );
                     break;
                case RECOGNITION_MODE :
                     changeModeTo( SELECTION_MODE );
                     break;
                default : 
                     System.err.println( "No matching mode.." );
                     break;
            }
            
            System.out.println( "single click" );
        }

        if ( show_parsed_objects ) {
            neighbors = getTranslatableNeighborObjects( point, 5 );
        } else {
            neighbors = getTranslatableNeighborObjectsOriginal( point, 5 );
        }
        
        
        System.out.print( "Clicked on ");
        if ( e.getClickCount() == 2 ) {
            objects.removeAll( neighbors );
            paint( Tablet.debug_graphics );
            System.out.println( "double click" );
            tablet.setRecognitionMode( true );
            return;
        }

        for ( int i=0; i<neighbors.size(); i++ ) {
            
            object = neighbors.elementAt(i);
            System.out.println( object );
            
            if ( object instanceof Polygon ) {
                Polygon polygon     = ((Polygon)object);
                double  angles[]    = polygon.getAbsolutePositiveAngles();    
                double  window_size = Tablet.window_width;
                Tablet.bar_statistics_module.plotBarGraph( angles,
                                                           0, 
                                                           Math.PI+window_size, 
                                                           window_size );

                double wrapped_angles[] = new double[angles.length*2];

                int j = 0;
                for ( j = 0;             j<angles.length;         j++ )
                    wrapped_angles[j] = angles[j];
                for ( j = angles.length; j<wrapped_angles.length; j++ )
                    wrapped_angles[j] = angles[j%angles.length] + Math.PI;

                System.out.print( "window_size " + window_size );
                Tablet.sliding_window_statistics_module.plotSlidingWindowGraph(
                    angles, 
                    0,
                    Math.PI, 
                    Math.PI/180,
                    window_size );
            }
        }
    }
    
    /**
    *
    * Handle mouse events
    *
    **/
    public void
    mouseEntered( MouseEvent e ) 
    {
    }
    
    /**
    *
    * Handle mouse events
    *
    **/
    public void
    mouseExited( MouseEvent e ) 
    {
    }
    
    /**
    *
    * Handle mouse events
    *
    **/
    public void
    mousePressed( MouseEvent e ) 
    {
        Point  point     = new Point( e.getPoint() );
        Vector neighbors;
        
        last_position = new Point( e.getPoint() );
    }
    
    /**
    *
    * Handle mouse events
    *
    **/
    public void
    mouseReleased( MouseEvent e ) 
    {
        if ( ( selected_objects != null    ) && 
             ( selected_objects.length > 0 ) ) {
            changeModeTo( SELECTION_MODE );
               paint( Tablet.debug_graphics );
        }
        //deselectObjects();
    }    
    
    /**
    *
    * Handle key stroke events
    *
    **/
    public void
    keyPressed( KeyEvent k )
    {
    }
     
     
    /**
    *
    * Handle key stroke events
    *
    **/
    public void
    keyReleased( KeyEvent k )
    {    
    }
    
    
    /**
    *
    * Handle key stroke events
    *
    **/
    public void
    keyTyped( KeyEvent k )
    {
        char key = k.getKeyChar();
        
        if ( key == 'p' ) {
            parse();
            return;
        }
    }
    
         
    /**
    *
    * Mouse events
    *
    **/
    public void
    mouseDragged( MouseEvent e )
    {
        Point point = new Point( e.getPoint() );
        
        if ( selected_objects != null ) {
            tablet.setRecognitionMode( false );
            for ( int i=0; i<selected_objects.length; i++ ) {
                if ( selected_objects[i] instanceof Translatable ) {
                    ((Translatable)selected_objects[i]).translate( 
                        (point.x-last_position.x), 
                        (point.y-last_position.y) );
                }
            }
            paint( Tablet.debug_graphics );
            last_position = point;
        }
    }


    /**
    *
    * Mouse events
    *
    **/
    public void
    mouseMoved( MouseEvent e )
    {
    }     
    
    
    /**
    *
    * Mouse events
    *
    **/
    public void
    changeModeTo( int mode )
    {
        this.mode = mode;
        if ( mode == RECOGNITION_MODE ) {
            tablet.setRecognitionMode( true );
            TabletGUI.setStatusMessage( "In recognition mode." );
        }
        if ( mode == SELECTION_MODE ) {
            tablet.setRecognitionMode( false );
            TabletGUI.setStatusMessage( "In selection mode. Select and drag..." );
        }
    }
    
    
    /**
    *
    * Deselect the objects 
    *
    **/
    public void
    deselectObjects()
    {
        selected_objects = null;
    }
    
         
    /**
    *
    * Select the objects in the argument vector.
    *
    **/
    public void
    selectObjects( Translatable[] translatables )
    {
        selected_objects = translatables;
        changeModeTo( SELECTION_MODE );
    }
    
         
    /**
    *
    * Select the objects in the argument vector.
    *
    **/
    public void
    selectObjects( Vector translatables )
    {
        selected_objects = new Translatable[translatables.size()];
        for ( int i=0; i<selected_objects.length; i++ ) {
            selected_objects[i] = (Translatable) translatables.elementAt( i );
        }
        changeModeTo( SELECTION_MODE );
        paint( Tablet.debug_graphics );
    }
    
         
    /**
    *
    * Call the parse method in spatial_parser
    *
    **/
    public void
    parse()
    {
        spatial_parser.parse( this );
        paint( Tablet.debug_graphics );
    }
    
         
    /**
    *
    * Launch a FileDialog and do a load
    *
    **/
    public void
    doOpen()
    {
        System.out.println( "Dialog launched" );
        FileDialog file_dialog = new FileDialog( tablet, 
                                                 "Open saved design",
                                                 FileDialog.LOAD );
        file_dialog.setFilenameFilter( new MechanicalDesignFilenameFilter() );
        file_dialog.setVisible(true);
        reset();
        
        if ( ( file_dialog.getFile() == "" ) || 
             ( file_dialog.getFile() == 
             MechanicalDesignFilenameFilter.MECHANICAL_DESIGN_FILE_SIGNATURE )){
            return; 
        }
        
        current_file_name      = file_dialog.getFile();
        current_file_directory = file_dialog.getDirectory();
        
        String path = current_file_directory + current_file_name;
        System.out.println( "Loading from " + path );

        addObjects( 
            LoadStoreModule.loadTerminalsFromFile( path ) );

	System.out.println("objects before " + objects.size());
	System.out.println("stroke_vertices before " + stroke_vertices.size());
        stroke_vertices = LoadStoreModule.loadVerticesFromFile(
                              path + "_vertices" );      
	System.out.println("stroke_vertices after " + stroke_vertices.size());
	System.out.println("objects after " + objects.size());

	System.out.println( ((GeometricObject)(objects.elementAt(0))).getOriginalVertices() );
        
	Polygon p = ((GeometricObject)(objects.elementAt(0))).getDataPoints();
	
	for (int i=0; i<p.xpoints.length; i++) {
	    System.out.println( p.xpoints[i] + " , " + p.ypoints[i] );
	}

     

	System.out.println( "p" + p );
	Vertex o_v[] = p.getOriginalVertices();
	
	for (int i=0; i<o_v.length; i++)
	    System.out.println( i + " " + o_v[i] ); 
	
	

	

	System.out.println( ((Polygon)(objects.elementAt(0))).xpoints );
        
	

        tablet.control_module.setStrokeList( current_file_name, 
                                             stroke_vertices.size() );
    }
    
         
    /**
    *
    * Launch a FileDialog and do a load
    *
    **/
    public void
    openRawVertices( String filename )
    {
        ArrayList strokes = LoadStoreModule.loadRawVerticesFromFile( filename );
        tablet.processStrokes( strokes );
    }

  public void openRawVertices_drg( String filename )
  {
    StrokeReader read = null;
    try {
      read = new StrokeReader( new FileReader( filename ) );
    }
    catch ( IOException e ) {
      System.err.println( e );
      return;
    }
    ArrayList strokes = new ArrayList( read.getStrokes() );
    Iterator it = strokes.iterator();
    ArrayList strokePoints = new ArrayList();
    while ( it.hasNext() ) {
      StrokeData sd = (StrokeData)it.next();
      strokePoints.add( sd.vertices );
    }
      
    tablet.processStrokes( strokePoints );
  }
        
         
    /**
    *
    * Launch a FileDialog and do a save
    *
    **/
    public void
    doSave()
    {
        if ( current_file_name == "Untitled" ) {
            doSaveAs();
        } else {
            String path = current_file_directory + current_file_name;
            System.out.println( "Saving in " + path ); 
                                
            LoadStoreModule.storeTerminalsInFile(
                path,
                objects );
            LoadStoreModule.storeVerticesInFile(
                path + "_vertices",
                stroke_vertices );
        }
    }


    /**
    *
    * Launch a FileDialog and do a save
    *
    **/
    public void
    saveRawVertices( String filename )
    {
        LoadStoreModule.storeRawVerticesInFile( filename, stroke_vertices );
    }
    
         
    /**
    *
    * Launch a FileDialog and do a save as
    *
    **/
    public void
    doSaveAs()
    {
        FileDialog file_dialog = new FileDialog( tablet, 
                                                    "Save design as",
                                                    FileDialog.SAVE );

        file_dialog.setFilenameFilter( new MechanicalDesignFilenameFilter() );
        file_dialog.setFile( 
            MechanicalDesignFilenameFilter.MECHANICAL_DESIGN_FILE_SIGNATURE );
        file_dialog.setVisible(true);
        
        if ( ( file_dialog.getFile() == "" ) || 
             ( file_dialog.getFile() == 
             MechanicalDesignFilenameFilter.MECHANICAL_DESIGN_FILE_SIGNATURE )){
            return; 
        }
        current_file_name      = file_dialog.getFile();
        current_file_directory = file_dialog.getDirectory();

        doSave();
    }
    
         
    /**
    *
    * do a load
    *
    **/
    public void
    openAndAppendData( String file_name )
    {
        addObjects( 
            LoadStoreModule.loadTerminalsFromFile( file_name ) );

        stroke_vertices = LoadStoreModule.loadVerticesFromFile(
                              file_name + "_vertices" );        
        
        tablet.control_module.setStrokeList( file_name, 
                                             stroke_vertices.size() );
    }
    
         
    /**
    *
    * do a save
    *
    **/
    public void
    saveData( String file_name )
    {
        System.out.println( "Saving in " + file_name ); 
                            
        LoadStoreModule.storeTerminalsInFile( file_name,
            objects );
        LoadStoreModule.storeVerticesInFile(
            file_name + "_vertices",
            stroke_vertices );
    }

         
    /**
    *
    * Write the images that show scale space data in a single gif file.
    *
    **/
    public void
    deriveProperties()
    {
        StrokeData data = null;
 
         for ( int i=0; i<stroke_vertices.size(); i++ ) {
            data = new StrokeData( (Vertex[])(stroke_vertices.elementAt(i)) );
            data.fit_method                 = LinearFit.Method.ROTATION;
            data.direction_window_width        = 7;
            data.test_line_scale            = 1.1;

             data.invalidateCaches();
            data.deriveProperties();
 
         }
        
        tablet.classifyData( data );        
        
    }

         
    /**
    *
    * Write the images that show scale space data in a single gif file.
    *
    **/
    public void
    deriveSnapshots()
    {
        StrokeData data;
        SpeedScaleSpaceViewer             v_scale_space_viewer     
            = new SpeedScaleSpaceViewer();
        DirectionChangeScaleSpaceViewer dd_dt_scale_space_viewer 
            = new DirectionChangeScaleSpaceViewer();
        
        for ( int i=0; i<stroke_vertices.size(); i++ ) {
            data = new StrokeData( (Vertex[])(stroke_vertices.elementAt(i)) );
            //data.fit_method                 = LinearFit.ROTATION_METHOD;
            data.fit_method                 = LinearFit.Method.SIMPLE_TANGENTS;
            data.direction_window_width        = 7;
            data.test_line_scale            = 1.1;

            if ( data.vertices.length < 10 ) {
                System.out.println( "Skipping stroke " + i );
                continue;
            }
            
            data.invalidateCaches();
            data.deriveProperties();
            data.deriveScaleSpaces();
            v_scale_space_viewer.setScaleSpaceData( data.speed_scale_space,
                                                    data.speed_gaussians,
                                                    data );
            dd_dt_scale_space_viewer.setScaleSpaceData( data.dd_dt_scale_space,
                                                        data.dd_dt_gaussians,
                                                        data );
            TabletDataProcessor.data                     = data;
            tablet.v_scale_space_viewer     = v_scale_space_viewer;
            tablet.dd_dt_scale_space_viewer = dd_dt_scale_space_viewer;
            tablet.v_scale_space_viewer.scale.addAdjustmentListener( tablet );
            tablet.dd_dt_scale_space_viewer.scale.addAdjustmentListener(tablet);
            try {
                System.out.println( "Trying stroke #" + i );
                GIFEncoder encoder  =
                    new GIFEncoder( dd_dt_scale_space_viewer.getImage() );
                OutputStream output = 
                    new BufferedOutputStream( 
                        new FileOutputStream( current_file_name + 
                                              "_ss_direction" + i + ".gif" ) );
            
                encoder.Write( output );
                
                System.out.println( "Trying stroke #" + i );
                encoder  = new GIFEncoder( v_scale_space_viewer.getImage() );
                output = new BufferedOutputStream( 
                            new FileOutputStream( current_file_name + 
                                              "_ss_speed" + i + ".gif" ) );
            
                encoder.Write( output );
            } catch( Exception exception ) {
                System.out.println( exception );
            }
        }
    }


    /**
    *
    * Print the feature vectors
    *
    **/
    public void
    printFeatureVectors()
    {
        System.out.print( getFeatureVectors() );
    }


    /**
    *
    * Feature vectors in string format
    *
    **/
    public String
    getFeatureVectors()
    {
        String feature  = "";
        String features = "";
        
        for ( int i=0; i<all_paths.size(); i++ ) {
            feature = GeneralPathProcessor.featureString(
                ((GeneralPath) all_paths.elementAt(i)) );        
            features += "Feature " + feature + "\n";
        }
        
        return features;
    }


    /**
    *
    * Save the feature vectors
    *
    **/
    public void
    saveFeatureVectors( String file_name )
    {
        LoadStoreModule.storeStringInFile( "features", getFeatureVectors() );
     }


    /**
    *
    * Save the feature vectors
    *
    **/
    public void
    runSVMClassifier( GeneralPath general_path )
    {
        LoadStoreModule.storeStringInFile( "feature",
            GeneralPathProcessor.featureString( general_path ) );
     }
}


/** 
  * 
  * $Log: ObjectManager.java,v $
  * Revision 1.1  2006-11-22 22:54:35  hammond
  * mit's drg code, will extract and refactor to get ladder code
  *
  * Revision 1.20  2006/02/17 20:25:16  moltmans
  * Changed names of LinearFit.Method variables
  *
  * Revision 1.19  2006/02/17 20:10:00  moltmans
  * The LinearFit class has been updated to use an enum instead of the less reliable int flag.
  *
  * Revision 1.18  2005/10/11 19:14:12  ouyang
  * Added Clearpanel support for Visipad and implemented the ellipse fit model from clocksketch.
  *
  * Revision 1.17  2005/03/29 02:32:58  hammond
  * got acute and obtuse auto gen mathematica working
  *
  * Revision 1.16  2005/03/18 22:23:15  hammond
  * updated show
  *
  * Revision 1.15  2005/03/08 21:07:34  mtsezgin
  * Fixed bugs in SimpleClassifier2.java SimpleClassifier3.java
  *
  * Added features to StrokeData.java DataManager.java ObjectManager.java
  *
  * Revision 1.14  2005/01/27 22:15:14  hammond
  * organized imports
  *
  * Revision 1.13  2003/11/05 01:42:02  moltmans
  * Found more ^M's  They should all be gone now... Again...  For good?
  *
  * Revision 1.12  2003/06/26 19:57:15  calvarad
  * Lots of bug fixes
  *
  * Revision 1.11  2003/05/07 20:58:54  mtsezgin
  *
  * Fixed some problems with the arc recognition in general, and recognition
  * on the acer tablet in particular.
  *
  * Revision 1.10  2003/03/06 01:08:52  moltmans
  * Added copyright to all the files.
  *
  * Revision 1.9  2002/07/22 21:03:33  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.7  2002/07/09 16:04:20  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.6  2001/11/26 18:29:03  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.5  2001/11/23 03:22:56  mtsezgin
  * Major reorganization
  *
  * Revision 1.4  2001/10/12 23:32:29  mtsezgin
  * Turned off printing...
  *
  * Revision 1.3  2001/10/12 22:25:52  mtsezgin
  * This is a commit of all files.
  * Shoapid
  * vi sux:q
  *
  * Revision 1.2  2001/04/03 16:08:01  uid6752
  * Minor changes to make the timing capture work.
  *
  * Revision 1.1.1.1  2001/03/29 16:25:01  moltmans
  * Initial directories for DRG
  *
  * Revision 1.21  2000/09/06 22:40:44  mtsezgin
  * Combinations of curves and polygons are successfully approximated
  * by straight lines and Bezier curves as appropriate. System works
  * quite reliably.
  *
  * Revision 1.14  2000/06/08 03:18:32  mtsezgin
  *
  * Added addObjects method for adding a Vector of objects.
  *
  * Revision 1.13  2000/06/03 01:52:13  mtsezgin
  *
  * Removed GeometryUtil.java and delegated the functionality to ObjectManager.
  *
  * Revision 1.12  2000/06/02 21:11:15  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.11  2000/05/26 20:46:44  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.9  2000/05/22 02:42:34  mtsezgin
  *
  * The current version enables polygons to be sketched in pieces.
  *
  * Revision 1.8  2000/05/04 01:36:27  mtsezgin
  *
  * Fixed minor bugs.
  * The current version successfuly parses Motor, Cross, Nail and Ground.
  * In addition the ParseSupervisor is introduced here.
  *
  * Revision 1.7  2000/04/28 04:45:04  mtsezgin
  *
  * Now each GeometricObject keeps the mouse input that was previously
  * discarded. User can switch between seeing the recognized mode and
  * the raw mode. setDataPoints( Polygon points ) and getDataPoints()
  * are added to GeometricObject, and all the implementors are modified
  * accordingly.
  *
  * Revision 1.6  2000/04/25 22:18:09  mtsezgin
  *
  * Fixed some bugs in addObject.
  *
  * Revision 1.5  2000/04/20 03:59:53  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.4  2000/04/13 06:24:08  mtsezgin
  *
  * The current version of the program recognized Crosses, and Shades.
  * Implementors of Terminal and their descendants were modified to
  * implement the changes in GeometricObject.
  *
  * Revision 1.3  2000/04/11 00:41:47  mtsezgin
  *
  * Now the whole package succesfully parses a motor.
  *
  * Revision 1.2  2000/04/06 19:16:23  mtsezgin
  *
  * Modified all the classes to use my Point class which extends java.awt.Point
  * instead of directly using java.awt.Point
  *
  * Revision 1.1.1.1  2000/04/01 03:07:07  mtsezgin
  * Imported sources
  *
  * Revision 1.2  2000/03/31 22:41:04  mtsezgin
  *
  * Started Log tracking.
  *
  *  
  **/
