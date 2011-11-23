package polygonsSWP.geometry;

public class LineSegment
{
  public Point a;
  public Point b;
  public LineSegment(Point _a, Point _b) {
    a = _a;
    b = _b;
    
    // TODO remove
    assert(!a.equals(b));
  }
  
  /**
   * Equality check.
   * 
   * @return true, if the edges are the same, regardless of their direction
   *         false, otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if(!(obj instanceof LineSegment))
      return false;
    
    LineSegment e = (LineSegment) obj;
    
    return ((e.a == a) && (e.b == b)) || ((e.a == b) && (e.b == a));
  }
  
  /**
   * Stolen from:
   * http://paulbourke.net/geometry/lineline2d/
   * Good explanation is also here:
   * http://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
   * 
   * Tests whether two line segments intersect or whether they are coincident.
   * 
   * @param e another edge
   * @param isect array of Points of length >= 1. isect[0] is used as out parameter.
   *        If it is null, the line segments are coincident.
   * @return true, if the line segments cross each other or are coincident
   */
  public boolean isIntersecting(LineSegment e, Point[] isect) {
       
    // TODO remove
    assert(isect != null && isect.length >= 1);
    
    double mua, mub;
    long denom, numera, numerb;

    denom  = (e.b.y - e.a.y) * (this.b.x - this.a.x) 
        - (e.b.x - e.a.x) * (this.b.y - this.a.y);
    numera = (e.b.x - e.a.x) * (this.a.y - e.a.y) 
        - (e.b.y - e.a.y) * (this.a.x - e.a.x);
    numerb = (this.b.x - this.a.x) * (this.a.y - e.a.y) 
        - (this.b.y - this.a.y) * (this.a.x - e.a.x);

    /* Are the lines coincident? */
    if(numera == 0 && numerb == 0 && denom == 0) {
      isect[0] = null;
      return true;
    }

    /* Are the lines parallel? */
    if(denom == 0) {
      return false;
    }

    /* Is the intersection along the segments? */
    mua = (double) numera / (double) denom;
    mub = (double) numerb / (double) denom;
    
    if(mua >= 0 && mua <= 1 && mub >= 0 && mub <= 1) {
      int isx = (int) Math.round(this.a.x + mua * (this.b.x - this.a.x));
      int isy = (int) Math.round(this.a.y + mua * (this.b.y - this.a.y));
      isect[0] = new Point(isx, isy);
      return true;
    }
    
    return false;
  }

  public float getSlope() {
    if(a.x < b.x)
      return (b.y - a.y) / (b.x - a.x);
    else if(a.x > b.x)
      return (a.y - b.y) / (a.x - b.x);
    else
      return Float.NaN;
  }
}
