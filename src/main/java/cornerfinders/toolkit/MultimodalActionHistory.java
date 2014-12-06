//$Id: MultimodalActionHistory.java,v 1.8 2008-02-29 21:04:04 bpaulson Exp $
//MultimodalActionHistory.java --
//Author: Aaron Adler <cadlerun@csail.mit.edu>
//Copyright: Copyright (C) 2004 by MIT
//Created: <Thu Mar 24 14:37:36 2005>

package cornerfinders.toolkit;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import edu.mit.sketch.language.edits.DeleteEdit;
import edu.mit.sketch.language.edits.EditGesture;
import edu.mit.sketch.language.edits.EraseEdit;
import edu.mit.sketch.language.edits.MoveEdit;
import edu.mit.sketch.language.edits.RedoAction;
import edu.mit.sketch.language.edits.UndoAction;
import edu.mit.sketch.language.shapes.DrawnShape;
import edu.mit.sketch.language.shapes.MultimodalAction;
import edu.mit.sketch.language.shapes.RRectangle;
import edu.mit.sketch.language.shapes.Stroke;
import edu.mit.sketch.language.speech.Phrase;
import edu.mit.sketch.language.speech.Silence;
import edu.mit.sketch.language.speech.Speech;
import edu.mit.sketch.language.speech.Word;
import edu.mit.util.ResourceFinder;
import edu.tamu.hammond.sketch.shapes.TCurve;
import edu.tamu.hammond.sketch.shapes.TEllipse;
import edu.tamu.hammond.sketch.shapes.TLine;
import edu.tamu.hammond.sketch.shapes.TPoint;
import edu.tamu.hammond.sketch.shapes.TText;

//note to self: do i need to create an exception for processing the XML?? ****
public class MultimodalActionHistory {

  private MultimodalActionFactory m_factory;
  private UUID m_id = UUID.randomUUID();
  private SketcherIndex m_sketcherIndex; // ids and other info
  private MediaIndex m_mediaIndex; // info about references to media files
  private String m_studyName;
  private String m_domainName;
  private int m_undoPointer = -1;
  private boolean m_isSortDelayed = false;
  /** The file (if any) history was loaded from. **/
  private File m_sourceFile = null;

  // for short ids
  private boolean m_shortIds = false; // default value;
  private HashMap<UUID, String> m_idTable;
  private int m_idCounter;

  private List<MultimodalAction> m_history = Collections.synchronizedList(new LinkedList<MultimodalAction>());

  private Hashtable<UUID, MultimodalAction> m_actionHash =
    new Hashtable<UUID, MultimodalAction>();
  private boolean m_inHiMetricUnits = false; // coordinates in HiMetric units?
  private boolean m_convertHiMetricToPixel = true; // convert HiMetric units in files to pixel values when loading a file

  /**
   * Construct a new MultimodalActionHistory with default values
   * for the sketcher ids, study name, and domain name.
   */
  public MultimodalActionHistory() {
    m_sketcherIndex = new SketcherIndex();
    m_mediaIndex = new MediaIndex();
    m_studyName = "none";
    m_domainName = "unknown";
  }

  /**
   * Construct a new MultimodalActionHistory with default values
   * for the sketcher ids, study name, and domain name. If shortIds
   * is true then use short IDs when writing to the output file, if
   * it is false then behave normally.
   */
  public MultimodalActionHistory(boolean shortIds) {
    this();
    m_shortIds = shortIds;
    m_idCounter = 0;
    m_idTable = new HashMap<UUID, String>();
  }

  /**
   * Construct a new MultimodalActionHistory with the specified values
   * for the sketcher ids, study name, and domain name.
   * @param studyName the name of the study
   * @param domainName the name of the domain
   * @param sketcherIds the sketcher ids
   */
  public MultimodalActionHistory(String studyName, String domainName, Set<String> sketcherIds) {
    this();
    m_domainName = domainName;
    for (String sketcherId : sketcherIds) {
      this.addSketcher(new SketcherInfo(sketcherId));
    }
    m_studyName = studyName;
  }

  /**
   * @param factory
   */
  public MultimodalActionHistory(MultimodalActionFactory factory) {
    this();
    setMultimodalActionFactory(factory);
  }

  public void setMultimodalActionFactory(MultimodalActionFactory factory){
    m_factory = factory;
    m_factory.setSketcherIndex(getSketcherIndex());
  }

  public MultimodalActionFactory getMultimodalActionFactory() {
    return m_factory;
  }

  /**
   * Create an empty copy of this MultimodalActionHistory with the same
   * header information
   * @return an empty copy with the header information preserved
   */
  public MultimodalActionHistory createEmptyCopyWithHeaderInfo() {
    MultimodalActionHistory history = new MultimodalActionHistory();

    // fill in the headers appropriately
    history.setDomainName(m_domainName);
    history.setStudyName(m_studyName);
    for (SketcherInfo sketcher : m_sketcherIndex.getSketchers()) {
      history.addSketcher(sketcher.copy());
    }
    for (MediaInfo mediaInfo : m_mediaIndex.getMedia()) {
      history.addMediaInfo(mediaInfo.copy());
    }
    history.m_inHiMetricUnits = m_inHiMetricUnits;

    return history;
  }

  /**
   * Get the file (if any) that the history was loaded from. Will return null if it was not loaded from a file.
   * @return the File the MultimodalActionHistory was loaded from, or null if there isn't one.
   */
  public File getSourceFile() {
    return m_sourceFile;
  }

  /**
   * Get the domain name for the MultimodalActionHistory
   * @return the domain name
   */
  public String getDomainName() {
    return m_domainName;
  }

  /**
   * Set the domain name for the MultimodalActionHistory
   * @param domainName The domain name to set.
   */
  public void setDomainName(String domainName) {
    m_domainName = domainName;
  }

  /**
   * Get the study name for the MultimodalActionHistory
   * @return the study name
   */
  public String getStudyName() {
    return m_studyName;
  }

  /**
   * Set the study name for the MultimodalActionHistory
   * @param studyName The study name to set.
   */
  public void setStudyName(String studyName) {
    m_studyName = studyName;
  }

  /**
   * Get the UUID for this sketch
   *
   * @return The UUID for this sketch
   */
  public UUID getId() {
    return m_id;
  }

  /**
   * Set the UUID for this sketch. This is useful if you have modified the
   * sketch and want to save it with a new Id.
   */
  public void changeId() {
    m_id = UUID.randomUUID();
  }

  /**
   * Get the SketcherIndex for this history. The SketcherIndex stores the SketcherInfo for
   * sketchers referenced in the history.
   * @return the SketcherIndex for this history
   */
  public SketcherIndex getSketcherIndex() {
    return m_sketcherIndex;
  }

  /**
   * Add a SketcherInfo to the MultimodalActionHistory
   * @param sketcher The sketcher to add.
   */
  public void addSketcher(SketcherInfo sketcher) {
    m_sketcherIndex.add(sketcher);
  }

  /**
   * Get the MediaIndex for this history. The MediaIndex stores the MediaInfo for
   * media files referenced in the history.
   * @return the MediaIndex for this history
   */
  public MediaIndex getMediaIndex() {
    return m_mediaIndex;
  }

  /**
   * Add a MediaInfo to the MultimodalActionHistory
   * @param mediaInfo The MediaInfo to add.
   */
  public void addMediaInfo(MediaInfo mediaInfo) {
    m_mediaIndex.add(mediaInfo);
  }

  /**
   * Determine if there is media with the specified filename in the history
   * @param filename the filename of the media to look for
   * @return true if there is a MediaInfo with the specified filename in the history, false otherwise
   */
  public boolean hasMediaInfo(String filename) {
    return m_mediaIndex.hasMediaInfoByFilename(filename);
  }

  /**
   * Get a set of authorIds containing all the authors of actions
   * whose source is the specified id
   * @param id the id to retreive all the author ids for
   * @return a set (could be empty) of all actions whose source is the specified id
   */
  public Set<String> getAuthorsForSource(String id) {
    HashSet<String> authors = new HashSet<String>();

    for (MultimodalAction ma : m_history) {
      if (ma instanceof Speech) {
        Speech s = (Speech)ma;
        String source = s.getProperty("source");
        if ((source != null) && (source.equals(id))) {
          String author = s.getProperty("author");
          if (author != null)
            authors.add(author);
        }
      }
    }

    return authors;
  }

  /**
   * Checks whether the coordinates are in himetric units or not
   * @return true if the coordinates are in himetric units, false otherwise
   */
  public boolean inHiMetricCoordinates() {
    return m_inHiMetricUnits;
  }

  /**
   * Sets whether the coordinates are in himetric units or not
   * @param inHiMetric sets the units to hiMetric if true, sets the untis to pixels if false
   */
  public void setHiMetricCoordinates(boolean inHiMetric) {
    m_inHiMetricUnits = inHiMetric;
  }

  /**
   * Sets whether to convert HiMetric coordinates to pixel coordinates when loading a file
   * @param convert if true convert from HiMetric to pixel coordinates, if false don't convert
   */
  public void setConvertHiMetricToPixel(boolean convert) {
    m_convertHiMetricToPixel = convert;
  }

  /**
   * Get the maximum dpi for the x coordinates
   * @return the maximum dpi for the x coordinates or -1.0f if there are no sketchers or the units are not in himetric coordinates
   */
  public double getMaxDpiX() {
    double maxX = -1.0;

    if (!inHiMetricCoordinates() || m_sketcherIndex.size() < 1)
      return maxX;

    for (SketcherInfo sketcher : m_sketcherIndex.getSketchers()) {
      if (sketcher.hasDpi())
        maxX = Math.max(maxX, sketcher.getDpiX());
    }

    return maxX;
  }

  /**
   * Get the maximum dpi for the y coordinates
   * @return the maximum dpi for the y coordinates or -1.0f if there are no sketchers or the units are not in himetric coordinates
   */
  public double getMaxDpiY() {
    double maxY = -1.0;

    if (!inHiMetricCoordinates() || m_sketcherIndex.size() < 1)
      return maxY;

    for (SketcherInfo sketcher : m_sketcherIndex.getSketchers()) {
      if (sketcher.hasDpi())
        maxY = Math.max(maxY, sketcher.getDpiY());
    }

    return maxY;
  }

  /**
   * Set the dpi for a particular sketcher in the multimodal action history.
   * The dpi are stored so that conversion to pixel coordinates is possible.
   * @param sketcherId the sketcher the resolution information is for
   * @param xDpi the x dpi
   * @param yDpi the y dpi
   */
  public void setDpi(String sketcherId, float xDpi, float yDpi) {
    SketcherInfo sketcher = m_sketcherIndex.getSketcherInfo(sketcherId);
    if (sketcher != null) {
      sketcher.setDpi(xDpi, yDpi);
      m_inHiMetricUnits = true;
    }
    else {
      // the sketcher should be in sketchers
      System.out.println("Error -- sketcher id: "+sketcherId+" expected but not found.");
    }
  }

  /**
   * Returns the shape at the particular element
   * @param key The key of the shape in the hash table.
   * @return The shape with that key.
   */
  public MultimodalAction get(UUID key){
    if(!m_actionHash.containsKey(key)){return null;}
    return m_actionHash.get(key);
  }

  /**
   * Efficently test if the history holds the given action.
   *
   * @param action
   *        The action in question
   * @return Check if the action is part of this history.
   */
  public boolean hasAction(MultimodalAction action) {
    return m_actionHash.containsKey(action.getId());
  }

  /**
   * Returns a sorted list of all the actions in this history.
   * @return A new list containing all of the actions in this history.
   */
  public ArrayList<MultimodalAction> getActions() {
    return new ArrayList<MultimodalAction>(m_history);
  }

  public MultimodalAction getAction(int i) {
    return m_history.get(i);
  }

  public int getNumActions() {
    return m_history.size();
  }

  /**
   * Get all of the strokes from this history, optionally including subStrokes.
   *
   * @param includeSubStrokes
   *        Should sub-strokes be included? (note that this means some points
   *        will be referenced 2 times, once by the stroke and once by the
   *        substroke.
   * @return A list of the strokes.
   */
  public List<Stroke> getStrokes(boolean includeSubStrokes) {
    ArrayList<Stroke> strokes = new ArrayList<Stroke>();
    for (MultimodalAction action : m_history) {
      if (action.isOfType("Stroke")) {
        Stroke stroke = (Stroke)action;
        // if we want substrokes or if it isn't a sub stroke anyway add it in
        if (includeSubStrokes || !stroke.isSubStroke()) {
          strokes.add(stroke);
        }
      }
    }
    return strokes;
  }

  /**
   * Get the maximum bounds of all the DrawnShapes in the history.
   * @return A Rectangle that is the bounding box of all the DrawnShapes or a 10x10 Rectangle if no shapes are in the history.
   */
  public Rectangle getMaxSketchBounds() {
    boolean boundsSet = false;
    RRectangle bounds = new RRectangle(0,0,10,10);

    for (MultimodalAction ma : m_history) {
      if (ma instanceof DrawnShape) {
        DrawnShape shape = (DrawnShape) ma;
        RRectangle r = shape.getBoundingBox();
        if (!r.isEmpty()) {
          if (!boundsSet) {
            // we don't have bounds yet, so override the default rectangle
            // and set the bounds to the rectangle we have
            bounds = r;
            boundsSet = true;
          }
          else
            bounds = bounds.getUnion(r);
        }
      }
    }

    // either the bounds were set, or we return the default rectangle
    return new Rectangle((int)bounds.getMinX(), (int)bounds.getMinY(), (int)bounds.getWidth(), (int)bounds.getHeight());
  }

  /**
   * Add an action to the history and send it to the factory (if there is one).
   * @param a the action to add
   * @return UUID of the added action
   */
  public UUID addLoad(MultimodalAction a){
    UUID u = add(a);
    // UndoAction and RedoAction are sent to the factory separately.
    if(UndoAction.class.isInstance(a) ||
        RedoAction.class.isInstance(a)){return u;}
    if(m_factory != null){m_factory.load(a);}
    else{System.out.println("factory is null in history");}
    return u;
  }

  /**
   * Add the element to the list and returns the
   * generated element of the action.
   * @param a The action added to the list.
   * @return the index of the DrawnShape
   */
  public UUID add(MultimodalAction a){
    if(get(a.getId()) == null){
      addToHistory(a);
      addToHash(a);
      if(!UndoAction.class.isInstance(a) &&
          !RedoAction.class.isInstance(a)){
        m_undoPointer = m_history.size() - 1;
      }
    }
    if(UndoAction.class.isInstance(a)){
      undo();
    }
    if(RedoAction.class.isInstance(a)){
      redo();
    }
    return a.getId();
  }

  private void undo(){
    boolean finished = false;
    // don't undo past the start of the list
    while(!finished && (m_undoPointer > -1)){
      MultimodalAction ma = m_history.get(m_undoPointer);
      if(!ma.isUndoable()){
        m_undoPointer--;
        continue;
      }
      ma.setUndoable(false);
      ma.setRedoable(true);
      if(EditGesture.class.isInstance(ma)){
        finished = true;
      }
      if(Stroke.class.isInstance(ma)){
        Stroke s = (Stroke)ma;
        for(DrawnShape sub: s.getComponents()){
          sub.setUndoable(false);
          sub.setRedoable(true);
          if(m_undoPointer > m_history.indexOf(sub)){
            if(m_history.indexOf(sub) == -1){
              System.out.println("can't find " + sub);
            } else {
              m_undoPointer = m_history.indexOf(sub);
            }
          }
        }
        finished = true;
      }
      if(m_factory != null){
        m_factory.undo(ma);
      }
      m_undoPointer--;
    }
  }

  private void redo(){
    boolean finished = false;
    boolean didsomething = false;
    while(!finished){
      if(m_undoPointer >= m_history.size()){
        System.out.println("undoPointer greater than history size");
        return;
      }
      MultimodalAction ma = m_history.get(m_undoPointer);
      if(EditGesture.class.isInstance(ma) ||
          TPoint.class.isInstance(ma) ||
          Stroke.class.isInstance(ma)){
        if(didsomething){return;}
      }
      m_undoPointer++;
      if(!ma.isRedoable()){continue;}
      ma.setRedoable(false);
      ma.setUndoable(true);
      if(Stroke.class.isInstance(ma)){
        DrawnShape s = (DrawnShape)ma;
        for(DrawnShape sub: s.getComponents()){
          sub.setUndoable(true);
          sub.setRedoable(false);
          if(m_undoPointer < m_history.indexOf(sub)){
            m_undoPointer = m_history.indexOf(sub);
          }
          continue;
        }
      }
      if(EditGesture.class.isInstance(ma) ||
          (!Stroke.class.isInstance(ma) &&
              !TPoint.class.isInstance(ma))){
        //  finished = true;
        if(m_factory != null){
          m_factory.redo(ma);
          didsomething = true;
        }
      }

    }
  }


  /**
   * This method converts the UUIDs to the format that they should
   * be in to output to the file. It checks m_shortIds to determine
   * if the ids should be shortened or not.
   * @param id the UUID to convert
   * @return the string for this UUID
   */
  private String convertId(UUID id) {
    if (!m_shortIds)
      return id.toString();
    if (m_idTable.containsKey(id))
      return m_idTable.get(id);
    String newId = Integer.toString(m_idCounter++);
    m_idTable.put(id, newId);
    return newId;
  }


  private synchronized void addToHistory(MultimodalAction a) {
    if(m_history.contains(a)) {return;}
    m_history.add(a);
    sortHistory();
    m_undoPointer++;
  }


  /**
   * Add the element to the list and returns the
   * generated element of the action.
   * @param a The action added to the list.
   * @return the index of the DrawnShape
   */
  private UUID addToHash(MultimodalAction a){
    if(get(a.getId()) == null) {
      m_actionHash.put(a.getId(), a);}
    return a.getId();
  }
  /**
   * Updates the shape at shapeID to be the new element
   * @param a action to be updated
   */
  public void update(MultimodalAction a){
    m_actionHash.remove(a.getId());
    m_actionHash.put(a.getId(), a);
  }

  private Document createXMLDocument() {
    DocumentBuilder domBuilder;
    try{
      domBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document doc = domBuilder.newDocument();

      Element sketch = doc.createElement("sketch");
      sketch.setAttribute("id", convertId(m_id));
      sketch.setIdAttribute("id", true);
      if (m_inHiMetricUnits)
        sketch.setAttribute("units", "himetric");
      else
        sketch.setAttribute("units", "pixel");
      doc.appendChild(sketch);

      //add sketcherId (or ids because there can be more than one, or none)
      if (m_sketcherIndex.isEmpty()) {
        sketch.appendChild(SketcherInfo.getDefaultSketcherInfo().getElement(doc));
      }
      for (SketcherInfo sketcher : m_sketcherIndex.getSketchers()) {
        sketch.appendChild(sketcher.getElement(doc));
      }

      // add MediaInfo data
      for (MediaInfo mediaInfo : m_mediaIndex.getMedia()) {
        sketch.appendChild(mediaInfo.getElement(doc));
      }

      Element study = doc.createElement("study");
      study.setTextContent(m_studyName);
      sketch.appendChild(study);

      Element domain = doc.createElement("domain");
      domain.setTextContent(m_domainName);
      sketch.appendChild(domain);

      checkComponentsInHistory();
      synchronized (this) {
        for(MultimodalAction ma : m_history) {
          writeMultimodalAction(ma,doc, sketch);
        }
      }
      return doc;
    }catch(ParserConfigurationException e){
      e.printStackTrace();
      System.out.println("XML writer failed: " + e.getMessage());
      System.exit(1);
      return null;
    }
  }
  /**
   *
   */
  private void checkComponentsInHistory() {
    boolean addedSomething = true;
    while(addedSomething){
      addedSomething = false;
      for(int i = 0; i < m_history.size(); i++){
        MultimodalAction ma = m_history.get(i);
        if(DrawnShape.class.isInstance(ma)){
          DrawnShape shape = (DrawnShape)ma;
          for(DrawnShape component : shape.getComponents()){
            if(get(component.getId()) == null){
              add(component);
              addedSomething = true;
            }
          }
          for(DrawnShape component : shape.getAliases()){
            if(get(component.getId()) == null){
              add(component);
              addedSomething = true;
            }
          }
        }
      }
    }
    sortHistory();
  }

  private void writeMultimodalAction(MultimodalAction ma, Document doc, Element sketch){
    if(UndoAction.class.isInstance(ma)
        || RedoAction.class.isInstance(ma)){
      Element edit = doc.createElement("edit");
      edit.setAttribute("id", ma.getId().toString());
      edit.setIdAttribute("id", true);
      edit.setAttribute("type", ma.getType());
      return;
    }
    if(EditGesture.class.isInstance(ma)) {
      System.out.println("adding an edit to type " + ma.getType());
      Element edit = doc.createElement("edit");
      edit.setAttribute("id", convertId(ma.getId()));
      edit.setIdAttribute("id", true);
      edit.setAttribute("type", ma.getType());
      edit.setAttribute("time", Long.toString(ma.getTime()));
      Element trigger = doc.createElement("trigger");
      MultimodalAction ma_trigger = ((EditGesture)ma).getTrigger();
      if (ma_trigger != null) {
        trigger.setAttribute("type", ma_trigger.getType());
        trigger.setTextContent(convertId(ma_trigger.getId()));
        edit.appendChild(trigger);
      }
      for (MultimodalAction ma_action : ((EditGesture)ma).getShapes()) {
        if (ma_action != null) {
          Element action = doc.createElement("arg");
          action.setAttribute("type", ma_action.getType());
          action.setTextContent(convertId(ma_action.getId()));
          edit.appendChild(action);
        }
      }
      sketch.appendChild(edit);
      return;
    }
    if(TPoint.class.isInstance(ma)){
      Element point = doc.createElement("point");
      point.setAttribute("id", convertId(ma.getId()));
      point.setIdAttribute("id", true);
      TPoint rpoint = (TPoint)ma;
      rpoint.setOrig();
      point.setAttribute("name", rpoint.getName());
      point.setAttribute("x", Double.toString(rpoint.getProp("x")));
      point.setAttribute("y", Double.toString(rpoint.getProp("y")));
      point.setAttribute("time", Long.toString(rpoint.getTime()));
      if((int)rpoint.getProp("pressure") != -1) {
        point.setAttribute("pressure", Integer.toString((int)rpoint.getProp("pressure")));         }
      rpoint.setCurrent();
      sketch.appendChild(point);
      return;
    }
    if(DrawnShape.class.isInstance(ma)){
      DrawnShape s = (DrawnShape)ma;
      s.setOrig();
      Element shape = doc.createElement("shape");
      shape.setAttribute("type", s.getType());
      if(s.getType().equals("Stroke")) {
        Stroke stroke = (Stroke)s;
        shape.setAttribute("laysInk", String.valueOf(stroke.isLaysInk()));
        // if it's a sub-stroke write out the parent
        // NOTE: this will get written out even if the parent is not actually
        // in this history.  This can happen when saving a subset of actions from a
        // history as in the labeler, viewer, etc...  This will be ok because the read
        // method just doesn't find the UUID for the parent, and ends up setting the
        // stroke's parent to null
        if(stroke.isSubStroke()) {
          UUID id = stroke.getSubStrokeOf().getId();
          shape.setAttribute("subStrokeOf", convertId(id));
        }
      }
      if(s.getType().equals("Line")){
        addToHash(s.get("p1"));
        addToHash(s.get("p2"));
        shape.setAttribute("p1", convertId(s.get("p1").getId()));
        shape.setAttribute("p2", convertId(s.get("p2").getId()));
      }
      if(s.getType().equals("Ellipse")){
        shape.setAttribute("x", Double.toString(s.getProp("x")));
        shape.setAttribute("y", Double.toString(s.getProp("y")));
        shape.setAttribute("width", Double.toString(s.getProp("width")));
        shape.setAttribute("height", Double.toString(s.getProp("height")));
      }
      if(s.getType().equals("Text")){
//      addToHash(s.get("location"));
//      shape.setAttribute("location", convertId(s.get("location").getId()));
        shape.setAttribute("text", ((TText)s).getText());
      }
      if(s.getType().equals("Rectangle")){
        shape.setAttribute("leftx", Double.toString(s.getProp("leftx")));
        shape.setAttribute("topy", Double.toString(s.getProp("topy")));
        shape.setAttribute("width", Double.toString(s.getProp("width")));
        shape.setAttribute("height", Double.toString(s.getProp("height")));
      }
      if(s.getType().equals("Curve")){
        addToHash(s.get("start"));
        addToHash(s.get("end"));
        addToHash(s.get("control1"));
        addToHash(s.get("control2"));
        shape.setAttribute("start", convertId(s.get("start").getId()));
        shape.setAttribute("end", convertId(s.get("end").getId()));
        shape.setAttribute("control1", convertId(s.get("control1").getId()));
        shape.setAttribute("control2", convertId(s.get("control2").getId()));
      }
      shape.setAttribute("name", s.getName());
      shape.setAttribute("id", convertId(s.getId()));
      shape.setIdAttribute("id", true);
      shape.setAttribute("time", Long.toString(s.getTime()));
      shape.setAttribute("orientation", Double.toString(s.getProp("orientation")));
//    s.setOrig();
      for(DrawnShape subshape : s.getComponents()){
        Element component = doc.createElement("arg");
        component.setAttribute("type", subshape.getType());
        component.setTextContent(convertId(subshape.getId()));
        shape.appendChild(component);
      }
      Set<String> set = s.getPropertyNames();
      for(String propname : set){
        //  while(enumeration.hasMoreElements()){
        //String propname = enumer
        if(!shape.hasAttribute(propname)){
//          System.out.println("writing prop " + propname + " "
//              + s.getProperty(propname));
          shape.setAttribute(propname, s.getProperty(propname));
        }
      }
      s.setCurrent();
      sketch.appendChild(shape);
      return;
    }

    // writing speech to a file
    if (ma instanceof Speech) {
      Element speech = doc.createElement("speech");
      speech.setAttribute("id", convertId(ma.getId()));
      speech.setIdAttribute("id", true);
      Speech sw = (Speech)ma;
      speech.setAttribute("startTime", Long.toString(sw.getStartTime()));
      speech.setAttribute("endTime", Long.toString(sw.getEndTime()));
      speech.setAttribute("type", ma.getType());

      if (ma instanceof Word) {
        speech.setAttribute("score", Long.toString(sw.getAcousticScore()));

        if (!(ma instanceof Silence)) {
          Word w = (Word)ma;
          speech.setAttribute("word", w.getWord());
          if (w.isAlternateForm())
            speech.setAttribute("wordForm", Integer.toString(w.getForm()));
        }
      }
      if (ma instanceof Phrase) {
        Phrase phrase = (Phrase)ma;
        for (Word word : phrase.getWords()) {
          Element component = doc.createElement("arg");
          component.setAttribute("type", word.getType());
          component.setTextContent(convertId(word.getId()));
          speech.appendChild(component);
        }
      }

      // get any other properties
      for(String propname : sw.getPropertyNames()){
        if(!speech.hasAttribute(propname)) {
          speech.setAttribute(propname, sw.getProperty(propname));
        }
      }

      sketch.appendChild(speech);
      return;
    }

    System.out.println(" ERROR:::: " + ma.getType() + "not written to file!");
  }

  /**
   * Adjust the time of the parent if the time of the new child is
   * after the current parent time. Also make sure to resort the
   * sorted list
   * @param parent the parent
   * @param child the child
   */
  private void adjustParentTime(DrawnShape parent, DrawnShape child) {
    // a parent cannot have a timestamp that is earlier
    // than any of its children.
    long parentTime = parent.getTime();
    long childTime = child.getTime();
    if (parentTime < childTime) {
      parent.setTime(childTime);
      sortHistory();
    }
    // we also need to update the start time (the earliest time of a component) of the parent
    // we need to do this for strokes and their points only
    // but only the original strokes, not the sub strokes
    // sub strokes should have their designated (end) times as their start time as they are
    // instantaneously created, not animated as original strokes are. -ADA 12-7-05
    if ((parent instanceof Stroke) && (!((Stroke)parent).isSubStroke())
        && (child instanceof TPoint))
      if (childTime < parent.getStartTime())
        parent.setStartTime(childTime);
  }

  public MultimodalAction loadElement(Element e, String parentUUID) {
    DrawnShape parent = null;
    DrawnShape child = null;
    MultimodalAction mparent = get(UUID.fromString(parentUUID));
    if(mparent!= null && DrawnShape.class.isInstance(mparent)) {
      parent = (DrawnShape)mparent;
    }
    MultimodalAction mchild = loadElement(e);
    if(mchild!= null && DrawnShape.class.isInstance(mchild)) {
      child = (DrawnShape)mchild;
    }
    if(parent != null && child != null) {
      parent.addComponent(child);

      // make sure the time stamps make sense
      // a parent cannot have a timestamp that is earlier
      // than any of its children.
      adjustParentTime(parent, child);
    }
    return mchild;
  }

  /**
   * Load the Element and all of its sub-components.
   * The sub-components must already be in the MultimodalActionHistory
   * @param e the element to load
   * @return the MultimodalAction that results from the given element
   */
  public MultimodalAction loadElement(Element e){

    MultimodalAction ma = processElement(e);
    processSubComponents(e);
    sortHistory();
    return ma;
  }

  private MultimodalAction processElement(Element e){
    if(e.getTagName().equals("sketcher")){
      addSketcher(new SketcherInfo(e));
    } else if (e.getTagName().equals("mediaInfo")) {
      addMediaInfo(new MediaInfo(e));
    } else if (e.getTagName().equals("study")){
      m_studyName = e.getTextContent().trim();
    } else if(e.getTagName().equals("domain")){
      m_domainName = e.getTextContent().trim();
    } else if(e.getTagName().equals("point")){
      UUID id = UUID.fromString(e.getAttribute("id"));
      String name = e.getAttribute("name");
      double x = Double.parseDouble(e.getAttribute("x"));
      double y = Double.parseDouble(e.getAttribute("y"));
      long time = Long.parseLong(e.getAttribute("time"));
      TPoint rpoint;
      if (e.hasAttribute("pressure")) {
        //double pressure = Double.parseDouble(e.getAttribute("pressure"));
        int pressure = Integer.parseInt(e.getAttribute("pressure"));
        rpoint = new TPoint(name, x, y, time, pressure);
      }
      else
        rpoint = new TPoint(name, x, y, time);
      rpoint.setId(id);
      add(rpoint);
      return rpoint;
      //rpoint.view ...
    } else if(e.getTagName().equals("shape")){
      UUID id = UUID.fromString(e.getAttribute("id"));
      String type = e.getAttribute("type");
      String name = e.getAttribute("name");
      double orientation = 0; // default to zero
      if (e.hasAttribute("orientation")) {
        orientation = Double.parseDouble(e.getAttribute("orientation"));
      }
      long time = Long.parseLong(e.getAttribute("time"));
      DrawnShape drawnShape = new DrawnShape(type, orientation);
      if(type.equals("Stroke")){
        drawnShape = new Stroke(orientation);
        if(e.hasAttribute("laysInk")){
          ((Stroke)drawnShape).setLaysInk(Boolean.parseBoolean(e.getAttribute("laysInk")));
        }
        if(e.hasAttribute("subStrokeOf")) {
          ((Stroke)drawnShape).setSubStrokeOf((Stroke)get(UUID.fromString(e.getAttribute("subStrokeOf"))));
        }
      }
      if(type.equals("EraseStroke")) {
        drawnShape = new Stroke(orientation);
        ((Stroke)drawnShape).setLaysInk(Boolean.parseBoolean(e.getAttribute("laysInk")));
        ((Stroke)drawnShape).setErase(true);
      }
      if (type.equals("Line")){
        TPoint p1 = (TPoint)get(UUID.fromString(e.getAttribute("p1")));
        TPoint p2 = (TPoint)get(UUID.fromString(e.getAttribute("p2")));
        drawnShape = new TLine(p1, p2);
      }
      if(type.equals("Ellipse")){
        double x = Double.parseDouble(e.getAttribute("x"));
        double y = Double.parseDouble(e.getAttribute("y"));
        double width = Double.parseDouble(e.getAttribute("width"));
        double height = Double.parseDouble(e.getAttribute("height"));
        drawnShape = new TEllipse(x, y, width, height);
      }
      if(type.equals("Text")){
        //RPoint location = (RPoint)get(UUID.fromString(e.getAttribute("location")));
        //System.out.println("location = " + location);
        String text = e.getAttribute("text");
        //      drawnShape = new RText(location, text);
        drawnShape = new TText(text);
      }
      if(type.equals("Rectangle")){
        double leftx = Double.parseDouble(e.getAttribute("leftx"));
        double topy = Double.parseDouble(e.getAttribute("topy"));
        double width = Double.parseDouble(e.getAttribute("width"));
        double height = Double.parseDouble(e.getAttribute("height"));
        drawnShape = new RRectangle(leftx, topy, width, height);
      }
      if(type.equals("Curve")){
        TPoint start = (TPoint)get(UUID.fromString(e.getAttribute("start")));
        TPoint end = (TPoint)get(UUID.fromString(e.getAttribute("end")));
        TPoint control1 = (TPoint)get(UUID.fromString(e.getAttribute("control1")));
        TPoint control2 = (TPoint)get(UUID.fromString(e.getAttribute("control2")));
        //drawnShape = new RCurve(start, end, control1, control2);
        TPoint[] array = new TPoint[4];
        array[0] = start;
        array[1] = control1;
        array[2] = control2;
        array[3] = end;
        drawnShape = new TCurve(array);
      }
      drawnShape.setName(name);
      drawnShape.setTime(time);
      drawnShape.setId(id);
      NamedNodeMap nnm = e.getAttributes();
      for(int count = 0; count < nnm.getLength(); count++){
        drawnShape.setProperty(nnm.item(count).getNodeName(),
            nnm.item(count).getNodeValue());
      }
      add(drawnShape);
      return drawnShape;
    } else if(e.getTagName().equals("edit")){
      UUID id = UUID.fromString(e.getAttribute("id"));
      String type = e.getAttribute("type");
      long time = Long.parseLong(e.getAttribute("time"));
      //NodeList components = e.getChildNodes();
      EditGesture edit = null;
      if(type.equals("Redo")){
        edit = new RedoAction();
      }
      if(type.equals("Undo")){
        edit = new UndoAction();
      }
      if(type.equals("Move")) {
        edit = new MoveEdit();
      }else if(type.equals("Delete")) {
        edit = new DeleteEdit();
      }else if(type.equals("Erase")) {
        edit = new EraseEdit();
      }
      if(edit != null) {
        edit.setId(id);
        edit.setTime(time);
        //	if(m_factory !=null) {m_factory.load(edit);}
        //System.out.println("Successfully loaded edit of type: " + type);
        add(edit);
      }else {
        System.out.println("EDIT OF WRONG TYPE: #" + type + "#");
      }
      return edit;
    } else if(e.getTagName().equals("speech")){
      UUID id = UUID.fromString(e.getAttribute("id"));
      String type = e.getAttribute("type");

      long startTime = Long.parseLong(e.getAttribute("startTime"));
      long endTime = Long.parseLong(e.getAttribute("endTime"));

      Speech s = null;

      if (type.equalsIgnoreCase("silence")) {
        long score = Long.parseLong(e.getAttribute("score"));
        s = new Silence(startTime, endTime, score);
      }
      else if (type.equalsIgnoreCase("word")) {
        long score = Long.parseLong(e.getAttribute("score"));
        String wordStr = e.getAttribute("word");
        if (e.hasAttribute("wordForm")) {
          int form = Integer.parseInt(e.getAttribute("wordForm"));
          s = new Word(startTime, endTime, wordStr, form, score);
        }
        else
          s = new Word(startTime, endTime, wordStr, score);
      }
      else if (type.equalsIgnoreCase("phrase")) {
        s = new Phrase();
      }

      s.setId(id);

      // get the rest of the properties
      NamedNodeMap nnm = e.getAttributes();
      for(int count = 0; count < nnm.getLength(); count++){
        s.setProperty(nnm.item(count).getNodeName(),
            nnm.item(count).getNodeValue());
      }

      add(s);
      return s;
    }
    else {
      System.out.println("Tag not found! " + e.getTagName());
    }
    return null;
  }

  private void processSubComponents(Element e){
    if(e.getTagName().equals("shape")){
      DrawnShape drawnShape = (DrawnShape)get(UUID.fromString(e.getAttribute("id")));
      NodeList components = e.getChildNodes();
      for(int k = 0; k < components.getLength(); k++){
        Node n = components.item(k);
        if (n instanceof Element) {
          Element component = (Element)n;
          String id = component.getTextContent().trim();
          if (id != "") {
            UUID id2 = UUID.fromString(id);
            DrawnShape subshape = (DrawnShape)get(id2);
            // if(subshape == null){return "what! the subshape is not found!");
            drawnShape.addComponent(subshape);
            // make sure the time stamps make sense
            // a parent cannot have a timestamp that is earlier
            // than any of its children.
            // also add a start time stamp for strokes
            adjustParentTime(drawnShape, subshape);
          }
        }
      }
    }if(e.getTagName().equals("edit")){
      EditGesture editGesture = (EditGesture)get(UUID.fromString(e.getAttribute("id")));
      NodeList components = e.getChildNodes();
      for(int k = 0; k < components.getLength(); k++){
        Node n = components.item(k);
        if (n instanceof Element) {
          Element component = (Element)n;
          //System.out.println("UUID is "+component.getTextContent().trim());
          String id = component.getTextContent().trim();
          if (id != "") {
            UUID id2 = UUID.fromString(id);
            MultimodalAction subshape = get(id2);
            if(component.getTagName() == "trigger") {
              editGesture.setTrigger(subshape);
              // give MoveEdits a start time
              if ((editGesture instanceof MoveEdit) && (subshape instanceof Stroke)) {
                editGesture.setStartTime(subshape.getStartTime());
              }
            }
            if(component.getTagName() == "arg") {
              editGesture.addShape((DrawnShape)subshape);
            }
          }
        }
      }
    }
    if (e.getTagName().equals("speech")) {
      Speech speech = (Speech)get(UUID.fromString(e.getAttribute("id")));
      if (speech.getType().equalsIgnoreCase("phrase")) {
        Phrase phrase = (Phrase)speech;
        NodeList components = e.getChildNodes();
        for(int k = 0; k < components.getLength(); k++){
          Node n = components.item(k);
          if (n instanceof Element) {
            Element component = (Element)n;
            String id = component.getTextContent().trim();
            if (id != "") {
              UUID id2 = UUID.fromString(id);
              Word phraseWord = (Word)get(id2);
              if (component.getTagName() == "arg")
                phrase.add(phraseWord);
            }
          }
        }
      }
    }
  }

  public void loadImage() {
    if (m_factory != null) {
      m_factory.setIsSortFactoryDelayed(true);
      for(MultimodalAction ma : m_history) {
        m_factory.load(ma);
      }
      m_factory.setIsSortFactoryDelayed(false);
      m_factory.setAnimationToEnd();
    }
  }

  public void playImage() {
    if (m_factory != null) {
      long currentTime = (new Date()).getTime();
      if (m_history.size() > 0) {
        long playTime =	m_history.get(0).getTime();
        for(MultimodalAction ma : m_history) {
          long timePassed = (new Date()).getTime() - currentTime;
          playTime += timePassed;
          currentTime += timePassed;
          long nextTime = ma.getTime();
          if(nextTime > playTime) {
            try{
              Thread.sleep(nextTime - playTime);
            }catch(InterruptedException e){
              e.printStackTrace();
            }
          }
          m_factory.play(ma);
        }
      }
    }
  }

  public boolean loadFile(File f) throws IOException{
    return loadFile(f, LOAD);
  }

  public boolean loadFile(URL u) throws IOException{
	return loadFile(u, LOAD);
  }

  public void playFile(File f) throws IOException {
    loadFile(f, PLAY);
  }

  private static int LOAD = 0;
  private static int PLAY = 1;

  @SuppressWarnings("finally")
public boolean loadFile(File f, int method) throws IOException{
    boolean returnVal = true;
	m_sourceFile = f;

    // set the path in the media index
    m_mediaIndex.setDirectory(m_sourceFile.getParentFile());

    DocumentBuilder db;
    Document doc;
    try {
      setIsSortHistoryDelayed(true);
      System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
      "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setIgnoringElementContentWhitespace(true);
      dbf.setValidating(true);
      dbf.setNamespaceAware(true);
      dbf.setAttribute(
          "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
          "http://www.w3.org/2001/XMLSchema");
      dbf.setAttribute(
          "http://java.sun.com/xml/jaxp/properties/schemaSource",
          ResourceFinder.getResourceURL("inkXML/sketch.xsd").toString());

      db = dbf.newDocumentBuilder();
      Validator handler = new Validator();
      db.setErrorHandler(handler);
      doc = db.parse(f);
      if (handler.validationError == true)
        System.err.println("The document had a validation error: "+handler.saxParseException.toString());
      else if (handler.warning == true)
        System.err.println("The document had a validation warning: "+handler.saxParseWarning.toString());

      NodeList nodes = doc.getChildNodes();
      Element sketch = null;
      for (int i = 0; i < nodes.getLength(); i++) {
        Node n = nodes.item(i);
        if (n instanceof Element) {
          Element e = (Element)n;
          if (e.getTagName() == "sketch")
            sketch = e;
        }
      }

      if(sketch == null) {
        System.err.println("error first item should be sketch");
      }

      m_id = UUID.fromString(sketch.getAttribute("id"));
      m_inHiMetricUnits = sketch.getAttribute("units").equalsIgnoreCase("himetric");
      NodeList shapes = sketch.getChildNodes();
      for(int i = 0; i < shapes.getLength(); i++){
        Node n = shapes.item(i);
        // only process elements and not text nodes
        if (n instanceof Element) {
          Element e = (Element)n;
          processElement(e);
        }
      }
      for(int i = 0; i < shapes.getLength(); i++){
        Node n = shapes.item(i);
        // only process elements and not text nodes
        if (n instanceof Element) {
          Element e = (Element)n;
          processSubComponents(e);
        }
      }
      // convert from himetric units if the file is in himetric units and the convert flag is true
      // the default setting is to do the conversion to pixels
      if (m_inHiMetricUnits && m_convertHiMetricToPixel)
        if (!Converter.convertCoordinates(this))
          System.err.println("Error converting history from HiMetric to pixel coordinates.");


      setIsSortHistoryDelayed(false);
      if(method == LOAD) {loadImage();}
      if(method == PLAY) {playImage();}
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
      returnVal = false;
    } catch (SAXException e1) {
      e1.printStackTrace();
      returnVal = false;
    }
    finally {
      setIsSortHistoryDelayed(false);
      return returnVal;
    }
  }

  @SuppressWarnings("finally")
  public boolean loadFile(URL u, int method) throws IOException{
      boolean returnVal = true;
      URLConnection c = u.openConnection();
      c.setDoInput(true);

      // set the path in the media index
      m_mediaIndex.setDirectory(m_sourceFile.getParentFile());

      DocumentBuilder db;
      Document doc;
      try {
        setIsSortHistoryDelayed(true);
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
        "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setValidating(true);
        dbf.setNamespaceAware(true);
        dbf.setAttribute(
            "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
            "http://www.w3.org/2001/XMLSchema");
        dbf.setAttribute(
            "http://java.sun.com/xml/jaxp/properties/schemaSource",
            ResourceFinder.getResourceURL("inkXML/sketch.xsd").toString());

        db = dbf.newDocumentBuilder();
        Validator handler = new Validator();
        db.setErrorHandler(handler);
        doc = db.parse(c.getInputStream());
        if (handler.validationError == true)
          System.err.println("The document had a validation error: "+handler.saxParseException.toString());
        else if (handler.warning == true)
          System.err.println("The document had a validation warning: "+handler.saxParseWarning.toString());

        NodeList nodes = doc.getChildNodes();
        Element sketch = null;
        for (int i = 0; i < nodes.getLength(); i++) {
          Node n = nodes.item(i);
          if (n instanceof Element) {
            Element e = (Element)n;
            if (e.getTagName() == "sketch")
              sketch = e;
          }
        }

        if(sketch == null) {
          System.err.println("error first item should be sketch");
        }

        m_id = UUID.fromString(sketch.getAttribute("id"));
        m_inHiMetricUnits = sketch.getAttribute("units").equalsIgnoreCase("himetric");
        NodeList shapes = sketch.getChildNodes();
        for(int i = 0; i < shapes.getLength(); i++){
          Node n = shapes.item(i);
          // only process elements and not text nodes
          if (n instanceof Element) {
            Element e = (Element)n;
            processElement(e);
          }
        }
        for(int i = 0; i < shapes.getLength(); i++){
          Node n = shapes.item(i);
          // only process elements and not text nodes
          if (n instanceof Element) {
            Element e = (Element)n;
            processSubComponents(e);
          }
        }
        // convert from himetric units if the file is in himetric units and the convert flag is true
        // the default setting is to do the conversion to pixels
        if (m_inHiMetricUnits && m_convertHiMetricToPixel)
          if (!Converter.convertCoordinates(this))
            System.err.println("Error converting history from HiMetric to pixel coordinates.");


        setIsSortHistoryDelayed(false);
        if(method == LOAD) {loadImage();}
        if(method == PLAY) {playImage();}
      } catch (ParserConfigurationException e) {
        e.printStackTrace();
        returnVal = false;
      } catch (SAXException e1) {
        e1.printStackTrace();
        returnVal = false;
      }
      finally {
        setIsSortHistoryDelayed(false);
        return returnVal;
      }
    }

  /**
   * If you are adding lots of stuff and want to delay sorting set this flag.
   * Unsetting it will automatically sort the history
   *
   * @param isIt Is sorting delayed until further notice?
   */
  public void setIsSortHistoryDelayed(boolean isIt) {
    m_isSortDelayed = isIt;
    sortHistory();
  }

  private void sortHistory() {
    if (!m_isSortDelayed) {
      Collections.sort(m_history);
    }
  }

  /**
   * Get the start time of the history.
   * This is not a thread safe method.
   * @return the start time of the history or -1 if the history is empty
   */
  public long getStartTime() {
    long startTime = -1;
    for (MultimodalAction action : m_history) {
      long start = action.getStartTime();
      if (startTime == -1)
        startTime = start;
      else
        startTime = Math.min(startTime, start);
    }
    return startTime;
  }

  /**
   * Get the end time of the history. Note that this function
   * sorts the history if sorting is delayed. The sort delay setting
   * is not altered.
   * This is not a thread safe method.
   * @return the start time of the history, or -1 if the history is empty
   */
  public long getEndTime() {
    if (m_isSortDelayed) {
      Collections.sort(m_history);
    }
    if (m_history.size() > 0)
      return getAction(m_history.size()-1).getTime();
    else
      return -1;
  }

  public void write(File f) throws IOException {
    Document d = createXMLDocument();
    OutputFormat format = new OutputFormat(d);
    format.setIndenting(true);
    format.setIndent(2);
    format.setLineWidth(0);

    // be certain to overrite the file, rather than append to it!!!
    FileWriter fileWriter = new FileWriter(f, false);
    XMLSerializer serializer = new XMLSerializer(fileWriter, format);
    serializer.serialize(d);
  }

  public Document getXML() {
	  return createXMLDocument();
  }

  /**
   * Adjust the history by removing the incorrectly labeled Phase.
   * You should NEVER remove anything from the history. This is a
   * special case where a speech Phrase has been incorrectly labeled.
   * @param phrase the incorrectly labeled phrase to remove from the history
   */
  public void repairSpeechPhrase(Phrase phrase) {
    // items to remove
    ArrayList<Speech> removeItems = new ArrayList<Speech>();
    removeItems.add(phrase);
    removeItems.addAll(phrase.getWords());

    // remove from history
    m_history.removeAll(removeItems);
    // shouldn't need to sort on removal

    // remove from hash
    for (Speech speech : removeItems) {
      m_actionHash.remove(speech);
    }

    // update undo pointer
    m_undoPointer = m_history.size() - 1;

    // fix factory
    if (m_factory != null)
      m_factory.repairSpeech(phrase);
  }

  public static void main(String[] args) {
    MultimodalActionHistory l = new MultimodalActionHistory();
    File f = new File("c:/foo.xml");
    try {
      l.write(f);
    }
    catch (IOException e) {
      e.printStackTrace();
      System.out.println("MultimodalActionHistory xml file output IO error");
    }

  }

  /**
   * Try the source file name, then the domain name and the study name, finally
   * just dump the UUID if we have nothing more useful.
   */
  @Override
  public String toString()
  {
   if (m_sourceFile != null) {
    return m_sourceFile.getPath();
   }
   if (!"none".equals(m_studyName) || !"unknown".equals(m_domainName)) {
     return m_domainName + ":" + m_studyName;
   }
   return m_id.toString();
  }

  /**
   * Validator for Schema reading
   */
  private class Validator extends DefaultHandler {
    public boolean validationError = false;
    public boolean warning = false;

    public SAXParseException saxParseException = null;
    public SAXParseException saxParseWarning = null;

    @SuppressWarnings("unused")
    @Override
    public void error(SAXParseException exception) throws SAXException {
      validationError = true;
      saxParseException = exception;
      System.err.println("Error: "+exception.toString());
    }

    @SuppressWarnings("unused")
    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
      validationError = true;
      saxParseException = exception;
      System.err.println("Fatal: "+exception.toString());
    }

    @SuppressWarnings("unused")
    @Override
    public void warning(SAXParseException exception) throws SAXException {
      warning = true;
      saxParseWarning = exception;
      System.err.println("Warning: "+exception.toString());
    }
  }

  /**
   *
   */
  public int size() {
    return m_history.size();

  }
}
