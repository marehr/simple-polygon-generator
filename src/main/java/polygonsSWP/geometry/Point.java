package polygonsSWP.geometry;

import java.text.DecimalFormat;
import java.util.Comparator;

import polygonsSWP.util.MathUtils;


/**
 * Implementation of point object. Just abstracts x and y coordinates to one
 * object.
 * 
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 */
public class Point
  implements Comparable<Point>
{
  private static DecimalFormat df = new DecimalFormat("###.###");

  public static final Comparator<Point> XCompare = new Comparator<Point>() {

    @Override
    public int compare(Point o1, Point o2) {
      return o1.compareTo(o2);
    }

  };

  public static final Comparator<Point> XCompareReverse = new Comparator<Point>() {

    @Override
    public int compare(Point o1, Point o2) {
      return o2.compareTo(o1);
    }

  };

  public static final Comparator<Point> YCompare = new Comparator<Point>() {

    @Override
    public int compare(Point o1, Point o2) {
      return o1.compareToByY(o2);
    }

  };

  public double x;
  public double y;

  public Point(int _x, int _y) {
    this((double) _x, (double) _y);
  }

  public Point(long _x, long _y) {
    // TODO remove
    assert (false);
  }

  public Point(double _x, double _y) {
    x = _x;
    y = _y;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Point)) return false;

    Point p = (Point) obj;
    return MathUtils.doubleEquals(x, p.x) && MathUtils.doubleEquals(y, p.y);
  }

  public String toString() {
    return "(" + df.format(x) + "," + df.format(y) + ")";
  }
  
  @Override
  public int hashCode() {
    return 31 * (new Double(x).hashCode()) + (new Double(y).hashCode());
  }

  /**
   * Calculates distance between two points.
   * 
   * @author Steve Dierker <dierker.steve@fu-berlin.de>
   * @param begin
   * @param end
   * @return
   */
  public double distanceTo(Point p) {
    double x = this.x - p.x,
           y = this.y - p.y;
    return Math.sqrt(x * x + y * y);
  }

  /**
   * Calculates the squared distance between two points.
   *
   * NOTE: this is not a metric its just to compare two or more distances
   * from a base point
   *
   * @see http://en.wikipedia.org/wiki/Euclidean_distance#Squared_Euclidean_Distance
   * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
   * @param p
   * @return
   */
  public double squaredDistanceTo(Point p) {
    double x = this.x - p.x,
           y = this.y - p.y;
    return x * x + y * y;
  }

  /**
   * Compares a point to another one first by x- then by y-values.
   * 
   * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
   * @param p2 other point
   * @return -1 if point is more to the left than the other point or if they
   *         have the same x-values and the other point is more to the bottom
   *         (has greater y-value). 0 if the points are equal. 1 if point is
   *         more to the right or if they have the same x-values and point is
   *         more to the bottom.
   */
  @Override
  public int compareTo(Point p2) {
    if (!MathUtils.doubleEquals(x, p2.x)) return x < p2.x ? -1 : +1;
    if (MathUtils.doubleEquals(y, p2.y)) return 0;
    return y < p2.y ? -1 : +1;
  }

  /**
   * Compares a point to another one first by y- then by x-values.
   * 
   * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
   * @param p2 other point
   * @return -1 if point is more to the top than the other point or if they have
   *         the same y-values and the other point is more to the right (has
   *         greater x-value). 0 if the points are equal. 1 if point is more to
   *         the bottom or if they have the same y-values and point is more to
   *         the right.
   */
  public int compareToByY(Point p2) {
    if (!MathUtils.doubleEquals(y, p2.y)) return y < p2.y ? -1 : +1;
    if (MathUtils.doubleEquals(x, p2.x)) return 0;
    return x < p2.x ? -1 : +1;
  }

  public Point clone() {
    return new Point(x, y);
  }
}
