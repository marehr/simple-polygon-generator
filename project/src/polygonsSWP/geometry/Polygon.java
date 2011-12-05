package polygonsSWP.geometry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


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
   * @return If point is in Polygon.
   */
  public abstract boolean containsPoint(Point p, boolean onLine);

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
   * Prints the polygon to a svg file, no questions asked. Debug method to avoid
   * having to write too much try..catch clauses.
   * 
   * @param filename filename to save svg into TODO remove or change
   */
  public void toSVGFile(String filename) {
    try {
      File f = new File(filename);
      f.createNewFile();
      OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(f));
      out.write(toSVG());
      out.close();
    }
    catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}