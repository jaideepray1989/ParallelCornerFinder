package cornerfinders.core.shapes;

import com.google.common.collect.Lists;
import cornerfinders.impl.KimCornerFinder;
import cornerfinders.impl.SezginCornerFinder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author hammond
 */
public class TStroke {

    List<TPoint> pointList = Lists.newArrayList();

    /**
     * Create a stroke from the initial point
     *
     * @param startpoint the inital point
     */
    public TStroke(TPoint startpoint) {
        addPoint(startpoint);
    }

    public TStroke() {
    }

    /**
     * Create a stroke from a list of points
     *
     * @param points
     */
    public TStroke(List<TPoint> points) {
        for (TPoint p : points) {
            addPoint(p);
        }
    }

    public void deleteInvalidPoint(TPoint point) {
        this.getPoints().remove(point);
    }


    /**
     * Adds a point to the stroke.
     * Syntactic sugar for addComponent.
     *
     * @param point the point to add.
     */
    public void addPoint(TPoint point) {
        if (numPoints() >= 1) {
            double distance = getLastPoint().distance(point);
            if (distance == 0) {
                return;
            }
        }
        pointList.add(point);
    }


    public RRectangle getBoundingBox() {
        return new RRectangle(getMinX(), getMinY(), getMaxX() - getMinX(), getMaxY() - getMinY());//getPoly().getBounds());
    }

    /**
     * Get the number of points stored in this stroke. (note: Same as
     * numComponents)
     *
     * @return The number of points in the stroke.
     */
    public int numPoints() {
        return pointList.size();
    }

    /**
     * Gets the ith component formated as a point
     *
     * @param i the number of point to get
     * @return the ith point
     */
    public TPoint getPoint(int i) {
        return pointList.get(i);
    }

    /**
     * Returns the first point of the stroke
     *
     * @return first point
     */
    public TPoint getFirstPoint() {
        return pointList.get(0);
    }

    /**
     * Returns the last point of the stroke
     *
     * @return last point
     */
    public TPoint getLastPoint() {
        return pointList.get(pointList.size() - 1);
    }

    /**
     * Returns the length of the stroke
     *
     * @return stroke length
     */
    public double getLength() {
        return pointList.size();
    }

    /**
     * Returns a debug string of the stroke
     *
     * @return
     */
    public String debug() {
        return "STROKE size:" + numPoints() + " length:" + getLength() + ", first:[" + getFirstPoint() + "], last:[" + getLastPoint();
    }


    /**
     * Get a list of all the points in this stroke.
     * Reformats all the components of the shape from a Shape to a Point
     *
     * @return The list of all points.
     */
    public List<TPoint> getPoints() {
        return pointList;
    }


    public static List<TStroke> getTStrokesFromXML(String xml) {
        ArrayList<TStroke> strokes = new ArrayList<TStroke>();
        //System.out.println(xml);
        int closeStroke = xml.indexOf("</stroke>") - 1;
        while (closeStroke > 0) {
            TStroke t = new TStroke();
            String xmlCopy = xml.substring(xml.indexOf("<stroke"));
            int colon = xmlCopy.indexOf("=");
            xmlCopy = xmlCopy.substring(colon + 1);
            boolean b = Boolean.parseBoolean(xmlCopy.substring(xmlCopy.indexOf('"'), xmlCopy.indexOf('"')).trim());
            colon = xmlCopy.indexOf('=');
            xmlCopy = xmlCopy.substring(colon + 1);
            colon = xmlCopy.indexOf('"');
            xmlCopy = xmlCopy.substring(colon + 1);
            Long time = Long.parseLong(xmlCopy.substring(0, xmlCopy.indexOf('"')).trim());
            int end = xml.indexOf(">");
            int dis = end + 1;
            xml = xml.substring(end + 1);
            end = xml.indexOf(">");
            dis += end + 1;
            while (dis < closeStroke) {
                xml = xml.substring(end + 1);
                TPoint p = TPoint.getFromXML(xml);
                if (p != null)
                    t.addPoint(p);
                end = xml.indexOf(">");
                dis += end + 1;
            }
            try {
                xml = xml.substring(xml.indexOf(">") + 1);
                xml = xml.substring(xml.indexOf(">") + 1);
                xml = xml.substring(xml.indexOf("<"));
            } catch (Exception e) {
            }
            closeStroke = xml.indexOf("</stroke>") - 1;
            strokes.add(t);
        }
        return strokes;
    }


    /**
     * Get a scaled version of the stroke
     *
     * @param x     initial x size of window the stroke should fit in
     * @param y     initial y size of window the stroke should fit in
     * @param scale scale factor for stroke
     * @return scaled version of stroke
     */
    public Image getScaledImage(int x, int y, double scale) {
        int offX = (int) getMinX() - 1, offY = (int) getMinY() - 1;
        int xLength = (int) (getMaxX() - getMinX());
        int yLength = (int) (getMaxY() - getMinY());
        BufferedImage image = new BufferedImage(xLength + 2, yLength + 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.BLACK);
        for (int i = 0; i < getPoints().size() - 1; i++) {
            g2.drawLine((int) (getPoint(i).getX() - offX), (int) (getPoint(i).getY() - offY),
                    (int) (getPoint(i + 1).getX() - offX), (int) (getPoint(i + 1).getY() - offY));
        }
        g2.dispose();
        return image.getScaledInstance((int) (x * scale), (int) (y * scale), Image.SCALE_AREA_AVERAGING);
    }

    public double getMaxX() {
        double max = Double.MIN_VALUE;
        for (TPoint p : getPoints()) {
            if (p.getX() > max)
                max = p.getX();
        }
        return max;
    }

    public double getMaxY() {
        double max = Double.MIN_VALUE;
        for (TPoint p : getPoints()) {
            if (p.getY() > max)
                max = p.getY();
        }
        return max;
    }

    public double getMinX() {
        double min = Double.MAX_VALUE;
        for (TPoint p : getPoints()) {
            if (p.getX() < min)
                min = p.getX();
        }
        return min;
    }

    public double getMinY() {
        double min = Double.MAX_VALUE;
        for (TPoint p : getPoints()) {
            if (p.getY() < min)
                min = p.getY();
        }
        return min;
    }

    public static List<TStroke> getTStrokesFromFile(String fileName) {
        List<TStroke> strokes = null;
        try {
            File f = new File(fileName);
            FileReader reader = new FileReader(f);
            BufferedReader in = new BufferedReader(reader);
            String line;
            StringBuilder xml = new StringBuilder();
            TStroke t = new TStroke();
            while ((line = in.readLine()) != null) {
                xml.append(line);
            }
            strokes = getTStrokesFromXML(xml.toString());
            in.close();
            reader.close();
        } catch (IOException ex) {
        }
        return strokes;
    }

    public int getSize() {
        return pointList.size();
    }

    public void printStroke() {
        for (int i = 0; i < this.getPoints().size(); i++) {
            this.getPoint(i).printPoint();
        }
    }

    public static void main(String[] args) {
        String filePath = "C:\\Users\\Anurag Garg\\Documents\\csce624\\ParallelCornerFinder\\src\\main\\java\\cornerfinders\\core\\shapes\\sketchData.xml";
        List<TStroke> strokes = getTStrokesFromFile(filePath);
        int i = 0;
        for (TStroke s : strokes) {
            System.out.println((++i) + " stroke points:" + s.numPoints());
        }
        System.out.println(strokes);
        KimCornerFinder cornerFinder = new KimCornerFinder();

        List<TPoint> cornerList = Lists.newArrayList();

        for (TStroke s : strokes) {
            ArrayList<Integer> corners = cornerFinder.findCorners(s);

            for (Integer index : corners) {
                System.out.println("printing corner :: ");
                s.getPoint(index).printPoint();
                System.out.println("\n ");
            }
        }


    }
}
