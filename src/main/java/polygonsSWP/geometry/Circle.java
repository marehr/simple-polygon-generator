package polygonsSWP.geometry;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import polygonsSWP.util.MathUtils;  


public class Circle
  extends Polygon
{
  private double _radius;
  private Point _center;

  public Circle(double radius, Point center) {
    _radius = radius;
    _center = center;
  }

  @Override
  public List<Point> getPoints() {
    List<Point> tmpLst = new LinkedList<Point>();
    tmpLst.add(_center);
    return tmpLst;
  }

  @Override
  public Polygon clone() {
    return new Circle(_radius, _center.clone());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Circle) {
      if (((Circle) obj).centerOfMass().equals(_center) &&
          ((Circle) obj).getRadius() == _radius) return true;
    }
    return false;
  }

  @Override
  public int size() {
    // Only center point
    return 1;
  }

  @Override
  public double getSurfaceArea() {
    // TODO Auto-generated method stub
    return Math.PI * (Math.pow(_radius, 2));
  }

  @Override
  public List<Point[]> intersect(LineSegment ls) {
    List<Point[]> returnList;
    returnList = this.intersect(ls.extendToLine());
    if (returnList.size() == 0) return returnList;
    else {
      List<Point[]> tmpList = new LinkedList<Point[]>();
      for (Point[] array : returnList) {
        if (ls.containsPoint(array[0])) tmpList.add(array);
      }
      return tmpList;
    }
  }

  @Override
  public List<Point[]> intersect(Ray r) {
    List<Point[]> returnList;
    returnList = this.intersect(r.extendToLine());
    if (returnList.size() == 0) return returnList;
    else {
      List<Point[]> tmpList = new LinkedList<Point[]>();
      for (Point[] array : returnList) {
        if (r.containsPoint(array[0])) tmpList.add(array);
      }
      return tmpList;
    }
  }

  /**
   * @author Steve Dierker <dierker.steve@fu-berlin.de>
   * @see http://mathworld.wolfram.com/Circle-LineIntersection.html
   */
  @Override
  public List<Point[]> intersect(Line l) {
    double dx = l._b.x - l._a.x;
    double dy = l._b.y - l._a.y;
    double dr = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    double D = l._a.x * l._b.y - l._b.x * l._a.y;

    double delta = Math.pow(_radius, 2) * Math.pow(dr, 2) - Math.pow(D, 2);
    if (delta < 0 - MathUtils.EPSILON) return new LinkedList<Point[]>();
    if (delta > 0 + MathUtils.EPSILON) {
      int sgn = (dy < 0 - MathUtils.EPSILON) ? -1 : 0;
      double x1 =
          (D * dy + sgn *
              dx *
              Math.sqrt(Math.pow(_radius, 2) * Math.pow(dr, 2) - Math.pow(D, 2))) /
              Math.pow(dr, 2);
      double x2 =
          (D * dy - sgn *
              dx *
              Math.sqrt(Math.pow(_radius, 2) * Math.pow(dr, 2) - Math.pow(D, 2))) /
              Math.pow(dr, 2);

      double y1 =
          (-D * dx + Math.abs(dy) *
              Math.sqrt(Math.pow(_radius, 2) * Math.pow(dr, 2) - Math.pow(D, 2))) /
              Math.pow(dr, 2);
      double y2 =
          (-D * dx - Math.abs(dy) *
              Math.sqrt(Math.pow(_radius, 2) * Math.pow(dr, 2) - Math.pow(D, 2))) /
              Math.pow(dr, 2);
      List<Point[]> tmpList = new LinkedList<Point[]>();
      Point[] tmpArray = new Point[3];
      tmpArray[0] = new Point(x1, y1);
      tmpArray[1] = l._a;
      tmpArray[2] = l._b;
      tmpList.add(tmpArray);
      tmpArray[0] = new Point(x2, y2);
      tmpArray[1] = l._a;
      tmpArray[2] = l._b;
      tmpList.add(tmpArray);
      return tmpList;
    }
    else {
      List<Point[]> tmpList = new LinkedList<Point[]>();
      Point[] tmpArray = new Point[3];
      tmpArray[0] =
          new Point((D * dy) / Math.pow(dr, 2), (-D * dx) / Math.pow(dr, 2));
      tmpArray[1] = l._a;
      tmpArray[2] = l._b;
      tmpList.add(tmpArray);
      return tmpList;
    }
  }

  @Override
  public Point createRandomPoint() {
    Random gen = new Random();
    // Get two random values between -radius and +radius
    double first = gen.nextDouble() * 2 * _radius;
    double second = gen.nextDouble() * 2 * _radius;
    // Just add both values to x and y of center and create a new point
    return new Point(_center.x + (first - _radius), _center.y +
        (second - _radius));
  }

  private double distanceToCenter(Point p) {
    return Math.sqrt(Math.pow(p.x - _center.x, 2) +
        Math.pow(p.y - _center.y, 2));
  }

  @Override
  public boolean containsPoint(Point p, boolean online) {
    if (MathUtils.doubleEquals(distanceToCenter(p), _radius) & online)
      return true;
    if (distanceToCenter(p) < _radius) return true;
    return false;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Circle: r: " + _radius);
    sb.append(" center: x: " + _center.x + "y: " + _center.y + "\n");
    return sb.toString();
  }

  @Override
  public String toSVG() {
    StringBuilder sb = new StringBuilder();

    sb.append("<?xml version=\"1.0\"?>\n");
    sb.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");
    sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n");
    sb.append("<circle cx=\"");
    sb.append(_center.x);
    sb.append("\" cy=\"");
    sb.append(_center.y);
    sb.append("\" r=\"");
    sb.append(_radius);
    sb.append("\" style=\"fill:lime;stroke:purple;stroke-width:1\" />\n");

    sb.append("</svg>\n");
    return sb.toString();
  }

  @Override
  public Point centerOfMass() {
    return _center;
  }

  public double getRadius() {
    return _radius;
  }

}
