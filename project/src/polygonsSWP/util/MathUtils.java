package polygonsSWP.util;

import java.util.ArrayList;
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

    int i = 0, lastEar = -1;
    do {
      ++lastEar;
      // Search three neighbors in polygon list
      Point pR = triPo.getPoints().get(triPo.getIndexInRange(i - 1)), pM =
          triPo.getPoints().get(triPo.getIndexInRange(i)), pL =
          triPo.getPoints().get(triPo.getIndexInRange(i + 1));
      // Check if convex or concave
      boolean isConvex = checkOrientation(pR, pL, pM) == 1 ? true : false;
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
    while ((lastEar <= (triPo.getPoints().size() * 2)) &&
        (triPo.getPoints().size() != 3));

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
   * triangle, creates random Point in triangle.
   * 
   * @param polygon Polygon to create random point in
   * @return random Point in given Polygon
   */
  public static Point createRandomPointInPolygon(Polygon polygon) {
    // TODO: use Triangularization, randomly choose triangle, use
    // createRandomPointInTriangle
    Point point = new Point(0, 0);
    return point;
  }

  /**
   * Creates a random Point in given Triangle. Mirror Triangle to create
   * Parallelogram. Chooses random Point in Parallelogram, then checks if Point
   * is in original Triangle. Chooses new Point, if that is not the case, until
   * true. TODO: Invert created Point if not in original Triangle instead of
   * simply rejecting it. Used for createRandomPointInPolygon.
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

    if (!checkIfPointIsInPolygon(polygon, point, true)) {
      point = createRandomPointInTriangle(polygon);
    }
    return point;
  }
}
