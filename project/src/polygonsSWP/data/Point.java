package polygonsSWP.data;

/**
 * Implementation of point object. Just abstracts x and y coordinates to one
 * object.
 * 
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 */
public class Point
{
  public long x;
  public long y;

  public Point(long _x, long _y) {
    x = _x;
    y = _y;
  }
  
  @Override
  public boolean equals(Object obj) {
    if(!(obj instanceof Point))
      return false;
    
    Point p = (Point) obj;
    return (p.x == x) && (p.y == y);
  }

  public String toString() {
    return "("+ x +"," + y +")";
  }
}
