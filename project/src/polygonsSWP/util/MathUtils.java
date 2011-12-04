package polygonsSWP.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;


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
  public static List<Point[]> getIntersectionsPolygonLine(Polygon poly,
      Point begin, Point end) {
    List<Point[]> intPoints = new ArrayList<Point[]>();
    // Get last element of list and test implicit edge first.
    Point last = poly.getPoints().get(poly.getPoints().size() - 1);
    for (Point item : poly.getPoints()) {
      // If it is not the same line, test for intersection.
      if (!((last.equals(begin) || item.equals(begin)) || (last.equals(end) || item.equals(end)))) {
        Point tmp = getIntersectionLineLine(begin, end, last, item);
        if (tmp != null) {
          if (checkIfPointOnLineSegment(last, item, tmp)) {

            if (!intPoints.contains(tmp)) {
              Point[] t = { tmp, last, item };
              intPoints.add(t);
            }
          }
        }
      }
      // Move one segment forward
      last = item;
    }
    return intPoints;
  }

  public static Point getIntersectionLineLine(Point aBegin, Point aEnd,
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
}
