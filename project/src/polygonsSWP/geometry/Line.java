package polygonsSWP.geometry;

public class Line
{
  public Point _a;
  public Point _b;

  public Line(Point a, Point b) {
    _a = a;
    _b = b;
  }

  /**
   * Check if given Point is in this line.
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @param p
   * @return
   */
  public boolean containsPoint(Point p) {
    Vector oa = new Vector(new Point(0, 0), _a);
    Vector ab = new Vector(_a, _b);
    long lambda1 = ((p.x - oa.v1) / ab.v1);
    long lambda2 = ((p.y - oa.v2) / ab.v2);
    if (lambda1 == lambda2) {
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Calculates the intersection of this line with a line segment.
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @param ls
   * @return null if lines and line segment do not intersect, array of length 0 if
   *         both a coincident, array containing intersection Point else.
   */
  public Point[] intersect(LineSegment ls) {
    Point[] intersection = intersect(ls.extendToLine());
    if (intersection == null || intersection.length == 0) { return intersection; }
    if (!this.containsPoint(intersection[0])) { return null; }
    return intersection;
  }

  /**
   * Calculates the intersection of this line with a ray.
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @param ls
   * @return null if lines and ray do not intersect, array of length 0 if
   *         both a coincident, array containing intersection Point else.
   */
  public Point[] intersect(Ray r) {
    Point[] intersection = intersect(r.extendToLine());
    if (intersection == null || intersection.length == 0) { return intersection; }
    if (!this.containsPoint(intersection[0])) { return null; }
    return intersection;
  }

  /**
   * Calculates the intersection of two lines.
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @param l2
   * @return null if lines parallel, array of length 0 if both lines coincident,
   *         array containing intersection Point else.
   */
  public Point[] intersect(Line l2) {

    long x1, x2, x3, x4, y1, y2, y3, y4;

    x1 = this._a.x;
    x2 = this._b.x;
    x3 = l2._a.x;
    x4 = l2._b.x;

    y1 = this._a.y;
    y2 = this._b.y;
    y3 = l2._a.y;
    y4 = l2._b.y;

    long denominator = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
    long nominator1 =
        (x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4);
    long nominator2 =
        (x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4);

    // if
    if (denominator == 0) {
      if (nominator1 == 0 && nominator2 == 0) {
        Point[] intersection = { };
        return intersection;
      }
      else {
        return null;
      }
    }

    double ua = nominator1 / (double) denominator;

    double x = x1 + ua * (x2 - x1);
    double y = y1 + ua * (y2 - y1);

    Point[] intersection = { new Point((long) x, (long) y) };

    return intersection;
  }
}
