package polygonsSWP.generators.rpa;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;

import polygonsSWP.data.History;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.data.Scene;
import polygonsSWP.generators.IllegalParameterizationException;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.geometry.Ray;
import polygonsSWP.geometry.Triangle;
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
      History steps)
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
    private final History steps;
    private final PolygonStatistics statistics;
    
    RandomPolygonAlgorithm(int n, int size, History steps, PolygonStatistics statistics) {
      this._n = n;
      this._size = size;
      this.steps = steps;
      this.statistics = statistics;
    }

    @Override
    public Polygon generate() {

      Random random = new Random(System.currentTimeMillis());

      // 1. generate 3 rand points -> polygon P
      // TODO: nicer way to choose points
      OrderedListPolygon polygon =
          new OrderedListPolygon(GeneratorUtils.createRandomSetOfPointsInSquare(
              3, _size, true));
     
      while (polygon.isClockwise() >= 0){
        polygon =
            new OrderedListPolygon(GeneratorUtils.createRandomSetOfPointsInSquare(
                3, _size, true));
      }
      
      List<Point> polygonPoints = polygon.getPoints();
      
      if (steps != null) {
        Scene scene = steps.newScene();
        scene.addPolygon(polygon, true);
        scene.save();          
      }

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
            visiblePolygonRegionFromLineSegment(polygon, Va, Vb, steps);
        
        
        System.out.println("visible region: " + visibleRegion.getPoints() + "\n");
        if (steps != null) {
          Scene scene = steps.newScene();
          scene.addPolygon(polygon, true);
          scene.addPolygon(visibleRegion, Color.GREEN);
          scene.save();          
        }
        
        
        // 2.c randomly select point Vc in P'
        
        Scene scene2 = null;
        if (steps != null) {
          scene2 = steps.newScene();
          scene2.addPolygon(polygon, Color.LIGHT_GRAY);
        }
        
        Triangle selectedTriangle;
        if (visibleRegion.size() > 3) {
          List<Triangle> triangles = ((OrderedListPolygon)visibleRegion).triangulate();
          System.out.println("Triangulation: ");
          for (Triangle triangle : triangles) {
            System.out.println(triangle.getPoints());
            if (steps != null) {
              scene2.addPolygon(triangle, false);
            }
          }
          selectedTriangle = Triangle.selectRandomTriangleBySize(triangles);
          System.out.println("selectedTriangle: " + selectedTriangle);
          if(steps != null){
            scene2.addPolygon(new OrderedListPolygon(selectedTriangle.getPoints()), Color.GRAY);
          }
        } else {
          selectedTriangle = new Triangle(visibleRegion.getPoints());
          System.out.println("SelectedRegion is whole polygon.");
          if (steps != null){
            scene2.addPolygon(new OrderedListPolygon(selectedTriangle.getPoints()), Color.LIGHT_GRAY);
          }
        }
        Point randomPoint = selectedTriangle.createRandomPoint();
        System.out.println("random point: " + randomPoint);
        if (scene2 != null){
          scene2.addPoint(randomPoint, true);
          scene2.save();
        }
        
        
        // 2.d add line segments VaVc and VcVb (delete line segment VaVb)
        polygonPoints.add((randomIndex + 1) % polygonPoints.size(), randomPoint);
        System.out.println("new polygon" + polygon.getPoints());
        System.out.println("-----------------\n");
        
        
        if (steps != null) {
          Scene scene = steps.newScene();
          scene.addPolygon(polygon, true);
          scene.save();          
        }
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
        Point va, Point vb, History steps) {
      
      CircularList<Point> polygonPoints = new CircularList<Point>();
      polygonPoints.addAll(polygon.getPoints());
      ListIterator<Point> polygonIter = polygonPoints.listIterator();
      
      System.out.println("va, vb: " + va + vb + "\n");
      
      
      /* a. Set clone with polygon.*/
      
      CircularList<Point> clonePoints = new CircularList<Point>();
      clonePoints.addAll(polygon.getPoints());
      
      
      /* b. intersect Line VaVb with clone, take first intersection on each side
      of line, if existent, insert them into clone*/
      
      LineSegment vaVb = new LineSegment(va, vb);
      
      Scene scene = null;
      if(steps !=null){
        scene = steps.newScene();
        scene.addPolygon(polygon, true);
        scene.addLineSegment(vaVb, true);
      }
      
      Ray rayVaVb = new Ray(va, vb);
      Ray rayVbVa = new Ray(vb, va);
      Point[] isec1 = polygon.firstIntersection(rayVaVb);
      Point[] isec2 = polygon.firstIntersection(rayVbVa);
      System.out.println("extending vavb");
      // if vx/vy exists and is no point of polygon
      if (isec1 != null && !clonePoints.contains(isec1[0])){
        insertTripleIntoPolygon(clonePoints, isec1);
        System.out.println("inserting " + isec1[0]);
        if(scene != null){
          scene.addPoint(isec1[0], true);
        }
      }
      if (isec2 != null && !clonePoints.contains(isec2[0])){
        insertTripleIntoPolygon(clonePoints, isec2); 
        System.out.println("inserting " + isec2[0]);
        scene.addPoint(isec2[0], true);
      }
      System.out.println("clone points after extension: " + clonePoints);
      if(scene != null){
        scene.save();
      }
      
      /* c. beginning with Va.next determine vertices(running variable vi)
      visible from both Va and Vb. maintain point last visible from both Va 
      and Vb.*/
      
      
      Point lastVisible = va;
      ListIterator<Point> cloneIter = clonePoints.listIterator(clonePoints.indexOf(va));
      
      while(!clonePoints.get(cloneIter.nextIndex()).equals(vb)) {
        
        Point vi = cloneIter.next();
        
        System.out.println("minorLoop1, visit all vertices");
        System.out.println("clonePoints: " + clonePoints);
        System.out.println("vb: " + vb + ", va: " + va + ", vi: " + vi);
        
        
        
        // visibility of vi form va and vb
        // Test if visible.
        boolean fromVa = isVertexVisibleFromInside(polygon, va, vi);
        boolean fromVb = isVertexVisibleFromInside(polygon, vb, vi);
        boolean prevFromVa = false;
        boolean prevFromVb = false;
        ListIterator<Point> prevIter = clonePoints.listIterator(clonePoints.indexOf(vi));
        Point prev;
        do {
          prev = prevIter.previous();
          System.out.println("minorLoop2, find previous:" + prev);
        } while(!polygonPoints.contains(prev));
        
        //test visibility of previous element of polygon (not clone).
        prevFromVa = isVertexVisibleFromInside(polygon, va, prev);
        prevFromVb = isVertexVisibleFromInside(polygon, vb, prev);
        
        System.out.println("after minorLoop2");
        System.out.println("found prev: " + prev);
        System.out.println("fromVa: " + fromVa + ", fromVb: " + fromVb + ", prevFromVa: " + prevFromVa + ", prevFromVb: " + prevFromVb);
        
        if(steps != null){
          scene = steps.newScene();
          scene.addPolygon(polygon, true);
          scene.addLineSegment(vaVb, true);
          if (fromVa && fromVb)
            scene.addPoint(vi, Color.GREEN);
          else if (fromVa && !fromVb)
            scene.addPoint(vi, Color.ORANGE);
          else if (!fromVa && fromVb)
            scene.addPoint(vi, Color.PINK);
          else
            scene.addPoint(vi, Color.RED);
          
          if (prevFromVa && prevFromVb)
            scene.addPoint(prev, Color.GREEN);
          else if (prevFromVa && !prevFromVb)
            scene.addPoint(prev, Color.ORANGE);
          else if (!prevFromVa && prevFromVb)
            scene.addPoint(prev, Color.PINK);
          else
            scene.addPoint(prev, Color.RED);
          scene.save();
        }
        
        // vi not visible from va or vb
        if (!fromVa || !fromVb) {
          System.out.println("not visible form va or vb: remove vi");
          cloneIter.remove();
        }
        // vi visible from va and vb
        if (fromVa && fromVb) {
          System.out.println("visible from va and vb");
          // case 1 viPrev visible from va and vb
          if (prevFromVa && prevFromVb) {
            System.out.println("case 1: set last visible");
            lastVisible = vi;
          }
          // case 2 viPrev not visible from va and vb
          else if (!prevFromVa && !prevFromVb) {
            System.out.println("case 2: 4 intersections, keep 2");
            Ray r1 = new Ray(va, lastVisible);
            Ray r2 = new Ray(vb, lastVisible);
            Ray r3 = new Ray(va, vi);
            Ray r4 = new Ray(vb, vi);

            Point[] u1 = polygon.firstIntersection(r1);
            Point[] u2 = polygon.firstIntersection(r2);
            Point[] u3 = polygon.firstIntersection(r3);
            Point[] u4 = polygon.firstIntersection(r4);

            if (u1 != null &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(va, u1[0], polygon) &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(vb, u1[0], polygon) &&
                !clonePoints.contains(u1[0])) {
              insertTripleIntoPolygon(clonePoints, u1);
            }
            if (u2 != null &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(va, u2[0], polygon) &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(vb, u2[0], polygon) &&
                !clonePoints.contains(u2[0])) {
              insertTripleIntoPolygon(clonePoints, u2);
            }
            if (u3 != null &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(va, u3[0], polygon) &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(vb, u3[0], polygon) &&
                !clonePoints.contains(u3[0])) {
              insertTripleIntoPolygon(clonePoints, u3);
            }
            if (u4 != null &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(va, u4[0], polygon) &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(vb, u4[0], polygon) &&
                !clonePoints.contains(u4[0])) {
              insertTripleIntoPolygon(clonePoints, u4);
            }
            lastVisible = vi;
          }
          // case 3+4 viPrev visible to one of va and vb
          else if (prevFromVa || prevFromVb) {
            System.out.println("case 3 4: from va visible, 2 intersections");
            Point vx;
            if(prevFromVa)
              vx = va;
            else
              vx = vb;
            
            Ray r1 = new Ray(vx, lastVisible);
            Ray r2 = new Ray(vx, vi);

            Point[] u1 = polygon.firstIntersection(r1);
            Point[] u2 = polygon.firstIntersection(r2);

            if (u1 != null &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(va, u1[0], polygon) &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(vb, u1[0], polygon) && 
                !clonePoints.contains(u1[0])) {
              insertTripleIntoPolygon(clonePoints, u1);
            }
            if (u2 != null &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(va, u2[0], polygon) &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(vb, u2[0], polygon) && 
                !clonePoints.contains(u2[0])) {
              insertTripleIntoPolygon(clonePoints, u2);
            }
          }
        }
        System.out.println("end of minorLoop1 \n");
      }
      List<Point> visibleRegionPoints = new ArrayList<Point>();
      while (true) {
        Point next = cloneIter.next();
        if (!visibleRegionPoints.contains(next))
          visibleRegionPoints.add(next);
        else break;
      }
      return new OrderedListPolygon(visibleRegionPoints);
    }
    
    private boolean insertTripleIntoPolygon(CircularList<Point> list, Point[] triple){
      System.out.println("triple to insert: " + triple[0] + triple[1] + triple[2]);    
      int indexPoint1 = list.indexOf(triple[1]);
      int indexPoint2 = list.indexOf(triple[2]);
      if (indexPoint1 == -1 || indexPoint2 == -1)
        return false;

      ListIterator<Point> iter = list.listIterator(indexPoint1);
      if(iter.previousIndex() == indexPoint2)
        iter.add(triple[0]);
      else {
        iter.next();
        iter.add(triple[0]);
      }
        
      return true;
    }
    
    private boolean isVertexVisibleFromInside(Polygon polygon, Point p1, Point p2){
      List<Point> points = polygon.getPoints();
      System.out.println("----------------");
      System.out.println("visibility from inside: ");
      System.out.println("from " + p1 + " to " + p2);
      
      // Test for intersections with polygon.
      boolean visible = 
          GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(p1, p2, polygon);
      
      System.out.println("test for sight: " + visible);
      
      if (visible) {
        
        Point next = points.get((points.indexOf(p1)+1)%points.size());
        Point prev = points.get((points.size() + points.indexOf(p1)-1)%points.size());
        
        if (p2.equals(next) || p2.equals(prev)){
          System.out.println("points are neightbours");
          return true;
        }
        
        // Test if only visible form outside.
        boolean insideLeft = MathUtils.checkOrientation(p1, next, p2) >= 0;
        boolean insideRight = MathUtils.checkOrientation(p1,prev, p2) <= 0;
        
        System.out.println("test if inside 'left': " + insideLeft);
        System.out.println("test if inside 'right': " + insideRight);
        
        return insideLeft && insideRight;
      }
      
      return false;
    }

    @Override
    public void stop() {
      dostop = true;
    }
  }
}
