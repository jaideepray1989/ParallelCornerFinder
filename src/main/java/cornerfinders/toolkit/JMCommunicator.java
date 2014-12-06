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
  * $Id: JMCommunicator.java,v 1.1 2006-11-22 22:54:35 hammond Exp $
  * $Name:  $
  * $Locker:  $
  * $Source: /cvsroot/tamudrg/code/src/edu/mit/sketch/toolkit/JMCommunicator.java,v $
  *
  **/

import edu.mit.sketch.geom.Point;

/**
  * ML communicator class
  *
  * If you want to run this on linux you need to make sure that you
  * set the environment variable:
  * LD_LIBRARY_PATH =
  *   /afs/csail/i386_linux24/matlab/latest/bin/glnx86:$HOME/drg/lib/linux
  * So it can find its libraries
  **/
public
class JMCommunicator
{
    /**
    *
    * The JMatLink engine
    *
    **/
    private static JMatLink engine = null;

    /**
    *
    * Initialize engine.
    *
    **/
    public static void
    initialize()
    {
      if ( engine == null ) {
        engine = new JMatLink();
        engine.engOpen();
        if (System.getProperty("os.name").equals("Linux")) {
          engine.engOpen("matlab");
          evalString("desktop");
        }
      }
    }


    /**
    *
    * delegate evalString
    *
    **/
    public static void
    evalString( String s )
    {
        System.out.println( "sending " + s );
        initialize();
        engine.engEvalString( s );
        System.out.println( "matlab: " + engine.engOutputBuffer() );
    }


    /**
    *
    * Get an array using the original JMatLink method
    *
    **/
    public static int[]
    getIntArrayOriginal( String s )
    {
        double data[][] = engine.engGetArray( s );
	
	if ( data.length == 0 ) {
	    return new int[0];
	}

        int int_data[] = new int[data[0].length];
        
        //System.out.println( "data.length "    + data.length );
        //System.out.println( "data[0].length " + data[0].length );

        for ( int i=0; i<int_data.length; i++ ) {
            //System.out.println( "data[0][i] = " + data[0][i] );
            int_data[i] = (int)(data[0][i]);
        }
        
        return int_data;
    }


    /**
    *
    * Get an array using the original JMatLink method
    *
    **/
    public static double[][]
    getArrayOriginal( String s )
    {
        return engine.engGetArray( s );
    }


    /**
    *
    * Put an array using the original JMatLink method
    *
    **/
    public static void
    putArrayOriginal( String s, double array[] )
    {
        engine.engPutArray( s, array );
    }


    /**
    *
    * Put an array using the original JMatLink method
    *
    **/
    public static void
    putArrayOriginal( String s, double array[][] )
    {
        engine.engPutArray( s, array );
    }


    /**
    *
    * Put an array using the original JMatLink method
    *
    **/
    public static void
    putIntArrayOriginal( String s, int array[] )
    {
        double data[] = new double[array.length];
        
        for ( int i=0; i<data.length; i++ ) {
            data[i] = array[i];
        }
        
        engine.engPutArray( s, data );
    }


    /**
    *
    * Put an array using the original JMatLink method
    *
    **/
    public static int[]
    getIntCellArray( String s )
    {
        setDebug( false );
        evalString("eval('tmp__array = " + s + "')");
        int result[] = getIntArrayOriginal( "tmp__array" );
        setDebug( true );
        return result;
    }


    /**
    *
    * Get an array using the original JMatLink method
    *
    **/
    public static void
    setDebug( boolean value )
    {
        engine.setDebug( value );
    }

    /**
    *
    * Get an array
    *
    **/
    public static double[][]
    getArray( String s )
    {
        String command = "";
        
        command = s + "_size = size( "  + s + " );";
        engine.engEvalString( command );

        command = s + "_rows = "  + s + "_size(1,1);";
        engine.engEvalString( command );

        command = s + "_cols = "  + s + "_size(1,2);";
        engine.engEvalString( command );

        command = s + "_rows";
        int rows = (int)(engine.engGetScalar( command ) );
        
        command = s + "_cols";
        int cols = (int)(engine.engGetScalar( command ) );
        
        double result[][] = new double[rows][cols];
        
        for ( int i=0; i<rows; i++ ) {
            for ( int j=0; j<cols; j++ ) {
                engine.engEvalString( "element = " + s + "(" + (i+1) +  "," + 
                                                               (j+1) + ");" );
                result[i][j] = engine.engGetScalar( "element" );
            }
        }
        
        return result;
    }


    /**
    *
    * Get an array
    *
    **/
    public static double[][][]
    get3DArray( String s )
    {
        String command = "";
        
        command = s + "_size = size( "  + s + " );";
        engine.engEvalString( command );

        command = s + "_i = "  + s + "_size(1);";
        engine.engEvalString( command );

        command = s + "_j = "  + s + "_size(2);";
        engine.engEvalString( command );

        command = s + "_k = "  + s + "_size(3);";
        engine.engEvalString( command );

        command = s + "_i";
        int i_end = (int)(engine.engGetScalar( command ) );
        command = s + "_j";
        int j_end = (int)(engine.engGetScalar( command ) );
        command = s + "_k";
        int k_end = (int)(engine.engGetScalar( command ) );
        
        
        double result[][][] = new double[i_end][j_end][k_end];
        setDebug( false );        
        for ( int i=0; i<i_end; i++ ) {
            for ( int j=0; j<j_end; j++ ) {
                for ( int k=0; k<k_end; k++ ) {
                    engine.engEvalString( "element = " + s + "(" + (i+1) +  "," + 
                                                                   (j+1) +  "," + 
                                                                   (k+1) + ");" );
                    result[i][j][k] = engine.engGetScalar( "element" );
                }
            }
        }
        setDebug( true );        
        
        return result;
    }


    /**
    *
    * Get an array
    *
    **/
    public static int[][][][]
    get3DCellWithIntArrayElements( String s )
    {
        String command = "";
        
        command = s + "_size = size( "  + s + " );";
        engine.engEvalString( command );

        command = s + "_i = "  + s + "_size(1);";
        engine.engEvalString( command );

        command = s + "_j = "  + s + "_size(2);";
        engine.engEvalString( command );

        command = s + "_k = "  + s + "_size(3);";
        engine.engEvalString( command );

        command = s + "_i";
        int i_end = (int)(engine.engGetScalar( command ) );
        command = s + "_j";
        int j_end = (int)(engine.engGetScalar( command ) );
        command = s + "_k";
        int k_end = (int)(engine.engGetScalar( command ) );
        
        
        int result[][][][] = new int[i_end][j_end][k_end][1];

        setDebug( false );
        for ( int i=0; i<i_end; i++ ) {
            for ( int j=0; j<j_end; j++ ) {
                for ( int k=0; k<k_end; k++ ) {
                    engine.engEvalString( "element = " + s + "{" + (i+1) +  "," + 
                                                                   (j+1) +  "," + 
                                                                   (k+1) + "};" );
                    result[i][j][k] = getIntArrayOriginal( "element" );
                }
            }
        }
        setDebug( true );
        
        return result;
    }


    /**
    *
    * Get the angle of the linear LSQE fit to the data
    *
    **/
    public static double
    getAngle( Point points[] )
    {
        initialize();
        String x = "x = [";
        String y = "y = [";
        for ( int i=0; i<points.length-1; i++ ) {
            x += points[i].x + ",";
            y += points[i].y + ",";
        }
        x += points[points.length-1].x + "]";
        y += points[points.length-1].y + "]";
        
        engine.engEvalString( x );
        engine.engEvalString( y );
        // System.out.println( x );
        // System.out.println( y); // ax+b
        engine.engEvalString( "p = polyfit( x, y, 1 )" );
        engine.engEvalString( "a = p( 1 )" );
        
        return Math.atan2( engine.engGetScalar("a"), 1 );
    } 


    /**
    *
    * Get the y intersection of the second order polynomial
    * that minimizes LSQE to the input points. (c in y=a*x*x+b*x+c)
    *
    **/
    public static double
    getYintersectionForLinearApproximation( Point points[] )
    {
        initialize();
        String x = "x = [";
        String y = "y = [";
        for ( int i=0; i<points.length-1; i++ ) {
            x += points[i].x + ",";
            y += points[i].y + ",";
        }
        x += points[points.length-1].x + "]";
        y += points[points.length-1].y + "]";
        
        engine.engEvalString( x );
        engine.engEvalString( y );
        // System.out.println( x );
        // System.out.println( y); // y=ax+b
        engine.engEvalString( "p = polyfit( x, y, 1 )" );
        engine.engEvalString( "b = p( 2 )" );
        
        return engine.engGetScalar("b");
    } 


    /**
    *
    * Get the y intersection of the second order polynomial
    * that minimizes LSQE to the input points. (c in y=a*x*x+b*x+c)
    *
    **/
    public static double
    getYintersectionForQuadraticApproximation( Point points[] )
    {
        initialize();
        String x = "x = [";
        String y = "y = [";
        for ( int i=0; i<points.length-1; i++ ) {
            x += points[i].x + ",";
            y += points[i].y + ",";
        }
        x += points[points.length-1].x + "]";
        y += points[points.length-1].y + "]";
        
        engine.engEvalString( x );
        engine.engEvalString( y );
        // System.out.println( x );
        // System.out.println( y);
        engine.engEvalString( "p = polyfit( x, y, 2 )" );
        engine.engEvalString( "c = p( 3 )" );
        
        return engine.engGetScalar("c");
    } 

  public static double getScalar(String s) 
  {
    initialize();
    return engine.engGetScalar(s);
  }
  
  


    /**
    *
    * Build MLArray
    *
    **/
    public static String
    buildMLArray( double x[] )
    {
        String result = "x = [";
        for ( int i=0; i<x.length-1; i++ ) {
            result += x[i] + ",";
        }
        result += x[x.length-1] + "]";
        
        return result;
    }
}

/**
  *
  * $Log: JMCommunicator.java,v $
  * Revision 1.1  2006-11-22 22:54:35  hammond
  * mit's drg code, will extract and refactor to get ladder code
  *
  * Revision 1.5  2005/04/01 21:35:03  moltmans
  * Updated db dumping (some time ago actually...).  Opening, viewing, etc...
  *
  * Revision 1.4  2004/04/05 18:38:29  moltmans
  * added a getScalar method.
  *
  * Revision 1.3  2003/05/07 20:58:54  mtsezgin
  *
  * Fixed some problems with the arc recognition in general, and recognition
  * on the acer tablet in particular.
  *
  * Revision 1.2  2003/03/06 01:08:52  moltmans
  * Added copyright to all the files.
  *
  * Revision 1.1  2002/07/22 21:59:32  mtsezgin
  * *** empty log message ***
  *
  *
  **/
