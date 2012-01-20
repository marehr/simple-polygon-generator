package polygonsSWP.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import polygonsSWP.geometry.Point;


public class SteadyGrowthConvexHull
  implements Cloneable
{

  private ArrayList<Point> upperHull = new ArrayList<Point>(),
      lowerHull = new ArrayList<Point>(), points = null;

  private Comparator<Point> pointCmp = new Comparator<Point>() {

    @Override
    public int compare(Point o1, Point o2) {
      return o1.compareTo(o2);
    }
  };

  private Comparator<Point> pointCmpReverse = new Comparator<Point>() {

    @Override
    public int compare(Point o1, Point o2) {
      return o2.compareTo(o1);
    }
  };

  @Override
  @SuppressWarnings("unchecked")
  public Object clone() {
    SteadyGrowthConvexHull hull = new SteadyGrowthConvexHull();
    hull.lowerHull = (ArrayList<Point>) this.lowerHull.clone();
    hull.upperHull = (ArrayList<Point>) this.upperHull.clone();
    return hull;
  }

  public void addPoint(Point point) {
    // System.out.println("containsPoint" + point + ": " +
    // containsPoint(point));
    // System.out.println("containsPoint" + point + ": lower: " + lowerHull);
    // System.out.println("containsPoint" + point + ": upper: " + upperHull);
    if (containsPoint(point)) return;
    points = null;

    addPoint(point, lowerHull, pointCmp);
    addPoint(point, upperHull, pointCmpReverse);
  }

  private boolean addPoint(Point point, ArrayList<Point> hull,
      Comparator<Point> cmp) {
    int pos = Collections.binarySearch(hull, point, cmp);

    // TODO: check if point is in collection - done?
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

  public boolean containsPoint(Point point) {
    if (size() <= 2) return false;

    return containsPoint(point, upperHull) && containsPoint(point, lowerHull);
  }

  private boolean containsPoint(Point point, ArrayList<Point> hull) {
    Point a, b;

    for (int i = 1; i < hull.size(); ++i) {
      a = hull.get(i - 1);
      b = hull.get(i);

      // ist auf der rechten seite oder auf der linie
      if (MathUtils.checkOrientation(a, b, point) <= 0) return false;
    }
    return true;
  }

  public List<Point> getPoints() {
    if (points != null) return points;

    points = new ArrayList<Point>(lowerHull);

    if (points.size() < 2) return points;

    points.remove(points.size() - 1);
    points.addAll(upperHull);
    points.remove(points.size() - 1);

    return points;
  }

  public int size() {
    return Math.max(upperHull.size(), upperHull.size() + lowerHull.size() - 2);
  }
}
