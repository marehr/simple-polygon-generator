package polygonsSWP.geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import polygonsSWP.util.Random;

import polygonsSWP.util.MathUtils;


/**
 * This class just represents the smallest kind of polygon, a triangle. It
 * contains a list of points, which are forming the polygon. They are assumed to
 * be ordered counter clockwise. Every method is implemented according to the
 * interface and documentation of OrderedListPolygon.
 * 
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
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

  /**
   * @return Returns the surface area of the triangle.
   */
  @Override
  public double getSurfaceArea() {
    List<Point> trianglePoints = this.getPoints();
    Vector u = new Vector(trianglePoints.get(0), trianglePoints.get(1));
    Vector v = new Vector(trianglePoints.get(0), trianglePoints.get(2));
    return Math.abs(u.x * v.y - u.y * v.x) / 2.0;
  }

  /**
   * @return Returns random point within the triangle.
   */
  @Override
  public Point createRandomPoint() {
    Random random = Random.create();

    Point a = _coords.get(0), b = _coords.get(1) , c = _coords.get(2);

    // Choose random Point in rectangle with length of edges according to
    // length of vector. Then scale Point to actual Point in Parallelogram.
    Vector v1 = new Vector(a, b);
    Vector v2 = new Vector(a, c);

    double random1 = random.nextDouble();
    double random2 = random.nextDouble();

    Vector p = v1.mult(random1).add(v2.mult(random2)).add(a);

    if(containsPoint(p, true)) return p;

    return v1.add(v2).sub(p.sub(a)).add(a);
  }

  /**
   * Randomly selects a Triangle from a list of Triangles weighted by its
   * Surface Area. It is assumed, that the given List of Polygons only contains
   * Triangles.
   * 
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

    Random random = Random.create();
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
