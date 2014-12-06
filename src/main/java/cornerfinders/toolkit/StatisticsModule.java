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

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
  *
  * This class is supposed to show basic statistical data about
  * the recognized objects.
  *
  **/
public
class      StatisticsModule
extends    Frame
implements ActionListener, 
           KeyListener,
           MouseMotionListener, 
           MouseListener
{    
    public Menu     file;
    public MenuBar  menubar;
    public MenuItem quit_menu;

    
    
    /**
    *
    * The constructor.
    *
    **/
    public StatisticsModule( String title )
    {
        super( title );

        menubar        = new MenuBar();
        file           = new Menu( "File" );
        quit_menu      = new MenuItem( "Quit" );
        
        file.add( quit_menu );
        menubar.add( file );
        setMenuBar( menubar );
        file.add( quit_menu );
        quit_menu.addActionListener( this );
    }

        
    /**
    *
    * Overloaded for double buffering.
    *
    **/
    public void
    paint( Graphics g )
    {
        update( g );
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
        
        if ( key == 'c' ) {
            repaint();
            return;
        }
                
        if ( key == 'q' ) {
            //System.exit( 0 );
            return;
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
    }
    
    /**
    *
    * Handle mouse events
    *
    **/
    public void
    mouseClicked( MouseEvent e ) 
    {
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
    }

    /**
    *
    * Handle mouse events
    *
    **/
    public void
    mouseReleased( MouseEvent e ) 
    {
    }
    
    /**
    *
    * Handle mouse motion events
    *
    **/
    public void
    mouseDragged( MouseEvent e ) 
    {
    }

    /**
    *
    * Clear screen
    *
    **/
    public void
    clearScreen()
    {
        Graphics g = getGraphics();

        if ( g == null )
            return;        

        g.setColor( Color.white );
        g.fillRect( 0, 0, 1000, 1000 );
    }
    
    /**
    *
    * Handle mouse motion events
    *
    **/
    public void
    mouseMoved( MouseEvent e ) 
    {
    }
    
    
    /**
    *
    * Handle double buffering and do the real drawing.
    *
    **/
    public void 
    update( Graphics g )
    {
        
    }


    /**
    *
    * Plot bar graph
    *
    **/
    public void 
    plotBarGraph( double data[], double min, double max, double dx )
    {
        int   max_bar_height = Math.max( getSize().height - 100, 50 );
        Point origin         = new Point( 50, getSize().height - 50 );

        int max_height     = 0;
        int heights[]      = new int[(int)((max-min)/dx+1)];
        int dx_pixel_width = (int)((getSize().width - origin.x)/
                                  ((max-min)/dx+1));
        
        for ( int i=0; i<data.length; i++ ) {
            heights[(int)((max-data[i])/dx)]++;            
        }

        for ( int i=0; i<heights.length; i++ ) {
            if ( max_height < heights[i] ) {
                max_height = heights[i];
            }
        }
        
        clearScreen();
        Graphics g = getGraphics();

        if ( g == null )
            return;
        

        g.setColor( Color.blue );
        for ( int i=0; i<heights.length; i++ ) {
            g.fillRect( (int)(origin.x + i*dx_pixel_width), 
                (int)(origin.y - max_bar_height*heights[i]/max_height),
                    (int)(dx_pixel_width),
                        (int)(max_bar_height*heights[i]/max_height ));
        }
    }


    /**
    *
    * Plot sliding window
    *
    **/
    public void 
    plotSlidingWindowGraph( double data[], 
                            double min, 
                            double max, 
                            double dx,
                            double window_width )
    {
        int max_height  = 0;
        int histogram[] = getSlidingWindowHistogram( data,
                                                     min,
                                                     max,
                                                     dx,
                                                     window_width );
        for ( int i=0; i<histogram.length; i++ ) {
            if ( max_height < histogram[i] )
                max_height = histogram[i];
        }
        
        int   max_bar_height = Math.max( getSize().height - 200, 50 );
        Point origin         = new Point( 50, getSize().height - 50 );
        int dx_pixel_width   = (int)( ((double)(getSize().width - origin.x))/
                                      ((double)(histogram.length+1)) );

        clearScreen();
        Graphics g = getGraphics();

        if ( g == null )
            return;
        
        g.setColor( Color.blue.darker().darker() );
        for ( int i=0; i<histogram.length-1; i++ ) {
            g.drawLine( (int)(origin.x + i*dx_pixel_width), 
                (int)(origin.y - max_bar_height*histogram[i]/max_height),
                    (int)(origin.x + (i+1)*dx_pixel_width), 
                        (int)(origin.y - max_bar_height*histogram[i+1]/max_height ));
        }
    }


    /**
    *
    * Plot sliding window
    *
    **/
    public static int[]
    getSlidingWindowHistogram( double data[], 
                               double min, 
                               double max, 
                               double dx,
                               double window_width )
    {
        int max_height  = 0;
        int data_points = 0;
        int histogram[] = new int[(int)((max-min)/dx)];
        
        for ( int i=0; i<histogram.length; i++ ) {
            for ( int j=0; j<data.length; j++ ) {
                if ( ( data[j] > min+dx*i-window_width ) && 
                     ( data[j] < min+dx*i+window_width ) ) {
                    data_points++;
                }
            }
            histogram[i] = data_points;
            if ( max_height < data_points )
                max_height = data_points;
            data_points = 0;
        }
        
        return histogram;
    }
}