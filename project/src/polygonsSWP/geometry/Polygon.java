package polygonsSWP.geometry;

import java.util.ArrayList;
import java.util.List;

import polygonsSWP.util.MathUtils;

/**
 * Polygon interface for simple _and_ complex polygons. Subclasses should never
 * assume they contain a simple polygon.
 * 
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 */
public abstract class Polygon
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
   *         containing the intersection point. returns null if there is no
   *         intersection. returns a Point-array {null, a, b} if the line
   *         segment a, b is coincident with given line segment. it is assured, that the
   *         points a, b are in the same order as in the polygon.
   */
  public List<Point[]> intersect(LineSegment ls) {
    List<Point> points = this.getPoints();
    int size = points.size();
    List<Point[]> intersections = new ArrayList<Point[]>();
    for (int i = 0; i < points.size(); i++) {
      Point a = points.get(i % size);
      Point b = points.get(i + 1 % size);
      Line line = new Line(a, b);
      Point[] isec = line.intersect(ls);
      if (isec != null) {
        if (isec.length != 0) {
          Point[] triple = { isec[0], a, b };
          intersections.add(triple);
        }
      }
    }
    if (intersections.size() == 0) {
      return null;
    }
    else {
      return intersections;
    }
  }

  /**
   * @return list of point triplets. first of each triple is an intersection
   *         point, the following are the points representing the line segment
   *         containing the intersection point. returns null if there is no
   *         intersection. returns a Point-array {null, a, b} if the line
   *         segment a, b is coincident with given ray. it is assured, that the
   *         points a, b are in the same order as in the polygon.
   */
  public List<Point[]> intersect(Ray r) {
    List<Point> points = this.getPoints();
    int size = points.size();
    List<Point[]> intersections = new ArrayList<Point[]>();
    for (int i = 0; i < points.size(); i++) {
      Point a = points.get(i % size);
      Point b = points.get(i + 1 % size);
      Line line = new Line(a, b);
      Point[] isec = line.intersect(r);
      if (isec != null) {
        if (isec.length != 0) {
          Point[] triple = { isec[0], a, b };
          intersections.add(triple);
        }
      }
    }
    if (intersections.size() == 0) {
      return null;
    }
    else {
      return intersections;
    }
  }

  /**
   * @return list of point triplets. first of each triple is an intersection
   *         point, the following are the points representing the line segment
   *         containing the intersection point. returns null if there is no
   *         intersection. returns a Point-array {null, a, b} if the line
   *         segment a, b is coincident with given line. it is assured, that the
   *         points a, b are in the same order as in the polygon.
   */
  public List<Point[]> intersect(Line l) {
    List<Point> points = this.getPoints();
    int size = points.size();
    List<Point[]> intersections = new ArrayList<Point[]>();
    for (int i = 0; i < points.size(); i++) {
      Point a = points.get(i % size);
      Point b = points.get(i + 1 % size);
      Line line = new Line(a, b);
      Point[] isec = line.intersect(l);
      if (isec != null) {
        if (isec.length != 0) {
          Point[] triple = { isec[0], a, b };
          intersections.add(triple);
        }
      }
    }
    if (intersections.size() == 0) {
      return null;
    }
    else {
      return intersections;
    }
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
    Point first = pList.get(pList.size() - 1);

    int j = 0;
    for (int i = 0; i < nPoints; i++) {
      j++;
      if (j == nPoints) j = 0;

      if (pList.get(i).y < p.y && pList.get(j).y >= p.y ||
          pList.get(j).y < p.y && pList.get(i).y >= p.y) {
        if (pList.get(i).x + (double) (p.y - pList.get(i).y) /
            (double) (pList.get(j).y - pList.get(i).y) *
            (pList.get(j).x - pList.get(i).x) < p.y) {
          isInside = !isInside;
        }
      }
      if (onLine)
        if (MathUtils.checkOrientation(first, pList.get(i), p) == 0) { return true; }
      first = pList.get(i);
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
}
