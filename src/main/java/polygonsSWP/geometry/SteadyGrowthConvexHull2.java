package polygonsSWP.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import polygonsSWP.util.MathUtils;


public class SteadyGrowthConvexHull2 extends SteadyGrowthConvexHull
{

  private ArrayList<Point> points = new ArrayList<Point>();

  @Override
  @SuppressWarnings("unchecked")
  public Polygon clone() {
    SteadyGrowthConvexHull2 hull = new SteadyGrowthConvexHull2();
    hull.points = (ArrayList<Point>) this.points.clone();
    return hull;
  }

  public void addPoint(Point point) {
    addPointReturnInsertIndex(point);
  }

  public int addPointReturnInsertIndex(Point point) {
    // wenn der punkt innerhalb convexen huelle ist,
    // dann brauchen wir nichts aktualisieren
    if (containsPoint(point, true)) return -1;

    return addPoint0(point);
  }

  /**
   * Concave:
   * Segment curr, base intersects the interior of the confex hull
   * 
   * Reflex:
   * Segment curr, base does not intersect the interior of the confex hull
   * 
   * Supporting:
   * prev, next on the same side from the base, curr line
   */

  private enum State{ CONCAVE, REFLEX, SUPPORTING, UNDEFINED};

  private State getState(int currIndex, Point base){
    Point curr = points.get(currIndex);
    Point prev = points.get((currIndex + points.size() - 1) % points.size());
    Point next = points.get((currIndex + 1) % points.size());
    int prevOrients = MathUtils.checkOrientation(base, curr, prev);
    int nextOrients = MathUtils.checkOrientation(base, curr, next);

    // assuming CCW
    // prev, next on the same side from the base, curr line -> SUPPORTING
    if(prevOrients == nextOrients) return State.SUPPORTING;

    // prev turns right and next turns left -> CONCAVE
    if(prevOrients < 0 && nextOrients > 0) return State.CONCAVE;

    // prev turns left and next turns right -> REFLEX
    if(prevOrients > 0 && nextOrients < 0) return State.REFLEX;

    return State.UNDEFINED;
  }

  int minXIndex, maxXIndex, minYIndex, maxYIndex;

  private int[] binSearch(Point base, boolean left){
    int low  = maxXIndex;
    int high = maxXIndex;
    int mid  = minXIndex;
    int size = points.size();

    Point leftBoundary = points.get(minXIndex),
          rightBoundary = points.get(maxXIndex);

    // base lies in x - boundary box
    if(leftBoundary.x <= base.x && base.x <= rightBoundary.x) {
      low = high = maxYIndex;
      mid = minYIndex;
    }

    int[] supports = {-1, -1};
    int insertIndex = 0;

    while (low <= high){
      State state = getState(mid, base);
      int next = (mid + 1) % size;
      int prev = (mid + size - 1) % size;

      System.out.println("\n~~~~~~~~~~~~~~~~~~~");
      System.out.println("Low: " + low);
      System.out.println("High: " + high);
      System.out.println("Mid: " + mid);
      System.out.println("Left?: " + (left ? "YES":"NO"));
      System.out.println("State: " + state);

      if (state == State.REFLEX && left || state == State.CONCAVE && !left){
        System.out.println("Case1: ");
        System.out.println("GOTO LEFT");
        low = next;
      } else if (state == State.CONCAVE && left || state == State.REFLEX && !left) {
        System.out.println("Case2: ");
        System.out.println("GOTO RIGHT");
        high = prev;
      } else {
        System.out.println("Support: ");

        boolean insert = true;

        if(insertIndex > 0) {
          Point supportOld = points.get(supports[insertIndex - 1]);
          Point supportNew = points.get(mid);

          // the old and new support are collinear with the base
          if(MathUtils.checkOrientation(base, supportOld, supportNew) == 0){
            insert = false;

            double distanceOld = base.squaredDistanceTo(supportOld),
                   distanceNew = base.squaredDistanceTo(supportNew);

            // update support if the new collinear point is closer to the base point
            if(distanceOld > distanceNew) {
              supports[insertIndex - 1] = mid;
            }
          }
        }

        if(insert) {
          supports[insertIndex++] = mid;
        }

        if(insertIndex == 2) break;

        if(left){
          low = next;
          System.out.println("GOTO LEFT");
        } else {
          high = prev;
          System.out.println("GOTO RIGHT");
        }
      }

      mid = (low + high) >>> 1;
      System.out.println("NEW LOW: " + low);
      System.out.println("NEW HIGH: " + high);
      System.out.println("NEW MID: " + mid);

    }

    return supports;
  }

  private int[] getSupports(Point base){
    System.out.println("================= LEFT ===================== \n");

    int[] supports = binSearch(base, true);
    System.out.println("\n==> left: " + supports[0] + ", right: "+ supports[1]);

    // first binSearch found both supports
    if(supports[1] != -1) return supports;

    int leftSupport = supports[0];

    System.out.println("================= RIGHT ===================== \n");
    supports = binSearch(base, false);
    System.out.println("\n==> left: " + supports[0] + ", right: "+ supports[1]);

    // second binSearch found both supports
    if(supports[1] != -1) return supports;

    return new int[] {leftSupport, supports[0]};
  }

  private int addPoint0(Point point) {
    int[] supports = getSupports(point);
    Point leftSupport  = points.get(supports[0]),
          rightSupport = points.get(supports[1]);

    // point has to lie on the right side of the line leftSupport nad rightSupport
    if(MathUtils.checkOrientation(leftSupport, rightSupport, point) >= 0){
      int tmp1 = supports[0];
      supports[0] = supports[1];
      supports[1] = tmp1;

      Point tmp    = leftSupport;
      leftSupport  = rightSupport;
      rightSupport = tmp;
    }

    System.out.println("points: " + points);
    System.out.println("leftSupportIndex: " + supports[0]);
    System.out.println("rightSupportIndex: " + supports[1]);
    System.out.println("leftSupport: " + leftSupport);
    System.out.println("rightSupport: " + rightSupport);
    return 0;
  }

  @Override
  public boolean containsPoint(Point point, boolean onLine) {
    if (size() <= 2) return false;

    Point a, b;

    for (int p = size() - 1, q = 0; q < size(); p = q++) {
      a = points.get(p);
      b = points.get(q);

      // NOTE: hier muessen wir nicht auf LineSegment.containsPoint
      // testen, da selbst wenn ein Punkt auf der Geraden a-b
      // und ausserhalb der Huelle liegt, es bei einer anderen
      // Kante der Huelle festgestellt wird, ob der Punkt wirklich
      // innerhalb lag. Da eine ConvexeHuelle convex ist :D
      int orients = MathUtils.checkOrientation(a, b, point);
//      System.out.println("~~~~~~~~~~~~~~~~~~");
//      System.out.println("a: " + a);
//      System.out.println("b: " + b);
//      System.out.println("point: " + point);
//      System.out.println("orients: " + (orients < 0 ? "RIGHT" : (orients > 0 ? "LEFT" : "ONLINE")));

      // wir liegen auf einer Kante des Polygons und wir sagen,
      // dass bei onLine = false der Punkt ausserhalb des Polygons liegt
      if (!onLine && orients == 0) return false;

      // bei CCW muessen alle Punkte LINKS liegen, damit sie
      // innerhalb des Polygons sind.

      // Der Punkt ist aber auf der RECHTen seite, also sicher ausserhalb des
      // Polygons
      if (orients < 0) return false;
    }
    return true;
  }

  @Override
  public List<Point> getPoints() {
    return points;
  }

  @Override
  public int size() {
    return points.size();
  }

  @Override
  public Point createRandomPoint() {
    throw new UnsupportedOperationException();
  }

  public static void main(String[] args) {
    SteadyGrowthConvexHull2 hull = new SteadyGrowthConvexHull2();
    List<Point> points = hull.getPoints();

    points.addAll(Arrays.asList(
      new Point(2,4),
      new Point(3,2),
      new Point(4,1),
      new Point(6,1),
      new Point(8,3),
      new Point(9,5),
      new Point(8,7),
      new Point(6,8),
      new Point(4,8),
      new Point(3,7)
    ));

    hull.minXIndex = points.indexOf(Collections.min(points));
    hull.maxXIndex = points.indexOf(Collections.max(points));
    hull.minYIndex = points.indexOf(Collections.min(points, Point.YCompare));
    hull.maxYIndex = points.indexOf(Collections.max(points, Point.YCompare));

    hull.addPoint(new Point(6, 9));
    //hull.addPoint(new Point(7, 8));
//    hull.addPoint(new Point(7.9, 7.9));
//    hull.addPoint(new Point(9, 9));
//    hull.addPoint(new Point(14, 6));
//    hull.addPoint(new Point(13, 2));
//    hull.addPoint(new Point(8, 2));
//    hull.addPoint(new Point(5, 0.1));
//    hull.addPoint(new Point(-3, -3));
//    hull.addPoint(new Point(1.1, 5));
//    hull.addPoint(new Point(5, 8.125));
//    hull.addPoint(new Point(2, 5.5));
  }
}
