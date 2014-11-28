package cornerfinders.impl;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Dimension;
import javax.swing.*;

import java.util.*;
import java.io.*;

import edu.tamu.hammond.io.FileHelper;
import edu.tamu.hammond.sketch.shapes.TPoint;
import edu.tamu.hammond.sketch.shapes.TStroke;


/**
 * NOTE: I don't use this anymore since I merged the important parts of the code
 * with CornerFinderDebug.
 * 
 * If you update it, Dhivya, I've commented out parts of the code that include:
 * 
 * temp_view_corner
 * add_corner
 * 
 * Since those are not in StrokePanel anymore
 */


public class Div_corners extends JFrame implements KeyListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	private enum DataType { UserStudy, Kim };
	private DataType debugDataType = DataType.UserStudy;
	
	private File userStudyData = new File("C:\\Eclipse\\workspace\\CornerFinderData\\UserStudy");
	private File kimData = new File("C:\\Eclipse\\workspace\\CornerFinderData\\Kim\\Files");

	//private File userStudyData = new File("C:\\Users\\Aaron Wolin\\workspace\\SR2007\\src\\awolin\\CornerFinderData\\UserStudy");
	//private File kimData = new File("C:\\Users\\Aaron Wolin\\workspace\\SR2007\\src\\awolin\\CornerFinderData\\Kim");
	
	private File debugFolder = userStudyData;	//change it to Aaron Data
	
	private StrokePanel drawPanel;
	private JPanel buttonPanel;
	private JButton correctButton;
		
	private File dir;
	private String[] strokeFiles;
	private int currentFile = Integer.MAX_VALUE;
	private boolean traversing = false;
	private int i, j;
	private FileOutputStream fout;
	
	
//	 Corner finder components
	
	
	Div_corners(){
		
		drawPanel = new StrokePanel();
		drawPanel.setPreferredSize(new Dimension(1000, 600));
		
		
		setTitle("Corner Debugging");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		initMenu();
		initButtonPanel();
		correctButton.setEnabled(false);
		
		add(drawPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		setPreferredSize(new Dimension(1000, 700));
			
		i = 0;
		j = 0;		
		
		drawPanel.addKeyListener(this);
		drawPanel.setEnabled(true);
		drawPanel.setFocusable(true);
		  
		getAllStrokes(debugFolder);
		pack();	
	}
	
	
	private void initMenu(){
			
		//create Menu Bar
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu dataMenu = new JMenu("Data");
		
		//File Menu: 
		
		JMenuItem fileChooser = new JMenuItem("File Chooser");
		ActionListener fileChooserAction = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {

				// Choose a file to open
				JFileChooser c = new JFileChooser(debugFolder);
				c.setFileSelectionMode(JFileChooser.FILES_ONLY);
				c.setDialogTitle("Select a stroke to open");
				c.showOpenDialog(null);
				File f = c.getSelectedFile();

				// Get the stroke from the file
				TStroke s = getStroke(f);

				setTitle(f.getName());
				drawPanel.setStrokeBlank(s);
			}
	    };
	    fileChooser.addActionListener(fileChooserAction);
	    /*
	     * DEBUG DATA MENU
	     */

	    // Debug Data
	    ActionListener userStudyDataAction = new ActionListener() {
	    	public void actionPerformed(ActionEvent actionEvent) {
	    		changeDataType(DataType.UserStudy);
		    }
	    };

	    ButtonGroup dataGroup = new ButtonGroup();
	    JRadioButtonMenuItem userStudyData = new JRadioButtonMenuItem("User Study Data");
	    userStudyData.setSelected(true);
	    dataGroup.add(userStudyData);
	    userStudyData.addActionListener(userStudyDataAction);

	    ActionListener kimDataAction = new ActionListener() {
	    	public void actionPerformed(ActionEvent actionEvent) {
	    		changeDataType(DataType.Kim);
	    	}
	    };

	    JRadioButtonMenuItem kimData = new JRadioButtonMenuItem("Kim Data");
	    dataGroup.add(kimData);
	    kimData.addActionListener(kimDataAction);
		    
	    fileMenu.add(fileChooser);
	    menuBar.add(fileMenu);
	    dataMenu.add(userStudyData);
	    dataMenu.add(kimData);
	    menuBar.add(dataMenu);
	    
	    setJMenuBar(menuBar);
	  	
	}
	
	
	private void initButtonPanel(){
		this.buttonPanel = new JPanel();
		buttonPanel.setPreferredSize(new Dimension(100, 120));
		
		correctButton = new JButton("Next File");
		ActionListener correctAction = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				nextFile(true);
			}
		};
		correctButton.addActionListener(correctAction);
		buttonPanel.add(correctButton);
	}
	
	
	private void nextFile(boolean correct){ // go to Next File in the folder
		
		//drawPanel.temp_view_corner(0);
    	try {
			fout.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}	 

		//getAllStrokes(debugFolder);
		getNextFile();
	}
	
	
	private void getAllStrokes(File directory)
	{ 
		dir = directory;
		strokeFiles = dir.list();
		
		getNextFile();
	}

//	    drawPanel.removeKeyListener(keyaction);
//		System.out.println(drawPanel.getKeyListeners().length);
//   		drawPanel.addKeyListener(keyaction);
//   drawPanel.setEnabled(true);
//    drawPanel.setFocusable(true);
	
	private void getNextFile()
	{
		j = 0;
	    
		if (strokeFiles == null)
	    {
	        // Either dir does not exist or is not a directory
	    }
	    else
	    {	    		   	
	    	currentFile = i;
	    	traversing = true;
	    	correctButton.setEnabled(true);   	 
	    	    		    	
	    	String filename = strokeFiles[currentFile];
	    		i++;
        	if (filename.endsWith(".xml"))
        	{//PROB: stores in tamudrg directly
        	    try {
        	    	int a = filename.length()-4;
        	    	String name = filename.substring(0, a);
					name +=".txt";
					fout = new FileOutputStream (name);
					
				} catch (IOException e1) {
					
					e1.printStackTrace();
				}   
				 	        		
			   	TStroke s = getStroke(new File(dir + "\\" + filename));
    	    	drawPanel.setStrokeBlank(s);
    	     	
    	    	drawPanel.requestFocusInWindow();
    			
    			System.out.println("j = " + j);      	         	       	    
    	    }
        	
	    }
	}

	private TStroke getStroke(File file)
	{
		List<TStroke> strokesRead;

		if (this.debugDataType == DataType.UserStudy)
		{
			strokesRead = TStroke.getTStrokesFromXML_old(FileHelper.load(file));
		}
		else
		{
			strokesRead = TStroke.getTStrokesFromXML(FileHelper.load(file));
		}

		TStroke s = null;

		// Get only the first stroke
		try
		{
			s = strokesRead.get(0);
		}
		catch (IndexOutOfBoundsException e)
		{
			System.err.println("Error: no strokes in the file!");
		}

		return s;
	}
	
	
	private void changeDataType(DataType dType)
	{
		switch (dType)
		{
		case UserStudy:
			debugFolder = userStudyData;
			break;
		case Kim:
			debugFolder = kimData;
			break;
		default:
			debugFolder = userStudyData;
		}

		this.debugDataType = dType;
	}
	
	
	/** Handle the key typed event from the text field. */
    public void keyTyped(KeyEvent e) 
	{
		//System.out.println("Typed: " + e.getKeyChar());
	}
	
	/** Handle the key-pressed event from the text field. */
    public void keyPressed(KeyEvent e) 
	{
		switch (e.getKeyCode())
		{
	  		case KeyEvent.VK_LEFT: 
	  			j--;
	  			//drawPanel.temp_view_corner(j);
	  			break;
	  	
	  		case KeyEvent.VK_RIGHT: 
	  			j++; //System.out.print("2");
	  			//drawPanel.temp_view_corner(j);
	  			break;
	  		
	  		case KeyEvent.VK_ENTER: 
	  			//drawPanel.add_corner(j);
	  			new PrintStream(fout).println (j);
	  			break;
	  		
	  		default:
	  			break;
		}
	}
	
	/** Handle the key-released event from the text field. */
    public void keyReleased(KeyEvent e) {
    	//System.out.println("Released: " + e.getKeyChar());
    }
    
	
	public static void main(String[] args) {
		
		JFrame dw = new Div_corners();
		dw.setVisible(true);
	}

}
