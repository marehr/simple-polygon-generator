package polygonsSWP.geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import polygonsSWP.util.MathUtils;


/**
 * This class just represents the smallest kind of polygon, a triangle. It
 * contains a list of points, which are forming the polygon. They are assumed to
 * be ordered counter clockwise. Every method is implemented according to the
 * interface and documentation of OrderedListPolygon.
 * 
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 */

public class Triangle
  extends Polygon
{
  private List<Point> _coords;

  public Triangle(List<Point> points) {
    _coords = points;
  }

  public Triangle() {
    this(new ArrayList<Point>());
  }

  public Triangle(Point a, Point b, Point c) {
    _coords = new ArrayList<Point>();
    _coords.add(a);
    _coords.add(b);
    _coords.add(c);
  }

  @Override
  public List<Point> getPoints() {
    return _coords;
  }

  @Override
  public Polygon clone() {
    List<Point> tmpList = new ArrayList<Point>();
    tmpList.addAll(_coords);
    return new Triangle(tmpList);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Triangle) {
      List<Point> tmpList = ((Triangle) obj).getPoints();
      if (tmpList.contains(_coords.get(0))) {
        int index = tmpList.indexOf(_coords.get(0));
        if (tmpList.get((index + 1) % 3).equals(_coords.get(1)) &&
            tmpList.get((index + 2) % 3).equals(_coords.get(2))) return true;
      }
      return false;
    }
    else {
      // TODO: check for other polygon types with 3 points would be fancy!
      return false;
    }
  }

  /**
   * @param onLine: whether on the edge is counted as inor outside the polygon.
   * @return Checks whether the triangle contains the given point.
   */
  @Override
  public boolean containsPoint(final Point p, final boolean onLine) {
    boolean isInside = true;
    boolean isOnLine = false;
    Point first = _coords.get(2);
    for (int i = 0; i < _coords.size(); ++i) {
      if (!(MathUtils.checkOrientation(first, _coords.get(i), p) == 1))
        isInside = false;
      first = _coords.get(i);
      if (p.isBetween(first, _coords.get(i))) isOnLine = true;
      first = _coords.get(i);
    }
    if (onLine) return isInside || isOnLine;
    else return isInside;
  }

  /**
   * @return Returns the surface area of the triangle.
   */
  @Override
  public double getSurfaceArea() {
    List<Point> trianglePoints = this.getPoints();
    Vector u = new Vector(trianglePoints.get(0), trianglePoints.get(1));
    Vector v = new Vector(trianglePoints.get(0), trianglePoints.get(2));
    return Math.abs(u.v1 * v.v2 - u.v2 * v.v1) / 2.0;
  }

  /**
   * @return Returns random point within the triangle.
   */
  @Override
  public Point createRandomPoint() {
    Point retval;
    Random random = new Random(System.currentTimeMillis());
    do {
      // Choose random Point in rectangle with length of edges according to
      // length
      // of vector. Then scale Point to actual Point in Parallelogram.
      Vector u = new Vector(_coords.get(0), _coords.get(1));
      Vector v = new Vector(_coords.get(0), _coords.get(2));
      double randomPoint1 = random.nextDouble();
      double randomPoint2 = random.nextDouble();
      double x = u.v1 * randomPoint1 + v.v1 * randomPoint2;
      double y = u.v2 * randomPoint1 + v.v2 * randomPoint2;

      retval = new Point((long) x, (long) y);

    }
    while (!containsPoint(retval, true));
    return retval;
  }

  public MonotonPolygon getMonotonPolygon() {
    List<LineSegment> tmpList = new ArrayList<LineSegment>();
    tmpList.add(new LineSegment(_coords.get(0), _coords.get(1)));
    tmpList.add(new LineSegment(_coords.get(1), _coords.get(2)));
    tmpList.add(new LineSegment(_coords.get(2), _coords.get(0)));
    return new MonotonPolygon(tmpList);
  }

  public OrderedListPolygon getOrderedListPolygon() {
    return new OrderedListPolygon(_coords);
  }

  /**
   * Randomly selects a Triangle from a list of Triangles weighted by its
   * Surface Area. It is assumed, that the given List of Polygons only contains
   * Triangles. TODO: still safe although surface areas calculated as doubles?
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @param polygons
   * @return
   */
  public static Triangle selectRandomTriangleBySize(List<Triangle> polygons) {
    // This algorithm works as follows:
    // 1. sum the weights (totalSurfaceArea)
    // 2. select a uniform random value (randomValue) u 0 <= u < sum of weights
    // 3. iterate through the items, keeping a running total (runnigTotal) of
    // the weights of the items you've examined
    // 4. as soon as running total >= random value, select the item you're
    // currently looking at (the one whose weight you just added).

    Random random = new Random(System.currentTimeMillis());
    HashMap<Triangle, Long> surfaceAreaTriangles =
        new HashMap<Triangle, Long>();
    long totalSurfaceArea = 0;
    for (Triangle polygon2 : polygons) {
      long polygon2SurfaceArea =
          Math.round(Math.ceil(polygon2.getSurfaceArea()));
      totalSurfaceArea += polygon2SurfaceArea;
      surfaceAreaTriangles.put(polygon2, polygon2SurfaceArea);
    }
    long randomValue =
        Math.round(Math.ceil(random.nextDouble() * totalSurfaceArea));
    long runningTotal = 0;
    for (Triangle polygon2 : polygons) {
      runningTotal += surfaceAreaTriangles.get(polygon2);
      if (runningTotal >= randomValue) { return polygon2; }
    }
    return null;
  }
}
