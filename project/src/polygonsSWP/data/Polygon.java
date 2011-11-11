package polygonsSWP.data;

import java.util.List;

/**
 * Polygon interface for simple _and_ complex polygons. Subclasses should 
 * never assume they contain a simple polygon.
 * 
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de
 */
public interface Polygon
{
  /**
   * @return Returns the ordered list of points associated with the polygon,
   * with an implicit edge between ret[ret.size()] and ret[0].
   */
  public List<Point> getPoints();
}
