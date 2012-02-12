package polygonsSWP.util;

import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.Point;


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
