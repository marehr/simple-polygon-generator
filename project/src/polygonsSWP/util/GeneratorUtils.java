package polygonsSWP.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;


public class GeneratorUtils
{
  @SuppressWarnings("unchecked")
  public static List<Point> createOrUsePoints(Map<String, Object> params) {
    Integer n = (Integer) params.get("n");
    Integer size = (Integer) params.get("size");
    List<?> s = (List<?>) params.get("points");

    // TODO remove
    assert (s != null || (n != null && size != null));

    if (s == null) s = MathUtils.createRandomSetOfPointsInSquare(n, size);

    return (List<Point>) s;
  }

  /**
   * Sorts points (in-place) by y-coordinate. All points on the same same
   * y-coordinate will be ordered ascending by the x-coordinate
   * 
   * @param points
   */
  public static void sortPointsByY(List<Point> points) {
    Collections.sort(points, new Comparator<Point>() {

      @Override
      public int compare(Point p1, Point p2) {
        if (p1.y != p2.y) return p1.y < p2.y ? -1 : +1;
        if (p1.x == p2.x) return 0;
        return p1.x < p2.x ? -1 : +1;
      }

    });
  }

  /**
   * Sorts points (in-place) by x-coordinate. All points on the same same
   * x-coordinate will be ordered ascending by the y-coordinate
   * 
   * @param points
   */
  public static void sortPointsByX(List<Point> points) {
    Collections.sort(points, new Comparator<Point>() {

      @Override
      public int compare(Point p1, Point p2) {
        if (p1.x != p2.x) return p1.x < p2.x ? -1 : +1;
        if (p1.y == p2.y) return 0;
        return p1.y < p2.y ? -1 : +1;
      }

    });
  }

  /**
   * Generates the convex Hull of a given set of points note: this is just a
   * naive approach, that should/could be replaced later on time complexity:
   * O(n^2)
   * 
   * @param pointSet
   * @return convexHull in counter clock wise order
   */
  public static OrderedListPolygon convexHull(List<Point> pointSet) {
    List<Point> hull = new ArrayList<Point>(pointSet.size()), points =
        new ArrayList<Point>(pointSet); // copy point set!

    Point sk, sl, pi;

    // pre-sort the points
    // NOTE: y-coordinate ordering on the same x-coordinate is crucial for
    // this algorithm, at least for the last ordered points!
    sortPointsByX(points);

    // compute the lower side of the convex hull

    hull.add(points.get(0));
    hull.add(points.get(1));

    int k = 1, n = points.size();
    for (int i = 2; i < n; ++i) {
      pi = points.get(i);

      while (k >= 1) {
        sk = hull.get(k);
        sl = hull.get(k - 1);

        if (MathUtils.checkOrientation(sl, sk, pi) >= 0) break;

        hull.remove(k);
        k -= 1;
      }

      hull.add(pi);
      k += 1;
    }

    // compute the upper side of the convex hull

    int lowerSize = k - 1;
    k = 1;

    for (int i = n - 3; i >= 0; --i) {
      pi = points.get(i);

      while (k >= 1) {
        sk = hull.get(lowerSize + k);
        sl = hull.get(lowerSize + k - 1);

        if (MathUtils.checkOrientation(sl, sk, pi) >= 0) break;

        hull.remove(lowerSize + k);
        k -= 1;
      }

      hull.add(pi);
      k += 1;
    }

    return new OrderedListPolygon(hull);
  }

  /**
   * This function calculates the visible region of a line segment of the
   * polygon determined by the Points pBegin and pEnd and returns a polygon
   * representing the region. It is assumed, that the points in polygon are
   * ordered counterclockwise. In this order, Vb is left from Va (Assume to
   * continue from the beginning if reached the end of the list.)
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @param polygon
   * @param p1
   * @param p2
   * @return
   */
  public static Polygon visiblePolygonRegionFromLineSegment(Polygon polygon,
      Point Va, Point Vb) {
        return polygon;
    // a. Set clonedPolygon with polygon
    
  }
}
