package polygonsSWP.util;

import polygonsSWP.geometry.LineSegment;
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
public class SweepLineResult
{
  public Point rightIntersect = null;
  public Point leftIntersect = null;
  public LineSegment rightEdge = null;
  public LineSegment leftEdge = null;

  @Override
  public String toString() {
    String res = "";
    if (rightEdge != null) res += "RightEdge: " + rightEdge;
    if (rightIntersect != null) res += "\nRightIntersect: " + rightIntersect;
    if (leftEdge != null) res += "\nLeftEdge: " + leftEdge;
    if (leftIntersect != null) res += "\nLeftIntersect: " + leftIntersect;
    return res;
  }
}
