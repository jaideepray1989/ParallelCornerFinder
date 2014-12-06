package cornerfinders.impl;

import cornerfinders.core.shapes.TStroke;

import java.awt.Graphics;
import javax.swing.JPanel;

import java.util.ArrayList;
import java.util.Collections;


/**
 * Stroke drawing panel
 * 
 * @author Aaron Wolin
 */
public class StrokePanel extends JPanel
{
	/**
	 * Default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Corner finder
	 */
	private CornerFinder cf;
	
	/**
	 * Stroke showing
	 */
	private TStroke stroke;
	
	/**
	 * Corners found
	 */
	private ArrayList<Integer> corners;
	
	/**
	 * Current corner selected
	 */
	private int currentSelectedCorner;
	
	
	/**
	 * Default constructor
	 */
	StrokePanel() {
		super();
		setBackground(java.awt.Color.WHITE);
			
		stroke = null;
		corners = null;
	}
	
	
	/**
	 * Set the corner finder
	 * @param cf	Corner finder to set
	 */
	public void setCornerFinder(CornerFinder cf)
	{
		this.cf = cf;
	}
	
	
	/**
	 * Sets the stroke and computes the corners for the stroke
	 * 
	 * @param s		Stroke to set
	 */
	public void setStroke(TStroke s)
	{
		this.corners = cf.findCorners(s);
		this.stroke = cf.getStroke();
		
		repaint();
	}
	
	
	/**
	 * 
	 *
	 */
	public void addSelectedCorner(int index)
	{
		this.corners.add(index);
		Collections.sort(corners);
	}
	
	
	public void displayCorner(int index)
	{
		this.currentSelectedCorner = index;
		repaint();
	}
	
	
	/** 
	 * Set Stroke without a corner finder
	 * 
	 * @param s
	 */
	public void setStrokeBlank(TStroke s)
    {
        this.corners = new ArrayList<Integer>();
        this.stroke = s;
        
        this.corners.add(0);
        this.corners.add(s.getPoints().size() - 1);
        
        repaint();
    }
	
	
	public void blankCorners()
	{
        this.corners = new ArrayList<Integer>();
        
        this.corners.add(0);
        this.corners.add(this.stroke.numPoints() - 1);
        
        repaint();
	}
	
	
	/**
	 * Paint override
	 */
	public void paint(Graphics g) {
		
		if (stroke != null && corners != null) {
		
			g.clearRect(0, 0, this.getWidth(), this.getHeight());
			
			g.setColor(java.awt.Color.BLACK);
			
			// Draw corners
			int radius = 10;
			g.setColor(java.awt.Color.RED);
			
			for (Integer corner : corners)
			{
				int x = (int)stroke.getPoint(corner).getX();
				int y = (int)stroke.getPoint(corner).getY();
				
				g.fillOval(x - 5, y - 5, radius, radius);
			}
			
			// Draw selected corner
			if (currentSelectedCorner > 0 && currentSelectedCorner < stroke.numPoints())
			{
				g.setColor(java.awt.Color.BLUE);
				int x = (int)stroke.getPoint(currentSelectedCorner).getX();
				int y = (int)stroke.getPoint(currentSelectedCorner).getY();
				
				g.fillOval(x - 5, y - 5, radius, radius);
			}
		}
	}
	
	
	/*
	 * Getters
	 */

	/**
	 * Returns the corners of the stroke associated with the panel
	 * 
	 * @return	Corners of the stroke
	 */
	public ArrayList<Integer> getCorners()
	{
		return this.corners;
	}
	
	public TStroke getStroke()
	{
		return this.stroke;
	}
}
