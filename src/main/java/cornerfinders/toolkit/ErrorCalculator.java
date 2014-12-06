// (c) MIT 2003.  All rights reserved.

package cornerfinders.toolkit;

import edu.mit.sketch.geom.Ellipse;
import edu.mit.sketch.geom.GeneralPath;
import edu.mit.sketch.geom.Line;
import edu.mit.sketch.geom.Polygon;

/**
 * The interface ErrorCalculator.  Allows the user to define
 * customized error measures
 */

public interface ErrorCalculator
{
    /**
    *
    * Get the error between the stroke_data and the shape.
    *
    **/
    public double getPolygonError(StrokeData stroke_data, Polygon poly);

    /**
    *
    * Get the error between the stroke_data and the shape.
    *
    **/
    public double getLineError(StrokeData stroke_data, Line line);

    /**
    *
    * Get the error between the stroke_data and the shape.
    *
    **/
    public double getEllipseError(StrokeData stroke_data, Ellipse ellipse);

    /**
    *
    * Get the error between the stroke_data and the shape.
    *
    **/
    public double getGeneralPathError(StrokeData stroke_data,
                                      GeneralPath general_path);
}
