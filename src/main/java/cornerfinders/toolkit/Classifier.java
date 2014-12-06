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
// Cleaned -Metin

package cornerfinders.toolkit;

import edu.mit.sketch.geom.Ellipse;
import edu.mit.sketch.geom.GeneralPath;
import edu.mit.sketch.geom.Line;
import edu.mit.sketch.geom.Polygon;

/** 
 * This is the interface for a basic recognition toolkit.  The
 * Classifier will deal with single stroke input only.  There is a
 * Classifier object associated with each stroke.
 *
 * In conjunction with this class is the StrokeData class.  The users
 * must creare a StrokeData object and pass it to this class.
 *
 */
public abstract class Classifier
{
    public static final int POINT = -1;
    /**
    * Constant for POLYGON
    **/
    public static final int POLYGON = 0;

    /**
    * Constant for ELLIPSE
    **/
    public static final int ELLIPSE = 1;

    /**
    * Constant for COMPLEX
    **/
    public static final int COMPLEX = 2;

    /**
    * Constant for line
    **/
    public static final int LINE    = 3;

    /**
    * Constant for arc
    **/
    public static final int ARC    = 4;

    /**
    * Constant for spiral
    **/
    public static final int SPIRAL    = 5;
    /**
    * The stroke data
    **/
    protected StrokeData stroke_data;

    /**
    * The error calculator
    **/
    protected ErrorCalculator error_calculator;

    /**
    *
    * Returns the best fit type for the strokeData 
    *
    **/
    public abstract int classify();

    /**
    *
    * Returns the set of types that fit the stroke with error value less
    * than the bound specified.
    *
    **/
    public abstract int[] classify( double error_bound );

  
    /**
    *
    * Returns true if the best fit for strokeData is a polygon.
    *
    **/
    public abstract boolean isPolygon();


    /**
    *
    * Returns true if the best fit polygon has an error of less than
    * bound.  The error is calculated with the m_ErrorCalculator class,
    * which by default uses squared error (unless another error calculator
    * was passed in when the classifier was created.
    *
    **/
    public abstract boolean isPolygon( double error_bound );
  
    /**
    *
    * Returns true if the best fit for the strokeData is an Ellipse.
    *
    **/
    public abstract boolean isEllipse();

    /**
    *
    * Returns true if the best fit ellipse for the strokeData is less than
    * error_bound.
    *
    **/
    public abstract boolean isEllipse( double error_bound );
  
  
    /**
    *
    * Returns true if the best fit for the strokeData is a complex shape.
    *
    **/
    public abstract boolean isComplex();

    /**
    *
    * Returns true if the best fit complex shape has error measure less than
    * error_bound.
    *
    **/
    public abstract boolean isComplex( double error_bound );


    /**
    *
    * Returns true if the best fit shape for the strokeData is a Line.
    *
    **/
    public abstract boolean isLine();

    /**
    *
    * Returns true if the best fit line has an error of less than error_bound.
    *
    **/
    public abstract boolean isLine( double error_bound );

  

    /**
    *
    * Returns the error between the best fit polygon and the strokeData.
    * The error is calculated according to the ErrorCalculator, which uses
    * squared error if not set by the user.
    *
    **/
    public abstract double getPolygonError();

    /**
    *
    * Returns the error between the best fit ellipse and the strokeData.
    * The error is calculated according to the ErrorCalculator, which uses
    * squared error if not set by the user.
    *
    **/
    public abstract double getEllipseError();

    /**
    *
    * Returns the error between the best fit GeneralPath and the strokeData.
    * The error is calculated according to the ErrorCalculator, which uses
    * squared error if not set by the user.
    *
    **/
    public abstract double getComplexError();

    /**
    *
    * Returns the error between the best fit line and the strokeData.
    * The error is calculated according to the ErrorCalculator, which uses
    * squared error if not set by the user.
    *
    **/
    public abstract double getLineError();


    /**
    *
    * Returns the best fit Polygon for the StrokeData.  I have used the
    * class OurPolygon here because we will be using a standard library of
    * shapes which may or may not be the same as the Java defined shapes.
    * (That has yet to be determined.)
    *
    **/
    public abstract Polygon getPolygonApproximation();

    /**
    *
    * Returns the best fit Polygon for the StrokeData.  I have used the
    * class OurPolygon here because we will be using a standard library of
    * shapes which may or may not be the same as the Java defined shapes.
    * (That has yet to be determined.)
    *
    **/
    public abstract Ellipse getEllipseApproximation();

    /**
    *
    * Returns the best fit Polygon for the StrokeData.  I have used the
    * class OurPolygon here because we will be using a standard library of
    * shapes which may or may not be the same as the Java defined shapes.
    * (That has yet to be determined.)
    *
    **/
    public abstract GeneralPath getComplexApproximation();

    /**
    *
    * Returns the best fit Polygon for the StrokeData.  I have used the
    * class OurPolygon here because we will be using a standard library of
    * shapes which may or may not be the same as the Java defined shapes.
    * (That has yet to be determined.)
    *
    **/
    public abstract Line getLineApproximation();
}
