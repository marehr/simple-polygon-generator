package polygonsSWP.data;

/**
 * Implementation of point object. Just abstracts x and y coordinates to one
 * object.
 * 
 * @author bigzed
 */
public class Point
{
  public int x;
  public int y;

  public Point(int _x, int _y) {
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
}
