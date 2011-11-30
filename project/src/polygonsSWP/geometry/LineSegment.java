package polygonsSWP.geometry;

public class LineSegment
{
  public Point a;
  public Point b;

  public LineSegment(Point _a, Point _b) {
    a = _a;
    b = _b;

    // TODO remove
    assert (!a.equals(b));
  }

  public boolean contains(Point a_) {
    if (a.equals(a_) || b.equals(a_)) return true;
    else return false;
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
    return (this.contains(e.a) && this.contains(e.b) && !e.a.equals(e.b));
  }

  /**
   * Convenience function. Will find shared endpoints, too.
   */
  public boolean isIntersecting(LineSegment e, Point[] isect) {
    return isIntersecting(e, isect, false);
  }

  /**
   * Stolen from: http://paulbourke.net/geometry/lineline2d/ Good explanation is
   * also here:
   * http://stackoverflow.com/questions/563198/how-do-you-detect-where
   * -two-line-segments-intersect Tests whether two line segments intersect or
   * whether they are coincident.
   * 
   * @param e another edge
   * @param isect array of Points of length >= 1. isect[0] is used as out
   *          parameter. If it is null, the line segments are coincident.
   * @param ignoreSharedEndpoints if set, shared endpoints will not be treated
   *          as an intersection.
   * @return true, if the line segments cross each other or are coincident
   */
  public boolean isIntersecting(LineSegment e, Point[] isect,
      boolean ignoreSharedEndpoints) {

    // TODO remove
    assert (isect != null && isect.length >= 1);

    double mua, mub;
    long denom, numera, numerb;

    denom =
        (e.b.y - e.a.y) * (this.b.x - this.a.x) - (e.b.x - e.a.x) *
            (this.b.y - this.a.y);
    numera =
        (e.b.x - e.a.x) * (this.a.y - e.a.y) - (e.b.y - e.a.y) *
            (this.a.x - e.a.x);
    numerb =
        (this.b.x - this.a.x) * (this.a.y - e.a.y) - (this.b.y - this.a.y) *
            (this.a.x - e.a.x);

    /* Are the lines coincident? */
    if (numera == 0 && numerb == 0 && denom == 0) {
      isect[0] = null;
      return true;
    }

    /* Are the lines parallel? */
    if (denom == 0) { return false; }

    /* Is the intersection along the segments? */
    mua = (double) numera / (double) denom;
    mub = (double) numerb / (double) denom;

    if ((ignoreSharedEndpoints && (mua > 0 && mua < 1 && mub > 0 && mub < 1)) ||
        (!ignoreSharedEndpoints && (mua >= 0 && mua <= 1 && mub >= 0 && mub <= 1))) {
      long isx = Math.round(this.a.x + mua * (this.b.x - this.a.x));
      long isy = Math.round(this.a.y + mua * (this.b.y - this.a.y));
      isect[0] = new Point(isx, isy);
      return true;
    }

    return false;
  }
}
