package cornerfinders.toolkit;

import edu.mit.sketch.language.shapes.DrawnShape;
import edu.mit.sketch.language.shapes.MultimodalAction;

/**
 * A defualt MultimodalActionFactory
 * @author cadlerun
 */
public class DefaultMultimodalActionFactory extends MultimodalActionFactory {

  @Override
  public synchronized void load(MultimodalAction ma) {
    executeAction(ma);
  }
  
  @Override
  public void undo(MultimodalAction ma) {
    undoAction(ma);
  }
  
  
  @Override
  public void redo(MultimodalAction ma) {
    if(DrawnShape.class.isInstance(ma)){
      redoShape((DrawnShape)ma);
    }
  }
  
  @Override
  public void play(MultimodalAction ma) {
    animateAction(ma);
  }  
}
