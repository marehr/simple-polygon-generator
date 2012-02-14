package polygonsSWP.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
public abstract class Polygon
  implements Cloneable
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
   * @return the number of vertices
   */
  public abstract int size();

  /**
   * Calculates the circumference
   * 
   * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
   * @return Circumference of the polygon
   */
  public double getCircumference(){
    List<Point> vertices = getPoints();
    double circumference = 0.0;

    for(int i = 0, j = vertices.size() -1; i < vertices.size(); j = i++){
      circumference += vertices.get(i).distanceTo(vertices.get(j));
    }

    return circumference;
  }

  /**
   * Calculates the Surface Area using the Gaussian formula.
   * 
   * @author Steve Dierker <dierker.steve@fu-berlin.de>
   * @return Surface area of the polygon
   */
  public double getSurfaceArea() {
    assert (size() >= 3);
    List<Point> vertices = getPoints();

    double result = 0.0;
    for (int p = size() - 1, q = 0; q < size(); p = q++) {
      result +=
          vertices.get(p).x * vertices.get(q).y - vertices.get(q).x *
              vertices.get(p).y;
    }
    return result / 2.0;
  }

  /**
   * @return a random point in the polygon area (including on the edges).
   */
  public abstract Point createRandomPoint();

  /**
   * @return list of point triplets. first of each triple is an intersection
   *         point, the following are the points representing the line segment
   *         containing the intersection point. Returns empty list if there is
   *         no intersection. Returns a Point-array {null, a, b} if the line
   *         segment a, b is coincident with given line segment. It is assured,
   *         that the points a, b are in the same order as in the polygon.
   *         Finally it returns {x, null, null} if the intersections is a vertex
   *         of the polygon.
   */
  public List<Point[]> intersect(LineSegment ls) {
    return intersect(ls, true);
  }
  
  public List<Point[]> intersect(LineSegment ls, boolean includeEndPoints) {
    return abstractIntersect(ls._a, ls._b,
        new LineSegmentIntersectionMode(includeEndPoints));
  }

  /**
   * @return list of point triplets. first of each triple is an intersection
   *         point, the following are the points representing the line segment
   *         containing the intersection point. Returns empty list if there is
   *         no intersection. Returns a Point-array {null, a, b} if the line
   *         segment a, b is coincident with given line segment. It is assured,
   *         that the points a, b are in the same order as in the polygon.
   *         Finally it returns {x, null, null} if the intersections is a vertex
   *         of the polygon.
   */
  public List<Point[]> intersect(Ray r) {
    return intersect(r, true);
  }

  public List<Point[]> intersect(Ray r, boolean includeEndPoint) {
    return abstractIntersect(r._base, r._support, new RayIntersectionMode(
        includeEndPoint));
  }

  /**
   * @return list of point triplets. first of each triple is an intersection
   *         point, the following points x, y represent the line segment
   *         containing the intersection point. Returns empty list if there is
   *         no intersection. Returns a Point-array {null, x, y} if the line
   *         segment x, y is coincident with given line segment. It is assured,
   *         that the points x, y are in the same order as in the polygon.
   *         Finally it returns {x, null, null} if the intersections is a vertex
   *         of the polygon.
   */
  public List<Point[]> intersect(Line l) {
    return abstractIntersect(l._a, l._b, new LineIntersectionMode());
  }

  final private List<Point[]> abstractIntersect(Point a, Point b,
      IntersectionMode im) {
    List<Point[]> intersections = new ArrayList<Point[]>();
    List<Point> points = getPoints();
    IntersectionMode imv = new LineSegmentIntersectionMode(true);
    for (int i = 0, j = points.size() - 1; i < points.size(); j = i++) {
      Point vj = points.get(j);
      Point vi = points.get(i);
      Point[] isec = IntersectionUtils.intersect(vj, vi, a, b, imv, im);
      if (isec != null) {
        if (isec.length != 0) {

          if (isec[0].equals(vj)) {
            // Intersection on vertex of polygon.
            intersections.add(new Point[] { isec[0], null, null });
          }
          else if (!isec[0].equals(vi)) {
            // If isec[0] == vi, Intersection is a vertex of polygon, too,
            // but we add it only once.

            // Real intersection.
            intersections.add(new Point[] { isec[0], vj, vi });
          }

        }
        else {
          // Coincident with polygon edge.
          intersections.add(new Point[] { null, vj, vi });
        }
      }
    }

    return intersections;

  }

  /**
   * Simple test for equality. Should be improved if we introduce other
   * implementations of polygon.
   *
   * @return True if object equals polygon, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    // Is Object a Polygon?
    if (!(obj instanceof Polygon)) return false;

    Polygon that = (Polygon) obj;

    List<Point> p1 = this.getPoints(), p2 = that.getPoints();

    if (p1.size() != p2.size()) return false;

    // Get starting point and compare clockwise whole polygon
    Point startPoint = p1.get(0);
    int index = p2.indexOf(startPoint);

    if (index == -1) return false;

    for (int i = 1; i < p1.size(); ++i)
      if (!p1.get(i).equals(that.getPointInRange(index + i))) return false;

    return true;
  }

  /**
   * Gives Point at Position "pos"
   * 
   * @param pos
   * @return
   */
  public Point getPoint(final int pos){
    return getPoints().get(pos);
  }

  /**
   * Create an index via module which is always in range
   * 
   * @param index index to be modified
   * @return
   */
  public int getIndexInRange(final int index) {
    int result = index % size();
    return result < 0 ? result + size() : result;
  }

  /**
   * Return point with safe index.
   */
  public Point getPointInRange(final int index) {
    return getPoint(getIndexInRange(index));
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
      if ((pi.y - p.y > MathUtils.EPSILON) != (pj.y - p.y > MathUtils.EPSILON)) {
        if (p.x - ((pj.x - pi.x) * (p.y - pi.y) / (pj.y - pi.y) + pi.x) < MathUtils.EPSILON) {
          isInside = !isInside;
        }
      }

      // we check, if point is on the line segment pj, pi
      // (including endpoints)
      if (new LineSegment(pj, pi).containsPoint(p)) { return onLine; }
    }
    return isInside;
  }

  /**
   * Determines whether this polygon is in clockwise orientation.
   * 
   * @return -1 if counterclockwise, 1 if clockwise, or 0 if this is not
   *         decidable
   */
  public int isClockwise() {
    int n = size();
    if (n < 3) return 0;

    List<Point> points = getPoints();

    // source:
    // http://stackoverflow.com/questions/1165647/how-to-determine-if-a-list-of-polygon-points-are-in-clockwise-order
    double doubleArea = 0;
    for (int j = 0, i = n - 1; j < n; i = j++) {
      doubleArea += (points.get(j).x - points.get(i).x) *
                    (points.get(j).y + points.get(i).y);
    }

    if (doubleArea < 0) return -1;
    if (doubleArea > 0) return 1;
    return 0;
  }

  /**
   * Print the polygon in our own file format.
   */
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Point p : getPoints()) {
      sb.append(p.x);
      sb.append(" ");
      sb.append(p.y);
      sb.append("\n");
    }
    return sb.toString();
  }

  /**
   * @return Center of mass of Polygon points.
   */
  public Point centerOfMass() {
    double x = 0, y = 0;
    for (Point p : getPoints()) {
      x += p.x;
      y += p.y;
    }

    x /= size();
    y /= size();

    return new Point(x, y);
  }

  /**
   * Find first intersection of Ray and Polygon. Intersections with base and
   * support point of ray same as points of polygon are ignored , as well as 
   * collinear line segements.
   * 
   * @param r Ray
   * @return Returns array of points {intersection, a, b}, where ab is the
   *         lineSegment the intersection is on, or null.
   */
  public Point[] firstIntersection(final Ray r) {
    List<Point[]> isecs = intersect(r, false);
    
    Comparator<Point[]> isecComparator = new Comparator<Point[]>() {

      @Override
      public int compare(Point[] isec1, Point[] isec2) {
        if (isec1[0] != null && isec2[0] == null)
          return -1;
        else if (isec1[0] == null && isec2[0] != null)
          return 1;
        else if (isec1[0] == null && isec2[0] == null)
          return 0;
        
        else if(isec1[0].distanceTo(r._base) < isec2[0].distanceTo(r._base))
          return -1;
        else if(isec1[0].distanceTo(r._base) > isec2[0].distanceTo(r._base))
          return 1;
        else 
          return 0;
      }
    };
    
    Collections.sort(isecs, isecComparator);
    
    for (Point[] points : isecs) {
      if(points[0] != null && points[0] != r._support)
        return points;
    }
    
    return null;
  }
}
