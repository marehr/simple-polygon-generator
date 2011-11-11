package polygonsSWP.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import polygonsSWP.data.Point;
import polygonsSWP.data.Polygon;


/**
 * Helper class for reusable math functions.
 * 
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 */
public class MathUtils
{

  /**
   * Creates a random permutation of list p.
   */
  public static List<Point> permute(List<Point> p) {
    Random r = new Random(System.currentTimeMillis());
    List<Point> s = new ArrayList<Point>(p);
    List<Point> ret = new ArrayList<Point>();

    while (!s.isEmpty()) {
      ret.add(s.remove(r.nextInt(s.size())));
    }

    return ret;
  }

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
   * Tests if p is inside the given Polygon Uses Jordans Point in Polygon Test
   * <http://de.wikipedia.org/wiki/Punkt-in-Polygon-Test_nach_Jordan>
   * 
   * @param polygon Polygon to check if point is in it.
   * @param p Point to be checked if it is in polygon
   * @return 1 if P is in Polygon, -1 if P is not in Polygon, 0 if P is on
   *         Polygon
   */
  public static int checkIfPointIsInPolygon(Polygon polygon, Point p) {
    List<Point> pList = polygon.getPoints();
    int t = -1;
    // Get last point of list.
    Point first = pList.get(pList.size() - 1);
    for (int i = 0; i < pList.size() - 1; ++i) {
      t = t * crossProduktTest(p, first, pList.get(i));
      first = pList.get(i);
    }
    return t;
  }

  /**
   * Tests whether the ray from P crosses the line formed by Poly1 and Poly 2.
   * 
   * @param p Point to check rays from
   * @param poly1 Begining point of line
   * @param poly2 Ending point of line
   * @return -1 if ray crosses Poly1 Poly2, 0 if A on Poly1 Poly2, otherwise -1
   */
  private static int crossProduktTest(Point p, Point poly1, Point poly2) {
    if (p.y == poly1.y && p.y == poly2.y) {
      if ((poly1.x <= p.x && p.x <= poly2.x) ||
          (poly2.x <= p.x && p.x <= poly1.x)) {
        return 0;
      }
      else return 1;
    }
    if (poly1.y > poly2.y) {
      long tmp = poly1.y;
      poly1.y = poly2.y;
      poly2.y = tmp;
    }
    if (p.y <= poly1.y || p.y > poly2.y) return 1;
    long delta =
        (poly1.x - p.x) * (poly2.y - p.y) - (poly1.y - p.y) * (poly2.x - p.x);
    if (delta > 0) return 1;
    else if (delta < 0) return -1;
    else return 0;
  }
}
