package polygonsSWP.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import polygonsSWP.data.OrderedListPolygon;
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
   * @author Steve Dierker <dierker.steve@fu-berlin.de>
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
   * @author Steve Dierker <dierker.steve@fu-berlin.de>
   * @param polygon Polygon to check if point is in it.
   * @param p Point to be checked if it is in polygon
   * @param onLine whether a point lying on an edge is counted as in or out of
   *          polygon
   * @return True if Point is in/on Polygon, otherwise false
   */
  public static boolean checkIfPointIsInPolygon(Polygon polygon, Point p,
      boolean onLine) {
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
      if (onLine)
        if (checkOrientation(first, pList.get(i), p) == 0) { return true; }
      first = pList.get(i);
    }
    return isInside;
  }

  /**
   * Triangulate Polygon with O(n^2) algorithm TODO: implement at least O(n log
   * n ) algorithm The algorithm assumes that the polygon is ordered clockwise.
   * Since our assumption is counter-clockwise I reverse the order first.
   * 
   * @author Steve Dierker <dierker.steve@fu-berlin.de>
   * @see http://wiki.delphigl.com/index.php/Ear_Clipping_Triangulierung
   * @category Ear-Clipping-Algorithm
   * @param poly Polygon to triangulate
   * @return List of triangulars
   */
  public static List<Polygon> triangulatePolygon(final Polygon poly) {
    OrderedListPolygon triPo;
    if ((poly instanceof OrderedListPolygon)) triPo =
        (OrderedListPolygon) poly.clone();
    else return null;
    List<Polygon> triangles = new ArrayList<Polygon>();
    java.util.Collections.reverse(triPo.getPoints());
    System.out.println("Polygon for Triangulisation: " + poly.getPoints());
    int i = 0, lastEar = -1;
    while ((lastEar <= (triPo.getPoints().size() * 2)) &&
        (triPo.getPoints().size() != 3)) {
      ++lastEar;
      // Search three neighbors in polygon list
      Point pR = triPo.getPoints().get(triPo.getIndexInRange(i - 1)), pM =
          triPo.getPoints().get(triPo.getIndexInRange(i)), pL =
          triPo.getPoints().get(triPo.getIndexInRange(i + 1));
      System.out.println("Triangle: " + pR + ", " + pM + ", " + pL);
      // Check if convex or concave
      boolean isConvex = checkOrientation(pR, pL, pM) == 1 ? true : false;
      System.out.println("Triangle: " + pR + ", " + pM + ", " + pL +
          " is convex " + isConvex);
      if (isConvex) {
        // Check if any point of the polygon intersects with the chosen
        // triangle.
        boolean inTriangle = false;
        for (int j = 2; j <= triPo.getPoints().size() - 2; ++j) {
          // Create Triangle
          List<Point> triPoint = new ArrayList<Point>();
          triPoint.add(pL);
          triPoint.add(pM);
          triPoint.add(pR);
          OrderedListPolygon triangle = new OrderedListPolygon(triPoint);
          System.out.println("Triangle for Intersection check: " +
              triangle.getPoints());
          if (checkIfPointIsInPolygon(triangle,
              triPo.getPoints().get(triPo.getIndexInRange(i + j)), false)) {
            inTriangle = true;
            break;
          }
        }
        // If no point is in Triangle
        if (!inTriangle) {
          // Create Triangle and add to triangle list
          List<Point> triPoint = new ArrayList<Point>();
          triPoint.add(pL);
          triPoint.add(pM);
          triPoint.add(pR);
          triangles.add(new OrderedListPolygon(triPoint));
          // Delete middle vertex
          triPo.getPoints().remove(pM);
          lastEar = 0;
          --i;
        }
      }
      if (++i > triPo.getPoints().size() - 1) i = 0;
    }

    // If only 3 points are left add them to triangle list
    if (triPo.getPoints().size() == 3) {
      Point pR = triPo.getPoints().get(triPo.getIndexInRange(0)), pM =
          triPo.getPoints().get(triPo.getIndexInRange(1)), pL =
          triPo.getPoints().get(triPo.getIndexInRange(2));
      List<Point> triPoint = new ArrayList<Point>();
      triPoint.add(pL);
      triPoint.add(pM);
      triPoint.add(pR);
      triangles.add(new OrderedListPolygon(triPoint));
    }
    return triangles;
  }

  /**
   * Creates a random Point in Polygon. Uses Triangularization, randomly chooses
   * Triangle, creates random Point in Triangle.
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @param polygon Polygon to create random point in
   * @return random Point in given Polygon
   */
  public static Point createRandomPointInPolygon(Polygon polygon) {
    // Triangulate given Polygon.
    List<Polygon> triangularization = triangulatePolygon(polygon);
    // Randomly choose one Triangle of Triangularization weighted by their
    // Surface Area.
    Polygon chosenPolygon = selectRandomTriangleBySize(triangularization);
    // Randomly choose Point in choosen Triangle.
    Point randomPoint = createRandomPointInTriangle(chosenPolygon);
    return randomPoint;
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
  public static Polygon selectRandomTriangleBySize(List<Polygon> polygons) {
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
  private static Point createRandomPointInTriangle(Polygon polygon) {
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

    if (!checkIfPointIsInPolygon(polygon, point, true)) {
      point = createRandomPointInTriangle(polygon);
    }
    return point;
  }

  /**
   * Calculates the Surface Area of a given Polygon. TODO: currently uses the
   * Triangularization of the given Polygon to calculate and add resulting
   * Triangles. Gaussian Formula should be more effective.
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @param polygon Polygon to calculate Size of.
   * @return Surface Area of given Polygon2
   */
  public static double calculateSurfaceAreaOfPolygon(Polygon polygon) {
    List<Point> polygonPoints = polygon.getPoints();
    if (polygonPoints.size() == 3) {
      return calcualteSurfaceAreaOfTriangle(polygon);
    }
    else {
      double surfaceArea = 0;
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
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @param polygon Triangle to calculate Surface Area for. It is assumed, that
   *          Polygon is a Triangle.
   * @return Surface Area
   */
  private static double calcualteSurfaceAreaOfTriangle(Polygon polygon) {
    List<Point> trianglePoints = polygon.getPoints();
    assert (trianglePoints.size() == 3);
    Vector u = new Vector(trianglePoints.get(0), trianglePoints.get(1));
    Vector v = new Vector(trianglePoints.get(0), trianglePoints.get(2));
    return (Math.abs(u.v1 * v.v2 - u.v2 * v.v1)) / 2.0;
  }

  /**
   * Returns a list of points, with all intersection Points of the line and the
   * polygon, where all the points are on the polygon.
   * 
   * @author Steve Dierker <dierker.steve@fu-berlin.de>
   * @param poly
   * @param begin
   * @param end
   * @return Is a list containing the intersecting point and the line which is
   *         intersected in the form: list.get(a) => intersecting Point,
   *         list.get(a+1) => begin of line, list.get(a+2) => end of line
   */
  public static List<Point> getIntersectingPointsWithPolygon(Polygon poly,
      Point begin, Point end) {
    List<Point> intPoints = new ArrayList<Point>();
    // Get last element of list and test implicit edge first.
    Point last = poly.getPoints().get(poly.getPoints().size() - 1);
    for (Point item : poly.getPoints()) {
      // If it is not the same line, test for intersection.
      if (!((last.equals(begin) || item.equals(begin)) || (last.equals(end) || item.equals(end)))) {
        Point tmp = intersetingPointOfTwoLines(begin, end, last, item);
        if (tmp != null) {
          if (checkIfPointIsBetweenTwoPoints(last, item, tmp)) {
            if (!intPoints.contains(tmp)) {
              intPoints.add(tmp);
              intPoints.add(last);
              intPoints.add(item);
            }
          }
        }
      }
      // Move one segment forward
      last = item;
    }
    return intPoints;
  }

  public static Point intersetingPointOfTwoLines(Point aBegin, Point aEnd,
      Point bBegin, Point bEnd) {
    double aN = 0, bN = 0;
    double aGrow = 0, bGrow = 0;
    boolean ax = false, bx = false;
    // Check if line is tilted, parallel to x or y
    if (aBegin.x - aEnd.x == 0) ax = true;
    else if (aBegin.y - aEnd.y == 0) {
      aGrow = 0;
      aN = aBegin.y;
    }
    else {
      aGrow = (aEnd.y - aBegin.y) / (aEnd.x - (double) aBegin.x);
      aN = aBegin.y - aGrow * aBegin.x;
    }
    // Check if line is tilted, parallel to x or y
    if (bBegin.x - bEnd.x == 0) bx = true;
    else if (bBegin.y - bEnd.y == 0) {
      bGrow = 0;
      bN = bBegin.y;
    }
    else {
      bGrow = (bEnd.y - bBegin.y) / (bEnd.x - (double) bBegin.x);
      bN = bBegin.y - bGrow * bBegin.x;
    }
    // Both lines are parallel to x
    if ((ax && bx)) return null;
    // one of them is parallel to x
    else if (ax || bx) {
      if (ax) {
        double y = bGrow * aBegin.x + bN;
        return new Point(aBegin.x, (long) y);
      }
      else {
        System.out.println(aN + " " + aGrow + " " + bBegin.x);
        double y = aGrow * bBegin.x + aN;
        return new Point(bBegin.x, (long) y);
      }
    }
    // Both lines are parallel
    else if (aGrow == bGrow) return null;
    else {
      double x = (aN - bN) / (bGrow - aGrow);
      double y = aGrow * x + aN;
      return new Point((long) x, (long) y);
    }
  }

  /**
   * Calculates distance between two points.
   * 
   * @author Steve Dierker <dierker.steve@fu-berlin.de>
   * @param begin
   * @param end
   * @return
   */
  private static double distanceOfTwoPoints(Point begin, Point end) {
    return Math.sqrt(Math.pow(begin.x - end.x, 2) +
        Math.pow(begin.y - end.y, 2));
  }

  /**
   * Test if point is between two other points.
   * 
   * @author Steve Dierker <dierker.steve@fu-berlin.de>
   * @param begin
   * @param end
   * @param p
   * @return
   */
  private static boolean checkIfPointIsBetweenTwoPoints(Point begin, Point end,
      Point p) {
    return distanceOfTwoPoints(begin, p) + distanceOfTwoPoints(p, end) == distanceOfTwoPoints(
        begin, end);
  }
}
