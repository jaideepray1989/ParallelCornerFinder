/*
 * $Id: MultimodalActionFactory.java,v 1.8 2007-03-07 22:48:14 hammond Exp $
 * @author Tracy Hammond, hammond@csail.mit.edu
 * Created on May 18, 2005 at 11:05:22 PM
 * Copyright MIT 2005. All Rights Reserved
 */
package cornerfinders.toolkit;

import java.awt.Color;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.mit.sketch.language.S_UMLLine;
import edu.mit.sketch.language.ShapeCollection;
import edu.mit.sketch.language.edits.DeleteEdit;
import edu.mit.sketch.language.edits.EditGesture;
import edu.mit.sketch.language.edits.EraseEdit;
import edu.mit.sketch.language.edits.MoveEdit;
import edu.mit.sketch.language.edits.RedoAction;
import edu.mit.sketch.language.edits.UndoAction;
import edu.mit.sketch.language.parser.DomainList;
import edu.mit.sketch.language.parser.ShapeDef;
import edu.mit.sketch.language.recognizer.ShapeRecognizer;
import edu.mit.sketch.language.shapes.DrawnShape;
import edu.mit.sketch.language.shapes.MultimodalAction;
import edu.mit.sketch.language.shapes.RRectangle;
import edu.mit.sketch.language.shapes.Stroke;
import edu.mit.sketch.language.speech.Phrase;
import edu.mit.sketch.language.speech.SpeechTranscript;
import edu.mit.sketch.language.speech.Speech;
import edu.mit.sketch.language.speech.Word;
import edu.tamu.hammond.sketch.shapes.TEllipse;
import edu.tamu.hammond.sketch.shapes.TPoint;
import edu.tamu.hammond.sketch.shapes.TText;

/**
 * MultimodalActionFactory:
 * An abstract class for interacting with the MultimodalActionHistory
 * There are two distinct ways to play back strokes in the history.
 * The first way is to play back the actions in the order that they are in 
 * the history (sorted by end time); this is executing the actions.
 * The second way is to play back the actions as they occured (by start time);
 * this is animating the actions. Mixing the two methods of playback is not
 * recommended. 
 * This difference is really only important for sketches with multiple authors.
 * Otherwise only one stroke can be drawn at a time and the start and end time lists
 * would be the same.
 */
public abstract class MultimodalActionFactory {
  protected ShapeRecognizer m_recognizer = null;
  protected DomainList m_domainList = null;
  
  // for animation:
  protected List<MultimodalAction> m_startTimeList = Collections.synchronizedList(new LinkedList<MultimodalAction>());
  protected Comparator<MultimodalAction> m_startTimeComparator = new StartTimeComparator();
  private int m_animatePosition = 0; // keep track of where we are in the m_startTimeList
  
  // for animated playback:
  private AnimateTask m_animateTask = null;
  private ArrayList<InProgressAction> m_partialAnimate = new ArrayList<InProgressAction>();

  // used for display:
  protected ShapeCollection m_viewableShapes = new ShapeCollection();
  protected SpeechTranscript m_speech = new SpeechTranscript();
  //set this to true if you want text and circle indicators to be shown for strokes
  protected boolean m_showIndicator = false; 
  
  // info about the sketchers
  protected SketcherIndex m_sketcherIndex = null;
  
  protected ArrayList<Stroke> m_inProgressStrokes = new ArrayList<Stroke>(); // used for strokes as they are added to the history 
  protected ArrayList<TPoint> m_pointsInStrokes = new ArrayList<TPoint>();
  private ArrayList<AnimationListener> m_listeners = new ArrayList<AnimationListener>();
    
  // optional delay for sorting
  private boolean m_isSortDelayed = false;
  
  public abstract void load (MultimodalAction ma);
  public abstract void undo (MultimodalAction ma);
  public abstract void redo (MultimodalAction ma);
  public abstract void play (MultimodalAction ma);
  
  /**
   * Compare the START times for the multimodal actions. If they are the same, 
   * then return the result of comparing the UUIDs. 
   */
  protected class StartTimeComparator implements Comparator<MultimodalAction> {
    
    public int compare(MultimodalAction ma1, MultimodalAction ma2) {
      // first just use the start times
      Long t1 = new Long(ma1.getStartTime());
      Long t2 = new Long(ma2.getStartTime());
      int timeCompare = t1.compareTo(t2);
      if (timeCompare != 0)
        return timeCompare;

      // if the start times are equal, then
      // if they are both shapes, check for a subcomponent relationship
      if ((ma1 instanceof DrawnShape) && (ma2 instanceof DrawnShape)) {
        for(DrawnShape child : ((DrawnShape)ma1).getComponents()){
          if(child.equals(ma2)){
            //then ma2 is a component of ma1 and should have
            //a time stamp of LESS than this - 
            // or rather ma1 is > than ma2
            return -1;
          }
        }
        for(DrawnShape child : ((DrawnShape)ma1).getAliases()){
          if(child.equals(ma2)){
            return -1;
          }
        }
        for(DrawnShape child : ((DrawnShape)ma2).getComponents()){
          if(child.equals(ma1)){
            return 1;
          }
        }
        for(DrawnShape child : ((DrawnShape)ma2).getAliases()){
          if(child.equals(ma1)){
            return 1;
          }
        }
      }
      else if ((ma1 instanceof Speech) && (ma2 instanceof Speech)) {
        if ((ma1 instanceof Phrase) && (ma2 instanceof Word)) {
          for (Word w: ((Phrase)ma1).getWords()) {
            if (w.equals(ma2))
            // word is a component of this and should be before this in an ordering
            // or rather this is > than word
            return 1;
          }
        }
        
        if ((ma2 instanceof Phrase) && (ma1 instanceof Word)) {
          for (Word w: ((Phrase)ma2).getWords()) {
            if (w.equals(ma1)) 
              // reverse of the above situation
              return -1;
          }
        }
      }
      
      // the times are equal and there is not a subcomponent relationship
      // so use the ids for the tiebreaker
      return ma1.getId().compareTo(ma2.getId());
    }
  }
  
  /**
   * Set the recognizer for the factory.
   * @param recognizer The recognizer the factory should use
   */
  public void setRecognizer(ShapeRecognizer recognizer) {
    m_recognizer = recognizer;
  }
  
  /**
   * Get the recognizer for the factory.
   * @return The factory's recognizer (can be null)
   */
  public ShapeRecognizer getRecognizer() {
    return m_recognizer;
  }
  
  /**
   * Set the domain list for the factory.
   * @param domainList The domain list the factory should use
   */
  public void setDomainList(DomainList domainList) {
   m_domainList = domainList;
  }

  /**
   * Get the list of actions sorted by start time
   * @return the list of actions sorted by start time
   */
  public List<MultimodalAction> getStartOrderedActions() {
    return new ArrayList<MultimodalAction>(m_startTimeList);
  }
  
  /**
   * Get the domain list for the factory.
   * @return The factory's domain list (can be null)
   */
  public DomainList getDomainList() {
   return m_domainList;
  }
  
 /**
   * @return Returns the viewable shapes.
   */
  public ShapeCollection getViewableShapes() {
    return m_viewableShapes;
  }
  
  /**
   * Get the speech that have been spoken
   * @return Returns the spoken speech
   */
  public SpeechTranscript getSpeech() {
    return m_speech;
  }
  
  /**
   * Set the sketcher index for this factory
   * @param sketcherIndex the sketcher index to use for this factory
   */
  public void setSketcherIndex(SketcherIndex sketcherIndex) {
    m_sketcherIndex = sketcherIndex;
  }
  
/**
   * Reset the drawing to empty
   */
  protected void resetToStart() {
    // reset pointer
    m_animatePosition = 0;
    m_partialAnimate.clear();
    m_viewableShapes.reset();
    m_speech.reset();
  }
  
  /*
   * Jump to the end
   */
  //protected void jumpToEnd() {
  //  animateJumpTo(m_startTimeList.size());
  //}
  
  // check the list of strokes that aren't completed to see if any have been completed
  private void processInProgressStrokes() {
    if ((m_recognizer != null) && (m_inProgressStrokes.size() > 0)) {
      ArrayList<Stroke> completedStrokes = new ArrayList<Stroke>();
      for (Stroke stroke: m_inProgressStrokes) {
        if (stroke.isComplete())
          completedStrokes.add(stroke);
      }
      // make sure to remove the strokes BEFORE sending the strokes to the recognizer
      // otherwise the recognizer will get the strokes multiple times
      m_inProgressStrokes.removeAll(completedStrokes);
      for (Stroke stroke: completedStrokes)
        m_recognizer.addShape(stroke);
    }
  }

  private boolean isPointInStroke(DrawnShape shape) {
    if ((shape instanceof TPoint) && (m_pointsInStrokes.contains(shape)))
      return true;
    return false;
  }

  private boolean isIgnorePoint(DrawnShape shape) {
    if (shape instanceof TPoint) {
      //TODO come up with a better way to check this. We could create a point manually that wouldn't have subcomponents
      List<DrawnShape> components = shape.getComponents();
      for (DrawnShape component : components) {
        if (component instanceof Stroke)
          return false; // it's a point we want
      }
      return true; // ignore it
    }
    return false; // not a point
  }

  /* ******************* initialize **************************************************/
  
  /**
   * If you are adding lots of stuff and want to delay sorting set this flag.
   * The factory has a list of actions sorted by start time, which needs to be sorted.
   * Unsetting it will automatically sort the history
   * 
   * @param isIt Is sorting delayed until further notice?
   */
  public void setIsSortFactoryDelayed(boolean isIt) {
    m_isSortDelayed = isIt;
    sortFactory();
  }
  
  private void sortFactory() {
    if (!m_isSortDelayed) {
      Collections.sort(m_startTimeList, m_startTimeComparator);
    }
  }
  
  /**
   * Initialize the MultimodalAction by updating the appropriate data structures.
   * Does NOT execute the action (does NOT add it to the ShapeCollection).
   * @param ma the MultimodalAction to initialize
   */
  private void initialize(MultimodalAction ma) {
    // this method gets all of the multimodal actions and does:
    
    // - store all of the actions in a list (minus points in strokes)
    if (ma instanceof Stroke) {
      Stroke s = (Stroke)ma;
      // add points to the list of points in strokes and remove any points that are already in the list of all actions
      List<TPoint> points = ((Stroke)ma).getPoints();
      m_pointsInStrokes.addAll(points);
      // if the stroke is incomplete, add its points to the list of points in a stroke. 
      if (!s.isComplete())
        m_inProgressStrokes.add(s);
    }
    // check to see if any new points are in any incomplete strokes or if any incompete strokes are finished
    if (ma instanceof TPoint) {
      TPoint pt = (TPoint)ma;
      for (Stroke s : m_inProgressStrokes) {
        if (s.containsComponent(pt))
          m_pointsInStrokes.add(pt);
      }
      processInProgressStrokes();
    }
    
    // - create a sorted list of the actions by START time (to be used in playback) use the MultimodalAction.getStartTime method to get the start time
    m_startTimeList.add(ma);

    sortFactory();
  }

  /* ****************** shared execute and animate code *****************************/
  
  /**
   * Update the animation pointer to a particular action
   * @param action the action to move the pointer to
   */
  private void updatePointer(MultimodalAction action) {
    if ((getAnimatePosition() < m_startTimeList.size()) &&
        (m_startTimeList.get(getAnimatePosition()).equals(action)))
      setAnimatePosition(getAnimatePosition()+1);
    else {
      setAnimatePosition(m_startTimeList.indexOf(action)+1);
    }
  }
  
  /**
   * Execute or animate the specified DrawnShape. This code is here so that it is not duplicated
   * in animate and execute. Eventually this code should be much simplier when some of the special
   * cases are fixed.
   * @param shape the shape to execute or animate
   * @param animate if true, animate it (update partial playback), if false execute it (don't update partial playback)
   * @param toTime time to animate the shape to, ignored if animate is false
   */
  private synchronized void doShape(DrawnShape shape, boolean animate, long toTime) {
    // first check for points that are in a stroke
    if (isPointInStroke(shape))
      return;
    // then check for points that are not in a stroke, but aren't points that we want to send 
    // to the recognizer. This could be a point that is in a stroke that we don't have yet. 
    if (isIgnorePoint(shape))
      return;
    
    // first if there is a domain list, adjust the shape accordingly. 
    
    if(m_domainList != null){
      ShapeDef sd = m_domainList.getRecogShapeDef(shape.getRecogType());
      if(sd == null || shape.getRecogType().equals("")){
        sd = m_domainList.getRecogShapeDef(shape.getType());
      }
      if(sd != null){
        sd.setDisplay(shape);
        shape.setShapeDef(sd);
      }        
    }
    
    // adjust the color according to the properties in the shape
    String color = shape.getProperty("color");
    if(color != null){
      shape.setColor(new Color(Integer.valueOf(color),true));
    }      
    
    // now that the display of the shape is adjusted, add the shape to the viewable shapes
    //TODO: get rid of the special case for strokes by handling display in the domain lists
    // this doesn't display strokes that shouldn't be displayed
    //TODO: Make edit strokes a separate type/class/subtype of DrawnShape (something similar to EraseStroke) 
    //TODO: so that it's drawing properties are handled by the domain list so that it could be displayed as a 
    //TODO: certain color, or not displayed at all as specified in the domain list
    //TODO: Also strokes should be handled by the domain list
    if (shape instanceof Stroke) {
      //TODO erase strokes should draw them selves -- eg they lay white ink?
      if (animate) {
        // only do this for animation:
        InProgressAction partialStroke = new InProgressAction(shape);
        partialStroke.goToTime(toTime);
        m_partialAnimate.add(partialStroke);
      }
      
      // only add the stroke to the visible shapes if it lays ink
      if (((Stroke)shape).isLaysInk())
        m_viewableShapes.add(shape);
    }
    // shapes other than strokes should just be added
    if (!(shape instanceof Stroke))
      m_viewableShapes.add(shape);

    // now we want to send the shape to the recognizer, if there is one. 
    if (m_recognizer != null) {
      // check to see if any of the in-progress strokes are finished
      processInProgressStrokes();
      
      //TODO: scribble should not be a special type -- this should be handled in the recognizer....
      if(shape.isOfType("Scribble")){
        ArrayList<DrawnShape> toDelete = new ArrayList<DrawnShape>(); 
        for(DrawnShape viewable : m_viewableShapes.getShapes()){
          if(viewable.equals(shape)){continue;}
          if(viewable.equals(shape.getComponent(0))){continue;}
          if(S_UMLLine.isIntersectingStrokes(shape, viewable)) {
            toDelete.add(viewable);
          }
        }
        if (toDelete.size() > 0) {
          // have some shapes to delete
          // TODO: Add to history???
          EditGesture eg = new DeleteEdit(shape, toDelete);
          m_recognizer.addEdit(eg);
          m_viewableShapes.removeShape(shape);
          m_viewableShapes.removeShapes(toDelete);
          return;
        }
      }
      
      // add the shape to the recognizer
      // if it is a stroke, make sure it is completed
      if (!(shape instanceof Stroke) || ((Stroke)shape).isComplete()) {
        m_recognizer.addShape(shape);
      }
    }

  }
  
  /* ****************************** execute *****************************************/
  
  /**
   * Initialize a MulitmodalAction if necessary, execute it, and then update the 
   * animation position.
   * @param ma the action to execute
   */
  protected void executeAction(MultimodalAction ma) {
    if (!m_startTimeList.contains(ma))
      initialize(ma);
    // Dispatch to appropriate handler
    if (ma instanceof DrawnShape) {
      executeShape((DrawnShape)ma);
    }
    if (ma instanceof EditGesture) {
      executeEdit((EditGesture)ma);
    }
    if (ma instanceof Speech) {
      executeSpeech((Speech)ma);
    }
    
    updatePointer(ma);
  }

  protected void executeShape(DrawnShape shape) {
    doShape(shape, false, -1);
  }
  
  protected void executeEdit(EditGesture e) {
    if (e instanceof MoveEdit) {
      MoveEdit me = (MoveEdit)e;
      MultimodalAction trigger = me.getTrigger();
      if (trigger instanceof Stroke) {
        Stroke path = (Stroke)trigger;
        if (path.isComplete()) {
          TPoint start = path.getFirstPoint();
          TPoint end = path.getLastPoint();
          double shiftx = end.getProp("x") - start.getProp("x");
          double shifty = end.getProp("y") - start.getProp("y");
          // move the args
          for (DrawnShape shape: me.getShapes()) {
            shape.translate(shiftx, shifty);
          }
        }
      }
    }
    if ((e instanceof DeleteEdit) || (e instanceof EraseEdit)) {
      // remove the shapes being ersased/deleted from the viewable shapes
      m_viewableShapes.removeShapes(e.getShapes());
    }
    if (e instanceof UndoAction) {
      // TODO
    }
    if (e instanceof RedoAction) {
      // TODO
    }

  }
  
  protected void executeSpeech(Speech s) {
    m_speech.add(s);
  }
  
  /* ****************************** animate *****************************************/
  
  /**
   * Get the MultimodalAction at the specified index from the list of actions sorted by start time.
   * @param index Zero-based index of the MultimodalAction to get
   * @return the MultimodalAction at index index (or null if the index is out of range).
   */
  protected MultimodalAction getAnimateAction(int index) {
    if ((index < 0) || (index >= m_startTimeList.size()))
      return null;
    else
      return m_startTimeList.get(index);
  }
 
  /**
   * Get the MultimodalAction at the current animate position. Returns null if at the end of the list.
   * @return the next MultimodalAction to animate. null if no more actions to animate. 
   */
  protected MultimodalAction getAnimateAction() {
    int index = getAnimatePosition();
    if ((index < 0) || (index >= m_startTimeList.size()))
      return null; 
    else
      return m_startTimeList.get(getAnimatePosition());
  }
 
  /**
   * Get the index of the next action to animate.  If the value
   * equals the number of actions then all actions have been animated.
   * @return the index of the next action to animate.
   */
  public int getAnimatePosition() {
    return m_animatePosition;
  }

  /**
   * Set the index of the next action to animate.  If the newPosition is
   * greater than the number of actions, set the position to the number of actions
   * @param newPosition the next action to animate
   */
  protected void setAnimatePosition(int newPosition) {
    m_animatePosition = Math.min(newPosition, m_startTimeList.size());
    fireAnimationPosition(m_animatePosition);
  }
  
  /**
   * Move the animation position to the end of the list. Does NOT
   * affect anything except the position (does not call update methods).
   */
  public void setAnimationToEnd() {
    m_animatePosition = m_startTimeList.size();
  }
  
  
  /**
   * Jumps to the start time of the specified index in the animation stream
   * @param index the index to go to
   */
  public synchronized void animateJumpTo(int index) {
    /*
    if (index != getAnimatePosition()) {
      MultimodalAction action = getAnimateAction(index);
      if (action != null) {
        long startTime = action.getStartTime();
        animateJumpTo(startTime);
      }
    }*/
    if (index != getAnimatePosition()) {
      if (index < getAnimatePosition()) {
        // rewind! 
        rewindCollectionToTime(index);        
      }
      else {
        // go forward
        while (getAnimatePosition() < index) {
          long startTime = getAnimateAction().getStartTime();
          animateNext();
          updatePartialAnimates(startTime);
        }
      }
    }
  }
  
  /**
   * Jump to the specified time in the animation stream
   * @param toTime the time to jump to
   **/
  public void animateJumpToTime(long toTime) {
    // find the index to jump to
    int jumpIndex = findIndexForTime(toTime);
    if (jumpIndex == -1)
      resetToStart();
    else
      animateJumpTo(jumpIndex);
  }
  
  /**
   * Find the index corresponding to the time toTime so that 
   * the animation can go to this point.
   * @param toTime the time to find corresponding index for
   * @return the index to animate to, returns -1 if toTime is before the beginning of the first action
   */
  private int findIndexForTime(long toTime) {
    // the key is to find the index that has a start time
    // equal to or less than toTime and a following index that
    // has a start time greater than toTime
    int index = -1;
    boolean found = false;
    while (index < (m_startTimeList.size()-1)) {
      long actionStartTime = m_startTimeList.get(index+1).getStartTime();
      if (actionStartTime > toTime) {
        found = true;
        break;
      }
      index++;
    }
    if (!found)
      return m_startTimeList.size();
    else
      return index;
  }
  
  private void rewindCollectionToTime(int toIndex) {
    // first calculate the time to go to
    long toTime = getAnimateAction(toIndex).getStartTime();
    
    // first rewind the partially completed actions
    updatePartialAnimates(toTime);
    
    // the animate position is the next thing to animate, so we want to back up one
    for (int index = (getAnimatePosition()-1); index >= 0; index--) {
      MultimodalAction action = getAnimateAction(index);
      
      // first check to see if the action should be completely undone
      if (index >= toIndex) {
        undoAction(action);
        // move the animation pointer to this location
        setAnimatePosition(index);
      }
      // otherwise the action is AT LEAST started at that time, so we 
      // need to check to see if its ending time is after the toTime
      else if (action.getTime() > toTime) {
        if ((action instanceof DrawnShape) &&
            (m_viewableShapes.containsShape((DrawnShape)action)))
          m_viewableShapes.removeShape((DrawnShape)action);
        if (action instanceof Speech)
          m_speech.unspeak((Speech)action);
        animateAction(action, toTime, false);
      }
      // if neither of these cases are true, then we should just leave it alone.
    }
  }

  /*
  private void rewindCollectionToTime(long toTime) {
    // first rewind the partially completed actions
    updatePartialAnimates(toTime);
    
    // the animate position is the next thing to animate, so we want to back up one
    for (int index = (getAnimatePosition()-1); index >= 0; index--) {
      MultimodalAction action = getAnimateAction(index);
      
      // first check to see if the action should be completely undone
      if (action.getStartTime() > toTime) {
        undoAction(action);
        // move the animation pointer to this location
        setAnimatePosition(index);
      }
      // otherwise the action is AT LEAST started at that time, so we 
      // need to check to see if its ending time is after the toTime
      else if (action.getTime() > toTime) {
        animateAction(action, toTime, false);
      }
      // if neither of these cases are true, then we should just leave it alone.
    }
  }
  */
  
  /**
   * Update all the partial playbacks to the new end time
   * Returns a boolean to indicate if an update was performed
   * @param toTime update playback to this time
   * @return returns a boolean indicating if an update happened
   */
  private boolean updatePartialAnimates(long toTime) {
    // keep track of the finished actions to remove
    ArrayList<InProgressAction> finishedActions = new ArrayList<InProgressAction>();
    boolean changed = false;
    
    for (InProgressAction action : m_partialAnimate) {
      boolean updated = action.goToTime(toTime);
      if (updated)
        changed = true;
      
      // done can be at the end or start depending on the direction
      if (((action.getStartTime() <= toTime) && (action.isDone())) || 
          ((action.getStartTime() > toTime) && (action.atStart()))) { 
        action.cleanUp();
        finishedActions.add(action);
      }
    }
    
    m_partialAnimate.removeAll(finishedActions);
    return changed;
  }

  /**
   * Execture the next MultimodalAction in the list sorted by start time 
   */
  public void animateNext() {
    int pos = getAnimatePosition();
    if (pos < m_startTimeList.size()) {
      animateAction(getAnimateAction());
      //setAnimatePosition(pos+1);
    }
  }
  
  /**
   * Exectue the next MultimodalAction in the list sorted by start time 
   * to the specified time
   * @param toTime the time to animate until
   */
  public void animateNext(long toTime) {
    int pos = getAnimatePosition();
    if (pos < m_startTimeList.size()) {
      animateAction(getAnimateAction(), toTime);
      //setAnimatePosition(pos+1);
    }
  }
  
  /**
   * Animate the action to its startTime
   * @param ma the action to animate
   */
  protected void animateAction(MultimodalAction ma) {
    animateAction(ma, ma.getStartTime());
  }

  /**
   * Animate the action to the specified time
   * @param ma the action to animate
   * @param toTime the time to animate it to
   */
  protected void animateAction(MultimodalAction ma, long toTime) {
    animateAction(ma, toTime, true);
  }

  /**
   * Animate the action to the specified time
   * @param ma the action to animate
   * @param toTime the time to animate it to
   * @param updatePosition if true, update the animation position
   */
  protected void animateAction(MultimodalAction ma, long toTime, boolean updatePosition) {
    if (!m_startTimeList.contains(ma))
      initialize(ma);
    // Dispatch to appropriate handler
    if (ma instanceof DrawnShape) {
      animateShape((DrawnShape)ma, toTime);
    }
    if (ma instanceof EditGesture) {
      animateEdit((EditGesture)ma, toTime);
    }
    if (ma instanceof Speech) {
      animateSpeech((Speech)ma, toTime);
    }
    
    if (updatePosition) 
      updatePointer(ma);
  }

  /**
   * Animate the DrawnShape to its start time
   * @param shape the shape to animate
   */
  protected void animateShape(DrawnShape shape) {
    animateShape(shape, shape.getStartTime());
  }  
  
  /**
   * Animate the DrawnShape to the specified time
   * @param shape the shape to animate
   * @param toTime the time to animate it to
   */
  protected void animateShape(DrawnShape shape, long toTime) {
    doShape(shape, true, toTime);
  }
  
  /**
   * Animate the EditGesture to the start time of the edit.
   * @param e the edit 
   */
  protected void animateEdit(EditGesture e) {
    animateEdit(e, e.getStartTime());
  }
  
  /**
   * Animate the EditGesture to the specified time.
   * @param e the edit 
   * @param toTime the time to animate to
   */
  protected void animateEdit(EditGesture e, long toTime) {
    if (e instanceof MoveEdit) {
      InProgressAction partialMove = new InProgressAction(e);
      m_partialAnimate.add(partialMove);
      partialMove.goToTime(toTime);
    }
    if ((e instanceof DeleteEdit) || (e instanceof EraseEdit)) {
      // remove the shapes being ersased/deleted from the viewable shapes
      m_viewableShapes.removeShapes(e.getShapes());
    }
    if (e instanceof RedoAction) {
      //TODO
    }
    if (e instanceof UndoAction) {
      //TODO
    }
    
  }
  
  /** 
   * Animate the Speech to the specified time
   * @param s the speech
   * @param toTime the time to animate to
   */
  protected void animateSpeech(Speech s, long toTime) {
    InProgressAction partialSpeech = new InProgressAction(s);
    partialSpeech.goToTime(toTime);
    m_partialAnimate.add(partialSpeech);
    m_speech.speak(s);
  }
  

 /* ********************************** undo and redo *************************************/  
// TODO I think undo and redo are a bit wrong -- namely they don't add the UndoAction or 
  // RedoAction to the startTime list so this will cause problems with animztion and execution
  
  protected void undoAction(MultimodalAction a) {
    if (a instanceof DrawnShape)
      undoShape((DrawnShape)a);
    else if (a instanceof EditGesture) 
      undoEdit((EditGesture)a);
    else if (a instanceof Speech)
      undoSpeech((Speech)a);
  }
  
  protected void undoEdit(EditGesture e) {
    if (e instanceof MoveEdit) {
      MoveEdit me = (MoveEdit)e;
      MultimodalAction trigger = me.getTrigger();
      if (trigger instanceof Stroke) {
        Stroke path = (Stroke)trigger;
        TPoint start = path.getFirstPoint();
        TPoint end = path.getLastPoint();
        double shiftx = start.getProp("x") - end.getProp("x");
        double shifty = start.getProp("y") - end.getProp("y");
        // move the args
        for (DrawnShape shape: me.getShapes()) {
          shape.translate(shiftx, shifty);
        }
      }
    }
    else if ((e instanceof EraseEdit) || (e instanceof DeleteEdit)) {
      // TODO I think this should replace...
      for (DrawnShape shape : e.getShapes()) {
        // TODO not sure if this should be animate shape or not!
        animateShape(shape, shape.getTime());
      }
      // this:
      //m_viewableShapes.add(e.getShapes());
    }
  }
  
  protected void undoShape(DrawnShape shape) {
    m_viewableShapes.removeShape(shape);
    m_recognizer.removeShape(shape);
    
    //TODO Undo shape/fille in recognizer code
    //    if(!ds.getSubShapesFact().equals("")){
    //      m_recognizer.retractFact(shape);}   
    
    if (!(shape instanceof Stroke)) {
      //find latest subcomponent - don't add it back
      DrawnShape latest = null;
      for(DrawnShape s : shape.getComponents()){
        if(latest == null || latest.getTime() < s.getTime()){
          latest = s;
        }
      }
      for(DrawnShape s : shape.getComponents()){
        if(s == latest){continue;}
        if(s.getProperty("color") != null){
          s.setColor(new Color(Integer.valueOf(s.getProperty("color"))));
        }
        m_viewableShapes.addShape(s);
        m_recognizer.addShape(s);
        //TODO fix recognizer code
        //if(m_recognizer == null){
        //  System.out.println("recognzer is null");
        //}
        //   m_recognizer.assertFact(s.getSubShapesFact());
      }        
    }
  }
  
  protected void undoSpeech(Speech speech) {
    speech.setPercentSpoken(0.0);
    m_speech.unspeak(speech);
  }
  
  protected void redoShape(DrawnShape shape) {
    if(shape.getProperty("color") != null){
      shape.setColor(new Color(Integer.valueOf(shape.getProperty("color"))));
    }
    m_viewableShapes.add(shape);
    m_recognizer.addShape(shape);
    // TODO add appropriate recognizer code here
    //     if(!s.getSubShapesFact().equals("")){
    //       m_recognizer.assertFact(s.getSubShapesFact());}
  }
  
  /****************************** repair speech code *************************/
  
  /**
   * Adjust the history by removing the incorrectly labeled Phase.
   * You should NEVER remove anything from the history. This is a 
   * special case where a speech Phrase has been incorrectly labeled.
   * @param phrase the incorrectly labeled phrase to remove from the history
   */
  protected void repairSpeech(Phrase phrase) {
    m_speech.remove(phrase);
  }
  

  /* ******************************** Animation Code ************************ */
  
  private class AnimateTask extends TimerTask {
    private int m_animateStartIndex;
    private int m_animateEndIndex;
    private long m_animateEndTime;
    private long m_timeDifference;
    private long m_lastAnimationTime;
    private boolean m_timeSet = false;
    
    /**
     * Animate the actions in the list of actions sorted by start time from startIndex to endIndex.
     * The index is zero based.
     * This will animate the specific actions between the indexes, however, some animation may not be 
     * completed. 
     * @param startIndex the starting index in the list of actions sorted by start time
     * @param endIndex the ending index in the list of actions sorted by start time
     */
    public AnimateTask(int startIndex, int endIndex) {
      m_animateStartIndex = startIndex;
      m_animateEndIndex = endIndex;
      if (m_animateEndIndex >= m_startTimeList.size()) {
        // play back everything
        m_animateEndTime = Long.MAX_VALUE;
      }
      else
        m_animateEndTime = getAnimateAction(m_animateEndIndex).getStartTime();
    }
    
    public void doRun() 
    {
      // if we are at the end AND nothing is playing back then stop the timer and stop updating
      if (getAnimatePosition() >= m_animateEndIndex) {
        stopAnimation();
        return;
      }

      // updating is handled within executeNext, which updates the ShapeCollection appropriately
      long currentTime = System.currentTimeMillis();
      m_timeDifference = updateTimeDifference(currentTime, m_timeDifference, m_lastAnimationTime, getAnimateAction().getStartTime());
      long toTime = currentTime + m_timeDifference;      
      while (getAnimatePosition() < m_animateEndIndex) {
        if (getAnimateAction().getStartTime() < toTime) {
          animateNext();
          m_lastAnimationTime = toTime;
        }
        else {
          break;
        }
      }
      
      // don't animate past the end animate time
      boolean updated = updatePartialAnimates(Math.min(toTime, m_animateEndTime));
      if (updated)
        m_lastAnimationTime = toTime;
    }
    
    @Override
    public void run() {
      // set up the time difference between the animation time and the system time
      if (!m_timeSet) {
        m_timeDifference = getAnimateAction(m_animateStartIndex).getStartTime() - System.currentTimeMillis();
        m_timeSet = true;
      }
      if (!EventQueue.isDispatchThread()) {
        EventQueue.invokeLater(this);
      } 
      else {
        doRun();
      }
    }
  }
  
  /**
   * Update the time difference between the animation and system clock 
   * This is used to jump ahead in the animation.
   * @param currentTime The current system time
   * @param lastAnimationTime The last time something changed
   * @param nextStartTime The time the next action is scheduled to start
   * @return the new time difference
   */
  protected long updateTimeDifference(long currentTime, long timeDifference, long lastAnimationTime, long nextStartTime) {
    return timeDifference;
  }
  
  public void playAnimation(int startIndex, int endIndex) {
    if( m_animateTask != null ) {
      m_animateTask.cancel();
    }
    animateJumpTo(startIndex);
    Timer timer = new Timer();
    m_animateTask = new AnimateTask(startIndex, endIndex);
    timer.scheduleAtFixedRate(m_animateTask,0,100);    
  }
  
  public void stopAnimation() {
    // stop the animation playback
    if( m_animateTask != null ) {
      m_animateTask.cancel();
      m_animateTask = null;
      fireAnimationDone();
    }
  }
  
  protected class InProgressAction {
    private MultimodalAction m_action;
    private int m_index = 0; // index of last part completed (1 .. num parts based)
    
    // indicators
    private DrawnShape m_indicatorShape = null;
    private TText m_indicatorText = null;
    
    public InProgressAction(MultimodalAction action) {
      m_action = action;
      
      // set the stroke to only paint to 0
      if (m_action instanceof Stroke) {
        Stroke s = (Stroke)m_action;
        s.paintUntilPoint(0);
        
        // indicator code:
        if (m_showIndicator) {
          TPoint firstPoint = s.getPoint(0);
          if (s.isLaysInk())
            m_indicatorShape = new TEllipse(firstPoint.getProp("x")-5, firstPoint.getProp("y")-5, 10, 10);
          else
            m_indicatorShape = new RRectangle(firstPoint.getProp("x")-8, firstPoint.getProp("y")-8, 16, 16);
          if (m_sketcherIndex != null) {
            String id = s.getProperty("author");
            if (id != null) {
              m_indicatorShape.setColor(m_sketcherIndex.getColorForId(id));
              String label = m_sketcherIndex.getSketcherNickname(id);
              
              if (label == null)
                label = id;
              m_indicatorText = new TText(new TPoint(firstPoint.getProp("x"), firstPoint.getProp("y")-15, firstPoint.getTime()), label);
              m_indicatorText.setColor(m_sketcherIndex.getColorForId(id));
              m_viewableShapes.addShape(m_indicatorText);
            }
            else {
              m_indicatorText = new TText(new TPoint(firstPoint.getProp("x"), firstPoint.getProp("y")-15, firstPoint.getTime()), "Unknown");
              m_viewableShapes.addShape(m_indicatorText);
            }
              
          }
          m_viewableShapes.addShape(m_indicatorShape);
        }
      }
      else if (m_action instanceof Speech)
        ((Speech)m_action).setPercentSpoken(0.0);
    }
    
    public void cleanUp() {
      if (m_action instanceof Stroke) {
        Stroke stroke = (Stroke) m_action;
        // if we're at the start then we shouldn't display the stroke anymore
        if (atStart())
          m_viewableShapes.removeShape(stroke);
        stroke.paintWholeStroke();
        
        // remove the indicator shape
        if (m_showIndicator) {
          m_viewableShapes.removeShape(m_indicatorShape);
          m_viewableShapes.removeShape(m_indicatorText);
        }
      }
      else if (m_action instanceof Speech) {
        Speech s = (Speech) m_action;
        if (atStart()) {
          s.setPercentSpoken(0.0);
          m_speech.unspeak(s);
        }
      }
    }
    
    /**
     * Advance or rewind the in progress action to the time toTime. Return true if
     * the action was changed, false otherwise
     * @param toTime time to go to
     * @return true if changed, false if unchanged.
     */
    public boolean goToTime(long toTime) {
      boolean changed = false;
      if (m_action instanceof Stroke) {
        Stroke s = (Stroke)m_action;
        while (!isDone()) {
          long nextPointTime = s.getPoint(m_index).getTime();
          if (nextPointTime <= toTime) {
            m_index++;
            s.paintUntilPoint(m_index);
            changed = true;
            
            // update the indicator shape
            if (m_showIndicator && !isDone()) {
              m_indicatorShape.translate(s.getPoint(m_index).getProp("x")-m_indicatorShape.getProp("x"),
                  s.getPoint(m_index).getProp("y")-m_indicatorShape.getProp("y"));
              m_indicatorText.translate(s.getPoint(m_index).getProp("x")-m_indicatorText.getProp("x"),
                  s.getPoint(m_index).getProp("y")-m_indicatorText.getProp("y")-15);
            }
          }
          else 
            break;
        }
        while (!atStart()) {
          long previousPointTime = s.getPoint(m_index-1).getTime();
          if (previousPointTime > toTime) {
            m_index--;
            s.paintUntilPoint(m_index);
            changed = true;
            
            // update the indicator shape
            if (m_showIndicator && !atStart()) {
              m_indicatorShape.translate(s.getPoint(m_index).getProp("x")-m_indicatorShape.getProp("x"),
                  s.getPoint(m_index).getProp("y")-m_indicatorShape.getProp("y"));
              m_indicatorText.translate(s.getPoint(m_index).getProp("x")-m_indicatorText.getProp("x"),
                  s.getPoint(m_index).getProp("y")-m_indicatorText.getProp("y")-15);
            }
          }
          else 
            break;
        }
        if (changed) {
          if (m_showIndicator) {
            ArrayList<DrawnShape> l = new ArrayList<DrawnShape>();
            l.add(s);
            l.add(m_indicatorShape);
            l.add(m_indicatorText);
            m_viewableShapes.changed(l);
          }
          else
            m_viewableShapes.changedShape(s);
        }
      }
      
      else if (m_action instanceof MoveEdit) {
        MoveEdit me = (MoveEdit)m_action;
        MultimodalAction trigger = me.getTrigger();
        if (trigger instanceof Stroke) {
          int oldIndex = m_index;
          Stroke path = (Stroke)trigger;
          while (!isDone()) {
            List<TPoint> points = path.getPoints();
            if (points.get(m_index+1).getTime() <= toTime)
              m_index++;
            else 
              break;
          }
          while (!atStart()) {
            List<TPoint> points = path.getPoints();
            if (points.get(m_index-1).getTime() > toTime)
              m_index--;
            else 
              break;
          }
          // if the index has changed
          if (oldIndex != m_index) {
            double shiftx = path.getComponent(m_index).getProp("x") - path.getComponent(oldIndex).getProp("x");
            double shifty = path.getComponent(m_index).getProp("y") - path.getComponent(oldIndex).getProp("y");
            for (DrawnShape shape : me.getShapes()) {
              shape.translate(-shiftx, -shifty);
              changed = true;
            }
          }
          if (changed)
            m_viewableShapes.changed(me.getShapes());
        }
      }
      
      else if (m_action instanceof Speech) {
        Speech s = (Speech)m_action;
        
        // don't want to exceed the end time
        long endTime = Math.min(toTime, s.getEndTime()); 
        double progress = endTime - s.getStartTime();
        double length = s.getEndTime() - s.getStartTime();
        double newPercent = progress/length;
        double currentPercentage = s.getPercentSpoken();
        
        if (newPercent != currentPercentage) {
          s.setPercentSpoken(newPercent);
          m_speech.changed(s);
          changed = true;
        }
      }
      
      return changed;
    }
    
    public long getStartTime() {
      return (m_action.getStartTime());
    }

    public boolean isDone() {
      if (m_action instanceof Stroke)
        return (m_index == ((Stroke)m_action).numPoints());
      else if (m_action instanceof MoveEdit) {
        MoveEdit me = (MoveEdit)m_action;
        MultimodalAction trigger = me.getTrigger();
        if (trigger instanceof Stroke)
          return (m_index == ((Stroke)trigger).numPoints());
        else
          // don't know how to handle triggers other than a stroke
          return true;
      }
      else if (m_action instanceof Speech) {
        return (((Speech)m_action).getPercentSpoken() == 1.0);
      }
      else
        return true;
    }

    public boolean atStart() {
      if (m_action instanceof Stroke)
        return (m_index == 0);
      else if (m_action instanceof MoveEdit) {
        MoveEdit me = (MoveEdit)m_action;
        MultimodalAction trigger = me.getTrigger();
        if (trigger instanceof Stroke)
          return (m_index == 0);
        else
          // don't know how to handle triggers other than a stroke
          return true;
      }
      else if (m_action instanceof Speech) {
        return (((Speech)m_action).getPercentSpoken() == 0.0);
      }
      else
        return true;
    }
  }
  
  
  private synchronized void fireAnimationDone() 
  {
    for (AnimationListener listener : m_listeners) {
      listener.animationDone();
    }
  }

  private synchronized void fireAnimationPosition(int position) 
  {
    for (AnimationListener listener : m_listeners) {
      listener.animationPosition(position);
    }
  }

  public synchronized void addAnimationListener(AnimationListener listener) {
    m_listeners.add(listener);
  }
  
  public synchronized void removeAnimationListener(AnimationListener listener) {
    m_listeners.remove(listener);
  }



}

