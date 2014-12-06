// (c) MIT 2003.  All rights reserved.

package cornerfinders.toolkit;

import edu.mit.sketch.geom.Vertex;
import edu.mit.sketch.system.data.StrokeSet;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;


/**
 * 
 * <p>
 * Created:   Thu May  2 20:18:18 2002<br>
 * Copyright: Copyright (C) 2002 by MIT.  All rights reserved.<br>
 * 
 * @author <a href="mailto:moltmans@ai.mit.edu">Michael Oltmans</a>
 * @version $Id: StrokeSetWriter.java,v 1.1 2006-11-22 22:54:36 hammond Exp $
 **/

public class StrokeSetWriter extends StrokeWriter {
  
  private StrokeSet m_strokeSet;
  
  public StrokeSetWriter( Writer writer, double timeConversion, StrokeSet ss )
  {
    m_strokeSet = ss;
    m_version = "v2.1";
    m_writer = new PrintWriter( new BufferedWriter( writer ) );
    String name = makeName( ss );
    writeHeader( m_version, timeConversion, name );
  }

  protected String makeName( StrokeSet ss )
  {
    String ret = new String();
    Iterator it = ss.getConstraints().iterator();
    while( it.hasNext() ) {
      ret += it.next().toString() + " ";
    }
    ret += ":";
    it = ss.getShapes().iterator();
    while ( it.hasNext() ) {
      ret += " " + it.next().toString();
    }
    return ret;
  }

  public void writeStrokes()
  {
    writeStrokes( m_strokeSet.getStrokes() );
  }

  public void writeStroke( StrokeData stroke )
  {
    m_writer.print( "{ " );
    Vertex[] points = stroke.getVertices();
    for( int i = 0; i < points.length; i++ ) {
      m_writer.print( "( " + points[i].x + " " + points[i].y + " " +
              points[i].getTimeStamp() + " ) ");
    }
    m_writer.println( " }" );
  }
  
  
}// StrokeSetWriter
