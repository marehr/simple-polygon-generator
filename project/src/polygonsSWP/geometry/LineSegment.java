package polygonsSWP.geometry;

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
    return _a.distanceTo(p) + _b.distanceTo(p) == _a.distanceTo(_b);
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
   * @author malte, jannis
   * @param e another edge
   * @param isect array of Points of length >= 1. isect[0] is used as out
   *          parameter. If it is null, the line segments are coincident.
   * @param ignoreSharedEndpoints if set, shared endpoints will not be treated
   *          as an intersection.
   * @return null if line segments do not intersect, array of length 0 if both
   *         are coincident, array containing intersection Point else. TODO:
   *         change in using methods
   */
  public Point[] intersect(LineSegment e, boolean ignoreSharedEndpoints) {

    double mua, mub;
    long denom, numera, numerb;

    denom =
        (e._b.y - e._a.y) * (this._b.x - this._a.x) - (e._b.x - e._a.x) *
            (this._b.y - this._a.y);
    numera =
        (e._b.x - e._a.x) * (this._a.y - e._a.y) - (e._b.y - e._a.y) *
            (this._a.x - e._a.x);
    numerb =
        (this._b.x - this._a.x) * (this._a.y - e._a.y) -
            (this._b.y - this._a.y) * (this._a.x - e._a.x);

    /* Are the lines coincident? */
    if (numera == 0 && numerb == 0 && denom == 0) {
      Point[] intersection = { };
      return intersection;
    }

    /* Are the lines parallel? */
    if (denom == 0) { return null; }

    /* Is the intersection along the segments? */
    mua = (double) numera / (double) denom;
    mub = (double) numerb / (double) denom;

    if ((ignoreSharedEndpoints && (mua > 0 && mua < 1 && mub > 0 && mub < 1)) ||
        (!ignoreSharedEndpoints && (mua >= 0 && mua <= 1 && mub >= 0 && mub <= 1))) {
      long isx = Math.round(this._a.x + mua * (this._b.x - this._a.x));
      long isy = Math.round(this._a.y + mua * (this._b.y - this._a.y));
      Point[] intersection = { new Point(isx, isy) };
      return intersection;
    }

    return null;
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
}
