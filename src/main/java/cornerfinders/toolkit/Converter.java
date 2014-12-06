/**
 * 
 */
package cornerfinders.toolkit;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.Vector;

import edu.mit.sketch.geom.Point;
import edu.mit.sketch.language.shapes.MultimodalAction;
import edu.mit.sketch.language.shapes.Stroke;
import edu.tamu.hammond.sketch.shapes.TPoint;

/**
 * This class converts drs files into xml files.
 * @author cadlerun
 *
 */
public class Converter {

  private enum Flags {
    RESAVE,
    RESAVEDIRECTORY,
    COORDINATES,
    COLOR,
    SHORT_IDS,
    OUTPUT_FILE,
    OUTPUT_DIRECTORY,
    HELP,
    NONE
  }
  
  
  
  
  /**
   * Converts the input file (which is in drs format) to an 
   * output file (in xml format). The output file should not exist.
   * This method does not check to see if the file is there or not.
   * You should call convertFile which does do checks to make sure the
   * files exist and are of the correct type
   * @param input The input file
   * @param output The output file 
   * @return true if sucessful, false otherwise
   */
  private static boolean doConversion(File input, File output) {
    try {
      // Load the old file into a history
      MultimodalActionHistory mah = convertStrokesToHistory(StrokeReader.loadStrokes(input), null);
      
      // done creating the MultimodalActionHistory, now save the file out
      mah.write(output);
      return true;
      
    } catch (FileNotFoundException e) {
      System.out.println("ERROR: Input file not found.");
      return false;
    } catch (IOException e) {
      System.out.println("ERROR: Reading input file.");
      return false;
    }

  }
  
  /**
   * Convert a history with himetric coordinates into a history with 
   * pixel coordinates
   * @param history the history to convert
   * @return true if sucessful, false otherwise
   */
  public static boolean convertCoordinates(MultimodalActionHistory history) {
    if (!history.inHiMetricCoordinates()) {
      System.err.println("input file is not in himetric coordinates!");
      return false;
    }
    
    // get the resolution data
    double xRes = history.getMaxDpiX();
    double yRes = history.getMaxDpiY();
    
    if ((xRes < 0) || (yRes < 0)) {
      System.err.println("input file does not contain resolution data!");
      return false;
    }
    
    // convert the document
    ArrayList<MultimodalAction> actions = history.getActions();
    
    for (MultimodalAction action : actions) {
      if (action instanceof TPoint) {
        TPoint point = (TPoint)action;
        double oldX = point.getProp("x");
        double oldY = point.getProp("y");
        long time = point.getTime();
        double pressure = point.getProp("pressure");
        double newX = oldX * (xRes / 2540.0);
        double newY = oldY * (yRes / 2540.0);
        //System.out.println(newX + " "+ newY);
        Point newLoc = new Point(newX, newY, time, (int)pressure);
        point.setOrigP(newLoc.getX(), newLoc.getY());
      }
      // need to convert height and width too
      if (action instanceof Stroke) {
        Stroke stroke = (Stroke)action;
        if (stroke.hasProperty("height")) {
          double height = Double.parseDouble(stroke.getProperty("height"));
          height = height * (yRes / 2540.0);
          stroke.setProperty("height", Double.toString(height));
        }
        if (stroke.hasProperty("width")) {
          double width = Double.parseDouble(stroke.getProperty("width"));
          width = width * (xRes / 2540.0);
          stroke.setProperty("width", Double.toString(width));
        }
      }
    }
    
    history.setHiMetricCoordinates(false);
    return true;
  }
  
  /**
   * Change the colors of strokes in a history to higher alpha values 
   * @param history the history to convert
   * @return true if sucessful, false otherwise
   */
  private static boolean changeColors(MultimodalActionHistory history) {
    // amount to adjust alpha by
    final int alphaAdjustment = 50;
    
    // convert the document
    ArrayList<MultimodalAction> actions = history.getActions();
    
    for (MultimodalAction action : actions) {
      if (action instanceof Stroke) {
        Stroke stroke = (Stroke)action;
        if (stroke.hasProperty("color")) {
          String color = stroke.getProperty("color");
          Color c = new Color(Integer.valueOf(color), true);
          if (c.getAlpha() < 255) {
            c = new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(255, c.getAlpha()+alphaAdjustment));
            stroke.setProperty("color", Integer.toString(c.getRGB()));
          }
        }
      }
    }

    return true;
  }
  
  /**
   * Convert an xml file with himetric coordinates into an xml file with
   * pixel coordinates
   * @param inputFile xml file with himetric coordinates
   * @param outputFile xml file with pixel coordinates
   */
  public static void convertCoordinates(File inputFile, File outputFile) {
    if (isFileStateOk(inputFile, outputFile, ".xml", ".xml")) {
      
      MultimodalActionHistory history = new MultimodalActionHistory();
      
      try {
        // loading the history now converts it
        history.loadFile(inputFile);
        history.write(outputFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    else {
      System.out.println("Could not convert "+inputFile.getName()+" to "+outputFile.getName());
    }
  }
  
  /**
   * Make the alpha values higher in an xml file and save to a new file
   * @param inputFile xml file with initial colors
   * @param outputFile xml file with higher alpha values
   */
  public static void changeColors(File inputFile, File outputFile) {
    if (isFileStateOk(inputFile, outputFile, ".xml", ".xml")) {
      
      MultimodalActionHistory history = new MultimodalActionHistory();
      
      try {
        // loading the history now converts it
        history.loadFile(inputFile);
        changeColors(history);
        history.write(outputFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    else {
      System.out.println("Could not convert "+inputFile.getName()+" to "+outputFile.getName());
    }
  }
  
  /**
   * Convert the given file in DRS format into a MultimodalActionHistory
   * with the specified factory.
   * @param drsFile drs file
   * @param factory the factory to use
   * @return a new MultimodalActionHistory
   */
  public static MultimodalActionHistory covertStrokes(File drsFile, MultimodalActionFactory factory) {
    try {
      return convertStrokesToHistory(StrokeReader.loadStrokes(drsFile), factory);
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return null;    
  }
  
  /**
   * Convert the given file in DRS format into a MultimodalActionHistory.
   * @param drsFile
   * @return A new MultimodalActionHistory
   */
  public static MultimodalActionHistory convertStrokes(File drsFile) {
    return covertStrokes(drsFile,null);
  }

  /**
   * Convert a set of stroke data objects into a MultimodalActionHistory using
   * the specified factory, or no factory if factory is null.
   * @param strokes The old style strokes
   * @param factory The factory to use, or null if no factory specified
   * @return A new MultimodalActionHistory
   */
  public static MultimodalActionHistory convertStrokesToHistory(Collection<StrokeData> strokes, 
      MultimodalActionFactory factory) {
    MultimodalActionHistory mah = new MultimodalActionHistory();
    if (factory != null)
      mah.setMultimodalActionFactory(factory);
    
    // turn off the coordinate conversion
    mah.setConvertHiMetricToPixel(false);
    
    for (StrokeData stroke : strokes) {
      // create the right type of points and add them to the MutlimodalActionHistory
      addStrokeDataToHistory(mah, stroke);
    }
    return mah;
  }
  
  /**
   * Add the stroke and it's supporting points to the history. Returns the UUID
   * of the stroke in the history. This is to help the transition to the new
   * data formats and is not part of MultimodalActionHistory because we want to
   * avoid mixing the two formats, as much as possible.
   * 
   * @param mah
   *        The history to add points and stroke to.
   * @param stroke
   *        The StrokeData object holding the points
   * @return The UUID of the new stroke in the history.
   */
  public static UUID addStrokeDataToHistory(MultimodalActionHistory mah, StrokeData stroke) {
    Vector<TPoint> newPoints = new Vector<TPoint>();
    Point[] points = stroke.getOrigPoints();
    long endTime = -1;

    for (Point point : points) {
      if (point.getTimeStamp() > endTime) {
        endTime = point.getTimeStamp();
      }
      TPoint p = new TPoint(point.x, point.y, point.getTimeStamp());
      mah.addLoad(p);
      newPoints.add(p);
    }
    
    // error check that it had a point
    if (endTime == -1) {
      return null;
    }
    
    // make the stroke and return it
    Stroke s = new Stroke(endTime, newPoints);
    return mah.addLoad(s);
  }
  
  /**
   * Converts the input file (which is in xml format) to an 
   * output file (in xml format). The UUIDs in the input file
   * are converted to short ids in the output file. 
   * @param inputFile The input file
   * @param outputFile The output file 
   * @return true if sucessful, false otherwise
   * @throws java.io.IOException
   */
  private static boolean convertToShortIds(File inputFile, File outputFile) throws IOException {
    if (isFileStateOk(inputFile, outputFile, ".xml", ".xml")) {
      MultimodalActionHistory mah = new MultimodalActionHistory(true);
      mah.setConvertHiMetricToPixel(false);
      mah.loadFile(inputFile);
      mah.write(outputFile);
      System.out.println("Converted to short ids "+inputFile.getName()+" to "+outputFile.getName());
      return true;
    }
    System.out.println("Could not convert "+inputFile.getName()+" to "+outputFile.getName());
    return false;
  }

  /**
   * Checks to make sure the input file exists and is readable and that the
   * output file does not exist and the directory containing the output file is 
   * writable. The input file must end in inputEnding and the output file must end in outputEnding
   * Returns true if everything is okay, and false otherwise.
   * @param input the input file
   * @param output the output file
   * @param inputEnding what the input file extension should be
   * @param outputEnding what the input file extension should be
   * @return true if the files are okay, false otherwise
   */
  private static boolean isFileStateOk(File input, File output, String inputEnding, String outputEnding) {
    if (!input.exists()) {
      System.out.println("Input file "+input.getName()+" does not exist.");
      return false;
    }
    if (!input.canRead()) {
      System.out.println("Input file "+input.getName()+" cannot be read.");
      return false;      
    }
    if (!input.getName().endsWith(inputEnding)) {
      System.out.println("Input file "+input.getName()+" does not end in \""+inputEnding+"\".");
      return false;      
    }
    if (output.exists()) {
      System.out.println("Output file "+output.getName()+" already exists.");
      return false;
    }
    if (!output.getAbsoluteFile().getParentFile().canWrite()) {
      System.out.println("Cannot write to directory containing output file "+output.getName()+".");
      return false;      
    }
    if (!output.getName().endsWith(outputEnding)) {
      System.out.println("Output file "+output.getName()+" does not end in \""+outputEnding+"\".");
      return false;      
    }
    return true;
  }
  
  /**
   * Converts the input file (in drs format) to the output file (in xml format). 
   * Reports errors to System.out if any are encountered. 
   * @param inputFile the input file
   * @param outputFile the output file
   */
  private static void convertFile(File inputFile, File outputFile) {
    if (isFileStateOk(inputFile, outputFile, ".drs", ".xml")) {
      boolean success = Converter.doConversion(inputFile, outputFile);
      if (success)
        System.out.println("Converted "+inputFile.getName()+" to "+outputFile.getName());
      else
        System.out.println("Could not convert "+inputFile.getName()+" to "+outputFile.getName());
    }
    else
      System.out.println("Could not convert "+inputFile.getName()+" to "+outputFile.getName());    
  }
  
  /**
   * Look for a flag in the arguments
   * @param args the arguments passed to the 
   * @return the type of flag or none if there isn't one
   */
  private static Flags getFlag(String[] args) {
    for (int i = 0; i < args.length; i++) {
      String string = args[i];
      if ((string.equalsIgnoreCase("-h")) || (string.equalsIgnoreCase("-help")))
        return Flags.HELP;
      else if (string.equalsIgnoreCase("-o")) {
        // check last argument
        String outputArg = args[args.length-1];
        File output = new File(outputArg);
        if (output.isDirectory())
          return Flags.OUTPUT_DIRECTORY;

        return Flags.OUTPUT_FILE;
      }
      else if (string.equalsIgnoreCase("-s")) {
        return Flags.SHORT_IDS;
      }
      else if (string.equalsIgnoreCase("-color")) {
        return Flags.COLOR;
      }
      else if (string.equalsIgnoreCase("-c")) {
        return Flags.COORDINATES;
      }
      else if (string.equals("-r")) {
        return Flags.RESAVE;
      }
      else if (string.equals("-R")) {
        return Flags.RESAVEDIRECTORY;
      }
    }
    
    // No flag found, return none
    return Flags.NONE;
  }

  /**
   * Convert a file with himetric coordinates to an output file with pixel coordinates 
   * @param args the arguments
   */
  private static void convertOutputFileCoordinates(String[] args) {
    if (args.length != 3) {
      System.out.println("You must specify exactly 3 arguments when you use the \"-c\" flag.");
      System.out.println("The first argument should be the \"-c\" flag, the second the input file name,");
      System.out.println("and the third the output file name.");
    }
    else if (!args[0].equalsIgnoreCase("-c")) {
        System.out.println("The first argument must be the \"-c\" flag,");
    }
    else {
      File inputFile = new File(args[1]);
      File outputFile = new File(args[2]);
      if (!outputFile.isDirectory()) 
        convertCoordinates(inputFile, outputFile);
      else 
        System.out.println("The last argument must be a file");   
    }
  }

  /**
   * Change the colors in a file to colors with higher alpha values 
   * @param args the arguments
   */
  private static void changeFileColors(String[] args) {
    if (args.length != 3) {
      System.out.println("You must specify exactly 3 arguments when you use the \"-color\" flag.");
      System.out.println("The first argument should be the \"-color\" flag, the second the input file name,");
      System.out.println("and the third the output file name.");
    }
    else if (!args[0].equalsIgnoreCase("-color")) {
        System.out.println("The first argument must be the \"-color\" flag,");
    }
    else {
      File inputFile = new File(args[1]);
      File outputFile = new File(args[2]);
      if (!outputFile.isDirectory()) 
        changeColors(inputFile, outputFile);
      else 
        System.out.println("The last argument must be a file");   
    }
  }

  /**
   * Open and then resave a file 
   * @param args the arguments
   */
  private static void resaveFile(String[] args) {
    if (args.length != 2) {
      System.out.println("You must specify exactly 2 arguments when you use the \"-r\" flag.");
      System.out.println("The first argument should be the \"-r\" flag, the second the file to resave.");
    }
    else if (!args[0].equals("-r")) {
        System.out.println("The first argument must be the \"-r\" flag,");
    }
    else {
      File file = new File(args[1]);
      if (!file.isDirectory()) 
        resave(file);
      else 
        System.out.println("The second argument must be a file");   
    }
  }

  /**
   * Open and then resave every xml file in the directory 
   * @param args the arguments
   */
  private static void resaveDirectory(String[] args) {
    if (args.length != 2) {
      System.out.println("You must specify exactly 2 arguments when you use the \"-R\" flag.");
      System.out.println("The first argument should be the \"-R\" flag, the second the direcory of files to resave.");
    }
    else if (!args[0].equals("-R")) {
        System.out.println("The first argument must be the \"-R\" flag,");
    }
    else {
      File dir = new File(args[1]);
      if (dir.isDirectory()) {
        File[] xmlFiles = dir.listFiles(new xmlFilenameFilter());
        for (int i = 0; i < xmlFiles.length; i++) {
          resave(xmlFiles[i]);
        } 
      } 
      else 
        System.out.println("The second argument must be a directory");   
    }
  }

  /**
   * Open and then save the specified xml file. 
   * @param file The  file
   */
  private static void resave(File file) {
    //try {
      MultimodalActionHistory mah = new MultimodalActionHistory();
      try {
        // don't touch the coordinates here
        mah.setConvertHiMetricToPixel(false);
        mah.loadFile(file);
        mah.write(file);
      } catch (IOException e) {
        System.out.println("IO exception "+e.toString());
      }
      System.out.println("Resaved "+file.getName());
    //}
    
  }

  /**
   * Convert a file with an output file specified to short ids
   * @param args the arguments
   * @throws java.io.IOException
   */
  private static void convertOutputFileShortIds(String[] args) throws IOException {
    if (args.length != 3) {
      System.out.println("You must specify exactly 3 arguments when you use the \"-s\" flag.");
      System.out.println("The first argument should be the \"-s\" flag, the second the input file name,");
      System.out.println("and the third the output file name.");
    }
    else if (!args[0].equalsIgnoreCase("-s")) {
        System.out.println("The first argument must be the \"-s\" flag,");
    }
    else {
      File inputFile = new File(args[1]);
      File outputFile = new File(args[2]);
      if (!outputFile.isDirectory()) 
        convertToShortIds(inputFile, outputFile);
      else 
        System.out.println("The last argument must be a file");   
    }
  }

  /**
   * Convert a file with an output file specified 
   * @param args the arguments
   */
  private static void convertOutputFileSpecified(String[] args) {
    if (args.length != 3) {
      System.out.println("You must specify exactly 3 arguments when you use the \"-o\" flag.");
      System.out.println("The first argument should be the \"-o\" flag, the second the input file name,");
      System.out.println("and the third the output file name.");
    }
    else if (!args[0].equalsIgnoreCase("-o")) {
        System.out.println("The first argument must be the \"-o\" flag,");
    }
    else {
      File inputFile = new File(args[1]);
      File outputFile = new File(args[2]);
      if (!outputFile.isDirectory()) 
        convertFile(inputFile, outputFile);
      else 
        System.out.println("The last argument must be a file");   
    }
  }
  
  /**
   * Replace the ending .drs with .xml. Assumes that the file name ends with .drs
   * @param inputFile the input file
   * @return the name of the output file with the ending .drs replaced with .xml
   */
  private static String getOutputFileName(File inputFile) {
    String name = inputFile.getName();
    return name.substring(0, name.length()-4)+".xml";
  }

  /**
   * Convert files with an output directory specified 
   * @param args the arguments
   */
  private static void convertOutputDirectorySpecified(String[] args) {
    int numberOfArgs = args.length;
    
    if (numberOfArgs < 3) {
      System.out.println("You must specify at least 3 arguments when you use the \"-o\" flag.");
      System.out.println("The first arguments should be the \"-o\" flag, the middle arguments");
      System.out.println("should be input file names, and the last argument should be the");
      System.out.println("output directory name.");
    }
    else if (!args[0].equalsIgnoreCase("-o")) {
      System.out.println("The first argument must be the \"-o\" flag.");
    }
    else {
      File outputDirectory = new File(args[numberOfArgs-1]);
      if (outputDirectory.isDirectory()) {
        for (int i = 1; i < numberOfArgs-1; i++) {
          String inputFileName = args[i];
          File inputFile = new File(inputFileName);
          File outputFile = new File(outputDirectory, getOutputFileName(inputFile));
          convertFile(inputFile, outputFile);
        }
      }
      else {
        System.out.println("The last argument must be a directory");        
      }
    }
  }
  
  /**
   * Convert a bunch of files  
   * @param args the arguments
   */
  private static void convertFiles(String[] args) {
    for (String inputFileName : args) {
      File inputFile = new File(inputFileName);
      File outputFile = new File(inputFile.getParent(), getOutputFileName(inputFile));
      convertFile(inputFile, outputFile);
    }
  }
  
  private static void printHelp() {
    System.out.println("This class converts drs files into xml files.");
    System.out.println("usage: Converter -hoscrR [drs file+] [dir|xml file name]");
    System.out.println("-h      prints this message");
    System.out.println("-o      output file or directory. Use this instead of the default name or location.");
    System.out.println("-s      convert xml file to xml file with short ids (not for use with directories)");
    System.out.println("-c      convert xml file with himetric coordinates to xml file with pixel coordinates (not for use with directories)");
    System.out.println("-color  decrease the alpha values for the colors in the given xml file");
    System.out.println("-r      convert xml files by opening and then resaving them");    
    System.out.println("-R      convert a directory of xml files by opening and then resaving them");    
    System.out.println();
    System.out.println("The default file name is the same as the original file name with the new extension.");
    System.out.println("The default directory is the current directory.");
  }
  
  public static void main(String[] args) {
    Flags f = getFlag(args);
    switch(f) {
      case HELP: 
        printHelp();
        break;
      case SHORT_IDS:
        try {
          convertOutputFileShortIds(args);
        }
        catch (IOException e) {
          e.printStackTrace();
        }
        break;
      case RESAVE:
        resaveFile(args);
        break;
      case RESAVEDIRECTORY:
        resaveDirectory(args);
        break;
      case COORDINATES:
        convertOutputFileCoordinates(args);
        break;
      case COLOR:
        changeFileColors(args);
        break;
      case OUTPUT_FILE: 
        convertOutputFileSpecified(args);
        break;
      case OUTPUT_DIRECTORY: 
        convertOutputDirectorySpecified(args);
        break;
      case NONE: 
        convertFiles(args);
        break;
    }
  }  
  
}

class xmlFilenameFilter implements FilenameFilter {

  public boolean accept(File dir, String filename) {
    return filename.endsWith(".xml");
  }
  
}

