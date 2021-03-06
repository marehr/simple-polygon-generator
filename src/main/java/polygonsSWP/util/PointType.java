package polygonsSWP.util;

import java.util.Comparator;

import polygonsSWP.geometry.Point;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public class PointType
{
  public enum PointClass {
    INT, MAX, MIN, HMAX, HMIN, IGNORE
  }


  public enum Direction {
    RIGHT, LEFT, BOTH, NONE
  }

  public PointType(Point p, Point left, Point right, PointType.PointClass type,
      PointType.Direction direct) {

    this.type = type;
    this.p = p;
    this.left = left;
    this.right = right;
    this.direct = direct;
  }

  public Point p;
  public Point left; // index(p) - 1
  public Point right; // index(p) + 1
  public PointType.PointClass type;
  public PointType.Direction direct;


  public static class PointComparator
    implements Comparator<PointType>
  {

    @Override
    public int compare(PointType pt1, PointType pt2) {

      Point p1 = pt1.p;
      Point p2 = pt2.p;
      // TODO: satisfy marcel
      if (p1.y < p2.y + MathUtils.EPSILON) return 1;
      else if (p1.y > p2.y - MathUtils.EPSILON) return -1;
      else {
        if (p1.x < p2.x - MathUtils.EPSILON) return 1;
        else if (p1.x > p2.x + MathUtils.EPSILON) return -1;
        else return 0;
      }
    }

  }

}