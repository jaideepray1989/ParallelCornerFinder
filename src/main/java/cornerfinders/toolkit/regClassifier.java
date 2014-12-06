package cornerfinders.toolkit;

import edu.mit.sketch.geom.Arc;
import edu.mit.sketch.geom.CompositeGeometricObject;
import edu.mit.sketch.geom.Ellipse;
import edu.mit.sketch.geom.GeneralPath;
import edu.mit.sketch.geom.GeometricObject;
import edu.mit.sketch.geom.Line;
import edu.mit.sketch.geom.Point;
import edu.mit.sketch.geom.Polygon;
import edu.mit.sketch.geom.Spiral;
import edu.mit.sketch.geom.Vertex;
import edu.mit.sketch.util.AWTUtil;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;

public class   regClassifier {

  public static int POLYGON 	= 0;
  public static int ELLIPSE 	= 1;
  public static int COMPLEX	= 2;
  public static int LINE 	= 3;
  public static int ARC 	= 4;
  public static int SPIRAL 	= 5;
  public static int CCURVE      = 6;
  public static int OCURVE      = 7;
  public static int POLYLINE    = 8;
  public static int TOOSMALL	= 9;
//DOT, DASH instead?
//SRIBLE?  
  public static double dd_dt_average_scale = 1.0;
  public static double speed_average_scale =.6;
 
  protected StrokeData stroke_data;
  
  protected boolean isClassified = false;
  protected int classification = -1;
  protected int len; 
  protected ArrayList div_points; 
  protected ArrayList parts;
  protected ArrayList segments; 
  protected boolean has_intersect = false;

/*************
  w vectors for closed, straight, long
  ***************/

/*****tablet data******/
//  protected double[] w_straight = {-58.7794, -0.0106, 68.4796, 0};
//  protected double[] w_closed = {.5139, -.2842, 2.4366, .0246};
//  protected double[] w_long = {-3.5583, 0.3999, -4.3741, .9128};

  protected int curvature_window = 10;
  protected int curve_length = 5;

  //array for speed and dir sum
  protected int TOOSMALL_bound = 9;
  protected double tail_size;
  protected double corner_radius; 


  public regClassifier( StrokeData stroke_data ) {
    this.stroke_data = stroke_data;
    len = stroke_data.vertices.length; 
    tail_size =15+ len*.07;
    corner_radius = 10;

  }


  protected int classify(){
    if (isClassified ) {
      return classification;
    }

    div_points = new ArrayList();   
    parts = new ArrayList();
    segments = new ArrayList();


    //only call what's needed here instead
    stroke_data.deriveProperties();
    stroke_data.derivePolarCoordinates();
    Vertex points[] = stroke_data.vertices;    
    Vertex speed_fit[] = AWTUtil.simplifyPolygon2( stroke_data.getSpeedFit( speed_average_scale ), Math.PI/10 );
    Vertex direction_fit[] = AWTUtil.simplifyPolygon2( stroke_data.getDirectionFit(dd_dt_average_scale ), Math.PI/10 );
    Vertex polyfit[] = Blackboard.decide( speed_fit, direction_fit, points, stroke_data );
    //Blackboard.printPoints(polyfit, "poly");
    Vertex union[]  = Blackboard.getUnion(speed_fit, direction_fit);
    //   Blackboard.printPoints(union, "union");
    //Blackboard.printPoints(direction_fit, "dir");

    double dd_sum = 0;
    int dd_len = stroke_data.dd_dt.length;
    for (int i = 0; i<dd_len;i++){
      dd_sum =dd_sum+ Math.abs(stroke_data.dd_dt[i]);
    } 

    //Get Corners  
    double dir_sum =0;
    double speed_sum=0;
    double curve_peak=0;
    Vertex centroid;
    int num;
    int index;
    double min_dist;
    for (int i = 0; i<union.length-1; i++) {
      if(union[i].time_stamp ==0){
        if((union[i].index>1)&&(union[i].index<stroke_data.d.length-1)){
          if (Math.abs(stroke_data.d[union[i].index+1]-stroke_data.d[union[i].index])<5.7){
            dir_sum = union[i].certainty;
          }
        }
        if ((i<=1)||(i>=union.length-2)) {
          dir_sum = union[i].certainty;
        } 
     }
     else {
       speed_sum = union[i].certainty;
     }
     curve_peak=Math.abs( getCurvature(i, 5, stroke_data));
     num = 1;    
     centroid = union[i];
     while(((i+1)<union.length)&&(centroid.distance(union[i+1])<corner_radius)) {
  
      if (union[i+1].time_stamp ==0) {
        if((union[i].index+1>1)&&(union[i].index+1<stroke_data.d.length-1)){
          if (Math.abs(stroke_data.d[union[i].index]-stroke_data.d[union[i].index+1])<5.7){
            dir_sum = dir_sum+union[i+1].certainty;
          }
        }
      }
      else {
        speed_sum = speed_sum+union[i+1].certainty;
    
      }
      centroid.x = (centroid.x*num +union[i+1].x)/(num+1);
      centroid.y = (centroid.y*num +union[i+1].y)/(num+1); 
      if  (curve_peak <Math.abs(getCurvature(i+1, 5, stroke_data))){
        curve_peak =Math.abs(getCurvature(i+1, 5, stroke_data));
      }    

      num++;
      i++;
    }
  
    index = union[i-num+1].index;
    min_dist = 1000;
    for (int j = i-num+1;j<=i;j++){
      if(union[j].distance(centroid)<min_dist){
        index = union[j].index;
        min_dist = union[j].distance(centroid);
      }
    }
    double dir = 0;
    if ((index<stroke_data.dd_dt.length)&&(Math.abs(stroke_data.d[index+1]-stroke_data.d[index])<6)){
      dir = (stroke_data.dd_dt[index]/(dd_sum/dd_len));
    }


    if((speed_sum!=0)&& (-1+3.3*speed_sum+1.8*dir_sum>0)){

      div_points.add(new Integer(index));
    }
 

    /*System.out.println("**index ="+index); 
    System.out.println((-1+3.3*speed_sum+1.8*dir_sum));
    System.out.println("speed sum="+speed_sum);
     System.out.println("dir sum="+dir_sum);
    System.out.println("curv = "+curve_peak);
    System.out.println();
    *//*System.out.println("avg dd = "+(dd_sum/dd_len));
    if (index<stroke_data.dd_dt.length){
      System.out.println(stroke_data.dd_dt[index]/(dd_sum/dd_len));
    } 
    *//*if(index<stroke_data.d.length-1){
      System.out.println("d");
      System.out.println(stroke_data.d[index+1]);
      System.out.println(stroke_data.d[index]);
    }*/
    speed_sum = 0;
    dir_sum = 0;
    } 


  //Get Intersections
  for(int i=0;i<points.length-1;i++) {
    for(int j=0; j<i-1; j++) {
      Line2D.Double l1 = new Line2D.Double(points[i], points[i+1]);
      Line2D.Double l2 = new Line2D.Double(points[j], points[j+1]);
      if (l1.intersectsLine(l2)) {
        div_points.add(new Integer(i));
        div_points.add(new Integer(j)); 
      }
    }
  }

  Collections.sort(div_points);
  Integer first = new Integer(0);
  Integer second = new Integer(0);
  ListIterator iter1 = div_points.listIterator(); 
  if(iter1.hasNext()) {
    first = (Integer)iter1.next();
  }
  while (iter1.hasNext()) {
    second = (Integer)iter1.next(); 
    if (second.intValue()==first.intValue() ) {
      iter1.remove(); 
     }
    first = second;
  }
  Collections.sort(div_points);
  if(div_points.size()>2){
    first = (Integer)div_points.get(0);
    second = (Integer)div_points.get(1);
    if(stroke_data.accumulated_length[second.intValue()]-stroke_data.accumulated_length[first.intValue()]<tail_size){
      div_points.remove(0);
    }
  }
 
  if(div_points.size()>2){
    first = (Integer)div_points.get(div_points.size()-2);
    second = (Integer)div_points.get(div_points.size()-1);
    if(stroke_data.accumulated_length[second.intValue()]-stroke_data.accumulated_length[first.intValue()]<tail_size){
      div_points.remove(div_points.size()-1);
    }
  }     
  Integer new_start = (Integer)div_points.get(0);
  Integer new_end = (Integer)div_points.get(div_points.size()-1);
  for (int i=new_start.intValue();i<new_end.intValue();i++) {
    for(int j=0; j<i-1; j++) {
      Line2D.Double l1 = new Line2D.Double(points[i], points[i+1]);
      Line2D.Double l2 = new Line2D.Double(points[j], points[j+1]);

      if (l1.intersectsLine(l2)) {
        has_intersect = true;

      }

    }
  }
 
  Collections.sort(div_points);
  ListIterator iter = div_points.listIterator(); 
  Integer start = new Integer(0);
  if (iter.hasNext()) {
    start = (Integer)iter.next();
  } 
  while( iter.hasNext()) {
    Integer end = (Integer)iter.next();
    int stroke_len  = end.intValue()-start.intValue()+1;
    if(stroke_len>3) {  
      Vertex[] vertices = new Vertex[stroke_len];
      for (int i=0;i<stroke_len;i++){
        vertices[i] = stroke_data.vertices[start.intValue()+i];
      }
      StrokeData stroke = new StrokeData(vertices);
      int temp; 
      temp = simple_classify(stroke);
        
      GeometricObject fit = simple_getFit(temp, stroke, stroke_len);
      parts.add(fit); 
      //=System.out.println(start+" "+end+" "+temp);
      segments.add(new Integer(temp));
    }
    start = end;
  }
   
 
  int result;
  if ((segments.size()==0)||((stroke_data.bounding_box.getWidth()<TOOSMALL_bound)&&(stroke_data.bounding_box.getHeight()<TOOSMALL_bound))) {
    result = TOOSMALL; 
  }
  else if (segments.size()==1){
    result =((Integer)segments.get(0)).intValue() ;
  }
  else { 
  /*  double theta_dif = Math.abs(stroke_data.polar_points[0].theta-stroke_data.polar_points[len-1].theta);
    double ed = stroke_data.getVertices()[len-1].distance(stroke_data.getVertices()[0]);
    double arclen = stroke_data.accumulated_length[len-1];
    */ 
    if(!has_intersect && test_closed(stroke_data) && (segments.size()>2)) {
      result = POLYGON;
    }
    else {
      result = POLYLINE;
    }
    for(int i = 0; i<segments.size(); i++) {
      if( ((Integer)segments.get(i)).intValue() !=LINE) { 
        result = COMPLEX;
      }
      
    } 
   } 
   isClassified = true;  
   classification = result; 

   return result;
  }


  protected int simple_classify(StrokeData stroke) {
   
    int result;
/*here
    stroke.deriveProperties();
    stroke.derivePolarCoordinates();
    int stroke_len = stroke.getVertices().length;
    double ed = stroke.getVertices()[stroke_len-1].distance(stroke.getVertices()[0]);
    double arclen = stroke.accumulated_length[stroke_len-1];   
    double bb_radius = (stroke.radius.width+stroke.radius.height)/2;
    double theta_dif = Math.abs(stroke.polar_points[0].theta-stroke.polar_points[stroke_len-1].theta);
    double curve = 0;
    for (int j = 0; j< stroke.vertices.length; j++) {
      curve = curve + Math.abs(getCurvature(j,curvature_window, stroke));
    }
*/
   // boolean straight = test_straight(ed, ed/arclen, curve);
    boolean straight = test_straight(stroke);
    //boolean  big = test_long(arclen, bb_radius, ed);
    boolean big = test_long(stroke);
    //boolean closed = test_closed(ed, theta_dif, arclen);
    boolean closed = test_closed(stroke);
    boolean inflect = test_inflection(stroke);


    if (straight) {
      result = LINE;
    }
    else if(inflect) {
      if (closed){
        result = CCURVE;
      }
      else {
        result = OCURVE;
      }
    }
    else {
     if (big) {
      result = SPIRAL;
    }
    else if (closed) {
      result = ELLIPSE;
    }
    else {
      result = ARC;
    }
  } 
    return result;
    
  }


  
  

  protected double getCurvature(int index, int window_size, StrokeData stroke) {
    int w =(int) Math.ceil(window_size/2.0);
    int start, end;
    if (index >=w){
      start = index-w;
    }
    else {
      start = 0;
    }
    if (index <stroke.vertices.length-w){
      end = index+w;
    }
    else {
      end = stroke.vertices.length-1;
    }
    double dx = stroke.vertices[start].x-stroke.vertices[end-1].x;
    double dx2 = stroke.vertices[start+1].x-stroke.vertices[end].x;
    
    double dy = stroke.vertices[start].y-stroke.vertices[end-1].y;
    double dy2 = stroke.vertices[start+1].y-stroke.vertices[end].y;

    double ddy =dy-dy2;
    double ddx = dx-dx2;

    double num = dx*ddy-dy*ddx;
    double den = Math.sqrt(Math.pow(dx*dx+dy*dy, 3));
    return num/den;
  }



  protected double dot_product(double[] array1, double[] array2) {
    double result = 0; 
    if(array1.length == array2.length) {
      for( int i = 0; i< array1.length; i++) { 
        result = result + array1[i]*array2[i];
      }
     }
     return result;
  }
     
    

 
 /* protected boolean test_straight(double ed, double ratio, double curve) {
    //double z = w_straight[0]+w_straight[1]*ed+w_straight[2]*ratio+w_straight[3]*curve;
    double z = -6.7241-0.2345*ed+10.6793*ratio+.2244*ed*ratio+6.7048*ratio*ratio;

    return z>0; 

  }
*/
  protected boolean test_straight(StrokeData stroke) {
    double[] w_straight = {-6.7241, -0.2345, 10.6793, 0, .2244, 6.7048}; 
    stroke.deriveProperties();
    stroke.derivePolarCoordinates();
    int stroke_len = stroke.getVertices().length;
    double ed = stroke.getVertices()[stroke_len-1].distance(stroke.getVertices()[0]);
    double arclen = stroke.accumulated_length[stroke_len-1]; 
    
    double[] features = {1, ed, ed/arclen, ed*ed, ed*ed/arclen, ed*ed/(arclen*arclen)};
    double z = dot_product(features, w_straight);
    return z>0;  
  }

/*  protected boolean test_long(double arclen, double bb_radius, double ed){
    double z =  w_long[0]+w_long[1]*arclen + w_long[2]*bb_radius ; 
     return z>0;

  }
*/
  protected boolean test_long(StrokeData stroke) {
    double[] w_long = {-3.5583, 0.3999, -4.3741, .9128};
    stroke.deriveProperties();
    stroke.derivePolarCoordinates();
    int stroke_len = stroke.getVertices().length;
    double arclen = stroke.accumulated_length[stroke_len-1];
    double bb_radius = (stroke.radius.width+stroke.radius.height)/2;
    double features[] = {1, arclen, bb_radius};
    double z = dot_product(features, w_long);
    return z>0;
  }

 /*protected boolean test_closed(double ed, double theta_dif, double arclen){
    double z = w_closed[0] + w_closed[1]*ed + w_closed[2]*theta_dif + w_closed[3]*arclen;
     return z>0; 

  }*/ 
  protected boolean test_closed(StrokeData stroke) {
    double[] w_closed = {.5139, -.2842, 2.4366, .0246};
    stroke.deriveProperties();
    stroke.derivePolarCoordinates();
    int stroke_len = stroke.getVertices().length;
    double ed = stroke.getVertices()[stroke_len-1].distance(stroke.getVertices()[0]);
    double arclen = stroke.accumulated_length[stroke_len-1];
    double theta_dif = Math.abs(stroke.polar_points[0].theta-stroke.polar_points[stroke_len-1].theta);
    double features[] = {1, ed, theta_dif, arclen};
    double z = dot_product(features, w_closed);
    return z>0;
  }

  protected boolean test_inflection(StrokeData stroke) {

     int long_pos = 0;
    int long_neg = 0;
    double sign;

  if (getCurvature(0,curvature_window,  stroke)>0) {
       sign = 1;
    }
    else {
        sign = -1;
    }
    int current = 0;
    for (int j = 0; j<stroke.vertices.length; j++) {

     if (getCurvature(j,curvature_window,stroke)*sign>0) {
          current++;
       }
       else {
         if ( (sign >0) && (current > long_pos)) {
            long_pos = current;
         }
         else if ( (sign <0) && (current > long_neg)) {
            long_neg = current;
         }
        sign = -sign;
        current = 0;
       }
       if (j == stroke.vertices.length-1) {
             if ( (sign >0) && (current > long_pos)) {
            long_pos = current;
         }
         else if ( (sign <0) && (current > long_neg)) {
            long_neg = current;
         }
       }
    }

   return ((long_pos >=curve_length  ) && (long_neg >= curve_length ));
  }

   

   protected GeometricObject simple_getFit(int type, StrokeData simple_stroke,int simple_len) {
      switch(type) {
        case 1: //ELLIPSE
            Ellipse ellipsefit ;
            ellipsefit = (simple_stroke.getEllipse());
            return ellipsefit;
        case 3: //LINE
          Line linefit =  new Line(simple_stroke.vertices[0], simple_stroke.vertices[simple_len-1]);
          return linefit;

        case 4: //ARC
           Arc arcfit = Arc.fitArc(simple_stroke);
           return arcfit;
        case 5: //SPIRAL
           Spiral spiralfit = Spiral.fitSpiral(simple_stroke);
           return spiralfit;
        case 6: //CCURVE
        case 7: //OCURVE
           GeneralPath curvefit = new GeneralPath();
           curvefit.moveTo((float)simple_stroke.vertices[0].x, (float)simple_stroke.vertices[0].y);
           curvefit.curveTo((float)simple_stroke.vertices[0].x, (float)simple_stroke.vertices[0].y, (float)simple_stroke.vertices[1].x, (float)simple_stroke.vertices[1].y,(float)simple_stroke.vertices[2].x, (float)simple_stroke.vertices[2].y); 
          int i =3; 
          int first = 1;
          int middle = 2;
          while(i<simple_len) {
            curvefit.curveTo((float)simple_stroke.vertices[first].x, (float)simple_stroke.vertices[first].y, (float)simple_stroke.vertices[middle].x, (float)simple_stroke.vertices[middle].y,(float)simple_stroke.vertices[i].x, (float)simple_stroke.vertices[i].y); 
           first = middle;
           middle =i;
                    
           i = i+3;
 
           }
           if (middle!= simple_len-1) {
             curvefit.curveTo((float)simple_stroke.vertices[first].x, (float)simple_stroke.vertices[first].y, (float)simple_stroke.vertices[middle].x, (float)simple_stroke.vertices[middle].y,(float)simple_stroke.vertices[simple_len-1].x, (float)simple_stroke.vertices[simple_len-1].y);
          }          
 
         return curvefit;
        default: 
           return new Point(0,0);
       }

   }


/***********public functions*************/

   

    public boolean isPolygon(){
        return ( classify() == POLYGON );
    }

    public boolean isEllipse(){
        return ( classify() == ELLIPSE );
    }

    public boolean isComplex() {
        return ( classify() == COMPLEX );
    }

    public boolean isLine() {
        return ( classify() == LINE );
    }

    public boolean isArc() {
       return ( classify() == ARC );
    }

    public boolean isSpiral() {
      return (classify() == SPIRAL );
    }
    public boolean isOCurve() {
       return ( classify() == OCURVE );
    }

    public boolean isCCurve() {
      return (classify() == CCURVE );
    }

    public boolean isPolyline() {
       return (classify() == POLYLINE );
    }

    public boolean isTooSmall() {
       return (classify() == TOOSMALL );
    }
 
    public int getBestClass() {
      return classify();
    }

    public GeometricObject getFit(int type) {
      classify();
      int i;
      switch(type) {
        case 0: //POLYGON
        case 8: //POLYLINE
           Point[] points = new Point[parts.size()+1];
           Line line = new Line();
            Iterator iter = parts.iterator(); 
           i = 0;
           while(iter.hasNext()) {
             Point t = new Point();
             line = (Line)iter.next();
             t.x =(int)line.x1;
             t.y = (int) line.y1; 
              points[i] = t;
              i++;
 
            }
           
          if (type == POLYGON) {
            points[parts.size()]=points[0];
          }
          else {
             Point t = new Point();
              t.x = (int)line.x2;

              t.y = (int)line.y2;
              points[parts.size()]= t; 
          }
           Polygon polyfit = new Polygon(points);
           return polyfit;
       case 2: //COMPLEX
           CompositeGeometricObject complexfit = new CompositeGeometricObject(parts);
           return complexfit;    

 
       case 1: //ELLIPSE
          if (type == classify()) {
               return (GeometricObject)parts.get(0);
             }
             else {
                 Ellipse ellipsefit ;
                 ellipsefit = (stroke_data.getEllipse());
                 return ellipsefit;
             } 

       case 3: //LINE
             if (type == classify()) {
               return (GeometricObject)parts.get(0);
             }
             else {
                 Line linefit =  new Line(stroke_data.vertices[0],stroke_data.vertices[len-1]);
                 return linefit;
             }
       case 4: //ARC
           if (type == classify()) {
               return (GeometricObject)parts.get(0);
             }
             else {
               Arc arcfit = Arc.fitArc(stroke_data);
               return arcfit;
             }

       case 5: //SPIRAL
            if (type == classify()) {
               return (GeometricObject)parts.get(0);
             }
             else {
                Spiral spiralfit = Spiral.fitSpiral(stroke_data);
                return spiralfit;
             }
 
       case 6: //CCURVE 
       case 7: //OCURVE
             if (type == classify()) {
               return (GeometricObject)parts.get(0);    
             }
             else {
                GeneralPath curvefit = new GeneralPath();
           curvefit.moveTo((float)stroke_data.vertices[0].x, (float)stroke_data.vertices[0].y);
           curvefit.curveTo((float)stroke_data.vertices[0].x, (float)stroke_data.vertices[0].y, (float)stroke_data.vertices[1].x, (float)stroke_data.vertices[1].y,(float)stroke_data.vertices[2].x, (float)stroke_data.vertices[2].y);
          i =3;
          int first = 1;
          int middle = 2;
          while(i<len) {
            curvefit.curveTo((float)stroke_data.vertices[first].x,(float) stroke_data.vertices[first].y, (float)stroke_data.vertices[middle].x, (float)stroke_data.vertices[middle].y,(float)stroke_data.vertices[i].x, (float)stroke_data.vertices[i].y);
           first = middle;
           middle =i;

           i = i+3;

           }

         return curvefit;
             }
 
 case 9: //TOOSMALL
          if ((Math.pow(stroke_data.bounding_box.getWidth(),2)*Math.pow(stroke_data.bounding_box.getHeight(),2)<Math.pow(stroke_data.accumulated_length[len-1],2))) {
           Ellipse smallfit;
           smallfit = (stroke_data.getEllipse());
           return smallfit;
          }
          else {
           Line smallfit = new Line(stroke_data.vertices[0], stroke_data.vertices[len-1]);
           return smallfit;
          } 
       default:
          return new Point(0,0); 
       }

   }
    public double getError(int type ) {
      GeometricObject fit = getFit(type);
      double error = 0;
      Point[] points;
      points= stroke_data.vertices;
      switch(type) {
       case 0:
       case 8:
          error = AWTUtil.leastSquaresForPolygon((Polygon)fit,points);
       case 1:
          error = stroke_data.leastSquaresForCircle();
       case 2:
           for(int i = 0; i<segments.size(); i++) {
              switch (((Integer)segments.get(i)).intValue()){
                case 1:
                  error = error + stroke_data.leastSquaresForCircle();
                case 3:
                 error =error+ AWTUtil.leastSquaresForLine((Line)fit, points); 
                case 4:
                  error = error +((Arc) fit).leastSquaresError();
                case 5:
                   error = error +((Spiral) fit).leastSquaresError();
                case 6:
                case 7:
                   error = error + ((GeneralPath) fit).getLSQError(stroke_data);               }
           }        
       case 3:
          error = AWTUtil.leastSquaresForLine((Line)fit, points);
       case 4:
           error = ((Arc) fit).leastSquaresError();
       case 5:
           error = ((Spiral) fit).leastSquaresError();
       case 6:
       case 7:
           error = ((GeneralPath) fit).getLSQError(stroke_data);
       case 9:
       if ((Math.pow(stroke_data.bounding_box.getWidth(),2)*Math.pow(stroke_data.bounding_box.getHeight(),2)<Math.pow(stroke_data.accumulated_length[len-1],2))) { 
        error = stroke_data.leastSquaresForCircle();
       }
       else {
         error = AWTUtil.leastSquaresForLine((Line)fit, points);
       }

      } 
     return error;     
       
    } 


}


