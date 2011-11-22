package polygonsSWP.geometry;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Polygon interface for simple _and_ complex polygons. Subclasses should 
 * never assume they contain a simple polygon.
 * 
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de
 */
public abstract class Polygon
{
  /**
   * @return Returns the ordered list of points associated with the polygon,
   * with an implicit edge between ret[ret.size()] and ret[0].
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
   * Creates a random Point in given Triangle. Mirror Triangle to create
   * Parallelogram. Chooses random Point in Parallelogram, then checks if Point
   * is in original Triangle. Chooses new Point, if that is not the case, until
   * true. TODO: Invert created Point if not in original Triangle instead of
   * simply rejecting it. Testing! Used for createRandomPointInPolygon.
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @see
   * @param polygon Triangle point is created in. It is assumed, that Polygon is
   *          Triangle.
   * @return Point inside Triangle, randomly chosen.
   */
  protected Point createRandomPointInTriangle(Polygon polygon) {
    Random random = new Random(System.currentTimeMillis());
    List<Point> polygonPoints = polygon.getPoints();
  
    assert (polygonPoints.size() == 3);
  
    // Choose random Point in rectangle with length of edges according to length
    // of vector. Then scale Point to actual Point in Parallelogram.
    Vector u = new Vector(polygonPoints.get(0), polygonPoints.get(1));
    Vector v = new Vector(polygonPoints.get(0), polygonPoints.get(2));
    double randomPoint1 = random.nextDouble();
    double randomPoint2 = random.nextDouble();
    double x = u.v1 * randomPoint1 + v.v1 * randomPoint2;
    double y = u.v2 * randomPoint1 + v.v2 * randomPoint2;
  
    Point point = new Point((long) x, (long) y);
  
    if (!polygon.containsPoint(point, true)) {
      point = createRandomPointInTriangle(polygon);
    }
    return point;
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
  protected Polygon selectRandomTriangleBySize(List<Polygon> polygons) {
    // This algorithm works as follows:
    // 1. sum the weights (totalSurfaceArea)
    // 2. select a uniform random value (randomValue) u 0 <= u < sum of weights
    // 3. iterate through the items, keeping a running total (runnigTotal) of
    // the weights of the items you've examined
    // 4. as soon as running total >= random value, select the item you're
    // currently looking at (the one whose weight you just added).
  
    Random random = new Random(System.currentTimeMillis());
    HashMap<Polygon, Long> surfaceAreaTriangles = new HashMap<Polygon, Long>();
    long totalSurfaceArea = 0;
    for (Polygon polygon2 : polygons) {
      long polygon2SurfaceArea =
          Math.round(Math.ceil(calcualteSurfaceAreaOfTriangle(polygon2)));
      totalSurfaceArea += polygon2SurfaceArea;
      surfaceAreaTriangles.put(polygon2, polygon2SurfaceArea);
    }
    long randomValue =
        Math.round(Math.ceil(random.nextDouble() * totalSurfaceArea));
    long runningTotal = 0;
    for (Polygon polygon2 : polygons) {
      runningTotal += surfaceAreaTriangles.get(polygon2);
      if (runningTotal >= randomValue) { return polygon2; }
    }
    return null;
  }


  /**
   * Calculates the Surface Area of a given Triangle by using two of its side
   * vectors.
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @param polygon Triangle to calculate Surface Area for. It is assumed, that
   *          Polygon is a Triangle.
   * @return Surface Area
   */
  protected double calcualteSurfaceAreaOfTriangle(Polygon polygon) {
    List<Point> trianglePoints = polygon.getPoints();
    assert (trianglePoints.size() == 3);
    Vector u = new Vector(trianglePoints.get(0), trianglePoints.get(1));
    Vector v = new Vector(trianglePoints.get(0), trianglePoints.get(2));
    return (Math.abs(u.v1 * v.v2 - u.v2 * v.v1)) / 2.0;
  }
}
