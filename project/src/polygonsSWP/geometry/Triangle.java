package polygonsSWP.geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import polygonsSWP.util.MathUtils;


/**
 * This class just represents the smallest kind of polygon, a triangle.
 * 
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 */

public class Triangle
  extends Polygon
{
  private List<Point> _coords;

  public Triangle(List<Point> points) {
    _coords = points;
  }

  public Triangle() {
    this(new ArrayList<Point>());
  }

  public Triangle(Point a, Point b, Point c) {
    _coords.add(a);
    _coords.add(b);
    _coords.add(c);
  }

  @Override
  public List<Point> getPoints() {
    return _coords;
  }

  @Override
  public Polygon clone() {
    List<Point> tmpList = new ArrayList<Point>();
    tmpList.addAll(_coords);
    return new Triangle(tmpList);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Triangle) {
      List<Point> tmpList = ((Triangle) obj).getPoints();
      if (tmpList.contains(_coords.get(0))) {
        int index = tmpList.indexOf(_coords.get(0));
        if (tmpList.get((index + 1) % 3).equals(_coords.get(1)) &&
            tmpList.get((index + 2) % 3).equals(_coords.get(2))) return true;
      }
      return false;
    }
    else {
      // TODO: check for other polygon types with 3 points would be fancy!
      return false;
    }
  }

  @Override
  public boolean containsPoint(Point p, boolean onLine) {
    // TODO: check if there is a faster algorithm for triangles.
    List<Point> pList = this.getPoints();
    boolean isInside = false;
    int nPoints = pList.size();
    Point first = pList.get(pList.size() - 1);

    int j = 0;
    for (int i = 0; i < nPoints; i++) {
      j++;
      if (j == nPoints) j = 0;

      if (pList.get(i).y < p.y && pList.get(j).y >= p.y ||
          pList.get(j).y < p.y && pList.get(i).y >= p.y) {
        if (pList.get(i).x + (double) (p.y - pList.get(i).y) /
            (double) (pList.get(j).y - pList.get(i).y) *
            (pList.get(j).x - pList.get(i).x) < p.y) {
          isInside = !isInside;
        }
      }
      if (onLine)
        if (MathUtils.checkOrientation(first, pList.get(i), p) == 0) { return true; }
      first = pList.get(i);
    }
    return isInside;
  }

  @Override
  public double getSurfaceArea() {
    List<Point> trianglePoints = this.getPoints();
    Vector u = new Vector(trianglePoints.get(0), trianglePoints.get(1));
    Vector v = new Vector(trianglePoints.get(0), trianglePoints.get(2));
    return Math.abs(u.v1 * v.v2 - u.v2 * v.v1) / 2.0;
  }

  @Override
  public Point createRandomPoint() {
    Point retval;
    Random random = new Random(System.currentTimeMillis());
    do {
      // Choose random Point in rectangle with length of edges according to
      // length
      // of vector. Then scale Point to actual Point in Parallelogram.
      Vector u = new Vector(_coords.get(0), _coords.get(1));
      Vector v = new Vector(_coords.get(0), _coords.get(2));
      double randomPoint1 = random.nextDouble();
      double randomPoint2 = random.nextDouble();
      double x = u.v1 * randomPoint1 + v.v1 * randomPoint2;
      double y = u.v2 * randomPoint1 + v.v2 * randomPoint2;

      retval = new Point((long) x, (long) y);

    }
    while (!containsPoint(retval, true));
    return retval;
  }

  public MonotonPolygon getMonotonPolygon() {
    List<LineSegment> tmpList = new ArrayList<LineSegment>();
    tmpList.add(new LineSegment(_coords.get(0), _coords.get(1)));
    tmpList.add(new LineSegment(_coords.get(1), _coords.get(2)));
    tmpList.add(new LineSegment(_coords.get(2), _coords.get(0)));
    return new MonotonPolygon(tmpList);
  }

  public OrderedListPolygon getOrderedListPolygon() {
    return new OrderedListPolygon(_coords);
  }
}
