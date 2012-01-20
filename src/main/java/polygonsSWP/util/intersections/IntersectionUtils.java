package polygonsSWP.util.intersections;

import polygonsSWP.geometry.Point;
import polygonsSWP.util.MathUtils;
import polygonsSWP.util.intersections.IntersectionMode;

public class IntersectionUtils
{
  /**
   * Generic intersection test.
   * 
   * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * 
   * @return null, if lines/line segments/rays do not intersect,
   *         array of length 0, if they are colinear
   *         array of length 1 where first element contains intersection otherwise.
   */
  public static Point[] intersect(Point a1, Point a2, Point b1, Point b2, IntersectionMode ima, IntersectionMode imb) {
    double mua, mub;
    double denom, numera, numerb;
   
    denom =
        (b2.y - b1.y) * (a2.x - a1.x) - (b2.x - b1.x) *
            (a2.y - a1.y);
    numera =
        (b2.x - b1.x) * (a1.y - b1.y) - (b2.y - b1.y) *
            (a1.x - b1.x);
    numerb =
        (a2.x - a1.x) * (a1.y - b1.y) -
            (a2.y - a1.y) * (a1.x - b1.x);

    /* Are the lines colinear? */
    if (MathUtils.doubleZero(numera) && 
        MathUtils.doubleZero(numerb) &&
        MathUtils.doubleZero(denom))
      return new Point[]{};

    /* Are the lines parallel? */
    if (MathUtils.doubleZero(denom)) 
      return null;

    /* Is the intersection along the segments/rays/lines? */
    mua = numera / denom;
    mub = numerb / denom;
    
    if(ima.test(mua) && imb.test(mub)) {
      double isx = a1.x + mua * (a2.x - a1.x);
      double isy = a1.y + mua * (a2.y - a1.y);
      Point[] intersection = { new Point(isx, isy) };
      return intersection;
    } else
      return null;
  }
}
