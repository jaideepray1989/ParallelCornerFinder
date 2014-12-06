package cornerfinders.toolkit;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

/**
 * Keeps track of the Sketchers for a history by keeping 
 * the SketcherInfo objects organized. Also performs functions
 * like assignment of Colors to Sketchers.
 * 
 * @author cadlerun
 *
 */
public class SketcherIndex {
  private HashMap<String, SketcherInfo> m_idMap; // maps ids to SketcherInfo

  // color picking variables
  private Hashtable<String, Color> m_colorMap = new Hashtable<String, Color>(); // keep track of which color which author has been assigned
  private int m_colorNum = 0; 
  private Color m_colors[] = {Color.RED, Color.BLUE, Color.GREEN, Color.PINK, Color.ORANGE}; // colors to use
  
  public SketcherIndex() {
    m_idMap = new HashMap<String, SketcherInfo>();
  }
  
  /**
   * Get the SketcherInfo object that has the specified id
   * @param id the id to look up the SketcherInfo object with 
   * @return the SketcherInfo object with the specified id or null if it doesn't exist
   */
  public SketcherInfo getSketcherInfo(String id) {
    return m_idMap.get(id);
  }
  
  /**
   * Add a SketcherInfo to the SketcherIndex
   * @param sketcherInfo The SketcherInfo to add.
   */
  public void add(SketcherInfo sketcherInfo) {
    m_idMap.put(sketcherInfo.getId(), sketcherInfo);
  }
  
  /**
   * Remove a SketcherInfo from the SketcherIndex
   * @param sketcherInfo The SketcherInfo to remove.
   */
  public void remove(SketcherInfo sketcherInfo) {
    m_idMap.remove(sketcherInfo.getId());
  }
  
  /**
   * Get the nick name for the Sketcher with the given id
   * @param id the id of the Sketcher 
   * @return the nick name of the Sketcher if the Sketcher is in the index and has a nick name, null otherwise
   */
  public String getSketcherNickname(String id) {
    SketcherInfo info = getSketcherInfo(id);
    if (info != null)
      return info.getNickName();
    
    return null;
  }
  
  /**
   * Set the nickname of the SketcherInfo with the given id. If no SketcherInfo
   * with the specified id is in the SketcherIndex, no change is made.
   * @param id the id to set the nick name for
   * @param nickname the nick name
   */
  public void setSketcherNickname(String id, String nickname) {
    SketcherInfo info = m_idMap.get(id);
    if (info != null) 
      info.setNickName(nickname);
  }
  
  /**
   * Get a Collection of all SketcherInfo objects in the SketcherIndex
   * @return a Collection of all the SketcherInfo objects in the SketcherIndex
   */
  public Collection<SketcherInfo> getSketchers() {
    return m_idMap.values(); 
  }
  
  /**
   * Get all the sketcher ids (names) in the SketcherIndex
   * @return the sketcher ids (names) in the SketcherIndex
   */
  public Set<String> getIds() {
    return m_idMap.keySet();
  }
  
  /**
   * Returns the number of Sketchers in this SketcherIndex
   * @return the number of Sketchers in this SketcherIndex
   */
  public int size() {
    return m_idMap.size();
  }
  
  /**
   * Returns true if this SketcherIndex has no Sketchers in it
   * @return returns true if this SketcherIndex has no Sketchers in it, false otherwise
   */
  public boolean isEmpty() {
    return m_idMap.isEmpty();
  }
  
  /**
   * Determine which color to use for the given sketcher id.
   * Always returns the same color for the same author.
   * Does not check to make sure id is in SketcherIndex
   * @param id the sketcher id to get a Color for
   * @return the Color assigned to the specified sketcher id
   */
  public Color getColorForId(String id) {
    if (m_colorMap.containsKey(id))
      return m_colorMap.get(id);
    else {
      Color newColor = m_colors[m_colorNum % m_colors.length];
      m_colorNum++;
      m_colorMap.put(id, newColor);
      return newColor;
    }
  }
  

}
