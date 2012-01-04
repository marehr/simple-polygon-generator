package polygonsSWP.generators.rpa;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;

import polygonsSWP.data.PolygonHistory;
import polygonsSWP.generators.IllegalParameterizationException;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory.Parameters;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.geometry.Ray;
import polygonsSWP.util.GeneratorUtils;
import polygonsSWP.util.MathUtils;


public class RandomPolygonAlgorithmFactory
  implements PolygonGeneratorFactory
{

  @Override
  public boolean acceptsUserSuppliedPoints() {
    return false;
  }

  @Override
  public List<Parameters> getAdditionalParameters() {
    return new LinkedList<Parameters>();
  }

  @Override
  public String toString() {
    return "RandomPolygonAlgorithm";
  }

  @Override
  public PolygonGenerator createInstance(Map<Parameters, Object> params,
      PolygonHistory steps)
    throws IllegalParameterizationException {
    Integer n = (Integer) params.get(Parameters.n);
    if (n == null)
      throw new IllegalParameterizationException("Number of points not set.",
          Parameters.n);

    Integer size = (Integer) params.get(Parameters.size);
    if (size == null)
      throw new IllegalParameterizationException(
          "Size of bounding box not set.", Parameters.size);

    return new RandomPolygonAlgorithm(n, size, steps);
  }


  private static class RandomPolygonAlgorithm
    implements PolygonGenerator
  {
    private boolean dostop = false;
    private int _n;
    private int _size;
    private PolygonHistory steps;

    RandomPolygonAlgorithm(int n, int size, PolygonHistory steps) {
      this._n = n;
      this._size = size;
      this.steps = steps;
    }

    @Override
    public Polygon generate() {

      Random random = new Random(System.currentTimeMillis());

      // 1. generate 3 rand points -> polygon P
      OrderedListPolygon polygon =
          new OrderedListPolygon(MathUtils.createRandomSetOfPointsInSquare(3,
              _size));

      List<Point> polygonPoints = polygon.getPoints();

      // 2. n-3 times:
      for (int i = 0; i < _n - 3;) {
        // test if algorithm should be canceled
        if (dostop) break;
        // 2.a select random line segment VaVb
        // (assumed that there will be less than 2^31-1 points)
        int randomIndex = random.nextInt(polygonPoints.size());
        Point Va = polygonPoints.get(randomIndex);
        Point Vb = polygonPoints.get(randomIndex + 1);
        // 2.b determine visible region to VaVb -> P'
        Polygon visibleRegion =
            visiblePolygonRegionFromLineSegment(polygon, Va, Vb);
        // 2.c randomly select point Vc in P'
        Point randomPoint = visibleRegion.createRandomPoint();
        // 2.d add line segments VaVc and VcVb (delete line segment VaVb)
        polygonPoints.add(randomIndex, randomPoint);
      }
      
      if (dostop) return null;

      return polygon;
    }

    /**
     * This function calculates the visible region of a line segment of the
     * polygon determined by the Points pBegin and pEnd and returns a polygon
     * representing the region. It is assumed, that the points in polygon are
     * ordered counterclockwise. In this order, Vb is left from Va
     * (vvvvvVbVavvvv)(Assume to continue from the beginning if reached the end
     * of the list.) TODO: check if visible form inside or outside!!!
     * 
     * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
     * @param polygon
     * @param p1
     * @param p2
     * @return
     */
    private Polygon visiblePolygonRegionFromLineSegment(Polygon polygon,
        Point Va, Point Vb) {
      // a. Set clone with polygon
      Polygon clone = polygon.clone();
      // b. intersect Line VaVb with clone, take first intersection on each side
      // of line, if existent, isert them into clone
      Ray rayVaVb = new Ray(Va, Vb);
      Ray rayVbVa = new Ray(Vb, Va);
      Point[] vx = rayVaVb.getPointClosestToBase(polygon.intersect(rayVaVb));
      Point[] vy = rayVbVa.getPointClosestToBase(polygon.intersect(rayVbVa));
      // if vx/vy exists and is no point of polygon
      boolean insVx = (vx != null && vx[1] != null);
      boolean insVy = (vy != null && vy[1] != null);
      ListIterator<Point> cloneIterator = clone.getPoints().listIterator(0);
      while (cloneIterator.hasNext()) {
        Point current = cloneIterator.next();
        if ((current.equals(vx[1]) || current.equals(vx[2])) && insVx) {
          cloneIterator.add(vx[0]);
          insVx = false;
        }
        if ((current.equals(vy[1]) || current.equals(vy[2])) && insVy) {
          cloneIterator.add(vy[0]);
          insVx = false;
        }
      }
      // c. beginning with Va.next determine vertices(running variable vi)
      // visible
      // from both Va and Vb. maintain point last visible from both Va and Vb.
      Point lastVisible = Va;
      cloneIterator =
          clone.getPoints().listIterator(polygon.getPoints().indexOf(Va));
      while (true) {
        List<Point> clonePoints = clone.getPoints();
        if (!cloneIterator.hasNext()) {
          cloneIterator = clonePoints.listIterator(0);
        }
        Point vi = cloneIterator.next();
        // break if all points in clone visited
        if (vi.equals(Vb)) {
          break;
        }
        boolean isVisibleFromVa =
            GeneratorUtils.isPolygonPointVisible(Va, vi, polygon);
        boolean isVisibleFromVb =
            GeneratorUtils.isPolygonPointVisible(Vb, vi, polygon);
        // check if viPrev is visible to Va and/or Vb
        // therefore iterate backwards over clone and search for first point in
        // polygon (clone and polygon differ by now). this point is viPrev.
        boolean isViPrevVisibleFromVa = false;
        boolean isViPrevVisibleFromVb = false;
        ListIterator<Point> prevIterator =
            clone.getPoints().listIterator(clonePoints.indexOf(vi));
        while (true) {
          if (!prevIterator.hasPrevious()) {
            prevIterator = clonePoints.listIterator(clonePoints.size() - 1);
          }
          Point currentPrevious = prevIterator.previous();
          if (polygon.getPoints().contains(currentPrevious)) {
            isViPrevVisibleFromVa =
                GeneratorUtils.isPolygonPointVisible(currentPrevious, Va,
                    polygon);
            isViPrevVisibleFromVb =
                GeneratorUtils.isPolygonPointVisible(currentPrevious, Vb,
                    polygon);
            break;
          }
        }
        // vi not visible from va or vb
        if (!isVisibleFromVa && !isVisibleFromVb) {
          cloneIterator.remove();
        }
        // vi visible from va and vb
        if (isVisibleFromVa && isVisibleFromVb) {
          // case 1 viPrev visible from va and vb
          if (isViPrevVisibleFromVa && isViPrevVisibleFromVb) {
            lastVisible = vi;
          }
          // case 2 viPrev not visible from va and vb
          if (!isViPrevVisibleFromVa && !isViPrevVisibleFromVb) {
            Ray r1 = new Ray(Va, lastVisible);
            Ray r2 = new Ray(Vb, lastVisible);
            Ray r3 = new Ray(Va, vi);
            Ray r4 = new Ray(Vb, vi);

            Point u1 = r1.getPointClosestToBase(polygon.intersect(r1))[0];
            Point u2 = r2.getPointClosestToBase(polygon.intersect(r2))[0];
            Point u3 = r3.getPointClosestToBase(polygon.intersect(r3))[0];
            Point u4 = r4.getPointClosestToBase(polygon.intersect(r4))[0];

            if (u1 != null &&
                GeneratorUtils.isPolygonPointVisible(Va, u1, polygon) &&
                GeneratorUtils.isPolygonPointVisible(Vb, u1, polygon)) {
              cloneIterator.add(u1);
            }
            if (u2 != null &&
                GeneratorUtils.isPolygonPointVisible(Va, u2, polygon) &&
                GeneratorUtils.isPolygonPointVisible(Vb, u2, polygon)) {
              cloneIterator.add(u2);
            }
            if (u3 != null &&
                GeneratorUtils.isPolygonPointVisible(Va, u3, polygon) &&
                GeneratorUtils.isPolygonPointVisible(Vb, u3, polygon)) {
              cloneIterator.add(u3);
            }
            if (u4 != null &&
                GeneratorUtils.isPolygonPointVisible(Va, u4, polygon) &&
                GeneratorUtils.isPolygonPointVisible(Vb, u4, polygon)) {
              cloneIterator.add(u4);
            }
            lastVisible = vi;
          }
          // case 3 viPrev visible to one of va and vb
          if (isViPrevVisibleFromVa && !isViPrevVisibleFromVb) {
            Ray r1 = new Ray(Va, lastVisible);
            Ray r2 = new Ray(Va, vi);

            Point u1 = r1.getPointClosestToBase(polygon.intersect(r1))[0];
            Point u2 = r2.getPointClosestToBase(polygon.intersect(r2))[0];

            if (u1 != null &&
                GeneratorUtils.isPolygonPointVisible(Va, u1, polygon) &&
                GeneratorUtils.isPolygonPointVisible(Vb, u1, polygon)) {
              cloneIterator.add(u1);
            }
            if (u2 != null &&
                GeneratorUtils.isPolygonPointVisible(Va, u2, polygon) &&
                GeneratorUtils.isPolygonPointVisible(Vb, u2, polygon)) {
              cloneIterator.add(u2);
            }
          }
          // case 4 viPrev visible to one of va and vb
          if (!isViPrevVisibleFromVa && isViPrevVisibleFromVb) {
            Ray r1 = new Ray(Vb, lastVisible);
            Ray r2 = new Ray(Vb, vi);

            Point u1 = r1.getPointClosestToBase(polygon.intersect(r1))[0];
            Point u2 = r2.getPointClosestToBase(polygon.intersect(r2))[0];

            if (u1 != null &&
                GeneratorUtils.isPolygonPointVisible(Va, u1, polygon) &&
                GeneratorUtils.isPolygonPointVisible(Vb, u1, polygon)) {
              cloneIterator.add(u1);
            }
            if (u2 != null &&
                GeneratorUtils.isPolygonPointVisible(Va, u2, polygon) &&
                GeneratorUtils.isPolygonPointVisible(Vb, u2, polygon)) {
              cloneIterator.add(u2);
            }
          }
        }
      }
      return clone;
    }

    @Override
    public void stop() {
      dostop = true;
    }
  }
}