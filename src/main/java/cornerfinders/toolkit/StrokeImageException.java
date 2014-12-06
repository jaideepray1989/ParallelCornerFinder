package cornerfinders.toolkit;


/**
 * Exception class for writting stroke sets to image files.
 * <p>
 *
 * Created:   Thu May 27 19:08:15 2004<br>
 * Copyright: Copyright (C) 2004 by MIT.  All rights reserved.<br>
 * 
 * @author <a href="mailto:moltmans@ai.mit.edu">Michael Oltmans</a>
 * @version $Id: StrokeImageException.java,v 1.1 2006-11-22 22:54:35 hammond Exp $
 **/

public class StrokeImageException extends Exception
{
  public StrokeImageException() {
    super();
  }
  public StrokeImageException(String message) {
    super(message);
  }
  public StrokeImageException(String message, Throwable cause) {
    super(message, cause);
  }
  public StrokeImageException(Throwable cause) {
    super(cause);
  }
}
