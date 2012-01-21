package polygonsSWP.geometry;

import java.util.ArrayList;
import java.util.List;

import polygonsSWP.util.MathUtils;
import polygonsSWP.util.intersections.IntersectionMode;
import polygonsSWP.util.intersections.IntersectionUtils;
import polygonsSWP.util.intersections.LineIntersectionMode;
import polygonsSWP.util.intersections.LineSegmentIntersectionMode;
import polygonsSWP.util.intersections.RayIntersectionMode;

/**
 * Polygon interface for simple _and_ complex polygons. Subclasses should never
 * assume they contain a simple polygon.
 * 
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 */
public abstract class Polygon implements Cloneable
{
  /**
   * @return Returns the ordered list of points associated with the polygon,
   *         with an implicit edge between ret[ret.size()] and ret[0].
   */
  public abstract List<Point> getPoints();

  /**
   * @return Returns a copy of the polygon instance.
   */
  public abstract Polygon clone();

  /**
   * @return True if object equals polygon, false otherwise
   */
  public abstract boolean equals(Object obj);

  /**
   * @return the number of vertices
   */
  public abstract int size();
  
  /**
   * @return Surface area as double.
   */
  public abstract double getSurfaceArea();

  /**
   * @return a random point in the polygon area (including on the edges).
   */
  public abstract Point createRandomPoint();
  
  /**
   * @return list of point triplets. first of each triple is an intersection
   *         point, the following are the points representing the line segment
   *         containing the intersection point. Returns empty list if there is no
   *         intersection. Returns a Point-array {null, a, b} if the line
   *         segment a, b is coincident with given line segment. It is assured, 
   *         that the points a, b are in the same order as in the polygon.
   *         Finally it returns {x, null, null} if the intersections is a
   *         vertex of the polygon.
   */
  public List<Point[]> intersect(LineSegment ls) {
    return abstractIntersect(ls._a, ls._b, new LineSegmentIntersectionMode(true));
  }

  /**
   * @return list of point triplets. first of each triple is an intersection
   *         point, the following are the points representing the line segment
   *         containing the intersection point. Returns empty list if there is no
   *         intersection. Returns a Point-array {null, a, b} if the line
   *         segment a, b is coincident with given line segment. It is assured, 
   *         that the points a, b are in the same order as in the polygon.
   *         Finally it returns {x, null, null} if the intersections is a
   *         vertex of the polygon.
   */
  public List<Point[]> intersect(Ray r) {
    return abstractIntersect(r._base, r._support, new RayIntersectionMode(true));
  }
  
  /**
   * @return list of point triplets. first of each triple is an intersection
   *         point, the following are the points representing the line segment
   *         containing the intersection point. Returns empty list if there is no
   *         intersection. Returns a Point-array {null, a, b} if the line
   *         segment a, b is coincident with given line segment. It is assured, 
   *         that the points a, b are in the same order as in the polygon.
   *         Finally it returns {x, null, null} if the intersections is a
   *         vertex of the polygon.
   */
  public List<Point[]> intersect(Line l) {
    return abstractIntersect(l._a, l._b, new LineIntersectionMode());
  }
  
  final private List<Point[]> abstractIntersect(Point a, Point b, IntersectionMode im) {
    List<Point[]> intersections = new ArrayList<Point[]>();
    List<Point> points = getPoints();
    IntersectionMode imv = new LineSegmentIntersectionMode(true);
    for (int i = 0, j = points.size() - 1; i < points.size(); j = i++) {
      Point vj = points.get(j);
      Point vi = points.get(i);
      Point[] isec = IntersectionUtils.intersect(vj, vi, a, b, imv, im);
      if (isec != null) {
        if (isec.length != 0) {
          
          if(isec[0].equals(vj)) {
            // Intersection on vertex of polygon.
            intersections.add(new Point[] { isec[0], null, null });
          } else if(!isec[0].equals(vi)) {
            // If isec[0] == vi, Intersection is a vertex of polygon, too,
            // but we add it only once.
            
            // Real intersection.
            intersections.add(new Point[] { isec[0], a, b });
          }
          
        } else {
          // Coincident with polygon edge.
          intersections.add(new Point[] { null, a, b });
        }
      }
    }
    
    return intersections;
  }
  
  /**
   * Tests if p is inside the given Polygon.
   * <http://geosoft.no/software/geometry/Geometry.java.html> Added a test to
   * check if Point is on line.
   * 
   * @author Steve Dierker <dierker.steve@fu-berlin.de>
   * @param p Point to be checked if it is in polygon
   * @param onLine whether a point lying on an edge is counted as in or out of
   *          polygon
   * @return True if Point is in/on Polygon, otherwise false
   */
  public boolean containsPoint(Point p, boolean onLine) {
    List<Point> pList = this.getPoints();
    boolean isInside = false;
    int nPoints = pList.size();

    for (int i = 0, j = nPoints - 1; i < nPoints; j = i++) {
      Point pi = pList.get(i);
      Point pj = pList.get(j);

      /* 
       * Found here:
       * http://stackoverflow.com/questions/217578/point-in-polygon-aka-hit-test
       * Originial Source:
       * http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
       */
      if((pi.y - p.y > MathUtils.EPSILON) != (pj.y - p.y > MathUtils.EPSILON)) {
        if(p.x - ((pj.x - pi.x) * (p.y - pi.y) / (pj.y - pi.y) + pi.x) < MathUtils.EPSILON) {
          isInside = !isInside;
        }
      }

      // we check, if point is on the line segment pj, pi
      // (including endpoints)
      if (new LineSegment(pj, pi).containsPoint(p)){
        return onLine;
      }
    }
    return isInside;
  }

  /**
   * Print the polygon in our own file format.
   */
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for(Point p : getPoints()) {
      sb.append(p.x);
      sb.append(" ");
      sb.append(p.y);
      sb.append("\n");
    }
    return sb.toString();
  }
  
  /**
   * @return a SVG representation of the polygon contained in a string
   */
  public String toSVG() {
    StringBuilder sb = new StringBuilder();
    List<Point> points = getPoints();

    sb.append("<?xml version=\"1.0\"?>\n");
    sb.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");
    sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n");
    if (points.size() != 0) {
      sb.append("<polygon points=\"");
      for (Point p : points) {
        sb.append(p.x);
        sb.append(",");
        sb.append(p.y);
        sb.append(" ");
      }
      sb.append("\" style=\"fill:lime;stroke:purple;stroke-width:1\" />\n");
    }
    sb.append("</svg>\n");
    return sb.toString();
  }

  /**
   * @return Center of mass of Polygon points.
   */
  public Point centerOfMass() {
    double x = 0, y = 0;
    for(Point p : getPoints()) {
      x += p.x;
      y += p.y;
    }
    
    x /= size();
    y /= size();
    
    return new Point(x, y);
  }
}
