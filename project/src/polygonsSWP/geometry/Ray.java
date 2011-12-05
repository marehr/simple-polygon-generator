package polygonsSWP.geometry;

import java.util.List;


public class Ray
{
  public Point _base;
  public Point _support;

  public Ray(Point initial, Point support) {
    _base = initial;
    _support = support;
  }

  /**
   * Check if given Point is in this ray.
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @param p
   * @return
   */
  public boolean containsPoint(Point p) {
    Vector oa = new Vector(new Point(0, 0), _base);
    Vector ab = new Vector(_base, _support);
    long lambda1 = ((p.x - oa.v1) / ab.v1);
    long lambda2 = ((p.y - oa.v2) / ab.v2);
    if (lambda1 == lambda2 && lambda1 >= 0) {
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Get intersection between this ray and a lineSegment if existing.
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @param l LineSegment to intersect with.
   * @return
   */
  public Point[] intersect(LineSegment ls) {
    Line line = this.extendToLine();
    Point[] intersection = line.intersect(ls.extendToLine());
    if (intersection == null || intersection.length == 0) { return intersection; }
    if (!(this.containsPoint(intersection[0]) && ls.containsPoint(intersection[0]))) { return null; }
    return intersection;
  }

  public Point[] intersect(Ray r) {
    Line line = this.extendToLine();
    Point[] intersection = line.intersect(r.extendToLine());
    if (intersection == null || intersection.length == 0) { return intersection; }
    if (!(this.containsPoint(intersection[0]) && r.containsPoint(intersection[0]))) { return null; }
    return intersection;
  }

  public Line extendToLine() {
    return new Line(_base, _support);
  }

  /**
   * Get Point on Ray that is closest to its initial base point.
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @param intersections
   * @return Point[] triple of Points.
   */
  public Point[] getPointClosestToBase(List<Point[]> intersections) {
    Point[] closest = null;
    double distance = Double.MAX_VALUE;
    if (intersections != null) {
      for (Point[] points : intersections) {
        if (points[0] != null) {
          double newDistance = points[0].distanceTo(_base);
          if (newDistance < distance) {
            closest = points;
            distance = newDistance;
          }
        }
      }
    }
    return closest;
  }
}
