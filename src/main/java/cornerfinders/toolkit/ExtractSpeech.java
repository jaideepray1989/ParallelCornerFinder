/**
 * 
 */
package cornerfinders.toolkit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import edu.mit.sketch.language.shapes.MultimodalAction;
import edu.mit.sketch.language.speech.Silence;
import edu.mit.sketch.language.speech.Word;

/**
 * This class extracts speech events from an xml file into a csv file. 
 * @author cadlerun
 *
 */
public class ExtractSpeech {

  /**
   * Extracts speech events from the input file (which is in xml format) to an 
   * csv output file. The output file should not exist or else it will be appended to.
   * @param input The input file
   * @param output The output file 
   * @param splitTimes If true, split the long times into two parts so that excel can display them
   * @param includeSilence If true output silence words, otherwise do not output them
   * @return true if sucessful, false otherwise
   */
  private static boolean extractSpeech(File input, File output, boolean splitTimes, boolean includeSilence) {
    try {
      if (isFileStateOk(input, output, ".xml", ".csv")) {

        // Load the xml file into a history
        MultimodalActionHistory history = new MultimodalActionHistory();
        history.loadFile(input);
        
        // get the actions
        ArrayList<MultimodalAction> actions = history.getActions();
        
        // open the output file in append mode
        PrintWriter writer = new PrintWriter(new FileWriter(output, true), true);
        
        int split = 100000000;
        
        // extract the words and start and end times
        // and the speaker
        for (MultimodalAction action : actions) {
          if ((action instanceof Word) && (includeSilence || !(action instanceof Silence))) {
            Word w = (Word)action;
            long startTime = w.getStartTime();
            long endTime = w.getEndTime();
            String word = w.getWord();
            
            // determine speaker
            String speakerId = w.getProperty("author");
            String nickName = history.getSketcherIndex().getSketcherNickname(speakerId);
            String speaker = (nickName != null) ? nickName : speakerId;
            
            //output to csv file
            String outputString;
            if (splitTimes)
              outputString = startTime/split+","+startTime%split+","+endTime/split+","+endTime%split+","+speaker+","+word;
            else
              outputString = startTime+","+endTime+","+speaker+","+word;
            writer.println(outputString);
          }
        }
        
        return true;
      }
      else {
        System.out.println("Could not convert "+input.getName()+" to "+output.getName());
        return false;
      }
    } catch (FileNotFoundException e) {
      System.out.println("ERROR: File not found.");
      return false;
    } catch (IOException e) {
      System.out.println("ERROR: IO Exception.");
      return false;
    }

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
  
  public static List<File> getInputFiles() {
    JFileChooser inputChooser = new JFileChooser();
    inputChooser.setFileFilter(new FileFilter() {
      @Override
      public boolean accept(File arg0) {
        if (arg0.isDirectory() || (arg0.getName().endsWith(".xml")))
          return true;
        else
          return false;
      }

      @Override
      public String getDescription() {
        return "XML Files";
      }
    
    });
    inputChooser.setMultiSelectionEnabled(true);
    int result = inputChooser.showOpenDialog(null);
    if (result == JFileChooser.APPROVE_OPTION) {
      ArrayList<File> files = new ArrayList<File>();
      for (File file : inputChooser.getSelectedFiles()) {
        files.add(file);
      }
      return files;
    }
    else
      return new ArrayList<File>();
  }
  
  public static File getOutputFile() {
    JFileChooser outputChooser = new JFileChooser();
    outputChooser.setFileFilter(new FileFilter() {
      @Override
      public boolean accept(File arg0) {
        if (arg0.isDirectory() || (arg0.getName().endsWith(".csv")))
          return true;
        else
          return false;
      }

      @Override
      public String getDescription() {
        return "CSV Files";
      }
    
    });
    outputChooser.setMultiSelectionEnabled(false);
    int result = outputChooser.showSaveDialog(null);
    if (result == JFileChooser.APPROVE_OPTION) {
      return outputChooser.getSelectedFile();
    }
    else
      return null;
  }

  public static void main(String[] args) {
    List<File> inputFiles = getInputFiles();
    
    if (inputFiles.size() > 0) {
      File outputFile = getOutputFile();
    
      if (outputFile != null)
        // check to make sure file doesn't already exist
        if (outputFile.exists()) {
          System.out.println("Output file "+outputFile.getName()+" already exists.");
        }
        else {
          // get additional options from user
          int splitQ = JOptionPane.showConfirmDialog(null, "Do you want to split the start and end times for Excel compatibility?",
              "Split times?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
          int silenceQ = JOptionPane.showConfirmDialog(null, "Do you want to include silence words in the output?",
              "Include silence words?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
          
          for (File inputFile : inputFiles) {
            extractSpeech(inputFile, outputFile, (splitQ == JOptionPane.YES_OPTION), (silenceQ == JOptionPane.YES_OPTION));
          } 
        }
    }
  }
  
}
