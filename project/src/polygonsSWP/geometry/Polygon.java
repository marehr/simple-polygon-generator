package polygonsSWP.geometry;

import java.util.List;

/**
 * Polygon interface for simple _and_ complex polygons. Subclasses should 
 * never assume they contain a simple polygon.
 * 
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 */
public abstract class Polygon
{
  /**
   * @return Returns the ordered list of points associated with the polygon,
   * with an implicit edge between ret[ret.size()] and ret[0].
   */
  public abstract List<Point> getPoints();
  
  /**
   * @return Returns a copy of the polygon instance.
   */
  public abstract Polygon clone();
  
  /**
   * @return True if object equals polygon, false otherwise
   */
  public abstract boolean equals(Object obj);
  
  /**
   * @return If point is in Polygon.
   */
  public abstract boolean containsPoint(Point p, boolean onLine);
  
  /**
   * @return Surface area as double.
   */
  public abstract double getSurfaceArea();
  
  /**
   * @return a random point in the polygon area (including on the edges).
   */
  public abstract Point createRandomPoint();
  
}
