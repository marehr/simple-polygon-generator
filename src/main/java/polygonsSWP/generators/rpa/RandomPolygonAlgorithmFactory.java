package polygonsSWP.generators.rpa;

import java.awt.Color;
import java.util.Currency;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;

import polygonsSWP.data.PolygonHistory;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.data.Scene;
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
      PolygonStatistics stats,
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
    
    return new RandomPolygonAlgorithm(n, size, steps, stats);
  }


  private static class RandomPolygonAlgorithm
    implements PolygonGenerator
  {
    private boolean dostop = false;
    private int _n;
    private int _size;
    private final PolygonHistory steps;
    private final PolygonStatistics statistics;
    
    RandomPolygonAlgorithm(int n, int size, PolygonHistory steps, PolygonStatistics statistics) {
      this._n = n;
      this._size = size;
      this.steps = steps;
      this.statistics = statistics;
    }

    @Override
    public Polygon generate() {

      Random random = new Random(System.currentTimeMillis());

      // 1. generate 3 rand points -> polygon P
      OrderedListPolygon polygon =
          new OrderedListPolygon(GeneratorUtils.createRandomSetOfPointsInSquare(
              3, _size, true));

      List<Point> polygonPoints = polygon.getPoints();

      // 2. n-3 times:
      for (int i = 0; i < _n - 3; i++) {
        System.out.println("main loop:" + i);
        // test if algorithm should be canceled
        if (dostop) break;
        // 2.a select random line segment VaVb
        // (assumed that there will be less than 2^31-1 points)
        int randomIndex = random.nextInt(polygonPoints.size());
        Point Vb = polygonPoints.get(randomIndex);
        Point Va = polygonPoints.get((randomIndex + 1) % polygonPoints.size());
        // 2.b determine visible region to VaVb -> P'
        Polygon visibleRegion =
            visiblePolygonRegionFromLineSegment(polygon, Va, Vb);
        System.out.println("visible region: " + visibleRegion.getPoints() + "\n");
        if (steps != null) {
          Scene scene = steps.newScene();
          scene.addPolygon(polygon, Color.GRAY);
          scene.addPolygon(polygon, Color.LIGHT_GRAY);
          scene.save();          
        }
        // 2.c randomly select point Vc in P'
        Point randomPoint = visibleRegion.createRandomPoint();
        System.out.println("random point: " + randomPoint);
        // 2.d add line segments VaVc and VcVb (delete line segment VaVb)
        polygonPoints.add(randomIndex, randomPoint);
        System.out.println("new polygon" + polygon.getPoints());
        System.out.println("-----------------\n");
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
      
      CircularList<Point> polygonPoints = new CircularList<Point>();
      polygonPoints.addAll(polygon.getPoints());
      ListIterator<Point> polygonIter = polygonPoints.listIterator();
      
      System.out.println("va, vb: " + Va + Vb + "\n");
      
      
      /* a. Set clone with polygon.*/
      
      Polygon clone = polygon.clone();
      CircularList<Point> clonePoints = new CircularList<Point>();
      clonePoints.addAll(clone.getPoints());
      
      
      /* b. intersect Line VaVb with clone, take first intersection on each side
      of line, if existent, insert them into clone*/
      
      Ray rayVaVb = new Ray(Va, Vb);
      Ray rayVbVa = new Ray(Vb, Va);
      Point[] isec1 = rayVaVb.getPointClosestToBase(polygon.intersect(rayVaVb));
      Point[] isec2 = rayVbVa.getPointClosestToBase(polygon.intersect(rayVbVa));
      // if vx/vy exists and is no point of polygon
      if (isec1 != null && !clonePoints.contains(isec1[0]))
        insertTripleIntoPolygon(clonePoints, isec1);
      if (isec2 != null && !clonePoints.contains(isec2[0]))
        insertTripleIntoPolygon(clonePoints, isec2);      
      
      
      /* c. beginning with Va.next determine vertices(running variable vi)
      visible from both Va and Vb. maintain point last visible from both Va 
      and Vb.*/
      
      
      Point lastVisible = Va;
      ListIterator<Point> cloneIter = clonePoints.listIterator(clonePoints.indexOf(Va));
      
      while(!clonePoints.get(cloneIter.nextIndex()).equals(Vb)) {
        
        Point vi = cloneIter.next();
        
        System.out.println("minorLoop1, visit all vertices");
        System.out.println("va: " + Va + ", vb: " + Vb + ", vi: " + vi);
        System.out.println("clonePoints: " + clonePoints);
        
        // visibility of vi form Va and Vb
        //direct test
        boolean fromVa = 
            GeneratorUtils.isPointOnPolygonVisible(Va, vi, polygon);
        boolean fromVb =
            GeneratorUtils.isPointOnPolygonVisible(Vb, vi, polygon);
        ListIterator<Point> prevIter = clonePoints.listIterator(clonePoints.indexOf(vi));
        Point viPrev = prevIter.previous();
        if(fromVa && MathUtils.checkOrientation(Va, vi, viPrev) == 1)
          fromVa = false;
        if(fromVb && MathUtils.checkOrientation(Vb, vi, viPrev) == 1)
          fromVb = false;
        
        // visibility of vi.prev (from P') from Va and Vb
        // therefore iterate backwards over clone and search for first point in
        // polygon (clone and polygon differ by now). this point is viPrev.
        boolean prevFromVa = false;
        boolean prevFromVb = false;
        prevIter = clonePoints.listIterator(clonePoints.indexOf(vi));
        Point prev;
        do {
          prev = prevIter.previous();
          System.out.println("minorLoop2, find previous:" + prev);
        } while(!polygonPoints.contains(prev));
        
        prevFromVa = GeneratorUtils.isPointOnPolygonVisible(prev, Va, polygon);
        prevFromVb = GeneratorUtils.isPointOnPolygonVisible(prev, Va, polygon);
        
        System.out.println("after minorLoop2");
        System.out.println("found current: " + prev);
        System.out.println("fromVa: " + fromVa + ", fromVb: " + fromVb + ", prevFromVa: " + prevFromVa + ", prevFromVa: " + prevFromVb);
        
        // vi not visible from va and vb
        if (!fromVa || !fromVb) {
          System.out.println("not visible form va, vb: remove vi");
          cloneIter.remove();
        }
        // vi visible from va and vb
        else if (fromVa && fromVb) {
          System.out.println("visible from va and vb");
          // case 1 viPrev visible from va and vb
          if (prevFromVa && prevFromVb) {
            System.out.println("case 1: set last visible");
            lastVisible = vi;
          }
          // case 2 viPrev not visible from va and vb
          if (!prevFromVa && !prevFromVb) {
            System.out.println("case 2: 4 intersections, keep 2");
            Ray r1 = new Ray(Va, lastVisible);
            Ray r2 = new Ray(Vb, lastVisible);
            Ray r3 = new Ray(Va, vi);
            Ray r4 = new Ray(Vb, vi);

            Point[] u1 = r1.getPointClosestToBase(polygon.intersect(r1));
            Point[] u2 = r2.getPointClosestToBase(polygon.intersect(r2));
            Point[] u3 = r3.getPointClosestToBase(polygon.intersect(r3));
            Point[] u4 = r4.getPointClosestToBase(polygon.intersect(r4));

            if (u1 != null &&
                GeneratorUtils.isPointOnPolygonVisible(Va, u1[0], polygon) &&
                GeneratorUtils.isPointOnPolygonVisible(Vb, u1[0], polygon) &&
                !clonePoints.contains(u1[0])) {
              insertTripleIntoPolygon(clonePoints, u1);
            }
            if (u2 != null &&
                GeneratorUtils.isPointOnPolygonVisible(Va, u2[0], polygon) &&
                GeneratorUtils.isPointOnPolygonVisible(Vb, u2[0], polygon) &&
                !clonePoints.contains(u2[0])) {
              insertTripleIntoPolygon(clonePoints, u2);
            }
            if (u3 != null &&
                GeneratorUtils.isPointOnPolygonVisible(Va, u3[0], polygon) &&
                GeneratorUtils.isPointOnPolygonVisible(Vb, u3[0], polygon) &&
                !clonePoints.contains(u3[0])) {
              insertTripleIntoPolygon(clonePoints, u3);
            }
            if (u4 != null &&
                GeneratorUtils.isPointOnPolygonVisible(Va, u4[0], polygon) &&
                GeneratorUtils.isPointOnPolygonVisible(Vb, u4[0], polygon) &&
                !clonePoints.contains(u4[0])) {
              insertTripleIntoPolygon(clonePoints, u4);
            }
            lastVisible = vi;
          }
          // case 3+4 viPrev visible to one of va and vb
          else if (prevFromVa || prevFromVb) {
            System.out.println("case 3: from va visible, 2 intersections");
            Point vx;
            if(prevFromVa)
              vx = Va;
            else
              vx = Vb;
            
            Ray r1 = new Ray(vx, lastVisible);
            Ray r2 = new Ray(vx, vi);

            Point[] u1 = r1.getPointClosestToBase(polygon.intersect(r1));
            Point[] u2 = r2.getPointClosestToBase(polygon.intersect(r2));

            if (u1 != null &&
                GeneratorUtils.isPointOnPolygonVisible(Va, u1[0], polygon) &&
                GeneratorUtils.isPointOnPolygonVisible(Vb, u1[0], polygon) && 
                !clonePoints.contains(u1[0])) {
              insertTripleIntoPolygon(clonePoints, u1);
            }
            if (u2 != null &&
                GeneratorUtils.isPointOnPolygonVisible(Va, u2[0], polygon) &&
                GeneratorUtils.isPointOnPolygonVisible(Vb, u2[0], polygon) && 
                !clonePoints.contains(u2[0])) {
              insertTripleIntoPolygon(clonePoints, u2);
            }
          }
        }
        cloneIter = clonePoints.listIterator(clonePoints.indexOf(vi));
        System.out.println("end of minorLoop1 \n");
      }
      return clone;
    }
    
    private boolean insertTripleIntoPolygon(CircularList<Point> list, Point[] triple){
      int indexPoint1 = list.indexOf(triple[1]);
      int indexPoint2 = list.indexOf(triple[2]);
      if (indexPoint1 == -1 || indexPoint2 == -1)
        return false;
      else if(indexPoint1 > indexPoint2)
        list.add(indexPoint1, triple[0]);
      else 
        list.add(indexPoint2, triple[0]);
      return true;
    }

    @Override
    public void stop() {
      dostop = true;
    }
  }
}
