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
  * $Id: DirectionChangeScaleSpaceViewer.java,v 1.1 2006-11-22 22:54:35 hammond Exp $
  * $Name:  $
  * $Locker:  $
  * $Source: /cvsroot/tamudrg/code/src/edu/mit/sketch/toolkit/DirectionChangeScaleSpaceViewer.java,v $
  *
  **/


import edu.mit.sketch.geom.Line;
import edu.mit.sketch.geom.Point;
import edu.mit.sketch.geom.Range;
import edu.mit.sketch.geom.Vertex;
import edu.mit.sketch.ui.Tablet;
import edu.mit.sketch.ui.TabletGUI;
import edu.mit.sketch.util.AWTUtil;
import edu.mit.sketch.util.Gaussian;
import edu.mit.sketch.util.GraphicsUtil;
import edu.mit.sketch.util.LinearFit;
import edu.mit.sketch.util.Util;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;

/**
  *
  * This class is used for viewing scale space data.
  *
  **/

public
class   DirectionChangeScaleSpaceViewer
extends Frame
{
    private double     scale_space_data[][];
    private double     scale_space_x[][];
    private double     scale_space_y[][];
    private double     scale_space_d[][];
    private Gaussian   gaussians[];
    private int        feature_indices[][];
    private StrokeData data;
    private Panel      display_panel;
    private Panel      feature_panel;
    private Panel      feature_count_panel;
    private Panel      gaussian_panel;
    private int        first_index;
    public  int        interesting_scale;
    private int        interesting_scale_on_curve;
    private Line       feature_count_fit_lines[];
    
    public Scrollbar   scale;
    
    /**
    *
    * The constructor.
    *
    **/
    public DirectionChangeScaleSpaceViewer()
    {
        super( "Direction Change Scale Space Viewer" );
        
        display_panel       = new Panel();
        feature_panel       = new Panel();
        feature_count_panel = new Panel();
        gaussian_panel      = new Panel();
        
        scale = new Scrollbar( Scrollbar.VERTICAL );
        
        setLayout( new BorderLayout() );
        
        Panel center_panel = new Panel();
        
        GridBagLayout      layout      = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        
        center_panel.setLayout( layout );        
        
        GraphicsUtil.setConstraints( layout,
                                     display_panel,
                                     1,
                                     1,
                                     3,
                                     2,
                                     5.0,
                                     2.0,
                                     GridBagConstraints.BOTH,
                                     GridBagConstraints.CENTER,
                                     new Insets( 1, 1, 1, 1 ) );
                                     
        GraphicsUtil.setConstraints( layout,
                                     feature_panel,
                                     1,
                                     3,
                                     5,
                                     3,
                                     1.0,
                                     4.0,
                                     GridBagConstraints.BOTH,
                                     GridBagConstraints.CENTER,
                                     new Insets( 1, 1, 1, 1 ) );
                                     
        GraphicsUtil.setConstraints( layout,
                                     feature_count_panel,
                                     5,
                                     1,
                                     1,
                                     2,
                                     1.0,
                                     1.0,
                                     GridBagConstraints.BOTH,
                                     GridBagConstraints.CENTER,
                                     new Insets( 1, 1, 1, 1 ) );
                                     
        GraphicsUtil.setConstraints( layout,
                                     gaussian_panel,
                                     4,
                                     1,
                                     1,
                                     2,
                                     1.0,
                                     1.0,
                                     GridBagConstraints.BOTH,
                                     GridBagConstraints.CENTER,
                                     new Insets( 1, 1, 1, 1 ) );
                                     
        center_panel.add( feature_panel  );
        center_panel.add( display_panel );
        center_panel.add( gaussian_panel );
        center_panel.add( feature_count_panel );
        feature_panel.setBackground( new Color( 0xddffff ) );
        display_panel.setBackground( new Color( 0xffddff ) );
        gaussian_panel.setBackground( new Color( 0xddffdd ) );
        feature_count_panel.setBackground( new Color( 0xffffdd ) );
        
        add( scale,        BorderLayout.EAST   );
        add( center_panel, BorderLayout.CENTER );
        

        validate();
    }
    

    /**
    *
    * Derive feature indices
    *
    **/
    public void
    deriveFeatureIndices()
    {
        double data_values[];
        double derivative[];
        int    local_maxima_indices[];
        
        scale_space_x   = new double[scale_space_data.length][0];
        scale_space_y   = new double[scale_space_data.length][0];
        scale_space_d   = new double[scale_space_data.length][0];
        feature_indices = new int[scale_space_data.length][0];
        
        for ( int scale=0; scale<scale_space_data.length; scale++ ) {
            data_values = scale_space_data[scale];
            derivative  = new double[data_values.length-1];
            for ( int i=0; i<derivative.length; i++ ) {
                derivative[i] = (Math.abs( data_values[i+1] ) - 
                                 Math.abs( data_values[ i ] ) )/
                                (( data.time[i+2] - data.time[i] )/2);
            }
        
            local_maxima_indices = 
                Util.getNegativeZeroCrossingIndices( derivative );
            
            //local_maxima_indices = data.filterCollinearVertices( 
            //    local_maxima_indices );
            
            double direction[] = new double[data_values.length];
            direction[0] = data.d[0];
            for ( int i=1; i<direction.length; i++ ) {
                direction[i] = direction[i-1] +
                                ( data_values[i-1]*
                                  (data.accumulated_length[i] -
                                   data.accumulated_length[i-1]) );
            }
            
            
            
            scale_space_x[scale]    = new double[data.vertices.length];
            scale_space_y[scale]    = new double[data.vertices.length];
            scale_space_x[scale][0] = data.vertices[0].x;
            scale_space_y[scale][0] = data.vertices[0].y;
            for ( int i=1; i<direction.length+1; i++ ) {
                double length = data.accumulated_length[i] - 
                                data.accumulated_length[i-1];
                scale_space_x[scale][i] = scale_space_x[scale][i-1] +
                                          Math.cos( direction[i-1] )*length;
                scale_space_y[scale][i] = scale_space_y[scale][i-1] +
                                          Math.sin( direction[i-1] )*length;
            }

            
                        
                
            int fit_indices[] = 
                new int[local_maxima_indices.length+2];

            fit_indices[0]                    = 0;
            fit_indices[fit_indices.length-1] = data_values.length - 1;
            for ( int i=0; i<local_maxima_indices.length; i++ ) {
                fit_indices[i+1] = local_maxima_indices[i];
            }
                
            feature_indices[scale] = fit_indices;
        }
    }
    

    /**
    *
    * update the panel
    *
    **/
    public void
    printData( int scale_index )
    {
        Util.printArray( scale_space_data[scale_index], "\tdata_\t" + scale_index );
    }

    /**
    *
    * update the panel
    *
    **/
    public void
    displayData( int scale_index, int y_value, Tablet tablet )
    {
        if ( !isVisible() ) {
            return;
        }

        double data_values[]   = scale_space_data[scale_index];
        int    fit_indices[]   = feature_indices[scale_index];
        double maxima_values[] = new double[fit_indices.length];
        int    time_stamps[]   = new int[fit_indices.length];
        int    y_value_array[] = new int[fit_indices.length];
        
        tablet.displayFitByIndices( fit_indices );
        TabletGUI.setStatusMessage( fit_indices.length + " vertices" );
            
            
        int filtered_indices[] = data.filterVerticesByLSQE( fit_indices, 1.2 );
        
        System.out.println( "Filtered out " + ( fit_indices.length - 
                                               filtered_indices.length ) + 
                                               " points." );    
        tablet.displayFitByIndices( filtered_indices, Color.cyan, Color.orange);
        
        /*tablet.displayFitByPositions( scale_space_x[scale_index], 
                                      scale_space_y[scale_index], 
                                      fit_indices );*/
        
        for ( int i=0; i<fit_indices.length; i++ ) {
            time_stamps[i]   = (int) data.time[fit_indices[i]];
            y_value_array[i] = y_value;
            System.out.println( "data_values.length " + data_values.length + "fit_indices[i] " +fit_indices[i] );
            maxima_values[i] = data_values[fit_indices[i]];
        }
        
        
        GraphicsUtil.clearComponent( display_panel );

        AffineTransform affine_transform = new 
            AffineTransform( Tablet.t_scale,
                             0, 
                             0, 
                             -Tablet.d_theta_scale,
                             40, 
                             5*display_panel.getSize().height/10 );
        AWTUtil.paintFvsG( data.time,
                           data_values, 
                           Color.blue,      
                           display_panel, 
                           affine_transform );
        AWTUtil.paintDots( time_stamps,
                           maxima_values, 
                           Color.red,      
                           display_panel, 
                           affine_transform,
                           2 );
        
        affine_transform = new 
            AffineTransform( 2, 
                             0, 
                             0, 
                             -.5, 
                             40, 
                             9*feature_panel.getSize().height/10 );
        AWTUtil.paintDots( fit_indices,
                           y_value_array,
                           AWTUtil.colors2[(int)(Math.random()*
                                              AWTUtil.colors2.length)],
                           feature_panel, 
                           affine_transform );

        affine_transform = new 
            AffineTransform( .5, 
                             0, 
                             0, 
                             -.75, 
                             20, 
                             9*feature_count_panel.getSize().height/10 );
        paintFeatureCountFit( affine_transform );
                             
        AWTUtil.paintDot( y_value,
                          fit_indices.length,
                          AWTUtil.colors2[(int)(Math.random()*
                                          AWTUtil.colors2.length)],
                          feature_count_panel, 
                          affine_transform );

        affine_transform = new 
            AffineTransform( 2, 
                             0, 
                             0, 
                             -50, 
                             gaussian_panel.getSize().width/2, 
                             9*gaussian_panel.getSize().height/10 );

        GraphicsUtil.clearComponent( gaussian_panel );
        for ( int i=0; i<gaussians[scale_index].g.length; i++ ) {
            AWTUtil.paintDot( i,
                              gaussians[scale_index].g[i],
                              Color.black,
                              gaussian_panel, 
                              affine_transform );
        }
        
        tablet.joint_display.paintChangeInDirection( 
            data_values,
            data.accumulated_length );
        
        
        System.out.println( fit_indices.length + ", " + 
                            scale_index        + ", " + interesting_scale );
    }
    

    /**
    *
    * print the scale space data into a file
    * scale - #of zero crossings - index of zero crossing
    **/
    public void
    printData( String file_name )
    {
        try{
            FileOutputStream fos = new FileOutputStream( file_name );

            for ( int scale=0; scale<scale_space_data.length; scale++ ) {
                 for ( int i=0; i<feature_indices[scale].length; i++ ) {
                    fos.write( ( 
                        scale                         + "\t" +
                        feature_indices[scale].length + "\t" +
                        feature_indices[scale][i]     + "\n" ).getBytes() );
                    System.out.print(".");
                }
             }
        } catch( Exception e ) {        
        }
     }
    
    
    /**
    *
    * setScaleSpaceData
    *
    **/
    public void
    setScaleSpaceData( double   scale_space_data[][], 
                       Gaussian gaussian_array[],
                       StrokeData data )
    {
        this.scale_space_data = scale_space_data;
        this.gaussians        = gaussian_array;
        this.data             = data;
        scale.setBlockIncrement( 1 );
        scale.setUnitIncrement( 1 );
        scale.setMinimum( 1 );
        scale.setMaximum( scale_space_data.length );
        deriveFeatureIndices();
        interesting_scale = getInterestingScale();
    }
    
    
    /**
    *
    * Find a good scale in the feature-count -- feature graph by fitting 
    * a sigmoid function like polyline to the feature-count -- feature data.
    * 
    **/
    public int
    getInterestingScale()
    {
        feature_count_fit_lines = new Line[2];
        
        AffineTransform affine_transform = new 
            AffineTransform( .1, 
                             0, 
                             0, 
                             -.75, 
                             20, 
                             9*feature_count_panel.getSize().height/10 );
    
        first_index = 0;
        for ( int i=0; i<feature_indices.length; i++ ) {
            if ( feature_indices[i].length != feature_indices[i+1].length ) {
                first_index = i;
                break;
            }
        }
        
        Point  points[] = new Point[feature_indices.length-first_index];
        double x[]      = new double[feature_indices.length-first_index];
        double y[]      = new double[feature_indices.length-first_index];
        for ( int i=first_index; i<feature_indices.length; i++ ) {
            points[i-first_index] = new Point( i, feature_indices[i].length );
            x[i-first_index]      = points[i-first_index].x;
            y[i-first_index]      = points[i-first_index].y;
        }
        
        Vertex fit_vertices[] = new Vertex[3];
        
        fit_vertices[0] = new Vertex( points[0] );
        fit_vertices[1] = new Vertex( points[1] );
        fit_vertices[2] = new Vertex( points[points.length-1] );
        
        fit_vertices[0].setIndex( 0 );
        fit_vertices[1].setIndex( 1 );
        fit_vertices[2].setIndex( points.length-1 );

        double total_x = 0.0;
        double total_y = 0.0;
        for ( int i=0; i < x.length; i++ ) {
            total_x += x[i];
            total_y += y[i];
        }

        Line   first_line;
        Line   second_line;
        Point  intersection;
        Range  range           = new Range();
        double error           = Double.MAX_VALUE;
        double min_error       = Double.MAX_VALUE;
        int    min_error_scale = 1;
        int    min_error_index = 1;
        int    second_index    = 1;
        int    first_center_x  = 0;
        int    first_center_y  = 0;
        int    second_center_x = 0;
        int    second_center_y = 0;
        double x_sum           = x[0];
        double y_sum           = y[0];
        double first_angle     = 0.0;
        double second_angle    = 0.0;
        
        for ( int i=1; i < points.length-1; i++ ) {
            first_angle  = LinearFit.findAngleFastRotation( x, y, 0, i              , 10 );
            second_angle = LinearFit.findAngleFastRotation( x, y, i, points.length-1, 10 );
            
            x_sum   += x[i];
            y_sum   += y[i];

            first_center_x = ((int) (x_sum/(i+1)));
            first_center_y = ((int) (y_sum/(i+1)));
            
            first_line = new 
                Line( first_center_x - Math.cos( first_angle )*10000,
                      first_center_y - Math.sin( first_angle )*10000,
                      first_center_x + Math.cos( first_angle )*10000,
                      first_center_y + Math.sin( first_angle )*10000 );

            second_center_x = ((int) ( (total_x-x_sum) / (points.length-i-1)));
            second_center_y = ((int) ( (total_y-y_sum) / (points.length-i-1)));
            
            second_line = new 
                Line( second_center_x - Math.cos( second_angle )*10000,
                      second_center_y - Math.sin( second_angle )*10000,
                      second_center_x + Math.cos( second_angle )*10000,
                      second_center_y + Math.sin( second_angle )*10000 );

            
            try {
                intersection = first_line.getIntersection( second_line );
            } catch( Exception e ) {
                System.err.println( "Error in finding intersection " +
                                    "in DirectionChangeScaleSpace"   + e );
                System.out.println( "first_line  " + first_line );
                System.out.println( "second_line " + second_line );
                break;
            }
             
            if ( isVisible() ) {
                Color color = AWTUtil.colors2[(int)(Math.random()*
                                              AWTUtil.colors2.length)];
                AWTUtil.paintLine( first_line,
                                   color,
                                   feature_count_panel, 
                                   affine_transform );
                AWTUtil.paintLine( second_line,
                                   color,
                                   feature_count_panel, 
                                   affine_transform );
            }            
            
            range.min = 0;
            range.max = i;
            error     = first_line.getLSQError( points, range );
            range.min = i;
            range.max = points.length;
            error    += second_line.getLSQError( points, range );
            
            //System.out.println("error =\t" + error );
            
            if ( error < min_error ) {
                min_error_index            = i;
                min_error_scale            = (int)intersection.x;
                min_error                  = error;
                feature_count_fit_lines[0] = new Line( first_line.x1/5,
                                                       first_line.y1,
                                                       first_line.x2/5,
                                                       first_line.y2 );
                feature_count_fit_lines[1] = new Line( second_line.x1/5,
                                                       second_line.y1,
                                                       second_line.x2/5,
                                                       second_line.y2 );
            }
        }
        
        return min_error_scale;
    }
    
    
    /**
    *
    * Find a good scale in the feature-count -- feature graph by fitting 
    * a sigmoid function like polyline to the feature-count -- feature data.
    * 
    **/
    public int
    getInterestingScaleOnCurve()
    {
        first_index = 0;
        for ( int i=0; i<feature_indices.length; i++ ) {
            if ( feature_indices[i].length != feature_indices[i+1].length ) {
                first_index = i;
                break;
            }
        }
        
        Point points[] = new Point[feature_indices.length-first_index];
        for ( int i=first_index; i<feature_indices.length; i++ ) {
            points[i-first_index] = new Point( i, feature_indices[i].length );
        }
        
        Vertex fit_vertices[] = new Vertex[3];
        
        fit_vertices[0] = new Vertex( points[0] );
        fit_vertices[1] = new Vertex( points[1] );
        fit_vertices[2] = new Vertex( points[points.length-1] );
        
        fit_vertices[0].setIndex( 0 );
        fit_vertices[1].setIndex( 1 );
        fit_vertices[2].setIndex( points.length-1 );
        
        double min_error       = Double.MAX_VALUE;
        int    min_error_index = 1;
        int    second_index    = 1;
        
        for ( int i=1; i < points.length; i++ ) {
            fit_vertices[1] = new Vertex( points[i] );
            fit_vertices[1].setIndex( i );
            double error = AWTUtil.leastSquaresForPolygon(fit_vertices, points);
            
            System.out.println("error =\t" + error );
            
            if ( error < min_error ) {
                min_error_index = i;
                min_error       = error;
            }
        }
        
        return min_error_index + first_index;
    }


    /**
    *
    * Get image
    *
    **/
    public Image
    getImage()
    {
        BufferedImage image = new BufferedImage( 1280,
                                                  1024,
                                                  BufferedImage.TYPE_INT_RGB );
        int scale_space_y_offset   = 1024;
        int feature_count_y_offset = 204;

        Graphics         g               = image.getGraphics();
        AffineTransform affine_transform = null ;
        g.setColor( Color.white );
        g.fillRect( 0, 0, image.getWidth(), image.getHeight() );

        for ( int scale_index = 0; 
                  scale_index < scale_space_data.length;
                  scale_index++ ) {
                
            double data_values[]   = scale_space_data[scale_index];
            int    fit_indices[]   = feature_indices[scale_index];
            double maxima_values[] = new double[fit_indices.length];
            int    time_stamps[]   = new int[fit_indices.length];
            int    y_value_array[] = new int[fit_indices.length];
    
            
            for ( int i=0; i<fit_indices.length; i++ ) {
                time_stamps[i]   = (int) data.time[fit_indices[i]];
                y_value_array[i] = scale_index/5;
                maxima_values[i] = data_values[fit_indices[i]];
            }
 
 
            affine_transform = new
                AffineTransform( 2, 0, 0, -.5, 40, 9*image.getHeight()/10 );
            AWTUtil.paintDots( fit_indices,
                               y_value_array,
                               Color.red,
                               image,
                               affine_transform );

            affine_transform = new
                AffineTransform( .5, 0, 0, -.75, 20, 2*image.getHeight()/10 );
 
            AWTUtil.paintDot( y_value_array[0],
                              fit_indices.length,
                              Color.blue,
                              image,
                              affine_transform );
        }
        
        for ( int i=-5; i<6; i++ ) {
            AWTUtil.paintDot( interesting_scale/5-i,
                              feature_indices[interesting_scale].length+i,
                              Color.black,
                              image,
                              affine_transform );
            AWTUtil.paintDot( interesting_scale/5+i,
                              feature_indices[interesting_scale].length+i,
                              Color.black,
                              image,
                              affine_transform );
        }
        
        displayFitByIndices( feature_indices[interesting_scale],
                             image,
                             new Point( 300, 0 ) );
        
        displayBlackboardFit( image,
                              new Point( 300, 0 ) );
        
        return image;
    }
    
    
    /**
    *
    * displayFitByIndices
    *
    **/
    public void 
    displayBlackboardFit( Image image, Point origin )
    {
        SimpleClassifier sc = new SimpleClassifier( data );
        Graphics2D       g  = (Graphics2D)image.getGraphics();

        sc.classify();
        
        g.setColor( Color.blue );
        g.setStroke( new BasicStroke( 1.0f ) );

        Vertex final_fit[] = sc.final_fit;
        
        for ( int i=0; i<final_fit.length-1; i++ ) {
            g.drawLine( (int)(origin.x + final_fit[i].x),
                (int)(origin.y + final_fit[i].y),
                (int)(origin.x + final_fit[i+1].x),
                (int)(origin.y + final_fit[i+1].y ));
        }
        
        g.setColor( Color.cyan );
                
        for ( int i=0; i<final_fit.length; i++ ) {
            g.drawOval( (int)(origin.x + final_fit[i].x-2),
                (int)(origin.y + final_fit[i].y-2),
                6,
                6);
        }
    }
    
    
    /**
    *
    * displayFitByIndices
    *
    **/
    public void 
    displayFitByIndices( int indices[], Image image, Point origin )
    {
        Graphics2D g = (Graphics2D)image.getGraphics();

        g.setColor( Color.black );
        g.setStroke( new BasicStroke( Tablet.brush_thickness ) );
                
        for ( int i=0; i<data.vertices.length-1; i++ ) {
            g.drawLine( (int)(origin.x + data.vertices[i].x),
                        (int)(origin.y + data.vertices[i].y),
                        (int)(origin.x + data.vertices[i+1].x),
                        (int)(origin.y + data.vertices[i+1].y ));
        }
        
        g.setColor( Color.green );
        g.setStroke( new BasicStroke( 1.0f ) );

        for ( int i=0; i<indices.length-1; i++ ) {
            g.drawLine( (int)(origin.x + data.vertices[indices[i]].x),
                        (int)(origin.y + data.vertices[indices[i]].y),
                        (int)(origin.x + data.vertices[indices[i+1]].x),
                        (int)(origin.y + data.vertices[indices[i+1]].y ));
        }
        
        g.setColor( Color.red );
                
        for ( int i=0; i<indices.length; i++ ) {
            g.drawOval( (int)(origin.x + data.vertices[indices[i]].x-2),
                        (int)(origin.y + data.vertices[indices[i]].y-2),
                        4,
                        4);
        }
    }


    /**
    *
    * paintFeatureCountFit
    *
    **/
    public void
    paintFeatureCountFit( AffineTransform affine_transform )
    {
        double x[] = new double[3];
        double y[] = new double[3];

        // The /5 is for scaling the x axis of the graph.
        x[0] = first_index/5;
        y[0] = feature_indices[0].length;

        x[1] = interesting_scale/5;
        y[1] = feature_indices[interesting_scale].length;

        x[2] = (feature_indices.length - 1)/5;
        y[2] = feature_indices[feature_indices.length - 1].length;

        AWTUtil.paintFvsG( x,
                           y,
                           Color.cyan,
                           feature_count_panel, 
                           affine_transform );
                           
        if ( feature_count_fit_lines != null ) {
            AWTUtil.paintLine( feature_count_fit_lines[0],
                               Color.red,
                               feature_count_panel, 
                               affine_transform );
            AWTUtil.paintLine( feature_count_fit_lines[1],
                               Color.blue,
                               feature_count_panel, 
                               affine_transform );
        }
    }
}

/**
  *
  * $Log: DirectionChangeScaleSpaceViewer.java,v $
  * Revision 1.1  2006-11-22 22:54:35  hammond
  * mit's drg code, will extract and refactor to get ladder code
  *
  * Revision 1.9  2006/02/17 20:10:00  moltmans
  * The LinearFit class has been updated to use an enum instead of the less reliable int flag.
  *
  * Revision 1.8  2005/07/25 17:12:10  dpitman
  * sketch.geom package now uses doubles for coordinate values instead of integers. Code in other packages was adjusted because it contained int-based Graphic calls, and thus, the coordinate values had to be type-casted.
  *
  * Revision 1.7  2005/03/29 02:32:58  hammond
  * got acute and obtuse auto gen mathematica working
  *
  * Revision 1.6  2005/01/27 22:15:13  hammond
  * organized imports
  *
  * Revision 1.5  2003/11/05 01:42:02  moltmans
  * Found more ^M's  They should all be gone now... Again...  For good?
  *
  * Revision 1.4  2003/03/06 01:08:52  moltmans
  * Added copyright to all the files.
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
  * Revision 1.10  2000/09/06 22:40:33  mtsezgin
  * Combinations of curves and polygons are successfully approximated
  * by straight lines and Bezier curves as appropriate. System works
  * quite reliably.
  *
  * Revision 1.4  2000/06/08 03:11:46  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.3  2000/06/03 01:52:31  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.2  2000/06/02 21:11:14  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.1  2000/05/26 20:39:25  mtsezgin
  *
  * This GUI lets the user play with the paramaters used by the
  * recognition algorithms.
  *
  * Revision 1.2  2000/05/24 01:53:22  mtsezgin
  *
  * The polygon angle normalization works reliably.
  *
  * Revision 1.1  2000/05/21 23:13:15  mtsezgin
  *
  * This module is used for plotting and investigating certain statistical
  * features.
  *
  *
  *
  **/
