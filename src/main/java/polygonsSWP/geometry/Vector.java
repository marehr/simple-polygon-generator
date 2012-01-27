package polygonsSWP.geometry;

public class Vector extends Point
{
  
  public Vector(Point p1, Point p2){
    super(p2.x - p1.x, p2.y - p1.y);
  }
  
  public Vector(double x, double y){
    super(x, y);
  }
  
  public Vector add(Point u){
    return new Vector(x + u.x, y + u.y);
  }
  
  public Vector sub(Point u){
    return new Vector(x - u.x, y - u.y);
  }
  
  public Vector mult(double scale){
    return new Vector(x * scale, y * scale);
  }
}
