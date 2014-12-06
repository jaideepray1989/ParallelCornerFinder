package cornerfinders.toolkit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import javax.imageio.ImageIO;


/**
 * 
 * <p>
 * Created:   Thu May 27 18:18:15 2004<br>
 * Copyright: Copyright (C) 2004 by MIT.  All rights reserved.<br>
 * 
 * @author <a href="mailto:moltmans@ai.mit.edu">Michael Oltmans</a>
 * @version $Id: StrokeImageWriter.java,v 1.1 2006-11-22 22:54:36 hammond Exp $
 **/

public class StrokeImageWriter {
  private float m_strokeWidth = 2.0f;
  private Color m_strokeColor = Color.blue;
  private Color m_backgroundColor = new Color(0,0,0,0);
  private int m_border = 5;
  private boolean m_cropped = true;

  public StrokeImageWriter() {}
  
  public StrokeImageWriter(float strokeWidth, int border,
                           Color strokeColor, Color backgroundColor,
                           boolean cropped)
  {
    m_strokeWidth = strokeWidth;
    m_strokeColor = strokeColor;
    m_backgroundColor = backgroundColor;
    m_border = border;
    m_cropped = cropped;
  }


  public void writeImage(String fileName, Collection<StrokeData> strokes)
    throws StrokeImageException
  {
    writeImage(new File(fileName), new StrokeSet(strokes));
  }

  public void writeImage(File file, Collection<StrokeData> strokes)
    throws StrokeImageException
  {
    writeImage(file, new StrokeSet(strokes));
  }

  public void writeImage(String fileName, StrokeSet strokes)
    throws StrokeImageException
  {
    writeImage(new File(fileName), strokes);
  }

  public void writeImage(File file, StrokeSet strokes)
    throws StrokeImageException
  {
    String name = file.getName();
    String suffix = name.substring(name.lastIndexOf(".")+1);
    
    BufferedImage image = makeImage(strokes);
    
    boolean isFine;
    try {
      isFine = ImageIO.write(image, suffix, file);
    }
    catch(IOException e) {
      throw new StrokeImageException(e);
    }
    if(!isFine) {
      throw new StrokeImageException("No writter for that format: " +
                                     suffix + " available formats are: " +
                                     availableTypesAsString());
    }
    
  }

  public BufferedImage makeImage(StrokeSet strokes) 
  {
    Rectangle bounds = strokes.getBounds();
    
    BufferedImage image;

    int width = Math.max(1, bounds.width + 2*m_border);
    int height = Math.max(1, bounds.height+ 2*m_border);
    int translateX = (int)Math.ceil(m_strokeWidth) + m_border;
    int translateY = (int)Math.ceil(m_strokeWidth) + m_border;

    // if not cropped then add the needed space to the top and left
    if(!m_cropped) {
      width += bounds.x;
      height += bounds.y;
    }
    else {
      translateX -= bounds.x;
      translateY -= bounds.y;
    }
    
    // change this to TYPE_INT_ARGB for transparency crap...
    image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = image.createGraphics();    

    // Fill the background before we adjust coordinate spaces
    g.setColor(m_backgroundColor);
    g.fillRect(0, 0, width, height);
    
    // set parameters and translate the surface
    g.translate(translateX, translateY);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                       RenderingHints.VALUE_ANTIALIAS_ON);
    g.setStroke(new BasicStroke(m_strokeWidth, BasicStroke.CAP_ROUND,
                                BasicStroke.JOIN_ROUND));

    // write the strokes
    g.setColor(m_strokeColor);
    strokes.paint(g);
    return image;
  }

  public static String availableTypesAsString() 
  {
    String result = "";
    String[] types = ImageIO.getWriterFormatNames();
    for(int i=0; i<types.length; i++) {
      result += types[i] + " ";
    }
    return result;
  }
  

  public static void main(String[] args) {
    if(args.length != 2) {
      System.out.println("Usage: input.drs out.type");
      
    }
    
    try {
      List<StrokeData> strokes = StrokeReader.loadStrokes(new File(args[0]));
      StrokeImageWriter writer =
        new StrokeImageWriter(2.0f, 10,
                              Color.blue,
                              //Color.white,
                              new Color(1.0f,1.0f,1.0f, 1.0f),
                              true);
      writer.writeImage(args[1], strokes);   
    }
    catch(Exception e) {
      e.printStackTrace();
      System.out.println(e.getMessage());
    }
    
  }
}
