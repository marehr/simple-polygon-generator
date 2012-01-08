package polygonsSWP.geometry;

import polygonsSWP.util.MathUtils;
import polygonsSWP.util.intersections.IntersectionUtils;
import polygonsSWP.util.intersections.LineIntersectionMode;
import polygonsSWP.util.intersections.LineSegmentIntersectionMode;
import polygonsSWP.util.intersections.RayIntersectionMode;

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
    double lambda1 = ((p.x - oa.v1) / ab.v1);
    double lambda2 = ((p.y - oa.v2) / ab.v2);
    return MathUtils.doubleEquals(lambda1, lambda2);
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
    return IntersectionUtils.intersect(_a, _b, ls._a, ls._b, 
        new LineIntersectionMode(), new LineSegmentIntersectionMode(true));
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
    return IntersectionUtils.intersect(_a, _b, r._base, r._support, 
        new LineIntersectionMode(), new RayIntersectionMode(true));
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
    return IntersectionUtils.intersect(_a, _b, l2._a, l2._b, 
        new LineIntersectionMode(), new LineIntersectionMode());
  }
  
  public Line clone() {
    return new Line(_a.clone(), _b.clone());
  }
}
