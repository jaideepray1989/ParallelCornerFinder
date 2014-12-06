// (c) MIT 2003.  All rights reserved.

package cornerfinders.toolkit;

import edu.mit.sketch.geom.Point;
import edu.mit.sketch.system.BBItemType;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import javax.swing.JFileChooser;
import java.awt.Component;
import edu.mit.util.FileTypeFilter;


/**
 * This class facilitates the loading of a series of
 * strokes.  It stores (x, y, time) triples in series of strokes.<br>
 * <code>
 *   version=v1.0
 *   timeConversion=1.0
 *   name=Circles
 *   {x1 y1 t1 x2 y2 t2 x3 y3 t3 ...}
 *   {x1 y1 t1 x2 y2 t2 x3 y3 t3 ...}
 *   ...
 * </code>
 * <p>
 * Created: Thu May 2 16:49:44 2002<br> Copyright: Copyright (C) 2002
 * by MIT.  All rights reserved.<br>
 * 
 * @author <a href="mailto:moltmans@ai.mit.edu">Michael Oltmans</a>
 * @version $Id: StrokeReader.java,v 1.1 2006-11-22 22:54:35 hammond Exp $
 **/

public class StrokeReader {
  private static Logger LOG = Logger.getLogger( StrokeReader.class );
  
  private StreamTokenizer m_tokenizer;
  private String m_version;
  private double m_timeConversion;
  private String m_name;
  private List<StrokeData> m_strokes;
  private List m_shapes;
  private List m_constraints;
  
  public StrokeReader( Reader reader ) throws IOException
  {
    org.apache.log4j.BasicConfigurator.configure();
    
    m_strokes = new ArrayList<StrokeData>();
    m_tokenizer = new StreamTokenizer( reader );
    m_tokenizer.slashSlashComments( true );
    m_tokenizer.slashStarComments( true );
    m_tokenizer.parseNumbers();

    parseHeader();
    m_tokenizer.eolIsSignificant( false );
    parseStrokes();
  }

  public List<StrokeData> getStrokes()
  {
    return m_strokes;
  }

  public List getConstraints()
  {
    if ( m_version.equals( "v2.1" ) ) {
      return getConstrFromName();
    }
    else {
      return new ArrayList();
    }
  }

  public List getShapes()
  {
    if ( m_version.equals( "v2.1" ) ) {
      return getShapesFromName();
    }
    else {
      return new ArrayList();
    }
  }

  /**
   * This is convenience method for loading strokes directly from a file.
   **/
  public static List<StrokeData> loadStrokes(File theFile) throws IOException 
  {
    StrokeReader strokeReader = null;
    FileReader fileReader = new FileReader(theFile);
    strokeReader = new StrokeReader(fileReader);
    fileReader.close();
    
    return strokeReader.getStrokes();
  }


  /**
   * Open a file chooser dialog box to prompt for a drs file and
   * return the list of strokes therein.
   **/
  public static List promptLoadStrokes(Component parentComponent)
    throws IOException
  {
    return promptLoadStrokes(parentComponent, null);
  }
  
  /**
   * Open a file chooser dialog box to prompt for a drs file and
   * return the list of strokes therein.
   **/
  public static List promptLoadStrokes(Component parentComponent, String dir) 
    throws IOException
  {
    JFileChooser fc = new JFileChooser(dir);
    fc.addChoosableFileFilter(new FileTypeFilter("drs",
                                                 "drs - Stroke Data Files"));
    int returnVal = fc.showOpenDialog(parentComponent);
    
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File theFile = fc.getSelectedFile();
      return loadStrokes(theFile);
    }
    return null;
  }

  protected List<BBItemType> getConstrFromName()
  {
    List<BBItemType> ret = new ArrayList<BBItemType>();
    String[] tokens = m_name.split("\\b");
    for ( int j = 0; j< tokens.length; j++ ) {
      tokens[j] = tokens[j].trim();
    }
    int i = 0;
    while ( (i < tokens.length) && !tokens[i].equals( ":" ) ) {
      LOG.debug( "constraint is " + tokens[i] );
      if ( !tokens[i].equals("") ) {
        BBItemType ctype = BBItemType.getType( tokens[i] );
        ret.add( ctype );
      }
      i++;
    }
    
    return ret;
  }

  protected List<BBItemType> getShapesFromName()
  {
    List<BBItemType> ret = new ArrayList<BBItemType>();
    String[] tokens = m_name.split("\\b");
    for ( int i = 0; i < tokens.length; i++ ) {
      tokens[i] = tokens[i].trim();
    }
    int i = 0;
    while ( !tokens[i].equals(":") ) {
      i++;
    }
    i++;
    while ( i < tokens.length ) {
      LOG.debug( "shape is " + tokens[i] );
      if ( !tokens[i].equals( "" ) ) {
        BBItemType stype = BBItemType.getType( tokens[i] );
        ret.add( stype );
      }
      i++;
    }

    return ret;
  }
  
  protected void parseHeader() throws IOException
  {
    confirmProperty( "version" );
    m_version = getWord();
    confirmProperty( "timeConversion" );
    m_timeConversion = getNumber();
    confirmProperty( "name" );
    m_name = getRestOfLine();
  }

  protected void parseStrokes() throws IOException
  {
    if ( m_version.equals( "v1.0" ) ) {
      while( m_tokenizer.nextToken() == '{' ) {
        m_strokes.add( parseDataV1() );
      }
    }
    else if ( m_version.equals( "v2.0" ) ) {
      while ( m_tokenizer.nextToken() == '{' ) {
        m_strokes.add( parseDataV2() );
      }
    }
    else if ( m_version.equals( "v2.1" ) ) {
      while ( m_tokenizer.nextToken() == '{' ) {
        m_strokes.add( parseDataV2() );
      }
    }
    else if ( m_version.equals( "v3.0" ) ) {
      while ( m_tokenizer.nextToken() == '{' ) {
        m_strokes.add( parseDataV3() );
      }
    }
    else {
      parserError( "Unknown version: " + m_version );
    }
  }

  protected StrokeData parseDataV1() throws IOException
  {
    ArrayList<Point> points = new ArrayList<Point>();
    int x, y;
    long t;
    while( m_tokenizer.nextToken() != '}' ) {
      m_tokenizer.pushBack();
      x = (int)getNumber();
      y = (int)getNumber();
      t = (long)getNumber();
      points.add( new Point( x, y, t ) );
    }
    Point[] pointArray = new Point[ points.size() ];
    return new StrokeData(points.toArray( pointArray ) );
  }

  protected StrokeData parseDataV2() throws IOException
  {
    ArrayList<Point> points = new ArrayList<Point>();
    int x, y;
    long t;
    while( m_tokenizer.nextToken() != '}' ) {
      m_tokenizer.pushBack();
      if ( m_tokenizer.nextToken() != '(' ) {
        parserError( "Expected to find (" );
      }
      x = (int)getNumber();
      y = (int)getNumber();
      t = (long)getNumber();
      if ( m_tokenizer.nextToken() != ')' ) {
        parserError( "Expected to find )" );
      }
      points.add( new Point( x, y, t ) );
    }
    Point[] pointArray = new Point[ points.size() ];
    return new StrokeData(points.toArray( pointArray ) );
  }
  protected StrokeData parseDataV3() throws IOException
  {
    ArrayList<Point> points = new ArrayList<Point>();
    int x, y, p;
    long t;
    while( m_tokenizer.nextToken() != '}' ) {
      m_tokenizer.pushBack();
      if ( m_tokenizer.nextToken() != '(' ) {
        parserError( "Expected to find (" );
      }
      x = (int)getNumber();
      y = (int)getNumber();
      t = (long)getNumber();
      p = (int)getNumber();
      if ( m_tokenizer.nextToken() != ')' ) {
        parserError( "Expected to find )" );
      }
      Point pt = new Point(x, y, t, p); 
      points.add( pt );
    }
    Point[] pointArray = new Point[ points.size() ];
    return new StrokeData(points.toArray( pointArray ) );
  }
  
  private void confirmProperty( String expectedValue )
    throws IOException
  {
    String s = getWord();
    if( ! expectedValue.equals( s ) ) {
      parserError( "Execpted to find: " + expectedValue + " but found: " + s );
    }

    int token = m_tokenizer.nextToken();
    if( token != '=' ) {
      parserError( "Expected equals sign after: " + expectedValue  );
    }
    
  }

  private String getRestOfLine() throws IOException
  {
    // Turn on EOL significance
    m_tokenizer.eolIsSignificant( true );
    
    // Turn off number parsing
    m_tokenizer.parseNumbers();
    int token;
    String ret = new String();
    try {
      token = m_tokenizer.nextToken();
    }
    catch( IOException e ) {
      parserError( "Trouble reading from file.", e );
      return null;
    }
    while ( token != StreamTokenizer.TT_EOL ) {
      if ( token == StreamTokenizer.TT_WORD ) {
	ret += " " + m_tokenizer.sval;
      }
      else {
	ret += " " + (char)token;
      }
      try {
	token = m_tokenizer.nextToken();
      }
      catch( IOException e ) {
	parserError( "Trouble reading from file.", e );
	return null;
      }
    }

    // Turn number parsing back on
    m_tokenizer.parseNumbers();
    return ret;    
  }
  
  private String getWord() throws IOException
  {
    int token;
    try {
      token = m_tokenizer.nextToken();
    }
    catch( IOException e ) {
      parserError( "Trouble reading from file.", e );
      return null;
    }
    
    if( token == StreamTokenizer.TT_WORD ) {
      return m_tokenizer.sval;
    }
    return null;
  }

  private double getNumber() throws IOException
  {
    int token;
    try {
      token = m_tokenizer.nextToken();
    }
    catch( IOException e ) {
      parserError( "Trouble reading from file.", e );
      return Double.NaN;
    }
    if( token == StreamTokenizer.TT_NUMBER ) {
      return m_tokenizer.nval;
    }

    LOG.debug( "(:  " + (token == '('));
    LOG.debug( "NUM:  " + (token == StreamTokenizer.TT_NUMBER));
    LOG.debug( "EOL:  " + (token == StreamTokenizer.TT_EOL));
    LOG.debug( "EOF:  " + (token == StreamTokenizer.TT_EOF));
    LOG.debug( "WORD: " + (token == StreamTokenizer.TT_WORD));
    
    parserError( "Expected a number.  Got a " + token);
    return Double.NaN;
  }
  
  private void parserError( String msg, IOException e ) throws IOException
  {
    LOG.error( msg + "(line: " + m_tokenizer.lineno() + ")", e );
    throw new IOException( msg + "(nested msg: " + e.getMessage() + ")" );
  }

  private void parserError( String msg ) throws IOException
  {
    LOG.error( msg + "(line: " + m_tokenizer.lineno() + ")" );
    throw new IOException( msg );
  }

  
}// StrokeFile


