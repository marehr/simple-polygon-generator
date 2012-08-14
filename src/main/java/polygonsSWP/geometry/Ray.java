package polygonsSWP.geometry;

import polygonsSWP.util.MathUtils;
import polygonsSWP.util.intersections.IntersectionUtils;
import polygonsSWP.util.intersections.LineSegmentIntersectionMode;
import polygonsSWP.util.intersections.RayIntersectionMode;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
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
   * @param p
   * @return
   */
  public boolean containsPoint(Point p) {
    Vector ap = new Vector(_base, p);
    Vector ab = new Vector(_base, _support);

    double lambda1 = ab.x == 0 ? 0 : (ap.x / ab.x) ;
    double lambda2 = ab.y == 0 ? 0 : (ap.y / ab.y);

    // catch horizontal lines
    if(MathUtils.doubleZero(ab.y))
      return MathUtils.doubleZero(ap.y) && lambda1 >= 0;

    // catch vertical lines
    if(MathUtils.doubleZero(ab.x))
      return MathUtils.doubleZero(ap.x) && lambda2 >= 0;

    return MathUtils.doubleEquals(lambda1, lambda2) && lambda1 >= 0;
  }

  /**
   * Get intersection between this ray and a lineSegment if existing.
   * 
   * @param l LineSegment to intersect with.
   * @return null, array of length 0 (if collinear), array containing the
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
   * @return null, array of length 0 (if collinear), array containing the
   *         intersection otherwise.
   */
  public Point[] intersect(Ray r) {
    return intersect(r, true);
  }
  
  public Point[] intersect(Ray r, boolean includeEndPoint) {
    return IntersectionUtils.intersect(_base, _support, r._base, r._support,
        new RayIntersectionMode(true), new RayIntersectionMode(includeEndPoint));
  }

  public Ray clone() {
    return new Ray(_base.clone(), _support.clone());
  }

  public String toString(){
    return "Ray[" + _base + "," + _support + "]";
  }
}
