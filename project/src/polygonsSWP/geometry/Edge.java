package polygonsSWP.geometry;

public class Edge
{
  public Point a;
  public Point b;
  public Edge(Point _a, Point _b) {
    a = _a;
    b = _b;
  }
  
  /**
   * Equality check.
   * 
   * @return true, if the edges are the same, regardless of their direction
   *         false, otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if(!(obj instanceof Edge))
      return false;
    
    Edge e = (Edge) obj;
    
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
  public boolean isIntersecting(Edge e) {
       
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
    
    /* 
     * REMARK: In the original version of this algorithm,
     * mua & mub were tested for greater than (less than) _or equal_ to
     * 0 (1). I removed this 'equal to' because I guess mua&mub are 0 (1) when
     * the line segments share the same start or end point.
     * This is often the case when we check our polygons for intersecting lines.
     * Still, I'm not really confident whether this didn't break the whole algorithm.
     * Need to investigate this.
     */
    if(mua > 0 && mua < 1 && mub > 0 && mub < 1) {
       return true;
    }
    
    return false;
  }

  public static Point intersetingPointOfTwoLines(Point aBegin, Point aEnd,
      Point bBegin, Point bEnd) {
    double aN = 0, bN = 0;
    double aGrow = 0, bGrow = 0;
    boolean ax = false, bx = false;
    // Check if line is tilted, parallel to x or y
    if (aBegin.x - aEnd.x == 0) ax = true;
    else if (aBegin.y - aEnd.y == 0) {
      aGrow = 0;
      aN = aBegin.y;
    }
    else {
      aGrow = (aEnd.y - aBegin.y) / (aEnd.x - (double) aBegin.x);
      aN = aBegin.y - aGrow * aBegin.x;
    }
    // Check if line is tilted, parallel to x or y
    if (bBegin.x - bEnd.x == 0) bx = true;
    else if (bBegin.y - bEnd.y == 0) {
      bGrow = 0;
      bN = bBegin.y;
    }
    else {
      bGrow = (bEnd.y - bBegin.y) / (bEnd.x - (double) bBegin.x);
      bN = bBegin.y - bGrow * bBegin.x;
    }
    // Both lines are parallel to x
    if ((ax && bx)) return null;
    // one of them is parallel to x
    else if (ax || bx) {
      if (ax) {
        double y = bGrow * aBegin.x + bN;
        return new Point(aBegin.x, (long) y);
      }
      else {
        System.out.println(aN + " " + aGrow + " " + bBegin.x);
        double y = aGrow * bBegin.x + aN;
        return new Point(bBegin.x, (long) y);
      }
    }
    // Both lines are parallel
    else if (aGrow == bGrow) return null;
    else {
      double x = (aN - bN) / (bGrow - aGrow);
      double y = aGrow * x + aN;
      return new Point((long) x, (long) y);
    }
  }
}
