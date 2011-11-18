package polygonsSWP.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import polygonsSWP.data.Point;
import polygonsSWP.data.Polygon;
import polygonsSWP.data.Vector;


/**
 * Helper class for reusable math functions.
 * 
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 */
public class MathUtils
{
  /**
   * Randomly creates a set of n points in a square defined by edge length s,
   * where each point holds 0 <= x < s && 0 <= y < s and no point equals another
   * point, so for all xi, xj in points: xi != xj.
   * 
   * @param n number of points
   * @param s length of edges of square
   * @return array of randomly distributed Points out of s^2, length of array ==
   *         n.
   */
  public static List<Point> createRandomSetOfPointsInSquare(int n, int s) {
    List<Point> retval = new ArrayList<Point>();
    Random r = new Random(System.currentTimeMillis());

    for (int i = 0; i < n; i++) {
      Point p = null;

      boolean notInSet = false;
      while (!notInSet) {
        p = new Point(r.nextInt(s), r.nextInt(s));

        notInSet = true;
        for (int j = 0; j < retval.size(); j++) {
          if (retval.get(j).equals(p)) {
            notInSet = false;
            break;
          }
        }
      }

      retval.add(p);
    }

    return retval;
  }

  /**
   * Tests if p is on the orientated segment given by 'begin' and 'end' or even
   * on the right or left side of the segment.
   * 
   * @see www-ma2.upc.es/~geoc/mat1q1112/OrientationTests.pdf
   * @see http 
   *      ://www.mochima.com/articles/cuj_geometry_article/cuj_geometry_article
   *      .html
   * @param p1 Starting point of the orientated segment (orientated line)
   * @param p2 End point of the orientated segment (orientated line)
   * @param p3 Point to test orientation for
   * @return if 1 => p is on the left side if -1 => p is on the right side if 0
   *         => p is on the segment
   */
  public static int checkOrientation(Point begin, Point end, Point p) {
    long result =
        begin.x * (end.y - p.y) + end.x * (p.y - begin.y) + p.x *
            (begin.y - end.y);
    if (result > 0) return 1;
    else if (result < 0) return -1;
    else return 0;
  }

  /**
   * Tests if p is inside the given Polygon.
   * <http://geosoft.no/software/geometry/Geometry.java.html> Added a test to
   * check if Point is on line.
   * 
   * @param polygon Polygon to check if point is in it.
   * @param p Point to be checked if it is in polygon
   * @return True if Point is in/on Polygon, otherwise false
   */
  public static boolean checkIfPointIsInPolygon(Polygon polygon, Point p) {
    List<Point> pList = polygon.getPoints();
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
      if (checkOrientation(first, pList.get(i), p) == 0) { return true; }
      first = pList.get(i);
    }
    return isInside;
  }

  /**
   * Triangulate Polygon with O(n^2) algorithm TODO: implement at least O(n log
   * n ) algorithm
   * 
   * @param poly Polygon to triangulate
   * @return List of triangulars
   */
  public static List<Polygon> triangulatePolygon(Polygon poly) {
    return null;
  }

  /**
   * Creates a random Point in Polygon. Uses Triangularization, randomly chooses
   * Triangle, creates random Point in Triangle.
   * 
   * @param polygon Polygon to create random point in
   * @return random Point in given Polygon
   */
  public static Point createRandomPointInPolygon(Polygon polygon) {
    //Triangulate given Polygon.
    List<Polygon> triangularization = triangulatePolygon(polygon);
    //Randomly choose one Triangle of Triangularization weighted by their
    //Surface Area.
    Polygon chosenPolygon = selectRandomPolygonBySize(triangularization);
    //Randomly choose Point in choosen Triangle.
    Point randomPoint = createRandomPointInTriangle(chosenPolygon);
    return randomPoint;
  }

  /**
   * Randomly selects a Polygon from a list of Polygons weighted by its Surface
   * Area.
   * 
   * @param polygons
   * @return
   */
  private static Polygon selectRandomPolygonBySize(List<Polygon> polygons) {
    // This algorithm works as follows:
    // 1. sum the weights (totalSurfaceArea)
    // 2. select a uniform random value (randomValue) u 0 <= u < sum of weights
    // 3. iterate through the items, keeping a running total (runnigTotal) of
    // the weights of the items you've examined
    // 4. as soon as running total >= random value, select the item you're
    // currently looking at (the one whose weight you just added).
    Random random = new Random();
    random.setSeed(System.currentTimeMillis());
    HashMap<Polygon, Long> surfaceAreaTriangles = new HashMap<Polygon, Long>();
    long totalSurfaceArea = 0;
    for (Polygon polygon2 : polygons) {
      surfaceAreaTriangles.put(polygon2,
          calcualteSurfaceAreaOfTriangle(polygon2));
    }
    long randomValue = Math.round(random.nextDouble() * totalSurfaceArea);
    long runningTotal = 0;
    for (Polygon polygon2 : polygons) {
      runningTotal += surfaceAreaTriangles.get(polygon2);
      if (runningTotal >= randomValue) { return polygon2; }
    }
    //This case should never occur! Bad style?
    assert(false);
    return null;
  }

  /**
   * Creates a random Point in given Triangle. Mirror Triangle to create
   * Parallelogram. Chooses random Point in Parallelogram, then checks if Point
   * is in original Triangle. Chooses new Point, if that is not the case, until
   * true. TODO: Invert created Point if not in original Triangle instead of
   * simply rejecting it. Testing! Used for createRandomPointInPolygon.
   * 
   * @param polygon Triangle point is created in. It is assumed, that Polygon is
   *          Triangle.
   * @return Point inside Triangle, randomly chosen.
   */
  private static Point createRandomPointInTriangle(Polygon polygon) {
    Random random = new Random();
    random.setSeed(System.currentTimeMillis());
    List<Point> polygonPoints = polygon.getPoints();

    assert (polygonPoints.size() == 3);

    // Choose random Point in rectangle with length of edges according to length
    // of vector. Then scale Point to actual Point in Parallelogram.
    Vector u = new Vector(polygonPoints.get(0), polygonPoints.get(1));
    Vector v = new Vector(polygonPoints.get(0), polygonPoints.get(2));
    double xUnscaled = random.nextDouble() * u.length();
    double yUnscaled = random.nextDouble() * v.length();
    double x = u.v1 * xUnscaled + v.v1 * yUnscaled;
    double y = u.v2 * xUnscaled + v.v2 * yUnscaled;

    Point point = new Point((long) x, (long) y);

    if (!checkIfPointIsInPolygon(polygon, point)) {
      point = createRandomPointInTriangle(polygon);
    }
    return point;
  }

  /**
   * Calculates the Surface Area of a given Polygon. TODO: currently uses the
   * Triangularization of the given Polygon to calculate and add resulting
   * Triangles. Gaussian Formula should be more effective.
   * 
   * @param polygon Polygon to calculate Size of.
   * @return Surface Area of given Polygon2
   */
  public static long calculateSurfaceAreaOfPolygon(Polygon polygon) {
    List<Point> polygonPoints = polygon.getPoints();
    if (polygonPoints.size() == 3) {
      return calcualteSurfaceAreaOfTriangle(polygon);
    }
    else {
      long surfaceArea = 0;
      List<Polygon> triangularization = triangulatePolygon(polygon);
      for (Polygon polygon2 : triangularization) {
        surfaceArea += calcualteSurfaceAreaOfTriangle(polygon2);
      }
      return surfaceArea;
    }
  }

  /**
   * Calculates the Surface Area of a given Triangle by using two of its side
   * vectors.
   * 
   * @param polygon Triangle to calculate Surface Area for. It is assumed, that
   *          Polygon is a Triangle.
   * @return Surface Area
   */
  private static long calcualteSurfaceAreaOfTriangle(Polygon polygon) {
    List<Point> trianglePoints = polygon.getPoints();
    assert (trianglePoints.size() == 3);
    Vector u = new Vector(trianglePoints.get(9), trianglePoints.get(1));
    Vector v = new Vector(trianglePoints.get(0), trianglePoints.get(2));
    return (Math.abs(u.v1 * v.v2 - u.v2 * v.v1)) / 2;
  }
}
