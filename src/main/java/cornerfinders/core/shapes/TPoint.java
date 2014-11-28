package cornerfinders.core.shapes;

import com.google.common.collect.Lists;

import java.util.ArrayList;

public class TPoint {
    private ArrayList<Double> m_xList = Lists.newArrayList();
    private ArrayList<Double> m_yList = Lists.newArrayList();
    private int m_currentElement = -1;
    private Long m_time;
    private int m_pressure = 1;


    public TPoint setOrig() {
        m_currentElement = 0;
        return this;
    }

    public TPoint setCurrent() {
        m_currentElement = m_xList.size() - 1;
        return this;
    }

    /**
     * Clone the point
     * Returns an exact copy of this point
     */
    @Override
    public TPoint clone() {
        ArrayList<Double> xlist = new ArrayList<Double>();
        ArrayList<Double> ylist = new ArrayList<Double>();
        for (int i = 0; i < m_xList.size(); i++) {
            xlist.add((double) m_xList.get(i));
            ylist.add((double) m_yList.get(i));
        }
        TPoint rp = new TPoint(getX(), getY(), getTime());
        rp.m_xList = xlist;
        rp.m_yList = ylist;
        rp.m_currentElement = m_currentElement;
        return rp;
    }

    public void setTime(long t) {
        m_time = t;
    }

    public void setP(double x, double y) {
        m_xList.add(x);
        m_yList.add(y);
        m_currentElement = m_xList.size() - 1;
    }

    public void setOrigP(double x, double y) {
        m_xList = new ArrayList<Double>();
        m_yList = new ArrayList<Double>();
        setP(x, y);
    }

    public TPoint(double x, double y) {
        setP(x, y);
    }

    public double getX() {
        return m_xList.get(m_currentElement);
    }

    public double getY() {
        return m_yList.get(m_currentElement);
    }

    public double getMinX() {
        return getX();
    }

    public double getMinY() {
        return getY();
    }

    public double getMaxX() {
        return getX();
    }

    public double getMaxY() {
        return getY();
    }

    public double getWidth() {
        return 1;
    }

    public double getHeight() {
        return 1;
    }

    public double getArea() {
        return 1;
    }

    public long getTime() {
        return m_time;
    }

    /**
     * Return the distance from point rp to this point.
     *
     * @param rp the other point
     * @return the distance
     */
    public double distance(TPoint rp) {
        return distance(rp.getX(), rp.getY());
    }

    public TPoint(double x, double y, long time) {
        m_xList.add(x);
        m_yList.add(y);
        m_time = time;
    }


    public TPoint(String data) {
        int comma = data.indexOf(",");
        String x = data.substring(0, comma).trim();
        data = data.substring(comma + 1);
        comma = data.indexOf(",");
        String y = data.substring(0, comma).trim();
        data = data.substring(comma + 1);
        String t = data.trim();
        setP(Double.parseDouble(x), Double.parseDouble(y));
        setTime(Long.parseLong(t));
    }

    public static TPoint getFromXML(String xml) {
        double x, y;
        long t;
        int eq = xml.indexOf("=");
        xml = xml.substring(eq + 2);
        int space = xml.indexOf('"');
        try {
            x = Double.parseDouble(xml.substring(0, space));
            eq = xml.indexOf("=");
            xml = xml.substring(eq + 2);
            space = xml.indexOf('"');
            y = Double.parseDouble(xml.substring(0, space));
            eq = xml.indexOf("=");
            xml = xml.substring(eq + 2);
            space = xml.indexOf('"');
            t = Long.parseLong(xml.substring(0, space));
            return new TPoint(x, y, t);
        } catch (Exception e) {
            return null;
        }
    }

    public static TPoint getFromXML_old(String xml) {
        double x, y;
        long t;
        int eq = xml.indexOf("=");
        xml = xml.substring(eq + 1);
        int space = xml.indexOf(" ");
        try {
            x = Double.parseDouble(xml.substring(0, space));
            eq = xml.indexOf("=");
            xml = xml.substring(eq + 1);
            space = xml.indexOf(" ");
            y = Double.parseDouble(xml.substring(0, space));
            eq = xml.indexOf("=");
            xml = xml.substring(eq + 1);
            space = xml.indexOf("/");
            t = Long.parseLong(xml.substring(0, space));
            return new TPoint(x, y, t);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Return the distance from the point specified by (x,y) to this point
     *
     * @param x the x value of the other point
     * @param y the y value of the other point
     * @return the distance
     */
    public double distance(double x, double y) {
        double xdiff = x - getX();
        double ydiff = y - getY();
        return Math.sqrt(xdiff * xdiff + ydiff * ydiff);
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        double xdiff = x1 - x2;
        double ydiff = y1 - y2;
        return Math.sqrt(xdiff * xdiff + ydiff * ydiff);
    }

}
