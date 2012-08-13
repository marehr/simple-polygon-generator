package polygonsSWP.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import polygonsSWP.util.MathUtils;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public class SteadyGrowthConvexHull extends Polygon
{

  public static int seqSearches = 0;

  public SteadyGrowthConvexHull() {
  }

  private ArrayList<Point> points = new ArrayList<Point>();

  @Override
  @SuppressWarnings("unchecked")
  public Polygon clone() {
    SteadyGrowthConvexHull hull = new SteadyGrowthConvexHull();
    hull.points = (ArrayList<Point>) this.points.clone();
    return hull;
  }

  public void addPoint(Point point) {
    addPointReturnAndInsertIndex(point);
  }

  public int addPointReturnAndInsertIndex(Point point) {
    if(point == null) return -1;

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

  private boolean updateCollinears(Point base, int[] supports, int lastIndex, int curr){
    if(lastIndex == 0) return true;

    Point supportOld = points.get(supports[lastIndex - 1]);
    Point supportNew = points.get(curr);

    if(supportOld.equals(supportNew))
      return false;

    if(MathUtils.checkOrientation(base, supportOld, supportNew) != 0)
      return true;

    // the old and new support are collinear with the base
    double distanceOld = base.squaredDistanceTo(supportOld),
           distanceNew = base.squaredDistanceTo(supportNew);

    // update support if the new collinear point is closer to the base point
    if(distanceOld < distanceNew) {
      supports[lastIndex - 1] = curr;
    }
    return false;
  }

  private int[] supportsBySeqSearch(Point base) {
    int[] supports = {-1, -1};
    int insertIndex = 0;

    for(int mid = 0; mid < size(); ++mid){
      State state = getState(mid, base);

      if(state == State.CONCAVE || state == State.REFLEX)
        continue;

      boolean insert = updateCollinears(base, supports, insertIndex, mid);
      if(!insert) continue;

      if(insert) {
        supports[insertIndex++] = mid;
      }

      if(insertIndex == 2) break;
    }

    seqSearches++;
    return supports;
  }

  private int[] getSupports(Point base){
    return supportsBySeqSearch(base);
  }

  private int splice(int leftSupport, int rightSupport, Point base){
    int distance = (rightSupport + points.size() - leftSupport) % points.size();
    int newSize = points.size() - distance + 2;

    Point[] oldPoints = points.toArray(new Point[points.size()]),
            newPoints = new Point[newSize];

    int srcFrom = 0, srcTo = leftSupport, length = 0,
        dstStart = 0, newIndex = leftSupport + 1;

    boolean updateFirst = base.compareTo(points.get(0)) < 0;
    boolean insertFromRight = updateFirst && leftSupport > rightSupport;
    boolean insertFromLeft = updateFirst && leftSupport == 0;

    if(insertFromRight) {
      // in this case we have an new minX
      srcFrom = rightSupport;
      srcTo   = leftSupport;
      dstStart = 1;
      newIndex = 0;
    }

    if(insertFromLeft) {
      srcFrom  = 1;
      dstStart = 1;
      newIndex = 0;
    }

    length = srcTo - srcFrom + 1;

    System.arraycopy(oldPoints, srcFrom, newPoints, dstStart, length);
    newPoints[newIndex] = base;

    if(insertFromLeft){
      newPoints[newPoints.length - 1] = oldPoints[0];
      newSize--;
    }

    // wir haben schon alle kopiert
    if(length + 1 == newSize) {
      points.clear();
      Collections.addAll(points, newPoints); 
      return newIndex;
    }

    dstStart = length + 1;
    srcFrom  = rightSupport;
    srcTo    = oldPoints.length - 1;

    System.arraycopy(oldPoints, srcFrom, newPoints, dstStart, srcTo - srcFrom + 1);

    points.clear();
    Collections.addAll(points, newPoints);
    return newIndex;
  }

  private int addPoint0(Point point) {
    if(size() == 0) {
      points.add(point);
      return 0;
    }

    if(size() == 1) {
      int index = point.compareTo(points.get(0)) < 0 ? 0 : 1;
      points.add(index, point);

      return index;
    }

    int[] supports = getSupports(point);
    Point leftSupport  = points.get(supports[0]),
          rightSupport = points.get(supports[1]);

    // point has to lie on the right side of the line leftSupport nad rightSupport
    if(MathUtils.checkOrientation(leftSupport, rightSupport, point) >= 0){
      int tmp = supports[0];
      supports[0] = supports[1];
      supports[1] = tmp;

      Point tmp1   = leftSupport;
      leftSupport  = rightSupport;
      rightSupport = tmp1;
    }

    int newIndex = splice(supports[0], supports[1], point);

    return newIndex;
  }

  @Override
  public boolean containsPoint(Point point, boolean onLine) {
    if (size() <= 2) return false;

    Point a, b;

    for (int size = size(), p = size - 1, q = 0; q < size; p = q++) {
      a = points.get(p);
      b = points.get(q);

      // NOTE: hier muessen wir nicht auf LineSegment.containsPoint
      // testen, da selbst wenn ein Punkt auf der Geraden a-b
      // und ausserhalb der Huelle liegt, es bei einer anderen
      // Kante der Huelle festgestellt wird, ob der Punkt wirklich
      // innerhalb lag. Da eine ConvexeHuelle convex ist :D
      int orients = MathUtils.checkOrientation(a, b, point);

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

}
