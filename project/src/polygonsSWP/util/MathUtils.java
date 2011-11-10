package polygonsSWP.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import polygonsSWP.data.Point;

/**
 * Helper class for reusable math functions.
 * 
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 */
public class MathUtils
{
  /**
   * Randomly creates a set of n points in a square defined by edge length s,
   * where each point holds 0 <= x < s && 0 <= y < s and no point equals
   * another point, so for all xi, xj in points: xi != xj.
   * 
   * @param n number of points
   * @param s length of edges of square
   * @return array of randomly distributed Points out of s^2,
   *         length of array == n.
   */
  public static List<Point> createRandomSetOfPointsInSquare(int n, int s) {
    List<Point> retval = new ArrayList<Point>();
    Random r = new Random(System.currentTimeMillis());
 
    for (int i = 0; i < n; i++) {
      Point p = null;
      
      boolean notInSet = false;
      while(!notInSet) {
        p = new Point(r.nextInt(s), r.nextInt(s));
        
        notInSet = true;
        for(int j = 0; j < retval.size(); j++) {
          if(retval.get(j).equals(p)) {
            notInSet = false;
            break;
          }
        }
      }
        
      retval.add(p);
    }

    return retval;
  }
}
