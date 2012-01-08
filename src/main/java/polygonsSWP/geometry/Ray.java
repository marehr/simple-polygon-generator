package polygonsSWP.geometry;

import java.util.List;

import polygonsSWP.util.MathUtils;
import polygonsSWP.util.intersections.IntersectionUtils;
import polygonsSWP.util.intersections.LineSegmentIntersectionMode;
import polygonsSWP.util.intersections.RayIntersectionMode;


public class Ray
{
  public Point _base;
  public Point _support;

  public Ray(Point initial, Point support) {
    _base = initial;
    _support = support;
  }

  /**
   * Check if given Point is in this ray.
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @param p
   * @return
   */
  public boolean containsPoint(Point p) {
    Vector oa = new Vector(new Point(0, 0), _base);
    Vector ab = new Vector(_base, _support);
    double lambda1 = ((p.x - oa.v1) / ab.v1);
    double lambda2 = ((p.y - oa.v2) / ab.v2);
    return MathUtils.doubleEquals(lambda1, lambda2) && lambda1 >= 0;
  }

  /**
   * Get intersection between this ray and a lineSegment if existing.
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @param l LineSegment to intersect with.
   * @return null, array of length 0 (if coincident), array containing the
   *         intersection otherwise.
   */
  public Point[] intersect(LineSegment ls) {
    return IntersectionUtils.intersect(_base, _support, ls._a, ls._b,
        new RayIntersectionMode(true), new LineSegmentIntersectionMode(true));
  }

  /**
   * Get the intersection between this ray and another one.
   * 
   * @param r the ray to intersect with
   * @return null, array of length 0 (if coincident), array containing the
   *         intersection otherwise.
   */
  public Point[] intersect(Ray r) {
    return IntersectionUtils.intersect(_base, _support, r._base, r._support,
        new RayIntersectionMode(true), new RayIntersectionMode(true));
  }

  /**
   * Extends this ray to a line.
   * 
   * @return a line matching this ray and extending into the other direction as
   *         well.
   */
  public Line extendToLine() {
    return new Line(_base, _support);
  }

  /**
   * Get Point on Ray that is closest to its initial base point. Does not return
   * coincident LineSegments/Rays/Lines.
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @param intersections
   * @return Point[] triple of Points.
   */
  public Point[] getPointClosestToBase(List<Point[]> intersections) {
    Point[] closest = null;
    double distance = Double.MAX_VALUE;
    for (Point[] points : intersections) {
      if (points[0] != null) {
        double newDistance = points[0].distanceTo(_base);
        if (newDistance < distance) {
          closest = points;
          distance = newDistance;
        }
      }
    }
    return closest;
  }

  public Ray clone() {
    return new Ray(_base.clone(), _support.clone());
  }
}
