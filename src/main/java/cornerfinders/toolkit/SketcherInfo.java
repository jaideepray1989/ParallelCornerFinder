package cornerfinders.toolkit;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Store the information associated with each sketcher.
 * This information includes the sketcher id and the 
 * dpi information if applicable. It also includes the 
 * sketchers nickname and any color associated with the 
 * Sketcher.
 * @author cadlerun
 *
 */
public class SketcherInfo {
  String m_id;  // sketcher id
  String m_nickName = null; // optional nickname of the sketcher
  private boolean m_hasDpi = false; // boolean indicating availability of resolution info
  private double m_xDpi = -1.0; // x resolution
  private double m_yDpi = -1.0; // y resolution
  
  public static final String DEFAULT_SKETCHER_ID = "unknown";
  
  /**
   * Create a SketcherInfo object with the specified id
   * @param id the id for the sketcher
   */
  public SketcherInfo(String id) {
    m_id = id;
  }
  
  /**
   * Create a SketcherInfo object from the XML Element
   * @param sketcher the XML representation of the sketcher
   */
  public SketcherInfo(Element sketcher) {
    NodeList children = sketcher.getChildNodes();
    // for backwards compatibility:
    Node firstNode = children.item(0);
    if ((firstNode.getNodeType() == Node.TEXT_NODE) && (firstNode.getTextContent().trim().length() > 0)) {
      m_id = sketcher.getTextContent().trim();
    }
    else {
      // new format
      for (int i = 0; i < children.getLength(); i++) {
        Node n = children.item(i);
        if (n instanceof Element) {
          Element child = (Element)n;
          if (child.getTagName().equalsIgnoreCase("id")) {
            m_id = child.getTextContent().trim();
          }
          else if (child.getTagName().equalsIgnoreCase("dpi")) {
            float xDpi = Float.parseFloat(child.getAttribute("x"));
            float yDpi = Float.parseFloat(child.getAttribute("y"));
            setDpi(xDpi, yDpi);
          }
          else if (child.getTagName().equalsIgnoreCase("nickname")) {
            m_nickName = child.getTextContent().trim();
          }
          else {
            System.out.println("Unrecognized tag in sketcher info: "+child.getTagName());
          }
        }
      }
    }
  }
  
  /**
   * Get a SketcherInfo object with the default sketcher id
   * @return a new SketcherInfo object with the defaul sketcher id
   */  
  public static SketcherInfo getDefaultSketcherInfo() {
    return new SketcherInfo(DEFAULT_SKETCHER_ID);
  }
  
  /**
   * Returns an XML element in the given XML document 
   * that contains all the information about the sketcher.
   * @param doc the XML document to create the new element in
   * @return an element with all the sketcher info in it
   */
  public Element getElement(Document doc) {
    Element sketcher = doc.createElement("sketcher");
    
    // id
    Element id = doc.createElement("id");
    id.setTextContent(m_id);
    sketcher.appendChild(id);
    
    // dpi
    if (m_hasDpi) {
      Element dpi = doc.createElement("dpi");
      dpi.setAttribute("x",Double.toString(m_xDpi));
      dpi.setAttribute("y",Double.toString(m_yDpi));
      sketcher.appendChild(dpi);
    }
    
    // nick name
    if (m_nickName != null) {
      Element nickname = doc.createElement("nickname");
      nickname.setTextContent(m_nickName);
      sketcher.appendChild(nickname);
    }
    
    return sketcher;
  }
  
  /**
   * Create a copy of this SketcherInfo
   * @return a copy of this SketcherInfo
   */
  public SketcherInfo copy() {
    SketcherInfo copy = new SketcherInfo(m_id);
    copy.setDpi(m_xDpi, m_yDpi);
    return copy;
  }
  
  public String getId() {
    return m_id;
  }
  
  /**
   * @return the nick name of this sketcher or null if there isn't one
   */
  public String getNickName() {
    return m_nickName;
  }
  
  /**
   * Set the nick name for the sketcher
   * @param nickname the new nick name
   */
  public void  setNickName(String nickname) {
    m_nickName = nickname;
  }
  
  public boolean hasDpi() {
    return m_hasDpi;
  }

  public double getDpiX() {
    return m_xDpi;
  }

  public double getDpiY() {
    return m_yDpi;
  }

  public void setDpi(double xDpi, double yDpi) {
    m_xDpi = xDpi;
    m_yDpi = yDpi;
    m_hasDpi = true;
  }
}