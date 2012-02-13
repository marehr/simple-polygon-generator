package polygonsSWP.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import polygonsSWP.util.MathUtils;


public class SteadyGrowthConvexHull extends Polygon
{

  private ArrayList<Point> upperHull = new ArrayList<Point>(),
      lowerHull = new ArrayList<Point>(), points = null;

  @Override
  @SuppressWarnings("unchecked")
  public Polygon clone() {
    SteadyGrowthConvexHull hull = new SteadyGrowthConvexHull();
    hull.lowerHull = (ArrayList<Point>) this.lowerHull.clone();
    hull.upperHull = (ArrayList<Point>) this.upperHull.clone();
    return hull;
  }

  public void addPoint(Point point) {
    // wenn der punkt innerhalb convexen huelle ist,
    // dann brauchen wir nichts aktualisieren
    if (containsPoint(point, true)) return;
    points = null;

    addPoint(point, lowerHull, Point.XCompare);
    addPoint(point, upperHull, Point.XCompareReverse);
  }

  private boolean addPoint(Point point, ArrayList<Point> hull,
      Comparator<Point> cmp) {
    int pos = Collections.binarySearch(hull, point, cmp);

    // wenn der punkt in der collection, dann haben wir nichts zutun
    if (pos >= 0) return false;

    pos = -pos - 1;

    // System.out.println("addPoint" + point + " at pos " + pos + ": " + hull);
    hull.add(Math.max(pos, 0), point);
    // System.out.println("addPoint" + point + ": " + hull);

    if (hull.size() >= 2) reconstructHull(hull, cmp);
    return true;
  }

  private void reconstructHull(ArrayList<Point> hull, Comparator<Point> cmp) {
    Point pi, sk, sl;
    int k = 1;

    while (k < hull.size() - 1) {
      // System.out.println("k: " + k + "; size: " + hull.size());
      pi = hull.get(k + 1);

      while (k >= 1) {
        sk = hull.get(k);
        sl = hull.get(k - 1);

        if (MathUtils.checkOrientation(sl, sk, pi) > 0) break;

        hull.remove(k);
        k -= 1;
      }

      k += 1;
    }
  }

  @Override
  public boolean containsPoint(Point point, boolean onLine) {
    if (size() <= 2) return false;

    return containsPoint(point, upperHull, onLine) &&
           containsPoint(point, lowerHull, onLine);
  }

  private boolean containsPoint(Point point, ArrayList<Point> hull, boolean onLine) {
    Point a, b;

    for (int i = 1; i < hull.size(); ++i) {
      a = hull.get(i - 1);
      b = hull.get(i);

      // NOTE: hier muessen wir nicht auf LineSegment.containsPoint
      // testen, da selbst wenn ein Punkt auf der Geraden a-b
      // und ausserhalb der Huelle liegt, es bei einer anderen
      // Kante der Huelle festgestellt wird, ob der Punkt wirklich
      // innerhalb lag. Da eine ConvexeHuelle convex ist :D
      int orients = MathUtils.checkOrientation(a, b, point);

      // wir liegen auf einer Kante des Polygons und wir sagen,
      // dass bei onLine = false der Punkt ausserhalb des Polygons liegt
      if (!onLine && orients == 0) return false;

      // bei Clockwise muessen alle Punkte RECHTS liegen, damit sie
      // innerhalb des Polygons sind.

      // Der Punkt ist aber auf der LINKen seite, also sicher ausserhalb des
      // Polygons
      if (orients < 0) return false;
    }
    return true;
  }

  @Override
  public List<Point> getPoints() {
    if (points != null) return points;

    points = new ArrayList<Point>(lowerHull);

    if (points.size() < 2) return points;

    points.remove(points.size() - 1);
    points.addAll(upperHull);
    points.remove(points.size() - 1);

    return points;
  }

  @Override
  public int size() {
    if(points != null) return points.size();
    return Math.max(upperHull.size(), upperHull.size() + lowerHull.size() - 2);
  }

  @Override
  public boolean equals(Object obj) {
    throw new UnsupportedOperationException();
  }

  @Override
  public double getSurfaceArea() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Point createRandomPoint() {
    throw new UnsupportedOperationException();
  }
}
