package cornerfinders.impl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;

import edu.tamu.hammond.io.FileHelper;
import edu.tamu.hammond.sketch.shapes.TPoint;
import edu.tamu.hammond.sketch.shapes.TStroke;

public class CornerFinderDebug extends JFrame implements KeyListener {
	
	private static final long serialVersionUID = -1790237730027757055L;
	
	private final boolean SEGMENT_OUTPUT = false;
	
	// Corner finder type to use
	private enum CornerFinderType {
		Merge, Sezgin, Kim, ShortStraw, Brandon
	};
	
	private CornerFinderType cfType = CornerFinderType.Merge;
	
	// Accuracy type to use
	private enum AccuracyType {
		AllOrNothing, FalseCorners
	};
	
	private AccuracyType debugAccuracyType = AccuracyType.AllOrNothing;
	
	// Data set to use
	private enum DataType {
		UserStudy, Kim, LineTest, LineTrain
	};
	
	private DataType debugDataType = DataType.UserStudy;
	
	// Data set folders
	private File userStudyData = new File(
	        "C:\\Users\\Aaron Wolin\\workspace\\SR2007\\src\\awolin\\CornerFinderData\\UserStudy");
	
	private File kimData = new File(
	        "C:\\Users\\Aaron Wolin\\workspace\\SR2007\\src\\awolin\\CornerFinderData\\Kim");
	
	private File lineTest = new File(
	        "C:\\Users\\Aaron Wolin\\workspace\\SR2007\\src\\awolin\\CornerFinderData\\ShortStraw\\Test");
	
	private File lineTrain = new File(
	        "C:\\Users\\Aaron Wolin\\workspace\\SR2007\\src\\awolin\\CornerFinderData\\ShortStraw\\Train");
	
	// Data folder holder
	private File debugFolder = userStudyData;
	
	// Debug panel components
	private StrokePanel drawPanel;
	
	private JPanel buttonPanel;
	
	private JButton correctButton;
	
	private JButton incorrectButton;
	
	private JSlider cornerSlider;
	
	private JButton manualCornerButton;
	
	private JButton nextCornerFileButton;
	
	// File components
	private File dir;
	
	private String[] strokeFiles;
	
	private int currentFile = Integer.MAX_VALUE;
	
	private double falsePositives;
	
	private double falseNegatives;
	
	private double correctCorners;
	
	private double totalCorners;
	
	private double allOrNothing;
	
	private double accuracy = 0.0;
	
	private boolean traversing = false;
	
	// Corner finder components
	private CornerFinder cf;
	
	private int currentSelectedCorner;
	
	private FileOutputStream cornersFOut;
	
	
	CornerFinderDebug() {
		cf = new MergeCornerFinder();
		
		drawPanel = new StrokePanel();
		drawPanel.setPreferredSize(new Dimension(1000, 600));
		drawPanel.setCornerFinder(cf);
		
		setTitle("Corner Debugging");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		initMenu();
		initButtonPanel();
		
		correctButton.setEnabled(false);
		incorrectButton.setEnabled(false);
		
		add(drawPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		
		setPreferredSize(new Dimension(1000, 700));
		
		this.changeCornerFinderType(CornerFinderType.ShortStraw);
		
		drawPanel.addKeyListener(this);
		drawPanel.setEnabled(true);
		drawPanel.setFocusable(true);
		
		pack();
	}
	

	private void initMenu() {
		// Create the menu bar.
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu debugMenu = new JMenu("Debug");
		JMenu cfMenu = new JMenu("Corner Finder");
		JMenu dataMenu = new JMenu("Data");
		JMenu accuracyMenu = new JMenu("Accuracy");
		
		/*
		 * FILE MENU
		 */

		// File chooser option
		JMenuItem fileChooser = new JMenuItem("File Chooser");
		ActionListener fileChooserAction = new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEvent) {
				
				// Choose a file to open
				JFileChooser c = new JFileChooser(debugFolder);
				c.setFileSelectionMode(JFileChooser.FILES_ONLY);
				c.setDialogTitle("Select a stroke to open");
				c.showOpenDialog(null);
				File f = c.getSelectedFile();
				
				if (f != null) {
					
					// Get the stroke from the file
					TStroke s = getStroke(f);
					
					setTitle(f.getName());
					drawPanel.setStroke(s);
				}
			}
		};
		fileChooser.addActionListener(fileChooserAction);
		
		/*
		 * DEBUG STYLE MENU
		 */

		// Debug (error rate) option
		JMenuItem cfAccuracyManual = new JMenuItem("Accuracy checker (Manual)");
		ActionListener cfAccuracyManualAction = new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEvent) {
				getAllStrokes(debugFolder);
			}
		};
		cfAccuracyManual.addActionListener(cfAccuracyManualAction);
		
		// Get the error rate automatically option
		JMenuItem cfAccuracyAuto = new JMenuItem("Accuracy checker (Automatic)");
		ActionListener cfAccuracyAutoAction = new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEvent) {
				getAccuracyOfDir(debugFolder);
			}
		};
		cfAccuracyAuto.addActionListener(cfAccuracyAutoAction);
		
		// Get the error rate automatically option
		JMenuItem cfCornerChoosing = new JMenuItem("Select Corners (Manual)");
		ActionListener cfCornerChoosingAction = new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEvent) {
				getCornersOfDir(debugFolder);
			}
		};
		cfCornerChoosing.addActionListener(cfCornerChoosingAction);
		
		/*
		 * CORNER FINDER MENU
		 */

		// Merge CF
		ActionListener mergeCFAction = new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEvent) {
				changeCornerFinderType(CornerFinderType.Merge);
			}
		};
		
		ButtonGroup cfGroup = new ButtonGroup();
		JRadioButtonMenuItem mergeCF = new JRadioButtonMenuItem("Merge");
		mergeCF.setSelected(true);
		cfGroup.add(mergeCF);
		mergeCF.addActionListener(mergeCFAction);
		
		// Sezgin CF
		ActionListener sezginCFAction = new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEvent) {
				changeCornerFinderType(CornerFinderType.Sezgin);
			}
		};
		
		JRadioButtonMenuItem sezginCF = new JRadioButtonMenuItem("Sezgin");
		cfGroup.add(sezginCF);
		sezginCF.addActionListener(sezginCFAction);
		
		// Kim CF
		ActionListener kimCFAction = new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEvent) {
				changeCornerFinderType(CornerFinderType.Kim);
			}
		};
		
		JRadioButtonMenuItem kimCF = new JRadioButtonMenuItem("Kim");
		cfGroup.add(kimCF);
		kimCF.addActionListener(kimCFAction);
		
		// ShortStraw CF
		ActionListener shortStrawCFAction = new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEvent) {
				changeCornerFinderType(CornerFinderType.ShortStraw);
			}
		};
		
		JRadioButtonMenuItem shortStrawCF = new JRadioButtonMenuItem(
		        "ShortStraw");
		cfGroup.add(shortStrawCF);
		shortStrawCF.addActionListener(shortStrawCFAction);
		
		// Brandon CF
		ActionListener brandonCFAction = new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEvent) {
				changeCornerFinderType(CornerFinderType.Brandon);
			}
		};
		
		JRadioButtonMenuItem brandonCF = new JRadioButtonMenuItem("Brandon");
		cfGroup.add(brandonCF);
		brandonCF.addActionListener(brandonCFAction);
		
		/*
		 * DEBUG DATA MENU
		 */

		ButtonGroup dataGroup = new ButtonGroup();
		
		// User Study
		ActionListener userStudyDataAction = new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEvent) {
				changeDataType(DataType.UserStudy);
			}
		};
		
		JRadioButtonMenuItem userStudyData = new JRadioButtonMenuItem(
		        "User Study Data");
		userStudyData.setSelected(true);
		dataGroup.add(userStudyData);
		userStudyData.addActionListener(userStudyDataAction);
		
		// Kim
		ActionListener kimDataAction = new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEvent) {
				changeDataType(DataType.Kim);
			}
		};
		
		JRadioButtonMenuItem kimData = new JRadioButtonMenuItem("Kim Data");
		dataGroup.add(kimData);
		kimData.addActionListener(kimDataAction);
		
		// Line Train
		ActionListener lineTrainDataAction = new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEvent) {
				changeDataType(DataType.LineTrain);
			}
		};
		
		JRadioButtonMenuItem lineTrainData = new JRadioButtonMenuItem(
		        "Line Train Data (US)");
		lineTrainData.setSelected(true);
		dataGroup.add(lineTrainData);
		lineTrainData.addActionListener(lineTrainDataAction);
		
		// Line Test
		ActionListener lineTestDataAction = new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEvent) {
				changeDataType(DataType.LineTest);
			}
		};
		
		JRadioButtonMenuItem lineTestData = new JRadioButtonMenuItem(
		        "Line Test Data (Kim)");
		lineTestData.setSelected(true);
		dataGroup.add(lineTestData);
		lineTestData.addActionListener(lineTestDataAction);
		
		/*
		 * ACCURACY STYLE MENU
		 */

		// All or Nothing
		ActionListener allOrNothingAction = new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEvent) {
				changeAccuracyType(AccuracyType.AllOrNothing);
			}
		};
		
		ButtonGroup accuracyGroup = new ButtonGroup();
		JRadioButtonMenuItem allOrNothingAccuracy = new JRadioButtonMenuItem(
		        "All Or Nothing");
		allOrNothingAccuracy.setSelected(true);
		accuracyGroup.add(allOrNothingAccuracy);
		allOrNothingAccuracy.addActionListener(allOrNothingAction);
		
		// False Corners
		ActionListener falseCornersAction = new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEvent) {
				changeAccuracyType(AccuracyType.FalseCorners);
			}
		};
		
		JRadioButtonMenuItem falseCornersAccuracy = new JRadioButtonMenuItem(
		        "False Corners");
		accuracyGroup.add(falseCornersAccuracy);
		falseCornersAccuracy.addActionListener(falseCornersAction);
		
		/*
		 * FINALIZING
		 */

		// Construct the menus
		fileMenu.add(fileChooser);
		menuBar.add(fileMenu);
		
		debugMenu.add(cfAccuracyAuto);
		debugMenu.add(cfAccuracyManual);
		debugMenu.add(cfCornerChoosing);
		menuBar.add(debugMenu);
		
		cfMenu.add(mergeCF);
		cfMenu.add(sezginCF);
		cfMenu.add(kimCF);
		cfMenu.add(shortStrawCF);
		cfMenu.add(brandonCF);
		menuBar.add(cfMenu);
		
		dataMenu.add(userStudyData);
		dataMenu.add(kimData);
		dataMenu.add(lineTrainData);
		dataMenu.add(lineTestData);
		menuBar.add(dataMenu);
		
		accuracyMenu.add(allOrNothingAccuracy);
		accuracyMenu.add(falseCornersAccuracy);
		menuBar.add(accuracyMenu);
		
		// Set the menu bar
		setJMenuBar(menuBar);
	}
	

	/**
	 * Initializes the buttons
	 */
	private void initButtonPanel() {
		this.buttonPanel = new JPanel();
		buttonPanel.setPreferredSize(new Dimension(100, 120));
		
		correctButton = new JButton("Correct");
		ActionListener correctAction = new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEvent) {
				nextFile(true);
			}
		};
		correctButton.addActionListener(correctAction);
		buttonPanel.add(correctButton);
		
		incorrectButton = new JButton("Wrong");
		ActionListener incorrectAction = new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEvent) {
				nextFile(false);
			}
		};
		incorrectButton.addActionListener(incorrectAction);
		buttonPanel.add(incorrectButton);
		
		cornerSlider = new JSlider(0, 0);
		cornerSlider.setMinorTickSpacing(1);
		cornerSlider.setPaintTicks(true);
		cornerSlider.setSnapToTicks(true);
		cornerSlider.setPaintLabels(true);
		
		buttonPanel.add(cornerSlider);
		
		nextCornerFileButton = new JButton("Next File");
		ActionListener nextCornerFileAction = new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEvent) {
				getNextCornerFile();
			}
		};
		nextCornerFileButton.addActionListener(nextCornerFileAction);
		
		manualCornerButton = new JButton("Manual Selection");
		ActionListener manualCornerAction = new ActionListener() {
			
			public void actionPerformed(ActionEvent actionEvent) {
				manualSelection();
			}
		};
		manualCornerButton.addActionListener(manualCornerAction);
		
		if (this.debugAccuracyType == AccuracyType.AllOrNothing) {
			cornerSlider.setEnabled(false);
		}
	}
	

	/**
	 * Gets all stroke files from a given directory
	 * 
	 * @param directory
	 *            Directory to traverse
	 */
	private void getAllStrokes(File directory) {
		dir = directory;
		
		strokeFiles = dir.list();
		
		if (strokeFiles == null) {
			// Either dir does not exist or is not a directory
		}
		else {
			currentFile = 0;
			traversing = true;
			correctButton.setEnabled(true);
			
			if (debugAccuracyType == AccuracyType.AllOrNothing) {
				incorrectButton.setEnabled(true);
			}
			else if (debugAccuracyType == AccuracyType.FalseCorners) {
				falsePositives = 0.0;
				falseNegatives = 0.0;
				correctCorners = 0.0;
				totalCorners = 0.0;
				accuracy = 0.0;
			}
			
			String filename = strokeFiles[currentFile];
			
			if (filename.endsWith(".xml")) {
				TStroke s = getStroke(new File(dir + "\\" + filename));
				drawPanel.setStroke(s);
				
				if (debugAccuracyType == AccuracyType.FalseCorners) {
					CornerFinder.SegType[] correctSegments = correctSegments(strokeFiles[currentFile]);
					int nCorrect = correctSegments.length + 1;
					
					cornerSlider.setMinimum(0);
					cornerSlider.setMaximum(drawPanel.getCorners().size());
					
					if (nCorrect < cornerSlider.getMaximum()) {
						cornerSlider.setValue(nCorrect);
					}
					else {
						cornerSlider.setValue(cornerSlider.getMaximum());
					}
				}
			}
		}
	}
	

	/**
	 * Gets the next file in the directory
	 * 
	 * @param correct
	 *            If the previous file was correct
	 */
	private void nextFile(boolean correct) {
		// Process the last file
		if (correct) {
			if (debugAccuracyType == AccuracyType.AllOrNothing) {
				allOrNothing += 1.0;
			}
			else if (debugAccuracyType == AccuracyType.FalseCorners) {
				CornerFinder.SegType[] correctSegments = correctSegments(strokeFiles[currentFile]);
				int nCorrect = correctSegments.length + 1;
				int falsePos = cornerSlider.getMaximum() - nCorrect;
				int falseNeg = nCorrect - cornerSlider.getValue();
				
				if (falsePos > 0)
					falsePositives += falsePos;
				if (falseNeg > 0)
					falseNegatives += falseNeg;
				
				correctCorners += cornerSlider.getValue();
				totalCorners += nCorrect;
				
				if (cornerSlider.getValue() == nCorrect
				    && cornerSlider.getMaximum() == nCorrect) {
					allOrNothing += 1.0;
				}
			}
		}
		
		// Go to the next file
		currentFile++;
		while (currentFile < strokeFiles.length
		       && !strokeFiles[currentFile].endsWith(".xml")) {
			currentFile++;
		}
		
		if (currentFile < strokeFiles.length) {
			TStroke s = getStroke(new File(dir + "\\"
			                               + strokeFiles[currentFile]));
			
			setTitle(strokeFiles[currentFile]);
			drawPanel.setStroke(s);
			
			if (debugAccuracyType == AccuracyType.FalseCorners) {
				CornerFinder.SegType[] correctSegments = correctSegments(strokeFiles[currentFile]);
				int nCorrect = correctSegments.length + 1;
				
				cornerSlider.setMinimum(0);
				cornerSlider.setMaximum(drawPanel.getCorners().size());
				
				if (nCorrect < cornerSlider.getMaximum()) {
					cornerSlider.setValue(nCorrect);
				}
				else {
					cornerSlider.setValue(cornerSlider.getMaximum());
				}
			}
		}
		else {
			traversing = false;
			
			correctButton.setEnabled(false);
			incorrectButton.setEnabled(false);
			
			allOrNothing = allOrNothing / (double) strokeFiles.length;
			
			if (this.debugAccuracyType == AccuracyType.FalseCorners) {
				System.out.println("False Positives: " + falsePositives);
				System.out.println("False Negatives: " + falseNegatives);
				System.out.println("Correct corners found: " + correctCorners);
				System.out.println("Total (correct) corners possible: "
				                   + totalCorners);
				System.out.println();
				System.out.println("Correct Corners Accuracy = "
				                   + correctCorners / totalCorners);
			}
			
			System.out.println("All-or-Nothing = " + allOrNothing);
		}
	}
	

	/**
	 * Gets the stroke from the file
	 * 
	 * @param file
	 *            Stroke file
	 * @return The TStroke in the file
	 */
	private TStroke getStroke(File file) {
		
		List<TStroke> strokesRead;
		
		if (this.debugDataType == DataType.UserStudy
		    || this.debugDataType == DataType.LineTrain) {
			strokesRead = TStroke.getTStrokesFromXML_old(FileHelper.load(file));
		}
		else {
			strokesRead = TStroke.getTStrokesFromXML(FileHelper.load(file));
		}
		
		TStroke s = null;
		
		// Get only the first stroke
		try {
			s = strokesRead.get(0);
		}
		catch (IndexOutOfBoundsException e) {
			System.err.println("Error: no strokes in the file!");
		}
		
		return s;
	}
	

	/**
	 * Automatically gets the accuracy of a directory
	 * 
	 * @param dir
	 *            Directory to traverse
	 */
	private void getAccuracyOfDir(File dir) {
		double correct = 0.0;
		double totalFiles = 0.0;
		
		try {
			FileOutputStream fout = new FileOutputStream("cornerDebugging.txt");
			PrintStream p = new PrintStream(fout);
			
			String[] files = dir.list();
			if (files == null) {
				// Either dir does not exist or is not a directory
			}
			else {
				CornerFinder.SegType[] previousSegments = null;
				int currSegCorrect = 0;
				int currSegTotal = 0;
				
				for (int f = 0; f < files.length; f++) {
					String filename = files[f];
					
					if (filename.endsWith(".xml")) {
						TStroke s = getStroke(new File(dir + "\\" + filename));
						
						// Get the stroke's corners
						ArrayList<Integer> corners = cf.findCorners(s);
						List<TPoint> pts = cf.getStroke().getPoints();
						
						// Get the ordered segments we find
						CornerFinder.SegType[] cornerSegments = cf
						        .strokeSegments(corners);
						
						// Get the actual segments the stroke should contain
						CornerFinder.SegType[] correctSegments = correctSegments(filename);
						
						if (previousSegments == null) {
							previousSegments = correctSegments;
						}
						
						// Update correct & total values
						if (dataSegmentsMatch(previousSegments, correctSegments)) {
							currSegTotal++;
						}
						else {
							p.print("\n");
							p.println("Accuracy = " + (double) currSegCorrect
							          / (double) currSegTotal + " ("
							          + currSegCorrect + "/" + currSegTotal
							          + ")");
							p.println();
							p.println();
							
							correct += currSegCorrect;
							totalFiles += currSegTotal;
							
							currSegCorrect = 0;
							currSegTotal = 1;
							
							previousSegments = correctSegments;
						}
						
						System.out.println("Looking at " + filename);
						p.print("Looking at " + filename + ": ");
						
						// See if the number of corners is equal for both
						
						// POWERSET
						// if (cf.powersetTest(corners, correctSegments))
						if (dataSegmentsMatch(cornerSegments, correctSegments)) {
							p.print(1 + "\n");
							currSegCorrect++;
						}
						else {
							p.print(0 + "\n");
						}
						
						// Segment output
						if (SEGMENT_OUTPUT) {
							for (int i = 0; i < cornerSegments.length; i++) {
								if (i == 0)
									p.print("[");
								
								p.print(cornerSegments[i]);
								
								if (i < cornerSegments.length - 1)
									p.print(", ");
								else
									p.print("]");
							}
							
							p.print("\t");
							
							for (int i = 0; i < correctSegments.length; i++) {
								if (i == 0)
									p.print("[");
								
								p.print(correctSegments[i]);
								
								if (i < correctSegments.length - 1)
									p.print(", ");
								else
									p.print("]");
							}
							
							p.print("\n");
						}
					}
				}
			}
			
			p.println("Correct:  " + correct);
			p.println("Total:    " + totalFiles);
			p.println("Accuracy: " + (double) correct / (double) totalFiles);
			
			System.out.println("Finished! Accuracy = " + (double) correct
			                   / (double) totalFiles);
			
			fout.close();
		}
		catch (IOException e) {
			System.err.println("Error: could not print to file");
			System.exit(-1);
		}
	}
	

	private void getCornersOfDir(File directory) {
		dir = directory;
		strokeFiles = dir.list();
		currentFile = 0;
		
		buttonPanel.remove(correctButton);
		buttonPanel.remove(incorrectButton);
		buttonPanel.remove(cornerSlider);
		
		buttonPanel.add(nextCornerFileButton);
		buttonPanel.add(manualCornerButton);
		buttonPanel.repaint();
		this.repaint();
		
		pack();
		
		cf = new MergeCornerFinder();
		
		getNextCornerFile();
	}
	

	private void getNextCornerFile() {
		currentSelectedCorner = 0;
		drawPanel.displayCorner(-1);
		
		if (strokeFiles == null) {
			// Either dir does not exist or is not a directory
		}
		else {
			if (currentFile > 0) {
				printCornerFile();
			}
			
			traversing = true;
			correctButton.setEnabled(true);
			
			String filename = strokeFiles[currentFile];
			currentFile++;
			
			if (filename.endsWith(".xml")) {
				try {
					int origname = filename.length() - 4;
					String cornername = filename.substring(0, origname);
					cornername += "_corners.txt";
					cornersFOut = new FileOutputStream(dir + "\\" + cornername);
					
				}
				catch (IOException e1) {
					
					e1.printStackTrace();
				}
				
				TStroke s = getStroke(new File(dir + "\\" + filename));
				drawPanel.setStroke(s);
				
				// Get the stroke's corners
				cf.findCorners(s);
			}
		}
	}
	

	private void manualSelection() {
		drawPanel.blankCorners();
		drawPanel.requestFocusInWindow();
		repaint();
	}
	

	private void printCornerFile() {
		PrintStream pStream = new PrintStream(cornersFOut);
		
		for (int c : drawPanel.getCorners()) {
			pStream.print(c + " ");
		}
		
		pStream.close();
	}
	

	private boolean dataSegmentsMatch(CornerFinder.SegType[] data1,
	        CornerFinder.SegType[] data2) {
		boolean match = false;
		
		if (data1.length == data2.length) {
			match = true;
			
			for (int i = 0; i < data1.length; i++) {
				if (data1[i] != data2[i]) {
					match = false;
					i = data1.length;
				}
			}
		}
		
		return match;
	}
	

	private CornerFinder.SegType[] correctSegments(String filename) {
		if (filename.contains("line")) {
			int numLines = Integer.parseInt(filename.substring(0, 1));
			CornerFinder.SegType[] fileSegsArray = new CornerFinder.SegType[numLines];
			
			for (int i = 0; i < numLines; i++)
				fileSegsArray[i] = CornerFinder.SegType.Line;
			
			return fileSegsArray;
		}
		else if (filename.contains("arc")) {
			return new CornerFinder.SegType[] { CornerFinder.SegType.Arc };
		}
		else {
			ArrayList<CornerFinder.SegType> fileSegs = new ArrayList<CornerFinder.SegType>();
			
			for (int i = 0; i < filename.length(); i++) {
				if (filename.charAt(i) == 'L')
					fileSegs.add(CornerFinder.SegType.Line);
				else if (filename.charAt(i) == 'A')
					fileSegs.add(CornerFinder.SegType.Arc);
				else
					break;
			}
			
			CornerFinder.SegType[] fileSegsArray = new CornerFinder.SegType[fileSegs
			        .size()];
			fileSegs.toArray(fileSegsArray);
			
			return fileSegsArray;
		}
	}
	

	private void changeCornerFinderType(CornerFinderType cfType) {
		switch (cfType) {
			case Merge:
				cf = new MergeCornerFinder();
				break;
			case Sezgin:
				cf = new SezginCornerFinder();
				break;
			case Kim:
				cf = new KimCornerFinder();
				break;
			case ShortStraw:
				cf = new ShortStrawCornerFinder();
				break;
			case Brandon:
				cf = new BrandonCornerFinder();
				break;
			default:
				debugFolder = userStudyData;
		}
		
		drawPanel.setCornerFinder(cf);
		this.cfType = cfType;
	}
	

	private void changeDataType(DataType dType) {
		switch (dType) {
			case UserStudy:
				debugFolder = userStudyData;
				break;
			case Kim:
				debugFolder = kimData;
				break;
			case LineTrain:
				debugFolder = lineTrain;
				break;
			case LineTest:
				debugFolder = lineTest;
				break;
			default:
				debugFolder = userStudyData;
		}
		
		this.debugDataType = dType;
	}
	

	private void changeAccuracyType(AccuracyType aType) {
		switch (aType) {
			case AllOrNothing:
				correctButton.setText("Correct");
				correctButton.setEnabled(true);
				incorrectButton.setEnabled(true);
				cornerSlider.setEnabled(false);
				break;
			
			case FalseCorners:
				correctButton.setText("Next");
				correctButton.setEnabled(true);
				incorrectButton.setEnabled(false);
				cornerSlider.setEnabled(true);
				break;
		}
		
		this.buttonPanel.repaint();
		this.debugAccuracyType = aType;
		
		// Possibly also disable automatic accuracy...
	}
	

	/** Handle the key typed event from the text field. */
	public void keyTyped(KeyEvent e) {
		// System.out.println("Typed: " + e.getKeyChar());
	}
	

	/** Handle the key-pressed event from the text field. */
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				if (currentSelectedCorner > 0)
					currentSelectedCorner--;
				
				drawPanel.displayCorner(currentSelectedCorner);
				break;
			
			case KeyEvent.VK_RIGHT:
				if (currentSelectedCorner < drawPanel.getStroke().numPoints() - 1)
					currentSelectedCorner++;
				
				drawPanel.displayCorner(currentSelectedCorner);
				break;
			
			default:
				break;
		}
		
	}
	

	/** Handle the key-released event from the text field. */
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_ENTER:
				drawPanel.addSelectedCorner(currentSelectedCorner);
				break;
			
			default:
				break;
		}
	}
	

	public static void main(String args[]) {
		JFrame dw = new CornerFinderDebug();
		dw.setVisible(true);
	}
}