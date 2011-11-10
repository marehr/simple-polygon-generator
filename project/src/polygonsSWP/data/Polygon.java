package polygonsSWP.data;

import java.util.ArrayList;
import java.util.List;


/**
 * Implementation of polygons. The assumption is, that the list of polygons is
 * ordered in order of appearance. So the polygon is drawn from point one to
 * point two to .. to point n to point 1. So be sure your self added point list
 * meets the assumption other wise it won't work.
 * 
 * @author bigzed
 */
public class Polygon
{
  List<Point> _coords = new ArrayList<Point>();
  PStatistics _stats = null;

  /**
   * Generates an empty polygon object which will contain no statistics or
   * history.
   */
  public Polygon() {
    this(new ArrayList<Point>(), null);
  }

  /**
   * Generates an polygon object which consists of the given points,
   * carrying no statistics object.
   */
  public Polygon(List<Point> coords) {
    this(coords, null);
  }
  
  /**
   * Generates an empty polygons object which will contain its statistics.
   * 
   * @param stats The statistics object which shall be used.
   */
  public Polygon(PStatistics stats) {
    this(new ArrayList<Point>(), stats);
  }
  
  /**
   * Generates an empty polygons object which will contain its statistics
   * and the given list of points.
   * 
   * @param stats The statistics object the Polygon should carry
   * @param points The list of points the polygon consists of
   */
  public Polygon(List<Point> coords, PStatistics stats) {
    _coords = coords;
    _stats = stats;
  }

  /**
   * @return Returns the ordered list of points associated with the polygon.
   */
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
   * @return Returns the statistics object associated. If none is present it
   *         returns null.
   */
  public PStatistics getStatictics() {
    return _stats;
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

  /**
   * Determines whether a given ordered list of points
   * forms a simple polygon.
   * 
   * @return true, if the polygon is simple, otherwise false.
   */
  public boolean isSimple() {
    /* Remark: The approach used here is very naive. As
     * Held writes in his paper, "The simplicity test for a polygon
     * is not done in linear time; rather we implemented a
     * straightforward quadratic approach. (As we will see later this
     * has no influence on the test results.)", I also decided to let
     * alone [Cha91] for now and simply check for crossing lines.
     * 
     * However, since we're going to implement polygon triangulation
     * anyway, we should definitely come back here later and improve this.
     * 
     * Maybe, instead of the proposed [Cha91], we could also use
     * the Bentley-Ottmann algorithm, see
     * http://en.wikipedia.org/wiki/Bentley%E2%80%93Ottmann_algorithm
     * for explanation.
     */
    
    int size = _coords.size();
    for(int i = 0; i < size; i++) {
      Edge a = new Edge(_coords.get(i), _coords.get((i+1) % size));
      for(int j = i + 1; j < i + size; j++) {
        Edge b = new Edge(_coords.get(j % size), _coords.get((j + 1) % size));
        
        if(a.isIntersecting(b))
          return false;
      }
    }
    
    return true;
  }
}
