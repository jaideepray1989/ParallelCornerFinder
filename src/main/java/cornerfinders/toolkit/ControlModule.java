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
  * $Id: ControlModule.java,v 1.1 2006-11-22 22:54:35 hammond Exp $
  * $Name:  $
  * $Locker:  $
  * $Source: /cvsroot/tamudrg/code/src/edu/mit/sketch/toolkit/ControlModule.java,v $
  *
  **/


import edu.mit.sketch.geom.GeometryUtil;
import edu.mit.sketch.geom.Vertex;
import edu.mit.sketch.ui.Tablet;
import edu.mit.sketch.ui.TabletDataProcessor;
import edu.mit.sketch.util.LinearFit;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

/**
  *
  * This class is used for controlling and changing the parameters
  * in the main application.
  *
  **/

public
class      ControlModule
extends    Frame
implements ActionListener,
           ItemListener
{
    private TabletDataProcessor tablet;
    
    private Menu      file;
    private MenuBar   menubar;
    private MenuItem  quit_menu;

    private TextField speed_average_scale;
    private TextField dd_dt_average_scale;
    private TextField circle_vs_speed_bias;
    private TextField circle_vs_general_path_bias;
    private TextField window_width;
    private TextField test_line_scale;
    private TextField direction_window_width;
    private TextField zoom_x;
    private TextField zoom_y;
    private Button    simple_tangents_method;
    private Button    swodr_method;
    private Button    rotational_odr_method;
    private Button    convolve_d;
    private Button    convolve_dd_dt;
    
    private Choice    direction_fit_method;

    private List polygon_fit_list;
    private List stroke_list;
    
    /**
    *
    * The constructor.
    *
    **/
    public ControlModule( TabletDataProcessor tablet )
    {
        super( "Controls" );
        this.tablet = tablet;

        menubar     = new MenuBar();
        file        = new Menu( "File" );
        quit_menu   = new MenuItem( "Quit" );
        
        
        speed_average_scale = 
            new TextField( "" + 
                tablet.speed_average_scale );
        dd_dt_average_scale = 
            new TextField( "" + 
                tablet.dd_dt_average_scale );
        test_line_scale   = 
            new TextField( "" + 
                tablet.test_line_scale );
        circle_vs_speed_bias        = 
            new TextField( "" + 
                tablet.circle_vs_speed_bias );
        circle_vs_general_path_bias = 
            new TextField( "" + 
                tablet.circle_vs_general_path_bias );
        window_width      = 
            new TextField( "" + 
                GeometryUtil.radian2degree( TabletDataProcessor.window_width ) );
        direction_window_width = 
            new TextField( "" + 
                tablet.direction_window_width );
        zoom_x = 
            new TextField( "1.0" );
        zoom_y = 
            new TextField( "1.0" );
        
        simple_tangents_method = new Button( "Tangents"        );
        swodr_method           = new Button( "SOWDR   "        );
        rotational_odr_method  = new Button( "RODR    "        );
        convolve_d             = new Button( "Convolve d"      );
        convolve_dd_dt         = new Button( "Convolve dd_dt " );

        direction_fit_method = new Choice();

        for (LinearFit.Method method : LinearFit.Method.values()) {
          direction_fit_method.addItem(method.toString());         
        }
        
//        direction_fit_method.addItem(LinearFit.Method.SIMPLE_TANGENTS_METHOD.toString());
//
//        direction_fit_method.addItem(LinearFit.Method.SWODR_METHOD.toString());
//
//        direction_fit_method.addItem(LinearFit.Method.ROTATION_METHOD.toString());
                
        direction_fit_method.select(Tablet.fit_method.toString());
        
        polygon_fit_list  = new List( 5 );
        stroke_list = new List( 5 );
        
        setLayout( new GridLayout( 2, 1 ) );

        
        Panel button_panel = new Panel();
        button_panel.setLayout( new GridLayout( 1, 3 ) );
        button_panel.add( simple_tangents_method  );
        button_panel.add( swodr_method              );
        button_panel.add( rotational_odr_method   );

        Panel text_field_panel = new Panel();
        setLayout( new GridLayout( 1, 3 ) );

        text_field_panel.setLayout( new GridLayout( 0, 2 ) );
        text_field_panel.add( new Label("v Average scale  ") );
        text_field_panel.add( speed_average_scale            );
        text_field_panel.add( new Label("dd_dt avrg. scale") );
        text_field_panel.add( dd_dt_average_scale            );
        text_field_panel.add( new Label("Test Line Scale  ") );
        text_field_panel.add( test_line_scale                );
        text_field_panel.add( new Label("Window Width     ") );
        text_field_panel.add( window_width                   );
        text_field_panel.add( new Label("Circle vs SF bias") );
        text_field_panel.add( circle_vs_speed_bias           );
        text_field_panel.add( new Label("Circle vs GP bias") );
        text_field_panel.add( circle_vs_general_path_bias    );
        text_field_panel.add( new Label("DSW Width")         );
        text_field_panel.add( direction_window_width         );
        text_field_panel.add( new Label("X Zooming factor")  );
        text_field_panel.add( zoom_x                         );
        text_field_panel.add( new Label("Y Zooming factor")  );
        text_field_panel.add( zoom_y                         );
        text_field_panel.add( new Label("Fit method")        );
        text_field_panel.add( direction_fit_method           );
        text_field_panel.add( new Label("Fit method")        );
        text_field_panel.add( button_panel                   );
        text_field_panel.add( convolve_d                     );
        text_field_panel.add( convolve_dd_dt                 );
        

        add( stroke_list );
        add( polygon_fit_list );
        add( text_field_panel );
        
        file.add( quit_menu );
        menubar.add( file );
        setMenuBar( menubar );
        file.add( quit_menu );
        
        direction_fit_method.addItemListener( this ) ;
        polygon_fit_list.addActionListener( this );
        stroke_list.addActionListener( this );
        quit_menu.addActionListener( this );
        speed_average_scale.addActionListener( this );
        dd_dt_average_scale.addActionListener( this );
         circle_vs_speed_bias.addActionListener( this );
         circle_vs_general_path_bias.addActionListener( this );
        window_width.addActionListener( this );
        test_line_scale.addActionListener( this );
        direction_window_width.addActionListener( this );
        zoom_x.addActionListener( this );
        zoom_y.addActionListener( this );
        swodr_method.addActionListener( this );
        convolve_d.addActionListener( this );
        convolve_dd_dt.addActionListener( this );
        rotational_odr_method.addActionListener( this );
        simple_tangents_method.addActionListener( this );
        
        pack();
        validate();
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
        
        if ( key == 'q' ) {
            //System.exit( 0 );
            return;
        }
    }

     
    /**
    *
    * Handle key stroke events
    *
    **/
    public void
    itemStateChanged( ItemEvent e )
    {
        Object source = e.getSource();
        
        if ( source == direction_fit_method ) {
          LinearFit.Method m = LinearFit.Method.values()[direction_fit_method.getSelectedIndex()];
          ((Tablet)tablet).setFitMehod(m);
        }
    }

     
    /**
    *
    * Handle action events
    *
    **/
    public void
    actionPerformed( ActionEvent e ) 
    {
        String command = e.getActionCommand();
        Object source  = e.getSource();

        if ( source == simple_tangents_method ) {
            ((Tablet)tablet).setFitMehod(LinearFit.Method.SIMPLE_TANGENTS );
            return;
        }             
        
        if ( source == swodr_method ) {
            ((Tablet)tablet).setFitMehod(LinearFit.Method.SWODR );
            return;
        }             
        
        if ( source == rotational_odr_method ) {
            ((Tablet)tablet).setFitMehod(LinearFit.Method.ROTATION );
            return;
        }             
            
        if ( source == convolve_d ) {
            ((Tablet)tablet).convolveDirection();
            return;
        }             
            
        if ( source == convolve_dd_dt ) {
            ((Tablet)tablet).convolveChangeInDirection();
            return;
        }             
            
        if ( source == polygon_fit_list ) {
            tablet.paint( Tablet.debug_graphics );
            Blackboard.hybrid_fits[polygon_fit_list.getSelectedIndex()].paint(
                Tablet.debug_graphics );
            return;
        }

        if ( source == stroke_list ) {
            int index = stroke_list.getSelectedIndex();
            System.out.println( "Stroke " + index + " selected" );
            
            Vertex vertices[] = 
                (Vertex[])tablet.object_manager.stroke_vertices.elementAt( index );
             StrokeData       data                  = new StrokeData( vertices );
             SimpleClassifier classifier            = new SimpleClassifier( data );
            classifier.fit_method                   = TabletDataProcessor.fit_method;
            classifier.direction_window_width       = tablet.direction_window_width;
            SimpleClassifier.dd_dt_average_scale         = tablet.dd_dt_average_scale;
            SimpleClassifier.speed_average_scale         = tablet.speed_average_scale;
            classifier.test_line_scale             = tablet.test_line_scale;

            classifier.classify();

            Blackboard.paintGeneralPathInSegments( Blackboard.general_path );
            
            return;
        }

        if ( ( source == speed_average_scale         ) || 
             ( source == dd_dt_average_scale         ) ||
             ( source == circle_vs_speed_bias        ) ||
             ( source == circle_vs_general_path_bias ) ||
             ( source == window_width                ) ||
             ( source == test_line_scale             ) ||
             ( source == direction_window_width      ) ||
             ( source == zoom_x                      ) ||
             ( source == zoom_y                      ) ) {

            double angle = GeometryUtil.degree2radian( 
                    Double.valueOf( command ).doubleValue() );

            double value = Double.valueOf( command ).doubleValue();

            if ( source == speed_average_scale ) {
                tablet.speed_average_scale = value;
            }
            
            if ( source == dd_dt_average_scale ) {
                tablet.dd_dt_average_scale = value;
            }
            
            if ( source == test_line_scale ) {
                tablet.test_line_scale = value;
            }
            
            if ( source == circle_vs_speed_bias     ) {
                tablet.circle_vs_speed_bias = value;
            }
            
            if ( source == circle_vs_general_path_bias ) {
                tablet.circle_vs_general_path_bias = value;
            }
            
            if ( source == window_width ) {
                TabletDataProcessor.window_width = angle;
            }
             
            if ( source == direction_window_width ) {
                System.out.println( "direction_window_width = " + command );
                tablet.direction_window_width = (int)value;
            }
            
            if ( source == zoom_x ) {
                System.out.println( "zooming with factor = " + value );
                ((Tablet)tablet).zoomX( value );
            }

            if ( source == zoom_y ) {
                System.out.println( "zooming with factor = " + value );
                ((Tablet)tablet).zoomY( value );
            }
        }

        System.out.println( command );

        if ( command == "Quit" ) {
            System.exit( 0 );
        }
    }


    /**
    *
    * Add the hybrid fits to the list
    *
    **/
    public void
    setStrokeList( String description, int strokes )
    {
        stroke_list.removeAll();
        
        for ( int i=0; i<strokes; i++ ) {
            stroke_list.add( description + " " + i );
        }
    }


    /**
    *
    * Add the hybrid fits to the list
    *
    **/
    public void
    setHybridFitList( Fit fits[] )
    {
        polygon_fit_list.removeAll();
        for ( int i=0; i<fits.length; i++ ) {
            polygon_fit_list.add( fits[i] + "" );
        }
    }
}

/**
  *
  * $Log: ControlModule.java,v $
  * Revision 1.1  2006-11-22 22:54:35  hammond
  * mit's drg code, will extract and refactor to get ladder code
  *
  * Revision 1.10  2006/02/17 20:25:16  moltmans
  * Changed names of LinearFit.Method variables
  *
  * Revision 1.9  2006/02/17 20:10:00  moltmans
  * The LinearFit class has been updated to use an enum instead of the less reliable int flag.
  *
  * Revision 1.8  2005/03/29 02:32:58  hammond
  * got acute and obtuse auto gen mathematica working
  *
  * Revision 1.7  2005/01/27 22:15:11  hammond
  * organized imports
  *
  * Revision 1.6  2003/11/05 01:42:02  moltmans
  * Found more ^M's  They should all be gone now... Again...  For good?
  *
  * Revision 1.5  2003/03/06 01:08:51  moltmans
  * Added copyright to all the files.
  *
  * Revision 1.4  2001/12/06 02:44:57  mtsezgin
  * Added comments to the simple classifier to make fine tuning to
  * parameters easy (i.e., no trial error to find out what a certain
  * parameter does)..
  *
  * Revision 1.3  2001/11/23 03:22:56  mtsezgin
  * Major reorganization
  *
  * Revision 1.2  2001/10/12 22:25:52  mtsezgin
  * This is a commit of all files.
  * Shoapid
  * vi sux:q
  *
  * Revision 1.1.1.1  2001/03/29 16:25:00  moltmans
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
