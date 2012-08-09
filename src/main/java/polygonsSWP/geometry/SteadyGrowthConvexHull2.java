package polygonsSWP.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import polygonsSWP.util.MathUtils;


public class SteadyGrowthConvexHull2 extends SteadyGrowthConvexHull
{

  public SteadyGrowthConvexHull2() {
    logger.addHandler(new Handler() {
      
      @Override
      public void publish(LogRecord record) {
        System.out.println(record.getMessage());
      }
      
      @Override
      public void flush() {
      }
      
      @Override
      public void close() throws SecurityException {
      }
    });
    //logger.setLevel(Level.FINE);
    logger.setLevel(Level.OFF);
  }

  private ArrayList<Point> points = new ArrayList<Point>();

  @Override
  @SuppressWarnings("unchecked")
  public Polygon clone() {
    SteadyGrowthConvexHull2 hull = new SteadyGrowthConvexHull2();
    hull.points = (ArrayList<Point>) this.points.clone();
    hull.minXIndex = minXIndex;
    hull.maxXIndex = maxXIndex;
    hull.minYIndex = minYIndex;
    hull.maxYIndex = maxYIndex;
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

  int minXIndex, maxXIndex, minYIndex, maxYIndex = 0;
  Logger logger = Logger.getAnonymousLogger();

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
    return supports;
  }

  private int[] supportsByBinSearch(int[] supports, Point base, boolean left){
    int low  = maxXIndex;
    int high = maxXIndex;
    int mid  = minXIndex;
    int offset = 0;
    int size = points.size();

    Point leftBoundary = points.get(minXIndex),
          rightBoundary = points.get(maxXIndex);

    logger.log(Level.FINEST,"base: " + base);
    logger.log(Level.FINEST,"leftBoundary: " + leftBoundary);
    logger.log(Level.FINEST,"rightBoundary: " + rightBoundary);
    logger.log(Level.FINEST,"within: " + (leftBoundary.x <= base.x && base.x <= rightBoundary.x));

    // base lies in x - boundary box
    if(leftBoundary.x <= base.x && base.x <= rightBoundary.x) {
      offset = minYIndex;
      low = high = (maxYIndex + size - offset) % size;
      mid = 0;
    }

    int insertIndex = 0;
    if(supports[0] != -1)
      insertIndex++;

    while (low <= high){
      if(mid == low && low == high) break;

      int realMid = (mid + offset) % size;

      State state = getState(realMid, base);
      int next = (mid + 1) % size;
      int prev = (mid + size - 1) % size;

      logger.log(Level.FINEST,"\n~~~~~~~~~~~~~~~~~~~");
      logger.log(Level.FINEST,"offset: " + offset);
      logger.log(Level.FINEST,"Low: " + low);
      logger.log(Level.FINEST,"High: " + high);
      logger.log(Level.FINEST,"Mid: " + mid);
      logger.log(Level.FINEST,"realMid: " + realMid);
      logger.log(Level.FINEST,"Left?: " + (left ? "YES":"NO"));
      logger.log(Level.FINEST,"State: " + state);

      if (state == State.REFLEX && left || state == State.CONCAVE && !left){
        logger.log(Level.FINEST,"Case1: ");
        logger.log(Level.FINEST,"GOTO LEFT");
        low = next;
      } else if (state == State.CONCAVE && left || state == State.REFLEX && !left) {
        logger.log(Level.FINEST,"Case2: ");
        logger.log(Level.FINEST,"GOTO RIGHT");
        high = prev;
      } else {
        logger.log(Level.FINEST,"Support: ");

        boolean insert = updateCollinears(base, supports, insertIndex, realMid);

        if(insert) {
          supports[insertIndex++] = realMid;
        }

        if(insertIndex == 2) break;

        if(left){
          low = next;
          logger.log(Level.FINEST,"GOTO LEFT");
        } else {
          high = prev;
          logger.log(Level.FINEST,"GOTO RIGHT");
        }
      }

      mid = (low + high) >>> 1;
      logger.log(Level.FINEST,"NEW LOW: " + low);
      logger.log(Level.FINEST,"NEW HIGH: " + high);
      logger.log(Level.FINEST,"NEW MID: " + mid);

    }

    return supports;
  }

  private int[] getSupports(Point base){
    logger.log(Level.FINER,"================= LEFT ===================== \n");

    int[] supports = supportsByBinSearch(new int[]{-1, -1}, base, true);
    logger.log(Level.FINER,"\n==> left: " + supports[0] + ", right: "+ supports[1]);

    if(supports[0] == supports[1] && supports[0] > -1) {
      logger.warning("supports are equal.1: " + supports[0]);
    }

    // first binSearch found both supports
    if(supports[1] != -1) return supports;

    logger.log(Level.FINER,"================= RIGHT ===================== \n");
    supports = supportsByBinSearch(supports, base, false);
    logger.log(Level.FINER,"\n==> left: " + supports[0] + ", right: "+ supports[1]);

    if(supports[0] == supports[1] && supports[0] > -1) {
      logger.warning("supports are equal.2: " + supports[0]);
    }

    // second binSearch found both supports
    if(supports[1] != -1) return supports;

    if(supports[0] == -1 || supports[1] == -1){
      logger.log(Level.FINE,"Support by Seq. search.");
      return supportsBySeqSearch(base);
    }

    return supports;
  }

  private void updateBoundaries(Point base, int newIndex, int distance, 
                                int leftSupport, int rightSupport,
                                boolean insertFromLeft, boolean insertFromRight){

    Point maxX = points.get(maxXIndex),
          minY = points.get(minYIndex),
          maxY = points.get(maxYIndex);

    int offset = insertFromLeft ? 1 : 2;

    logger.log(Level.FINE, "\n~~~ SETBOUNDARY - start");

    // minXIndex = 0, immer!
    if(base.compareTo(maxX) > 0) {
      maxXIndex = newIndex;
      logger.log(Level.FINE, "setMaxX.1: " + maxXIndex);
    } else if(leftSupport < maxXIndex) {
      maxXIndex -= distance - offset;
      logger.log(Level.FINE, "setMaxX.2: " + maxXIndex);
    } else if(insertFromRight) {
      maxXIndex -= rightSupport - 1;
      logger.log(Level.FINE, "setMaxX.3: " + maxXIndex);
    }

    if(base.compareToByY(minY) < 0) {
      minYIndex = newIndex;
      logger.log(Level.FINE, "setMinY.1: " + minYIndex);
    } else if(leftSupport < minYIndex) {
      minYIndex -= distance - offset;
      logger.log(Level.FINE, "setMinY.2: " + minYIndex);
    } else if(insertFromRight) {
      minYIndex -= rightSupport - 1;
      logger.log(Level.FINE, "setMinY.3: " + minYIndex);
    }

    if(base.compareToByY(maxY) > 0) {
      maxYIndex = newIndex;
      logger.log(Level.FINE, "setMaxY.1: " + maxYIndex);
    } else if(leftSupport < maxYIndex) {
      maxYIndex -= distance - offset;
      logger.log(Level.FINE, "setMaxY.2: " + maxYIndex);
    } else if(insertFromRight) {
      maxYIndex -= rightSupport - 1;
      logger.log(Level.FINE, "setMaxY.3: " + maxYIndex);
    }

    logger.log(Level.FINE, "~~~ SETBOUNDARY - end\n");
  }

  private int splice(int leftSupport, int rightSupport, Point base){
    int distance = (rightSupport + points.size() - leftSupport) % points.size();
    int newSize = points.size() - distance + 2;

    Point[] oldPoints = points.toArray(new Point[points.size()]),
            newPoints = new Point[newSize];

    logger.log(Level.FINE, "distance: " + distance);
    logger.log(Level.FINE, "newSize: " + newSize);

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

    updateBoundaries(base, newIndex, distance, leftSupport, rightSupport, insertFromLeft, insertFromRight);

    length = srcTo - srcFrom + 1;

    logger.log(Level.FINE, "length: " + length);
    logger.log(Level.FINE, "srcFrom: " + srcFrom);
    logger.log(Level.FINE, "srcTo: " + srcTo);
    logger.log(Level.FINE, "destStart: " + dstStart);
    logger.log(Level.FINE, "newIndex: " + newIndex);

    System.arraycopy(oldPoints, srcFrom, newPoints, dstStart, length);
    newPoints[newIndex] = base;

    if(insertFromLeft){
      newPoints[newPoints.length - 1] = oldPoints[0];
      newSize--;
    }

    logger.log(Level.FINE, "new hull: " + Arrays.asList(newPoints));

    // wir haben schon alle kopiert
    if(length + 1 == newSize) {
      points.clear();
      Collections.addAll(points, newPoints); 
      return newIndex;
    }

    dstStart = length + 1;
    srcFrom  = rightSupport;
    srcTo    = oldPoints.length - 1;

    logger.log(Level.FINE, "length: " + length);
    logger.log(Level.FINE, "srcFrom: " + srcFrom);
    logger.log(Level.FINE, "srcTo: " + srcTo);
    logger.log(Level.FINE, "destStart: " + dstStart);
    logger.log(Level.FINE, "newIndex: " + newIndex);

    System.arraycopy(oldPoints, srcFrom, newPoints, dstStart, srcTo - srcFrom + 1);

    logger.log(Level.FINE, "new hull: " + Arrays.asList(newPoints));

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

      maxXIndex = 1;

      if(points.get(0).compareToByY(points.get(1)) < 0){
        minYIndex = 0;
        maxYIndex = 1;
      } else {
        minYIndex = 1;
        maxYIndex = 0;
      }

      logger.log(Level.FINE, points.toString());
      return index;
    }

    logger.log(Level.FINE,"\n<><><><><><><><><><><><><><><><>");
    logger.log(Level.FINE,"minXIndex: " + minXIndex);
    logger.log(Level.FINE,"maxXIndex: " + maxXIndex);
    logger.log(Level.FINE,"minYIndex: " + minYIndex);
    logger.log(Level.FINE,"maxYIndex: " + maxYIndex);
    logger.log(Level.FINE,"minX: " + points.get(minXIndex));
    logger.log(Level.FINE,"maxX: " + points.get(maxXIndex));
    logger.log(Level.FINE,"minY: " + points.get(minYIndex));
    logger.log(Level.FINE,"maxY: " + points.get(maxYIndex));

    logger.log(Level.FINE,"\n<><><><><><><><><><><><><><><><>");
    logger.log(Level.FINE,"base: " + point);
    logger.log(Level.FINE,"points: " + points);

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

    logger.log(Level.FINE,"leftSupportIndex: " + supports[0]);
    logger.log(Level.FINE,"rightSupportIndex: " + supports[1]);
    logger.log(Level.FINE,"leftSupport: " + leftSupport);
    logger.log(Level.FINE,"rightSupport: " + rightSupport);

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

  public static void main(String[] args) {
    SteadyGrowthConvexHull2 hull = new SteadyGrowthConvexHull2();

    List<Point> points = hull.getPoints();
    points.addAll(Arrays.asList(
      new Point (330.698,19.73), new Point (374.22,319.351), new Point (403.635,525.373), new Point (401.405,558.494), new Point (370.454,439.91), new Point (364.079,381.414), new Point (354.978,289.581), new Point (346.337,195.225), new Point (331.085,25.187)
    ));

    hull.logger.setLevel(Level.FINE);

    hull.minXIndex = points.indexOf(Collections.min(points));
    hull.maxXIndex = points.indexOf(Collections.max(points));
    hull.minYIndex = points.indexOf(Collections.min(points, Point.YCompare));
    hull.maxYIndex = points.indexOf(Collections.max(points, Point.YCompare));

    hull.logger.setLevel(Level.ALL);
    hull.addPoint(new Point(594.598, 172.175));

//    hull.logger.setLevel(Level.OFF);
//    hull.addPoint(new Point(9, 9));
//    hull.addPoint(new Point(7.9, 7.9));
//    hull.addPoint(new Point(14, 6));
//    hull.addPoint(new Point(8, 2));
//    hull.addPoint(new Point(5, 0.1));
//    hull.addPoint(new Point(5, 4));
//    hull.addPoint(new Point(-3, -3));
//    hull.addPoint(new Point(1.1, 5));
//    hull.addPoint(new Point(5, 8.125));
//    hull.addPoint(new Point(2, 5.5));
//
//    // nicht in allgemeiner lage
//    System.out.println("\nNICHT allgemeine LAGE");
//    hull.addPoint(new Point(13, 2));
//    hull.addPoint(new Point(7, 8));
//    hull.addPoint(new Point(6, 9));
//    hull.addPoint(new Point(10, 5));
//    hull.addPoint(new Point(1, 5));
//    hull.addPoint(new Point(5, 0));
//    hull.addPoint(new Point(5, 8.5));
//    hull.logger.setLevel(Level.FINEST);
//    hull.addPoint(new Point(2, 6));
  }
}
