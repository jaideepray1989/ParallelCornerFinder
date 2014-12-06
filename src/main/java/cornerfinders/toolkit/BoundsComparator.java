package cornerfinders.toolkit;

import java.awt.Dimension;
import java.util.Comparator;



/**
 * BoundsComparator.java
 *
 *
 * Created: Fri Dec 19 13:31:52 2003
 *
 * @author <a href="mailto:moltmans@ai.mit.edu">Michael Oltmans</a>
 * @version 1.0
 */
public class BoundsComparator implements Comparator {
  public static final int AREA = 1;
  public static final int WIDTH = 2;
  public static final int HEIGHT = 3;
  public static final int MAX_DIMENSION = 4;

  private int m_dimension;

  public BoundsComparator(int dimension) {
    m_dimension = dimension;
  }

  /**
   * Describe <code>equals</code> method here.
   *
   * @param object an <code>Object</code> value
   * @return a <code>boolean</code> value
   */
  public boolean equals(Object object) {
    return  (object instanceof BoundsComparator) &&
      ((BoundsComparator)object).m_dimension == m_dimension;
  }

  /**
   * Describe <code>compare</code> method here.
   *
   * @param o1 an <code>Object</code> value
   * @param o2 an <code>Object</code> value
   * @return an <code>int</code> value
   */
  public int compare(Object o1, Object o2) {
    return compare((StrokeSet)o1, (StrokeSet)o2);
  }

  public int compare(StrokeSet ss1, StrokeSet ss2) 
  {
    Dimension d1 = ss1.getBounds().getSize();
    Dimension d2 = ss2.getBounds().getSize();    
    
    switch (m_dimension) {
    case AREA:
      return d1.width*d1.height - d2.width*d2.height;
      
    case WIDTH:
      return d1.width - d2.width;
      
    case HEIGHT:
      return d1.height - d2.height;

    case MAX_DIMENSION:
      return Math.max(d1.width, d1.height) - Math.max(d2.width, d2.height);

    default:
      throw new Error("Bad sort dimension for BoundsComparator");
    }
  }
  
  
}
