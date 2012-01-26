package polygonsSWP.geometry;

import polygonsSWP.util.MathUtils;

public class Vector
{
  public double v1;
  public double v2;
  
  public Vector(Point p1, Point p2){
    v1 = p2.x - p1.x;
    v2 = p2.y - p1.y;
  }
  
  public Vector(double _v1, double _v2){
    v1 = _v1;
    v2 = _v2;
  }
  
  public double length(){
    return Math.sqrt((v1*v1 + v2*v2));
  }
  
  public Vector add(Vector u){
    return new Vector(v1 + u.v1, v2 + u.v2);
  }
  
  public Vector sub(Vector u){
    return new Vector(v1 - u.v1, v2 - u.v2);
  }
  
  public Vector mult(double x){
    return new Vector(v1 * x, v2 * x);
  }
  
  @Override
  public boolean equals(Object obj) {
    if(!(obj instanceof Vector))
      return false;
    
    Vector v = (Vector) obj;
    return MathUtils.doubleEquals(v1, v.v1) && MathUtils.doubleEquals(v2, v.v2);
  }
}
