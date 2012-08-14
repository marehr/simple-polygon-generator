package polygonsSWP.geometry;

import polygonsSWP.util.MathUtils;
import polygonsSWP.util.intersections.IntersectionUtils;
import polygonsSWP.util.intersections.LineSegmentIntersectionMode;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public class LineSegment implements Cloneable
{
  public Point _a;
  public Point _b;

  public LineSegment(Point a, Point b) {
    _a = a;
    _b = b;

    // TODO remove
    // assert(!_a.equals(_b));
  }

  /**
   * Equality check.
   * 
   * @return true, if the edges are the same, regardless of their direction
   *         false, otherwise false if edge only consists of one point but twice
   *         in edge
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof LineSegment)) return false;
    LineSegment e = (LineSegment) obj;
    return ((e._a.equals(_a)) && (e._b.equals(_b))) ||
        ((e._a.equals(_b)) && (e._b.equals(_a)));
  }

  /**
   * Test if Point is in this LineSegment.
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
   * @param p Point that may be on this LineSegment.
   * @return true if p is in this LineSegment, else false.
   */
  public boolean containsPoint(Point p) {
    Vector ap = new Vector(_a, p);
    Vector ab = new Vector(_a, _b);

    double lambda1 = ab.x == 0 ? 0 : (ap.x / ab.x) ;
    double lambda2 = ab.y == 0 ? 0 : (ap.y / ab.y);

    // catch horizontal lines
    if(MathUtils.doubleZero(ab.y))
      return MathUtils.doubleZero(ap.y) && lambda1 >= 0 && lambda1 <= 1;

    // catch vertical lines
    if(MathUtils.doubleZero(ab.x))
      return MathUtils.doubleZero(ap.x) && lambda2 >= 0 && lambda2 <= 1;

    return MathUtils.doubleEquals(lambda1, lambda2) && lambda1 >= 0 && lambda1 <= 1;
  }

  /**
   * Get a point on this line segment a, b, whereas the factor
   * is the percentage of the way from a to b
   * 
   * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
   * @param factor in [0, 1]
   * @return Point
   */
  public Point getPointOnLineSegment(double factor){
    return new Point(factor * (_a.x - _b.x) + _b.x,
                     factor * (_a.y - _b.y) + _b.y);
  }

  /**
   * Convenience function. Will find shared endpoints, too.
   */

  public Point[] intersect(LineSegment e) {
    return intersect(e, false);
  }

  /**
   * Stolen from: http://paulbourke.net/geometry/lineline2d/ Good explanation is
   * also here:
   * http://stackoverflow.com/questions/563198/how-do-you-detect-where
   * -two-line-segments-intersect Tests whether two line segments intersect or
   * whether they are coincident.
   * 
   * @param e another edge
   * @param ignoreSharedEndpoints if set, shared endpoints will not be treated
   *          as an intersection.
   * @return null if line segments do not intersect, array of length 0 if both
   *         are collinear, array containing intersection Point else. 
   *         TODO: change in using methods
   */
  public Point[] intersect(LineSegment e, boolean ignoreSharedEndpoints) {
    return IntersectionUtils.intersect(this._a, this._b, 
        e._a, e._b, new LineSegmentIntersectionMode(!ignoreSharedEndpoints), 
        new LineSegmentIntersectionMode(!ignoreSharedEndpoints));
  }

  /**
   * Extends this line segment on the side of point a.
   * @return ray with base _b and support _b
   */
  public Ray extendToASide() {
    return new Ray(_b, _a);
  }

  /**
   * Extends this line segment on the side of point b.
   * @return ray with base _a and support _b
   */
  public Ray extendToBSide() {
    return new Ray(_a, _b);
  }

  /**
   * Extends this line segment to a line.
   */
  public Line extendToLine() {
    return new Line(_a, _b);
  }

  @Override
  public String toString() {
    return "[" + _a + "," + _b + "]";
  }

  @Override
  public LineSegment clone() {
    return new LineSegment(_a.clone(), _b.clone());
  }
}
