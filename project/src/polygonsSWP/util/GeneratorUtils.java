package polygonsSWP.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import polygonsSWP.data.OrderedListPolygon;
import polygonsSWP.data.Point;
import polygonsSWP.data.Polygon;


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
    // a. Set clonedPolygon with polygon
    Polygon cPolygon = polygon.clone();
    List<Point> cPolygonPoints = cPolygon.getPoints();
    // b. Extend line from Va and Vb and check for intersection.
    List<Point[]> intersectionsVaVb =
        MathUtils.getIntersectingPointsWithPolygon(polygon, Va, Vb);
    // get first intersecting Point on side of pEnd and pBegin
    PriorityQueue<Point[]> nextToVa = new PriorityQueue<Point[]>();
    PriorityQueue<Point[]> nextToVb = new PriorityQueue<Point[]>();
    for (Point[] pointTriple : intersectionsVaVb) {
      double distancePointVa =
          MathUtils.distanceBetweenTwoPoints(Va, pointTriple[0]);
      double distancePointVb =
          MathUtils.distanceBetweenTwoPoints(Vb, pointTriple[0]);
      if (distancePointVa < distancePointVb) {
        nextToVa.add(pointTriple);
      }
      else if (distancePointVa > distancePointVb) {
        nextToVb.add(pointTriple);
      }
      // distancePointPBegin == distacePointPEnd
      else {
        if (MathUtils.checkIfPointIsBetweenTwoPoints(Va, pointTriple[0],
            Vb)) {
          nextToVb.add(pointTriple);
        }
        else {
          nextToVa.add(pointTriple);
        }
      }
    }
    // add those Points to clonedPolygon
    Point[] firstTriple = nextToVa.remove();
    cPolygonPoints.add(cPolygonPoints.indexOf(firstTriple[2]),
        firstTriple[0]);
    firstTriple = nextToVb.remove();
    cPolygonPoints.add(cPolygonPoints.indexOf(firstTriple[2]),
        firstTriple[0]);
    return cPolygon;

    // c. for all vertices in polygon, determine if visible from Va and Vb
    Point lastVisible = Va;
    int cPolygonSize = cPolygonPoints.size();
    int i = cPolygonPoints.indexOf(Va) +1;
    int indexVb = cPolygonPoints.indexOf(Va);
    
    while (i != indexVb){
      
      // currently checked point Vi
      Point Vi = cPolygonPoints.get(i%cPolygonSize);
      
      boolean isViVisibleFromVa = true;
      boolean isViVisibleFromVb = true;
      
      // test if Vi is visible from Va
      List<Point[]> intersectionsVaVi = MathUtils.getIntersectingPointsWithPolygon(cPolygon, Va, Vi);
      // delete intersections outside of polygon
      for (Point[] pointTriple : intersectionsVaVi) {
        if (!MathUtils.checkIfPointIsBetweenTwoPoints(Va, Vi, pointTriple[0])){
          intersectionsVaVi.remove(pointTriple);
        }
      }
      if (intersectionsVaVi.size() > 0){
        isViVisibleFromVa = false;
      }
      // test if Vi is visible from Vb
      List<Point[]> intersectionsVbVi = MathUtils.getIntersectingPointsWithPolygon(cPolygon, Vb, Vi);
      // delete intersections outside of polygon
      for (Point[] pointTriple : intersectionsVbVi) {
        if (!MathUtils.checkIfPointIsBetweenTwoPoints(Vb, Vi, pointTriple[0])){
          intersectionsVbVi.remove(pointTriple);
        }
      }
      if (intersectionsVbVi.size() > 0){
        isViVisibleFromVb = false;
      }
      // if visible form one of Va or Vb, check if only visible from outside
      //TODO: implement check
      
      // Case1: Vi is neither visible from Va or Vb -> delete Vi from cPolygon
      if (){
        
      }
    }
  }
}
