package cornerfinders.recognizers;

import java.awt.geom.Point2D;

/**
 * CircularFit class - interface for various circular fits (arc, circle)
 * @author bpaulson
 */
public interface CircularFit {

	public double getRadius();
	public Point2D getCenter();
}
