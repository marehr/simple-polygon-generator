package polygonsSWP.data;

public class Edge
{
  public Point a;
  public Point b;
  public Edge(Point _a, Point _b) {
    a = _a;
    b = _b;
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
}
