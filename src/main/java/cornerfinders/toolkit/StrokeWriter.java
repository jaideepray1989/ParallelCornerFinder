// (c) MIT 2003.  All rights reserved.

package cornerfinders.toolkit;

import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import edu.mit.sketch.geom.Point;
import edu.mit.util.FileTypeFilter;



/**
 * 
 * <p>
 * Created:   Thu May  2 20:18:18 2002<br>
 * Copyright: Copyright (C) 2002 by MIT.  All rights reserved.<br>
 * 
 * @author <a href="mailto:moltmans@ai.mit.edu">Michael Oltmans</a>
 * @version $Id: StrokeWriter.java,v 1.1 2006-11-22 22:54:35 hammond Exp $
 * 
 * To use this function, do one of the following
 *  1) call writeStrokesToFile
 *  2) call StrokeWriter constructor, then call writeStrokes
 *  3) call StrokeWriter constructor, then call writeStroke for each stroke
 * There is no close method. you need to close your print writer on your own
 * when you are finished.
 **/

public class StrokeWriter {
  public static final String V1_0 = "v1.0";
  public static final String V2_0 = "v2.0";

  protected String m_version;

  protected PrintWriter m_writer;
  
  public StrokeWriter( Writer writer, double timeConversion, String name )
  {
    m_version = V2_0;
    m_writer = new PrintWriter( new BufferedWriter( writer ) );
    writeHeader( m_version, timeConversion, name );
  }

  public StrokeWriter( Writer writer, double timeConversion,
                       String name, String version)
  {
    m_version = version;
    m_writer = new PrintWriter( new BufferedWriter( writer ) );
    writeHeader( m_version, timeConversion, name );
  }

  protected StrokeWriter()
  {
  }

  public String getVersion()
  {
    return m_version;
  }
  
  public static void writeStrokesToFile(File file, double timeConversion,
                                        String name, 
                                        Collection<StrokeData> strokes)
    throws IOException
  {
    Writer writer = new FileWriter(file);       
    StrokeWriter strokeWriter = new StrokeWriter(writer, timeConversion,
                                                 name);
    strokeWriter.writeStrokes(strokes);
  }

  public void writeStrokes( Collection<StrokeData> strokes )
  {
    for (StrokeData stroke : strokes) {
      writeStroke( stroke );
    }
    m_writer.flush();
  }

  /**
   * Write the file header:
   * <code>
   *   version=v1.0
   *   timeConversion=1.0
   *   name=Circles
   * </code>
   *
   * @param version the version of the file format
   * @param timeConversion a conversion from the given time units to
   *         milliseconds.  i.e. <code>milliseconds / unit</code>
   * @param name a name for this dataset
   **/
  protected void writeHeader( String version, double timeConversion,
			      String name )
  {
    m_writer.println( "version=" + version );
    m_writer.println( "timeConversion=" + timeConversion );
    m_writer.println( "name=" + name );
    m_writer.flush();
  }
  

  
  public void writeStroke( StrokeData stroke ) 
  {
    m_writer.print( "{ " );
    Point[] points = stroke.getOrigPoints();
    for( int i = 0; i < points.length; i++ ) {
      if( m_version.equals( V2_0 ) ) {
        m_writer.print( "(" );
      }
      
      m_writer.print( points[i].x + " " + points[i].y + " " +
                      points[i].getTimeStamp() );
      if( m_version.equals( V2_0 ) ) {
        m_writer.print( ")" );
      }
    }
    m_writer.println( " }" );
    m_writer.flush();
  }
  
  /**
   * Open a file chooser dialog box to prompt for a drs file and
   * save a set of strokes therein.
   **/
  public static boolean promptSaveStrokes(Component parentComponent, String dir,
                                          Collection<StrokeData> strokes) 
  {
    JFileChooser fc = new JFileChooser(dir);
    fc.addChoosableFileFilter(new FileTypeFilter("drs",
                                                 "drs - Stroke Data Files"));
    
    int returnVal = fc.showSaveDialog(parentComponent);
    
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File saveFile = fc.getSelectedFile();
 
      try {
        FileWriter fileWriter = new FileWriter(saveFile);
        StrokeWriter writer = new StrokeWriter(fileWriter, 1.0,
                                               saveFile.getName());
        writer.writeStrokes(strokes);
        fileWriter.close();
      }
      catch (IOException e) {
        System.err.println("I/O error writing subset to " + saveFile);
        e.printStackTrace();
        JOptionPane.showMessageDialog(parentComponent,
                                      "There was a problem writing the file: " + saveFile,
                                      "Error",
                                      JOptionPane.ERROR_MESSAGE);
        // Save failed
        return false;
      }
      // File successfully saved
      return true;
    }
    // User cancelled operation
    return false;
  }
  

  
}// StrokeWriter
