package polygonsSWP.data;

import java.util.ArrayList;


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
  ArrayList<Point> _coords = new ArrayList<Point>();
  PStatistics _stats = null;
  PHistory _history = null;

  /**
   * Generates an empty polygon object which will contain no statistics or
   * history.
   */
  public Polygon() {

  }

  /**
   * Generates an empty polygons object which will contain his statistics and
   * history.
   * 
   * @param history The history object which shall be used.
   * @param stats The statistics object which shall be used.
   */
  public Polygon(PHistory history, PStatistics stats) {
    _stats = stats;
    _history = history;
  }

  /**
   * @return Returns the ordered list of points associated with the polygon.
   */
  public ArrayList<Point> getPoints() {
    return _coords;
  }

  /**
   * @param coords Sets a new list of ordered points.
   */
  public void setPoints(ArrayList<Point> coords) {
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
   * @return Returns the history object associated. If none is present it
   *         returns null.
   */
  public PHistory getHistory() {
    return _history;
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
   * @param p Point to delet.
   */
  public void deletePoint(Point p) {
    _coords.remove(p);
  }
}
