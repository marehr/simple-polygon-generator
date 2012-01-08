package polygonsSWP.geometry;

import polygonsSWP.util.intersections.IntersectionUtils;
import polygonsSWP.util.intersections.LineSegmentIntersectionMode;

public class LineSegment
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
   * @param p Point that may be on this LineSegment.
   * @return true if p is in this LineSegment, else false.
   */
  public boolean containsPoint(Point p) {
    double length = _a.distanceTo(_b);
    double combinedDist = _a.distanceTo(p) + _b.distanceTo(p);
    return Math.abs(combinedDist - length) < 0.0001;
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
   *         are coincident, array containing intersection Point else. 
   *         TODO: change in using methods
   */
  public Point[] intersect(LineSegment e, boolean ignoreSharedEndpoints) {
    return IntersectionUtils.intersect(this._a, this._b, 
        e._a, e._b, new LineSegmentIntersectionMode(!ignoreSharedEndpoints), 
        new LineSegmentIntersectionMode(!ignoreSharedEndpoints));
  }

  public Ray extendToASide() {
    return new Ray(_b, _a);
  }

  public Ray extendToBSide() {
    return new Ray(_a, _b);
  }

  public Line extendToLine() {
    return new Line(_a, _b);
  }

  @Override
  public String toString() {
    return "[" + _a + "," + _b + "]";
  }
  
  public LineSegment clone() {
    return new LineSegment(_a.clone(), _b.clone());
  }
}
