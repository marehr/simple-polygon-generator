package polygonsSWP.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import polygonsSWP.geometry.Point;

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
        p = new Point(r.nextDouble() * s, r.nextDouble() * s);

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
    double result =
        begin.x * (end.y - p.y) + end.x * (p.y - begin.y) + p.x *
            (begin.y - end.y);
    if (result > 0) return 1;
    else if (result < 0) return -1;
    else return 0;
  }
  
  /**
   * Really small number.
   */
  public static final double EPSILON = 10E-8d;
  
  /**
   * Decimal number equality.
   */
  public static boolean doubleEquals(double a, double b) {
    return Math.abs(a - b) < EPSILON;
  }
  
  /**
   * Decimal number equals zero.
   */
  public static boolean doubleZero(double a) {
    return Math.abs(a) < EPSILON;
  }
}