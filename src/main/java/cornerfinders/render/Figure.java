package cornerfinders.render;

import com.google.common.collect.Lists;
import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jaideepray on 12/13/14.
 */


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Figure extends JPanel {
    private static final List<TPoint> pointList = Lists.newArrayList();
    private static final List<TPoint> cornerList = Lists.newArrayList();
    public void drawShape(List<TPoint> shape) {
        pointList.clear();
        pointList.addAll(shape);
        repaint();
    }

    public void drawPoint(Graphics g, TPoint point) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.RED);
        g2d.drawOval((int) point.getX(), (int) point.getY(), 2, 2);
    }

    public void drawCorner(Graphics g, TPoint point) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLUE);
        g2d.drawOval((int) point.getX(), (int) point.getY(), 2, 2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (TPoint point : this.pointList) {
            drawPoint(g, point);
        }
//        for(TPoint point: this.cornerList){
//            drawCorner(g,point);
//        }
    }


    public void renderShape(List<TPoint> shape) {
        JFrame testFrame = new JFrame();
        final Figure comp = new Figure();
        comp.setPreferredSize(new Dimension(1000, 1000));
        testFrame.getContentPane().add(comp, BorderLayout.CENTER);
        JPanel buttonsPanel = new JPanel();
        JButton detectCorners = new JButton("Detect Corners");
        buttonsPanel.add(detectCorners);
        testFrame.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
        detectCorners.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // detect corners
            }
        });
        testFrame.pack();
        testFrame.setVisible(true);
        drawShape(shape);
    }

    public void renderFigure(List<TStroke> figure,List<TPoint> corners){
        JFrame testFrame = new JFrame();
        final Figure comp = new Figure();
        comp.setPreferredSize(new Dimension(1000, 1000));
        testFrame.getContentPane().add(comp, BorderLayout.CENTER);
        JPanel buttonsPanel = new JPanel();
        JButton detectCorners = new JButton("Detect Corners");
        buttonsPanel.add(detectCorners);
        testFrame.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
        detectCorners.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // detect corners
            }
        });
        testFrame.pack();
        testFrame.setVisible(true);
        List<TPoint> figure_points= new ArrayList<TPoint>();
        for (TStroke s: figure)
        {
                figure_points.addAll(s.getPoints());

        }
        drawShape(figure_points);
    }

    public static void main(String[] args) {
        Figure f = new Figure();
        List<TPoint> list = Lists.newArrayList();
        list.add(new TPoint(572.0, 234.0, 290832));
        f.renderShape(list);
    }

}

