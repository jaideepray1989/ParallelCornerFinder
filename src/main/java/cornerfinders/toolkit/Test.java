// (c) MIT 2003.  All rights reserved.

// $Id: Test.java,v 1.1 2006-11-22 22:54:35 hammond Exp $
// Test.java -- 
// Author: Michael Oltmans <moltmans@ai.mit.edu> 
// Created: <Fri Mar 23 11:17:36 2001> 
// Time-stamp: <2005-04-27 23:27:01 moltmans> 
package cornerfinders.toolkit;

import edu.mit.sketch.ui.SketchPanel;

/**
 * Test.java
 *
 *
 * Created: Fri Mar 23 11:17:41 2001
 *
 * @author <a href="mailto: "Michael Oltmans</a>
 * @version $Id: Test.java,v 1.1 2006-11-22 22:54:35 hammond Exp $
 */

public class Test extends SketchPanel{
  private StrokeData m_data;
  
  public Test (){
    addStrokeDataListener( new StrokeDataListener() {
    public void handleStroke( StrokeData sd ) 
    {
      m_data = sd;
    }
      } );
  }

  public StrokeData getData()
  {
    return m_data;
  }

  public SimpleClassifier getClassifier() 
  {
    return new SimpleClassifier(m_data);
  }

  public static void main(String[] args) {
    //    JMCommunicator.evalString("r = fact(6)");
    //    System.out.println(JMCommunicator.getScalar("r"));
    System.exit(0);
  }
  
  
}// Test
