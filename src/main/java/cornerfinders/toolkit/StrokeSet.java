package cornerfinders.toolkit;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import edu.mit.sketch.geom.Point;
import edu.mit.sketch.geom.Polygon;
import edu.mit.sketch.system.Logging;
import edu.mit.sketch.ui.Painter;

/**
 * Represent a set of strokes.  It has useful methods for finding (and
 * caching) the bounding box and start time of the collection.  More
 * functionality will likely be added but it should be very general
 * and should be limited to functionality that is only used by very
 * general tools.
 * 
 * <p>
 * Created:   Mon Nov 10 01:31:15 2003<br>
 * Copyright: Copyright (C) 2003 by MIT.  All rights reserved.<br>
 * 
 * @author <a href="mailto:moltmans@ai.mit.edu">Michael Oltmans</a>
 * @version $Id: StrokeSet.java,v 1.1 2006-11-22 22:54:35 hammond Exp $
 **/

public class StrokeSet implements Painter, Cloneable, Serializable {
  private static final long serialVersionUID = -8212082634069309108L;

  private static final Logger LOG = Logging.TOOLKIT;

  private static final double BBOX_EPSILON = 2e-10;

  private HashMap<String,Object> m_properties;
  
  /** The actual strokes **/
  private ArrayList<StrokeData> m_strokes;

  /** A cached bounding box **/
  private transient Rectangle2D m_boundingBoxCache;

  /** A cached bounding box that can have a rotation **/
  private transient edu.mit.sketch.geom.Rectangle m_rotatedBoundsCache;

  /** A cached start time. **/
  private transient Long m_startTimeCache;

  public StrokeSet() 
  {
    m_strokes = new ArrayList<StrokeData>();
    m_properties = new HashMap<String,Object>();
  }

  public StrokeSet( Collection<StrokeData> strokes ) 
  {
    this();
    for (StrokeData stroke : strokes) {
      m_strokes.add(stroke);
    }
  }

  /**
   * This is a convenience method that creates a stroke set for a
   * single stroke
   **/
  public StrokeSet( StrokeData stroke ) 
  {
    this();
    m_strokes.add(stroke);
  }

  /**
   * Set a property to the given value.  This can be used for things
   * like names, ids, etc...
   **/
  public void setProperty( String name, Object value) 
  {
    m_properties.put(name, value);
  }
  
  /**
   * Get a property's value.  This can be used for things
   * like names, ids, etc...
   **/
  public Object getProperty(String name) 
  {
    return m_properties.get(name);
  }

  /**
   * Remove the mapping for the given property.  It returns the value
   * of the removed key.  (note: null may mean no such property or
   * that the properties value was null)
   **/
  public Object removeProperty(String name) 
  {
    return m_properties.remove(name);
  }
  

  public ArrayList<StrokeData> getStrokes() 
  {
    return m_strokes;
  }
  
  /**
   * Scale all of the points in all of the strokes to get a scaled copy of this set.
   * @param scale
   * @return A new StrokeSet with new Points scaled appropriately.
   */
  public StrokeSet getScaledCopy(double scale) {
    return getScaledCopy(scale, scale);
  }

  /**
   * Scale all of the points in all of the strokes to get a scaled copy of this set.
   * @param xScale scale x coords by this factor
   * @param yScale scale y coords by this factor
   * @return A new StrokeSet with new Points scaled appropriately.
   */
  public StrokeSet getScaledCopy(double xScale, double yScale) {
    StrokeSet set = new StrokeSet();
    for (StrokeData stroke : m_strokes) {
      ArrayList<Point> points = new ArrayList<Point>();
      for (Point pt : stroke.getOrigPoints()) {
        Point newPt = (Point)pt.copy();
        newPt.setLocation(pt.getX()*xScale, pt.getY()*yScale);
        points.add(newPt);     
      }
      set.addStroke(new StrokeData(points.toArray(new Point[0])));
    }
    return set;
  }

  /**
   * Make a new stroke set that is rescaled so that the aspect ratio of the
   * bounding box, of arbitrary orientation, remains the same and the longest
   * axis of the new bounding box has the specified length.
   * 
   * @return A scaled copy ot the stroke set
   */
  public StrokeSet getCopySizeNormalizedOnMajorAxis(double majorAxis) {
    edu.mit.sketch.geom.Rectangle bounds = getRotatedBounds();
    double max = bounds.getMajorAxisLength();
    return getScaledCopy(majorAxis / max);
  }
  
  public void paint(Graphics g)
  {
    //    Rectangle clip = g.getClipRect();
    // if there is no clipping rectangle define then just paint
    // everything
    //    if( true || clip == null ) {
    for (StrokeData stroke : m_strokes) {    
      stroke.paint(g);
    }
      //    }

//     // otherwise only paint strokes inside the bounding box of the
//     // clip region
//     else {
//       while( it.hasNext() ) {
//         StrokeData stroke = (StrokeData)it.next();
//         Polygon points = stroke.getDataPoints();
//         if( points.getBounds().intersects(clip) ) {
//           g.drawPolyline(points.xpoints, points.ypoints, points.npoints);
//         }
//       }
//     }
  }

  public boolean removeStroke(StrokeData stroke) 
  {
    if( m_strokes.remove(stroke) ) {
      m_boundingBoxCache = null;
      m_startTimeCache = null;
      return true;
    }
    return false;    
  }

  public boolean replaceStroke(StrokeData stroke, 
                               List<StrokeData> newStrokes)
  {
    int pos = m_strokes.indexOf( stroke );
    if ( pos < 0 ) {
      return false;
    }
    m_strokes.remove( stroke );
    m_strokes.addAll( pos, newStrokes );
    return true;
  }
  
  /**
   * Get strokes and substrokes that are contained in the region
   * 
   * @param region
   * @return The set of strokes and substroke inside the given region
   */
  public StrokeSet getStrokesIn(Shape region) {
    StrokeSet allSubs = new StrokeSet();
    for (StrokeData stroke : m_strokes) {
      for (StrokeData sub : subStrokes(stroke, region)) {
        allSubs.addStroke(sub);
      }
    }
    return allSubs;
  }
  
  /**
   * Helper for getting strokes in a given region.
   * 
   * @param stroke
   * @param region
   * @return The stroke or substroke(s) contained inside the region
   */
  private static ArrayList<StrokeData> subStrokes(StrokeData stroke, Shape region) { 
    ArrayList<StrokeData> subs = new ArrayList<StrokeData>();
    ArrayList<Point> points = new ArrayList<Point>();
    for (Point point : stroke.getOrigPoints()) {
      // if the point is in the region, keep it
      if (region.contains(point)) {
        points.add(point);
      }
      // if it is not inside and previous ones were, then make them into a stroke
      else {
        if (!points.isEmpty()) {
          subs.add(new StrokeData(points.toArray(new Point[points.size()])));
          points = new ArrayList<Point>();
        }
      }
    }
    // Add the last chunk of points if there are any
    if (!points.isEmpty()) {
      subs.add(new StrokeData(points.toArray(new Point[points.size()])));
    }

    return subs;
  }

  
  public void clearStrokes()
  {
    m_strokes.clear();
    m_boundingBoxCache = null;
    m_startTimeCache = null;
  }

  public void addStroke(StrokeData stroke)
  {
    m_strokes.add(stroke);
    m_boundingBoxCache = null;
    m_startTimeCache = null;
  }
  
  public void addStrokes(StrokeSet strokes)
  {
    m_strokes.addAll(strokes.m_strokes);
    m_boundingBoxCache = null;
    m_startTimeCache = null;
  }

  /**
   * Get the union of the bounding boxes of each stroke.  If there are
   * no strokes in the set then just return an empty rectangle.
   **/
  public Rectangle getBounds() 
  {
    Rectangle2D bounds = getBounds2D();
    return new Rectangle((int)bounds.getX(),
                         (int)bounds.getY(),
                         (int)bounds.getWidth(),
                         (int)bounds.getHeight());
  }

  public Rectangle2D getBounds2D() {
    if (m_boundingBoxCache != null) {
      return (Rectangle2D) m_boundingBoxCache.clone();
    }
    Rectangle2D bounds;
    if( !m_strokes.isEmpty() ) {
      bounds = m_strokes.get(0).getOriginalBounds();
      for (int i = 1; i < m_strokes.size(); i++) {
        StrokeData stroke = m_strokes.get(i);
        Rectangle2D.union(stroke.getOriginalBounds(), bounds, bounds);
      }
    }
    else {
      bounds = new Rectangle2D.Double();
    }

    // This nasty mess is here so that when we do comparisons, the points used
    // to calculate the bounds are actually considered in the bounding box. This
    // is a result of how AWT defines insideness
    m_boundingBoxCache = new Rectangle2D.Double(bounds.getX()-BBOX_EPSILON,
                                                bounds.getY()-BBOX_EPSILON,
                                                bounds.getWidth()+2.0*BBOX_EPSILON,
                                                bounds.getHeight()+2.0*BBOX_EPSILON);
    return m_boundingBoxCache;
  }
  
  /**
   * Get the bounding rectangle at any angle as specified by
   * {@link Polygon#getRectangularBounds()}.
   * 
   * @return The rotated rectangle bounding box
   */
  public edu.mit.sketch.geom.Rectangle getRotatedBounds() {
    if( m_rotatedBoundsCache != null ) {
      return m_rotatedBoundsCache;
    }

    edu.mit.sketch.geom.Rectangle bounds;
    ArrayList<Point> points = new ArrayList<Point>();

    for (StrokeData stroke : m_strokes) {
      points.addAll(Arrays.asList(stroke.getOrigPoints()));
    }
    bounds = new Polygon(points.toArray(new Point[0])).getRectangularBounds();
    
    m_rotatedBoundsCache = bounds;
    return m_rotatedBoundsCache;
  }

  public StrokeData getStrokeAt(int index) 
  {
    return m_strokes.get(index);
  }

  public long getStartTime() 
  {
    if( m_startTimeCache != null ) {
      return m_startTimeCache.longValue();
    }
    
    long time = Long.MAX_VALUE;

    for (StrokeData stroke : m_strokes) {
      time = Math.min(time, stroke.getStartTime());
    }
    m_startTimeCache = new Long(time);
    return time;
  }

  public int getNumStrokes() 
  {
    return m_strokes.size();
  }
  
  public StrokeSet getTranslated(double dx, double dy) {
    StrokeSet s = new StrokeSet();
    for (StrokeData stroke : m_strokes) {
      s.addStroke(stroke.getTranslated(dx, dy));
    }
    return s;
  }
  
  public StrokeSet copy() 
  {
    return (StrokeSet)clone();
  }

  @Override
  public Object clone() 
  {
    StrokeSet ss = null;
    try {
      ss = (StrokeSet)super.clone();
    }
    catch (CloneNotSupportedException e) {
      LOG.error("Clone not supported in StrokeSet?!?!", e);
      return null;
    }
    
    ss.m_strokes = new ArrayList<StrokeData>(m_strokes);
    ss.m_properties = new HashMap<String,Object>(m_properties);
    ss.m_boundingBoxCache = null;
    ss.m_startTimeCache = null;
    return ss;
  }

  @Override
  public String toString() {
    return "StrokeSet: " + m_strokes.toString();
  }
  
}
