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
  
  public double length(){
    return Math.sqrt((v1*v1 + v2*v2));
  }
  
  @Override
  public boolean equals(Object obj) {
    if(!(obj instanceof Vector))
      return false;
    
    Vector v = (Vector) obj;
    return MathUtils.doubleEquals(v1, v.v1) && MathUtils.doubleEquals(v2, v.v2);
  }
}
