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

  public static int modulo(int i, int size) {
    i = i % size;
    return i < 0 ? size + i : i;
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
    if (result > 0 + EPSILON) return 1;
    else if (result < 0 - EPSILON) return -1;
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
  
  /**
   * Decimal number comparison.
   */
  public static int doubleCompare(double a, double b){
    if(a < b - EPSILON)
      return -1;
    else if(a > b + EPSILON)
      return 1;
    else
      return 0;
  }
}
