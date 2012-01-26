package polygonsSWP.geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import polygonsSWP.util.MathUtils;
import polygonsSWP.util.RandomNumbers;


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
  public Triangle clone() {
    List<Point> nList = new ArrayList<Point>(_coords.size());
    for (Point item : _coords)
      nList.add(item.clone());
    return new Triangle(nList);
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
    Random random = new RandomNumbers(System.currentTimeMillis());

    // Choose random Point in rectangle with length of edges according to
    // length
    // of vector. Then scale Point to actual Point in Parallelogram.
    Vector v0 = new Vector(_coords.get(0).x, _coords.get(0).y);
    Vector v1 = new Vector(_coords.get(1).x, _coords.get(1).y);
    Vector v2 = new Vector(_coords.get(2).x, _coords.get(2).y);

    // TODO: real [0,1]
    double random1 = random.nextDouble();
    double random2 = random.nextDouble();
    Vector x1 = v1.sub(v0).mult(random1).add(v2.sub(v0).mult(random2));

    // check if Ox is in triangle
    Vector v3 = v1.sub(v0).add(v2.sub(v0));
    Point point = new Point(x1.v1, x1.v2);

    if (!containsPoint(point, true)) {
      Vector x2 = v0.add(x1.sub(v3));
      point = new Point(-x2.v1, -x2.v2);
    }
    return point;
  }

  public Trapezoid getMonotonPolygon() {
    return new Trapezoid(_coords);
  }

  public OrderedListPolygon getOrderedListPolygon() {
    return new OrderedListPolygon(_coords);
  }

  /**
   * @author Steve Dierker <dierker.steve@fu-berlin.de>
   * @param middle
   * @param left
   * @param right
   * @return
   */
  public static boolean formsTriangle(LineSegment middle, LineSegment left,
      LineSegment right) {
    if (middle.equals(right) || middle.equals(left) || right.equals(left))
      return false;
    if (middle._a.equals(right._a)) {
      if (middle._b.equals(left._a)) if (left._b.equals(right._b)) return true;
      if (middle._b.equals(left._b)) if (left._a.equals(right._b)) return true;
    }
    if (middle._a.equals(right._b)) {
      if (middle._b.equals(left._a)) if (left._b.equals(right._a)) return true;
      if (middle._b.equals(left._b)) if (left._a.equals(right._a)) return true;
    }
    if (middle._b.equals(right._a)) {
      if (middle._a.equals(left._a)) if (left._b.equals(right._b)) return true;
      if (middle._a.equals(left._b)) if (left._a.equals(right._b)) return true;
    }
    if (middle._b.equals(right._b)) {
      if (middle._a.equals(left._a)) if (left._b.equals(right._a)) return true;
      if (middle._a.equals(left._b)) if (left._a.equals(right._a)) return true;
    }
    return false;
  }

  /**
   * Randomly selects a Triangle from a list of Triangles weighted by its
   * Surface Area. It is assumed, that the given List of Polygons only contains
   * Triangles. TODO: still safe although surface areas calculated as doubles?
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @param triangles
   * @return
   */
  public static Triangle selectRandomTriangleBySize(List<Triangle> triangles) {
    // This algorithm works as follows:
    // 1. sum the weights (totalSurfaceArea)
    // 2. select a uniform random value (randomValue) u 0 <= u < sum of weights
    // 3. iterate through the items, keeping a running total (runnigTotal) of
    // the weights of the items you've examined
    // 4. as soon as running total >= random value, select the item you're
    // currently looking at (the one whose weight you just added).

    System.out.println("-----------------\n");
    System.out.println("selectRandomTriangleBySize");
    Random random = new RandomNumbers(System.currentTimeMillis());
    HashMap<Triangle, Double> surfaceAreaTriangles =
        new HashMap<Triangle, Double>();
    double total = 0;
    for (Triangle triangle : triangles) {
      double triangleSurface = triangle.getSurfaceArea();
      total += triangleSurface;
      surfaceAreaTriangles.put(triangle, triangleSurface);
    }
    double randomValue = random.nextDouble() * total;
    double runningTotal = 0;
    for (Triangle triangle : triangles) {
      runningTotal += surfaceAreaTriangles.get(triangle);
      if (runningTotal >= randomValue - MathUtils.EPSILON) { return triangle; }
    }
    return null;
  }

  public String toString() {
    String result = "[";
    for (Point item : _coords)
      result += item + " ";
    return result.trim() + "]";
  }

  @Override
  public int size() {
    return 3;
  }
}
