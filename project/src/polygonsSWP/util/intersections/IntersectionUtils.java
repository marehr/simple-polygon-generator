package polygonsSWP.util.intersections;

import polygonsSWP.geometry.Point;
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
   *         array of length 0, if they are coincident (== many intersections)
   *         array of length 1 where first element contains intersection otherwise.
   */
  public static Point[] intersect(Point a1, Point a2, Point b1, Point b2, IntersectionMode ima, IntersectionMode imb) {
    double mua, mub;
    long denom, numera, numerb;
   
    denom =
        (b2.y - b1.y) * (a2.x - a1.x) - (b2.x - b1.x) *
            (a2.y - a1.y);
    numera =
        (b2.x - b1.x) * (a1.y - b1.y) - (b2.y - b1.y) *
            (a1.x - b1.x);
    numerb =
        (a2.x - a1.x) * (a1.y - b1.y) -
            (a2.y - a1.y) * (a1.x - b1.x);

    /* Are the lines coincident? */
    if (numera == 0 && numerb == 0 && denom == 0)
      return new Point[]{};

    /* Are the lines parallel? */
    if (denom == 0) 
      return null;

    /* Is the intersection along the segments/rays/lines? */
    mua = (double) numera / (double) denom;
    mub = (double) numerb / (double) denom;
    
    if(ima.test(mua) && imb.test(mub)) {
      long isx = Math.round(a1.x + mua * (a2.x - a1.x));
      long isy = Math.round(a1.y + mua * (a2.y - a1.y));
      Point[] intersection = { new Point(isx, isy) };
      return intersection;
    } else
      return null;
  }
}
