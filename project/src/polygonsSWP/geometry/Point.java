package polygonsSWP.geometry;

/**
 * Implementation of point object. Just abstracts x and y coordinates to one
 * object.
 * 
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 */
public class Point
  implements Comparable<Point>
{
  public long x;
  public long y;

  public Point(long _x, long _y) {
    x = _x;
    y = _y;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Point)) return false;

    Point p = (Point) obj;
    return (p.x == x) && (p.y == y);
  }

  public String toString() {
    return "(" + x + "," + y + ")";
  }

  /**
   * Test if point is between two other points.
   * 
   * @author Steve Dierker <dierker.steve@fu-berlin.de>
   * @param begin
   * @param end
   * @param p
   * @return
   */
  public boolean isBetween(Point begin, Point end) {
    if (this.equals(begin) || this.equals(end)) return true;
    return this.distanceTo(begin) + this.distanceTo(end) == begin.distanceTo(end);
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
    return Math.sqrt(Math.pow(this.x - p.x, 2) + Math.pow(this.y - p.y, 2));
  }

  /**
   * Compares a point to another one first by x- then by
   * y-values.
   * 
   * @param obj other point
   * @return -1 if point is more to the left than the other point
   *         or if they have the same x-values and the other point
   *         is more to the bottom (has greater y-value).
   *         0 if the points are equal.
   *         1 if point is more to the right or if they have the same
   *         x-values and point is more to the bottom.
   */
  @Override
  public int compareTo(Point obj) {
    if (((Point) obj).x > x) return -1;
    else if (((Point) obj).x < x) {
      return 1;
    }
    else {
      if (((Point) obj).y > y) return -1;
      else if (((Point) obj).y < y) return 1;
      else return 0;
    }
  }
}
