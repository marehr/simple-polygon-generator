package polygonsSWP.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import polygonsSWP.util.MathUtils;


/**
 * Implementation of Polygon using ordered list. The assumption is, that the
 * list of polygons is ordered in order of appearance. So the polygon is drawn
 * from point one to point two to .. to point n to point 1. So be sure your self
 * added point list meets the assumption otherwise it won't work.
 * 
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 */
public class OrderedListPolygon
  implements Polygon
{
  List<Point> _coords = new ArrayList<Point>();

  /**
   * Generates an empty polygon object which will contain no statistics or
   * history.
   */
  public OrderedListPolygon() {
    this(new ArrayList<Point>());
  }

  /**
   * Generates an polygon object which consists of the given points, carrying no
   * statistics object.
   */
  public OrderedListPolygon(List<Point> coords) {
    _coords = coords;
  }

  public List<Point> getPoints() {
    return _coords;
  }

  /**
   * @param coords Sets a new list of ordered points.
   */
  public void setPoints(List<Point> coords) {
    _coords = coords;
  }
  /**
   * Adds a point to the end of the list.
   * 
   * @param p The new point.
   */
  public void addPoint(Point p) {
    _coords.add(p);
  }

  /**
   * Adds a point to the desired position in the list.
   * 
   * @param p The new point.
   * @param pos Position in list.
   */
  public void addPoint(Point p, int pos) {
    _coords.add(pos, p);
  }

  /**
   * Deletes the given point.
   * 
   * @param p Point to delete.
   */
  public void deletePoint(Point p) {
    _coords.remove(p);
  }

  public void permute() {
    _coords = MathUtils.permute(_coords);
  }

  /**
   * Determines whether a given ordered list of points forms a simple polygon.
   * 
   * @return true, if the polygon is simple, otherwise false.
   */
  public boolean isSimple() {
    return findIntersections().size() == 0;
  }
  
  /**
   * Calculates the set of all intersections found in the polygon.
   * 
   * @return a list of intersections, where each item is an array of the two
   *         indices defining an intersection. 
   *         [x,y] --> edge(x,x+1) intersects edge(y,y+1)
   */
  public List<Integer[]> findIntersections() {
    /*
     * Remark: The approach used here is very naive. As Held writes in his
     * paper, "The simplicity test for a polygon is not done in linear time;
     * rather we implemented a straightforward quadratic approach. (As we will
     * see later this has no influence on the test results.)", I also decided to
     * let alone [Cha91] for now and simply check for crossing lines. However,
     * since we're going to implement polygon triangulation anyway, we should
     * definitely come back here later and improve this. Maybe, instead of the
     * proposed [Cha91], we could also use the Bentley-Ottmann algorithm, see
     * http://en.wikipedia.org/wiki/Bentley%E2%80%93Ottmann_algorithm for
     * explanation.
     */

    List<Integer[]> retval = new ArrayList<Integer[]>();
    int size = _coords.size();
    for (int i = 0; i < size - 1; i++) {
      Edge a = new Edge(_coords.get(i), _coords.get(i + 1));
      for (int j = i + 1; j < size; j++) {
        Edge b = new Edge(_coords.get(j), _coords.get((j + 1) % size));

        if (a.isIntersecting(b))
          retval.add(new Integer[] {i, j});
      }
    }

    return retval;
  }
  
  /**
   * Returns a randomly chosen intersection of the polygon.
   * 
   * @return 2-element array determining the intersection (see above)
   *         null, if there is no intersection
   */
  public Integer[] findRandomIntersection() {
    List<Integer[]> is = findIntersections();
    if(is.size() == 0)
      return null;
    
    return is.get(new Random(System.currentTimeMillis()).nextInt(is.size()));
  }


  /**
   * @return the number of vertices in the polygon
   */
  public int size() {
    return _coords.size();
  }
}
