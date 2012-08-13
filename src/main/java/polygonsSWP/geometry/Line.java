package polygonsSWP.geometry;

import polygonsSWP.util.MathUtils;
import polygonsSWP.util.intersections.IntersectionUtils;
import polygonsSWP.util.intersections.LineIntersectionMode;
import polygonsSWP.util.intersections.LineSegmentIntersectionMode;
import polygonsSWP.util.intersections.RayIntersectionMode;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public class Line
{
  public Point _a;
  public Point _b;

  public Line(Point a, Point b) {
    _a = a;
    _b = b;
  }

  /**
   * Check if given Point is in this line.
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
   * @param p
   * @return
   */
  public boolean containsPoint(Point p) {
    Vector ap = new Vector(_a, p);
    Vector ab = new Vector(_a, _b);

    // catch horizontal lines
    if(MathUtils.doubleZero(ab.y)) return MathUtils.doubleZero(ap.y);

    // catch vertical lines
    if(MathUtils.doubleZero(ab.x)) return MathUtils.doubleZero(ap.x);

    double lambda1 = (ap.x / ab.x);
    double lambda2 = (ap.y / ab.y);
    return MathUtils.doubleEquals(lambda1, lambda2);
  }

  /**
   * Calculates the intersection of this line with a line segment.
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @param ls
   * @return null if lines and line segment do not intersect, array of length 0 if
   *         both a collinear, array containing intersection Point else.
   */
  public Point[] intersect(LineSegment ls) {
    return IntersectionUtils.intersect(_a, _b, ls._a, ls._b, 
        new LineIntersectionMode(), new LineSegmentIntersectionMode(true));
  }

  /**
   * Calculates the intersection of this line with a ray.
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @param r
   * @return null if lines and ray do not intersect, array of length 0 if
   *         both a collinear, array containing intersection Point else.
   */
  public Point[] intersect(Ray r) {
    return IntersectionUtils.intersect(_a, _b, r._base, r._support, 
        new LineIntersectionMode(), new RayIntersectionMode(true));
  }

  /**
   * Calculates the intersection of two lines.
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @param l2
   * @return null if lines parallel, array of length 0 if both lines coincident,
   *         array containing intersection Point else.
   */
  public Point[] intersect(Line l2) {
    return IntersectionUtils.intersect(_a, _b, l2._a, l2._b, 
        new LineIntersectionMode(), new LineIntersectionMode());
  }
  
  public Line clone() {
    return new Line(_a.clone(), _b.clone());
  }
  
  
  /**
   * Calculates cutting angle of to lines.
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
   * @param l
   * @return angle Signed cutting angle in degree, orientated from this line.
   *         -90 <= angle <= 90
   */
  public double cuttingAngle(Line l){
    double denom1 = this._b.x - this._a.x;
    double denom2 = l._b.x - l._a.x;
    double num1 = this._b.y - this._a.y;
    double num2 = l._b.y - l._a.y;
    
    if (MathUtils.doubleZero(denom1) && MathUtils.doubleZero(denom2))
      return 0.0;
    if (MathUtils.doubleZero(denom1)|| MathUtils.doubleZero(denom2)) {
      if(MathUtils.doubleZero(num2) || MathUtils.doubleZero(num1)){
        return 90.0;
      }
      else{
        Line l1 = new Line(new Point(-this._a.y, this._a.x), new Point(-this._b.y, this._b.x));
        Line l2 = new Line(new Point(-l._a.y, l._a.x), new Point(-l._b.y, l._b.x));
        return l1.cuttingAngle(l2);
      }
    }
    
    double m1 = num1 / denom1;
    double m2 = num2 / denom2;
    
    double denom3 = (1 + m1*m2);
    
    if (MathUtils.doubleZero(denom3))
      return 90.0;
    
    return Math.atan((m2 - m1) / denom3) * 180 / Math.PI;
  }
}
