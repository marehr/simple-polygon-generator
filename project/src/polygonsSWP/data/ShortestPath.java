package polygonsSWP.data;

import java.util.ArrayList;


/**
 * Implementation of the shortest path object. Assumption is, that every
 * shortest path needs an polygon to which it is associated and that it needs to
 * no the start, end and every point on the path. So the path is saved in an
 * ordered list starting with start and always ending on the end point.
 * 
 * @author bigzed
 */
public class ShortestPath
{
  private ArrayList<Point> _path = new ArrayList<Point>();
  private Polygon _polygon;
  private SPStatistics _stats = null;
  private SPHistory _history = null;

  /**
   * Generates an empty shortest path for polygon.
   * 
   * @param polygon Polygon in which is shortest path.
   * @param start Start point of path.
   * @param end End point of path.
   */
  public ShortestPath(Polygon polygon, Point start, Point end) {
    _polygon = polygon;
    _path.add(start);
    _path.add(end);
  }

  /**
   * Generates an empty shortest path for polygon with history and statistics.
   * 
   * @param polygon Polygon in which is shortest path.
   * @param start Start point of path.
   * @param end End point of path.
   * @param stats Statistic object for statistics.
   * @param history History object for history.
   */
  public ShortestPath(Polygon polygon, Point start, Point end,
      SPStatistics stats, SPHistory history) {
    _polygon = polygon;
    _stats = stats;
    _history = history;
    _path.add(start);
    _path.add(end);
  }

  /**
   * Adds a new point to the path. Its always in front of the end point.
   * 
   * @param p Next point on path.
   */
  public void addPointToPath(Point p) {
    _path.add(_path.size() - 1, p);
  }

  /**
   * Adds point on position.
   * 
   * @param p Point.
   * @param i Position.
   */
  public void addPointToPathOnPosition(Point p, int i) {
    _path.add(i, p);
  }

  /**
   * Deletes point on path.
   * 
   * @param p Point to delete.
   */
  public void deletePointOnPath(Point p) {
    _path.remove(p);
  }

  /**
   * Returns path.
   * 
   * @return Ordered list with points.
   */
  public ArrayList<Point> getPath() {
    return _path;
  }

  /**
   * Returns associated polygon.
   * 
   * @return Polygon.
   */
  public Polygon getPolygon() {
    return _polygon;
  }

  /**
   * Returns statistics.
   * 
   * @return Statisctics.
   */
  public SPStatistics getStatistics() {
    return _stats;
  }

  /**
   * Returns History.
   * 
   * @return History.
   */
  public SPHistory getHistory() {
    return _history;
  }
}
