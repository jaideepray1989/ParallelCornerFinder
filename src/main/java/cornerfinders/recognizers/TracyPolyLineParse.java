/**
 *
 */
package cornerfinders.recognizers;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.hammond.sketch.PointFeatures;

/**
 * @author hammond
 *
 */
public class TracyPolyLineParse {

  public TracyPolyLineParse(StrokeFeatures sf, boolean debug) {
    /**
     * ArrayList<BStroke> fits = new ArrayList<BStroke>(); for(int corner = 0;
     * corner < m_curvatureCorners.size() - 1; corner++){ BStroke stroke =
     * getSubStroke(m_curvatureCorners.get(corner),
     * m_curvatureCorners.get(corner+1) ); stroke.recognize(false);
     * fits.add(stroke); System.out.println("Fit " + corner + " has error " +
     * stroke.getLineFit().getError() + " and length " + stroke.getLength()) ; }
     */
    // collection strategy
    ArrayList<PointFeatures> points = createPointFeatures(sf);

    boolean change = true;
    int numPasses = 5;
    // while(count > 1){
    int count = 0;

    while (change && count < sf.m_x.length + 1) {
      count++;
      change = false;
      // want to do a multipass.

      for (int pass = 1; pass <= numPasses; pass++) {
        if(mergePassThrough(pass, points, false)){change = true;}
      }
      if(mergePassThrough(numPasses, points, false)){change = true;}
    }

    for (PointFeatures point : points) {
      point.iscombo = false;
      point.iscurv = false;
      point.isspeed = false;
    }

    for (int curvCorner : sf.m_curvatureCorners) {
      double x = sf.m_x[curvCorner];
      double y = sf.m_y[curvCorner];
      PointFeatures closestPoint = points.get(0);
      double dist = Math.pow((x - closestPoint.x), 2)
          + Math.pow(y - closestPoint.y, 2);
      for (PointFeatures point : points) {
        double curdist = Math.pow((x - point.x), 2) + Math.pow(y - point.y, 2);
        if (curdist < dist) {
          dist = curdist;
          closestPoint = point;
        }
      }
      closestPoint.iscurv = true;
    }

    for (int speedCorner : sf.m_speedCorners) {
      double x = sf.m_x[speedCorner];
      double y = sf.m_y[speedCorner];
      PointFeatures closestPoint = points.get(0);
      double dist = Math.pow((x - closestPoint.x), 2)
          + Math.pow(y - closestPoint.y, 2);
      for (PointFeatures point : points) {
        double curdist = Math.pow((x - point.x), 2) + Math.pow(y - point.y, 2);
        if (curdist < dist) {
          dist = curdist;
          closestPoint = point;
        }
      }
      closestPoint.isspeed = true;
    }

    for (int finalCorner : sf.m_finalCorner) {
      double x = sf.m_x[finalCorner];
      double y = sf.m_y[finalCorner];
      PointFeatures closestPoint = points.get(0);
      double dist = Math.pow((x - closestPoint.x), 2)
          + Math.pow(y - closestPoint.y, 2);
      for (PointFeatures point : points) {
        double curdist = Math.pow((x - point.x), 2) + Math.pow(y - point.y, 2);
        if (curdist < dist) {
          dist = curdist;
          closestPoint = point;
        }
      }
      closestPoint.iscombo = true;
    }

    for (int i = points.size() - 2; i >= 1; i--) {
      PointFeatures point = points.get(i);

      if (!point.iscombo && !point.iscurv) {
        PointFeatures p0 = points.get(i - 1);
        PointFeatures p2 = points.get(i + 1);
        double diff = Math.min(Math.abs(point.dir - p0.dir), Math.abs(point.dir
            - p2.dir));
        double ratio = Math.min(Math.min(point.l / (point.l + p2.l), p2.l
            / (point.l + p2.l)), Math.min(point.l / (point.l + p0.l), p0.l
            / (point.l + p0.l)));
        if (diff < .35) {
          points.remove(point);
        }
        if (ratio < .2 && diff < Math.PI / 3.5) {
          points.remove(point);
        }
      }
    }
    double x[] = new double[points.size()];
    double y[] = new double[points.size()];
    double t[] = new double[points.size()];
    int element = 0;
    sf.m_tracyCorners = new ArrayList<Integer>();
    for (PointFeatures point : points) {

      // point.print();
      x[element] = point.x;
      y[element] = point.y;
      t[element] = point.time;
      element++;
      sf.m_tracyCorners.add(point.orignum);
    }
    sf.m_xTracyCorner = x;
    sf.m_yTracyCorner = y;
    /*
     * System.out.println("num points: " + points.size()); Plot plot = new
     * Plot("Tracy Corners"); plot.addLine(m_x, m_y, Color.cyan, 10);
     * plot.addLine( x, y, Color.black, 10); plot.plot();
     */
    //System.out.println("Collected Points");
    // ArrayList<PointFeatures> points = createPointFeatures();
    for (PointFeatures point : points) {
      if (debug)
        point.print();
    }
  }

  public ArrayList<PointFeatures> sortPoints(ArrayList<PointFeatures> points) {
    ArrayList<PointFeatures> sortedPoints = new ArrayList<PointFeatures>(points);

    for (int i = 0; i < sortedPoints.size() - 1; i++) {
      PointFeatures pi = sortedPoints.get(i);
      for (int j = i + 1; j < sortedPoints.size(); j++) {
        PointFeatures pj = sortedPoints.get(j);
        // System.out.println("processing " + i + " " + j + ": " + pi.orignum +
        // ":l=" + pi.l + " for " + pj.orignum + ":l=" + pj.l);

        if (pj.l > pi.l) {
          // System.out.println("swapping: " + pi.orignum + ":l=" + pi.l + " for
          // " + pj.orignum + ":l=" + pj.l);
          sortedPoints.set(i, pj);
          sortedPoints.set(j, pi);
          // System.out.println("swapping: " + sortedPoints.get(j).orignum +
          // ":l=" + sortedPoints.get(j).l + " for " +
          // sortedPoints.get(i).orignum + ":l=" + sortedPoints.get(i).l);
          pi = pj;
        }
      }
    }
    return sortedPoints;
  }

  public void mergePoints(ArrayList<PointFeatures> sortedPoints,
      ArrayList<PointFeatures> points, PointFeatures p0, PointFeatures p1,
      PointFeatures p2) {
    points.remove(p1);
    sortedPoints.remove(p1);
    p2.dx = p2.dx + p1.dx;
    p2.dy = p2.dy + p1.dy;
    p2.dt = p2.dt + p1.dt;
    p2.l = p2.l + p1.l;
    p2.v = p2.l / p2.dt;
    p2.dir = Math.atan2(p2.dy, p2.dx);
    if (p0 != null) {
      p2.acc = (p2.v - p0.v) / (p2.dt);
      p2.curv = Math.abs(p2.dir - p0.dir) / (p2.l);
      if (p1.l >= p2.l) {
        if (p1.iscombo) {
          p2.iscombo = true;
        }
        if (p1.isspeed) {
          p2.isspeed = true;
        }
        if (p1.iscurv) {
          p2.iscurv = true;
        }
      } else {
        if (p1.iscombo) {
          p0.iscombo = true;
        }
        if (p1.isspeed) {
          p0.isspeed = true;
        }
        if (p1.iscurv) {
          p0.iscurv = true;
        }
      }
    } else {
      p2.acc = (p2.v) / (p2.dt);
      p2.curv = Math.abs(p2.dir) / (p2.l);
      if (p1.iscombo) {
        p2.iscombo = true;
      }
      if (p1.isspeed) {
        p2.isspeed = true;
      }
      if (p1.iscurv) {
        p2.iscurv = true;
      }
    }

    for (int i = 0; i < points.size() - 1; i++) {
      p0 = points.get(i);
      p1 = points.get(i + 1);
      while (p1.dir - p0.dir > Math.PI) {
        p1.dir = p1.dir - 2 * Math.PI;
      }
      while (p0.dir - p1.dir > Math.PI) {
        p1.dir = p1.dir + 2 * Math.PI;
      }
    }
  }

  public boolean mergePassThrough(double strictness,
      ArrayList<PointFeatures> points, boolean debug) {
    //System.out.println("running with strictness: " + strictness
    //    + ", num points = " + points.size());
    boolean change = false;
    //sorts by line length
    ArrayList<PointFeatures> sortedPoints = sortPoints(points);
    if(debug){sortedPoints = (ArrayList<PointFeatures>)points.clone();}
    for (PointFeatures currentPoint : new ArrayList<PointFeatures>(sortedPoints)) {
      if(debug){currentPoint.print();}
      int i = points.indexOf(currentPoint);
      if (i < 1) {
        continue;
      }
      PointFeatures p0 = null;
      if (i > 1) {
        p0 = points.get(i - 2);
      }
      PointFeatures p1 = null;
      if (i > 0) {
        p1 = points.get(i - 1);
      }
      PointFeatures p2 = points.get(i);
      PointFeatures p3 = null;
      if (i < points.size() - 1) {
        p3 = points.get(i + 1);
      }
      // do small messy threshold first
      double messythreshold = .05 * strictness;
      double ratio = Math.min(p1.l / (p1.l + p2.l), p2.l / (p1.l + p2.l));
      if (ratio < .2) {
        messythreshold *= 1.5/(ratio/.2);
      }
//      if (ratio < .1) {
 //       messythreshold *= 2;
  //    }
      if(debug){

        System.out.println("ratio = " + ratio + ", messythreshold = " + messythreshold);
      }
      double diff = Math.abs(p2.dir - p1.dir);
      // System.out.println(i + ":" + p2.l + ": " + diff + " < " +
      // messythreshold + " = " + (diff < messythreshold));
      boolean merge = false;
      double smallline = 2 * strictness;
      boolean p1small = (p1.l < smallline);
      boolean p2small = (p2.l < smallline);
      boolean diffsmall = (diff < messythreshold);
      if ((i == 0 || i == points.size() - 1)
          && (p1small || p2small || diffsmall)) {
        merge = true;
      }
      if (i > 1) {
        // PointFeatures p0 = points.get(i-2);
        double otherdiff = Math.abs(p0.dir - p1.dir);
        // System.out.println("p1.dir = " + p1.dir + ", p2.dir = " + p2.dir + ",
        // p0.dir = " + p0.dir);
        // System.out.println("otherdiff = " + otherdiff);
        if (diff <= otherdiff && (p1small || p2small || diffsmall)) {
          merge = true;
        }
      }
      if (p3 != null) {
        p3 = points.get(i + 1);
        double otherdiff = Math.abs(p2.dir - p3.dir);
        // System.out.println("p1.dir = " + p1.dir + ", p2.dir = " + p2.dir + ",
        // p3.dir = " + p3.dir);
        // System.out.println("otherdiff = " + otherdiff);
        if (diff <= otherdiff && (p2small || diffsmall)) {
          merge = true;
        }
      }
      if (merge && !(p1.orignum == 0)) {
        mergePoints(sortedPoints, points, p0, p1, p2);
        // System.out.println("merged " + p1.orignum);
        change = true;
      }
    }
    return change;
  }

  // created by tracy

  public ArrayList<PointFeatures> createPointFeatures(StrokeFeatures sf) {
    ArrayList<PointFeatures> points = new ArrayList<PointFeatures>();
    for (int i = 0; i < sf.m_size; i++) {
      PointFeatures p = new PointFeatures();
      p.orignum = i;
      p.x = sf.m_x[i];
      p.y = sf.m_y[i];
      p.time = sf.m_time[i];
      if (i > 0) {
        p.dx = sf.m_dx[i - 1];
        p.dy = sf.m_dy[i - 1];
        p.dt = sf.m_dtime[i - 1];
        p.dir = sf.m_dir[i - 1];
        p.l = sf.m_segLength[i - 1];
        p.tl = sf.m_lengthSoFar[i - 1];
        p.v = sf.m_speed[i - 1];
      }
      if (i > 1) {
        p.acc = sf.m_acceleration[i - 2];
        p.curv = sf.m_curvature[i - 2];
        p.curvall = sf.m_totalCurvature[i - 2];
      }
      for (int j = 0; j < sf.m_xcurvCorner.length; j++) {
        if (sf.m_xcurvCorner[j] == sf.m_x[i]
            && sf.m_ycurvCorner[j] == sf.m_y[i]) {
          p.iscurv = true;
        }
      }
      for (int j = 0; j < sf.m_xspeedCorner.length; j++) {
        if (sf.m_xspeedCorner[j] == sf.m_x[i]
            && sf.m_yspeedCorner[j] == sf.m_y[i]) {
          p.isspeed = true;
        }
      }
      for (int j = 0; j < sf.m_xfinalCorner.length; j++) {
        if (sf.m_xfinalCorner[j] == sf.m_x[i]
            && sf.m_yfinalCorner[j] == sf.m_y[i]) {
          p.iscombo = true;
        }
      }
      points.add(p);
    }
    return points;
  }



}
