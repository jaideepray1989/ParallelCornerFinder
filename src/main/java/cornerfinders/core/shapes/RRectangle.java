package cornerfinders.core.shapes;



public class RRectangle{

  private double m_x;
  private double m_y;
  private double m_width;
  private double m_height;

  
  public double getMinX(){
    return m_x;
  }
  
  public double getMaxX(){
    return m_x + m_width;
  }
  
  public double getMinY(){
    return m_y;
  }
  
  public double getMaxY(){
    return m_y + m_height;
  }
  
  public double getWidth(){
    return m_width;
  }
  public double getHeight(){
    return m_height;
  }
  public double getX(){
    return m_x + m_width/2;
  }
  public double getY(){
    return m_y + m_height/2;
  }
  
  public RRectangle getBoundingBox(){
    return this;
  }
  public TPoint getCenter(){
    return new TPoint(getX(), getY());
  }
  
  public boolean contains(TPoint p){
    return contains(p.getX(), p.getY());
  }
  
  public boolean contains(double x, double y){
    if(x < getMinX()){return false;}
    if(x > getMaxX()){return false;}
    if(y < getMinY()){return false;}
    if(y > getMaxY()){return false;}
    return true;
  }

  public RRectangle getIntersection(RRectangle r){
    double minx = Math.max(getMinX(), r.getMinX());
    double miny = Math.max(getMinY(), r.getMinY());
    double maxx = Math.min(getMaxX(), r.getMaxX());
    double maxy = Math.min(getMaxY(), r.getMaxY());
    return new RRectangle(minx, miny, maxx - minx, maxy - miny);
  }
  
  public double getArea(){
    return getWidth() * getHeight();
  }
  
  public boolean isEmpty(){
    if(getArea() < 1){return true;}
    return false;
  }
  
  public RRectangle getUnion(RRectangle r){
    double minx = Math.min(getMinX(), r.getMinX());
    double miny = Math.min(getMinY(), r.getMinY());
    double maxx = Math.max(getMaxX(), r.getMaxX());
    double maxy = Math.max(getMaxY(), r.getMaxY());
    return new RRectangle(minx, miny, maxx - minx, maxy - miny);
  }
  
  
  public RRectangle(double x, double y, double width, double height){
    m_x = x;
    m_y = y;
    m_width = width;
    m_height = height;
  }  

  public void scale(double factor){
    if(factor == 0){return;}
    m_width *= factor;
    m_height *= factor;
    m_x *= factor;
    m_y *= factor; 
  }
  
}
