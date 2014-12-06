/**
 * 
 */
package cornerfinders.toolkit;

import java.util.UUID;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MediaInfo {
  private long m_startTimeMillis;
  String m_filename;
  String m_id;
  String m_type;
  
  /**
   * Create a new MediaInfo object
   * @param type type of media (eg audio, video)
   * @param startTimeMillis start time in milliseconds since 1970
   * @param filename filename of the file with the media
   * @param id id for the MediaInfo
   */
  private MediaInfo(String type, long startTimeMillis, String id, String filename) {
    m_type = type;
    m_startTimeMillis = startTimeMillis;
    m_id = id;
    m_filename = filename;
  }

  /**
   * Create a new MediaInfo object and generate a new id for it
   * @param type type of media (eg audio, video) 
   * @param startTimeMillis start time in milliseconds since 1970
   * @param filename filename of the file with the media
   */
  public MediaInfo(String type, long startTimeMillis, String filename) {
    this(type, startTimeMillis, UUID.randomUUID().toString(), filename);
  }

  
  /**
   * Create a new MediaInfo object from the Element
   * @param mediaInfo Element to create the MediaInfo from
   */
  public MediaInfo(Element mediaInfo) {
    // attributes
    m_startTimeMillis = Long.parseLong(mediaInfo.getAttribute("startTime"));
    m_type = mediaInfo.getAttribute("type");

    NodeList children = mediaInfo.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node n = children.item(i);
      if (n instanceof Element) {
        Element child = (Element)n;
        if (child.getTagName().equalsIgnoreCase("id")) {
          m_id = child.getTextContent().trim();
        }
        else if (child.getTagName().equalsIgnoreCase("filename"))
          m_filename = child.getTextContent().trim();
        else {
          System.out.println("Unrecognized tag in media info: "+child.getTagName());
        }
      }
    }
  }
  
  /**
   * Create a copy of this MediaInfo
   * @return a copy of this MediaInfo
   */
  public MediaInfo copy() {
    return new MediaInfo(m_type, m_startTimeMillis, m_id, m_filename);
  }

  /**
   * @return the start time of the media info in milliseconds
   */
  public long getStartTimeInMillis() {
    return m_startTimeMillis;
  }
  
  public String getId() {
    return m_id;
  }
  
  public String getFilename() {
    return m_filename;
  }
  
  public String getType() {
    return m_type;
  }
  
  /**
   * Returns an XML element in the given XML document 
   * that contains all the information about the media info.
   * @param doc the XML document to create the new element in
   * @return an element with all the media info in it
   */
  public Element getElement(Document doc) {
    Element mi = doc.createElement("mediaInfo");

    //attributes
    mi.setAttribute("startTime", Long.toString(this.getStartTimeInMillis()));
    mi.setAttribute("type", this.getType());

    // id
    Element id = doc.createElement("id");
    id.setTextContent(m_id);
    mi.appendChild(id);
    
    // filename
    Element filename = doc.createElement("filename");
    filename.setTextContent(m_filename);
    mi.appendChild(filename);
    
    return mi;
  }
}