// (c) MIT 2003.  All rights reserved.

// $Id: StrokeDataListener.java,v 1.1 2006-11-22 22:54:34 hammond Exp $
// StrokeDataListener.java -- 
// Author: Michael Oltmans <moltmans@ai.mit.edu> 
// Created: <Fri Mar 23 10:21:26 2001> 
// Time-stamp: <2001-12-06 13:34:50 moltmans> 
package cornerfinders.toolkit;

/**
 * StrokeDataListener.java
 *
 *
 * Created: Fri Mar 23 10:21:31 2001
 *
 * @author <a href="mailto: "Michael Oltmans</a>
 * @version $Id: StrokeDataListener.java,v 1.1 2006-11-22 22:54:34 hammond Exp $
 */

public interface StrokeDataListener {
  /**
   * This method is called by classes that collect StrokeData objects.
   **/
  public void handleStroke(StrokeData data);
  
}// StrokeDataListener
