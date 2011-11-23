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
   * Stolen from
   * http://paulbourke.net/geometry/lineline2d/
   * 
   * Tests whether two edges (line segments) intersect
   * or whether they are coincident.
   * 
   * @param e another edge
   * @return true, if the edges cross each other or are coincident
   */
  public boolean isIntersecting(LineSegment e) {
       
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
