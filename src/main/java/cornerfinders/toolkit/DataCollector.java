// (c) MIT 2003.  All rights reserved.

package cornerfinders.toolkit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Collect strokes as they are produced and store them.  To do this
 * simply add the <code>DataCollector</code> as a {@link
 * StrokeDataListener} to a source of stroke data events.  The strokes
 * can then be serialized into a file.  That file can then be laoded
 * by a {@link DataSimulator} to send the strokes to other
 * {@link StrokeDataListener}s.<p>
 *
 * Created: Wed Sep  5 17:38:43 2001<br>
 * Copyright: Copyright (C) 2001 by MIT.  All Rights Reserved.<br>
 *
 * @author <a href="mailto:moltmans@ai.mit.edu">Michael Oltmans</a>
 * @version $Id: DataCollector.java,v 1.1 2006-11-22 22:54:35 hammond Exp $
 **/
public class DataCollector implements StrokeDataListener {
  private static Logger m_logger = Logger.getLogger( DataCollector.class );
  
  private ArrayList m_data;

  /**
   * After creation be sure to add this object as a {@link StrokeDataListener}.
   **/
  public DataCollector ()
  {
    m_data = new ArrayList();
  }

  
  public void handleStroke( StrokeData stroke ) 
  {
    m_data.add( stroke );
  }

  /**
   * Serialize the strokes into a file specified by <code>file</code>.
   **/
  public void saveData( File file ) 
  {
    FileWriter writer = null;
    try {
      writer = new FileWriter( file );
      StrokeWriter strokeWriter = new StrokeWriter( writer, 1.0,
                            file.getName() );
      strokeWriter.writeStrokes( m_data );
      writer.flush();
    }
    catch( IOException e ) {
      m_logger.error( "Couldn't open file to write stroke data: " +
              file.getName() );
      return;
    }
    finally {
      try {
    writer.close();
      } catch( IOException e ) {
    m_logger.error( "Trouble closing the file: " + file.getName() );
      }
      
    }
  }

  /**
   * Delete all the data we have stored up til now.
   **/
  public void reset() 
  {
    m_data.clear();
  }
  
  /**
   * This is a convenience method that loads the strokes out of the
   * given file and returns them as a <code>List</code>.
   **/
  public static List loadData( URL url )
  {
    InputStream inputStream = null;
    StrokeReader strokeReader = null;
    try {
      inputStream = url.openStream();
      strokeReader = new StrokeReader( new InputStreamReader( inputStream ) );
      return strokeReader.getStrokes();
    }
    catch( IOException e ) {
      m_logger.error( "Couldn't open file to read stroke data: " + url );
      return null;
    }
    finally {
      try {
    inputStream.close();
      } catch( IOException e ) {
    m_logger.error( "Trouble closing the file: " + url );
      }
      
    }
  }
  
  
}// DataCollector
