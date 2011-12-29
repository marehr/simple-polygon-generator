package polygonsSWP.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import polygonsSWP.generators.PolygonGeneratorFactory.Parameters;
import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;


public class GeneratorUtils
{
  private static Random rand_ = new Random();

  /**
   * Tests whether a given set of points is in general position, which means
   * that no points are the same, no 3 points are colinear and no 4 points lie
   * on a circle.
   * 
   * @param pointSet the set of points
   * @return true, if in general position, false otherwise.
   */
  public static boolean isInGeneralPosition(List<Point> pointSet) {
    // First condition: No 2 points are the same.
    for (int i = 0; i < pointSet.size() - 1; i++) {
      for (int j = i + 1; j < pointSet.size(); j++) {
        if (pointSet.get(i).equals(pointSet.get(j))) return false;
      }
    }

    // Second condition: No 3 points are colinear.
    for (int i = 0; i < pointSet.size() - 2; i++) {
      for (int j = i + 1; j < pointSet.size() - 1; j++) {
        for (int k = j + 1; k < pointSet.size(); k++) {
          if (MathUtils.checkOrientation(pointSet.get(i), pointSet.get(j),
              pointSet.get(k)) == 0) return false;
        }
      }
    }

    // Third condition: No 4 points lie on a circle.
    // TODO implement
    // Please someone read this:
    // http://isthe.com/chongo/tech/math/n-cluster/
    // and tell me that they don't talk about GP there.

    return true;
  }

  /**
   * Convenience method for Generators able to use random or pre-defined points.
   * 
   * @param params params as handled over to the Generator
   * @param ensureGeneralPosition if set, this methods makes sure that the
   *          returned set of points is in general position. If it was a
   *          user-supplied set of points, an exception is thrown.
   * @return either the given set of points or a randomly created set if size n.
   */

  @SuppressWarnings("unchecked")
  public static List<Point> createOrUsePoints(Map<Parameters, Object> params,
      boolean ensureGeneralPosition) {
    Integer n = (Integer) params.get(Parameters.n);
    Integer size = (Integer) params.get(Parameters.size);
    List<Point> s = (List<Point>) params.get(Parameters.points);

    // TODO remove
    assert (s != null || (n != null && size != null));

    if (s == null) {

      do {
        s = MathUtils.createRandomSetOfPointsInSquare(n, size);
      }
      while (ensureGeneralPosition && !isInGeneralPosition(s));

    }
    else {

      if (ensureGeneralPosition && !isInGeneralPosition(s))
      // TODO throw sth proper
        throw new RuntimeException("user-defined set of points not in GP.");

    }

    // Note: We're creating a copy of the list here to avoid having
    // the same list object in several containers (eg in the GUI and
    // in the computed Polygon).
    return new ArrayList<Point>(s);
  }

  /**
   * Compatibility method for above.
   */

  public static List<Point> createOrUsePoints(Map<Parameters, Object> params) {
    return createOrUsePoints(params, false);
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
        return p1.compareToByY(p2);
      }

    });
  }

  /**
   * picks point at random and removes it
   * 
   * @param points set of points
   * @return random removed point
   */
  public static Point removeRandomPoint(List<Point> points) {
    return points.remove(rand_.nextInt(points.size()));
  }

  /**
   * Sorts points (in-place) by x-coordinate. All points on the same same
   * x-coordinate will be ordered ascending by the y-coordinate
   * 
   * @param points
   */
  public static void sortPointsByX(List<Point> points) {
    Collections.sort(points);
  }

  /**
   * Generates the convex Hull of a given set of points note: this is just a
   * naive approach, that should/could be replaced later on time complexity: O(n
   * log n)
   * 
   * @see http://www.ics.uci.edu/~eppstein/161/960307.html
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

    if (points.size() <= 3) return new OrderedListPolygon(points);

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

    hull.remove(hull.size() - 1);

    return new OrderedListPolygon(hull);
  }

  /**
   * Borders of polygon count as inside so lineSegments and Points coincident or
   * in lineSegment do not block sight.
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @param base
   * @param support
   * @param polygon
   */
  public static boolean
      isPolygonPointVisible(Point a, Point b, Polygon polygon) {
    List<Point[]> intersections = polygon.intersect(new LineSegment(a, b));
    boolean visible = true;
    for (Point[] points : intersections) {
      if (points[0] == null && points[1] == null) {
        visible = false;
      }
    }
    return visible;
  }
}
