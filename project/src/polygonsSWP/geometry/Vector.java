package polygonsSWP.geometry;

public class Vector
{
  public long v1;
  public long v2;
  
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
    return (v.v1 == v1) && (v.v2 == v2);
  }
}