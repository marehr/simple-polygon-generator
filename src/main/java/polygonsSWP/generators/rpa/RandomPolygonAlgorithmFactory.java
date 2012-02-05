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
import polygonsSWP.generators.rpa.RPAPoint.State;
import polygonsSWP.geometry.Line;
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
      PolygonStatistics stats, History steps)
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

      Random random = GeneratorUtils.rand_;

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
          scene2.addPolygon(polygon, true);
        }
        
        Triangle selectedTriangle;
        if (visibleRegion.size() > 3) {
          List<Triangle> triangles = ((OrderedListPolygon)visibleRegion).triangulate();
          System.out.println("Triangulation: ");
          for (Triangle triangle : triangles) {
            System.out.println(triangle.getPoints());
            if (steps != null) {
              scene2.addPolygon(triangle, Color.LIGHT_GRAY);
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
      System.out.println("va, vb: " + va + vb + "\n");
      
      
      /* a. Set clone with polygon.*/
      
      CircularList<RPAPoint> clonePoints = new CircularList<RPAPoint>();
      for (Point point : polygonPoints) {
        clonePoints.add(new RPAPoint(point));
      }
      
      
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
      
      
      RPAPoint lastVisible = clonePoints.get(clonePoints.indexOf(va));
      ListIterator<RPAPoint> cloneIter = clonePoints.listIterator(clonePoints.indexOf(va));
      ListIterator<Point> polygonIter = polygonPoints.listIterator(polygonPoints.indexOf(va));
      Point prev = lastVisible;
      
      while(!clonePoints.get(cloneIter.nextIndex()).equals(vb)) {
        
        // get new vi
        RPAPoint vi;
        do{
          vi = cloneIter.next();
        }while(vi.state == State.DEL);
        
        
        
        System.out.println("minorLoop1, visit all vertices");
        System.out.println("clonePoints: " + clonePoints);
        System.out.println("vb: " + vb + ", va: " + va + ", vi: " + vi);
        
        
        // visibility of vi form va and vb
        // Test if visible.
        boolean fromVa = isVertexVisibleFromInside(polygon, va, vi);
        boolean fromVb = isVertexVisibleFromInside(polygon, vb, vi);
        
        //test visibility of previous element of polygon (not clone).
        // synchronize previous
        System.out.println("---------------------");
        System.out.println("prev: " + prev);
        boolean prevFromVa = isVertexVisibleFromInside(polygon, va, prev);
        boolean prevFromVb = isVertexVisibleFromInside(polygon, vb, prev);
        
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
          vi.state = State.DEL;
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
            
            if(steps != null){
              scene = steps.newScene();
              scene.addPolygon(polygon, true);
              scene.addLineSegment(vaVb, true);

              if(r1._base != r1._support)
                scene.addRay(r1, Color.YELLOW);
              if(r2._base != r2._support)
                scene.addRay(r2, Color.YELLOW);
              if(r3._base != r3._support)
                scene.addRay(r3, Color.YELLOW);
              if(r4._base != r4._support)
                scene.addRay(r4, Color.YELLOW);
            }            

            Point[] u1 = polygon.firstIntersection(r1);
            Point[] u2 = polygon.firstIntersection(r2);
            Point[] u3 = polygon.firstIntersection(r3);
            Point[] u4 = polygon.firstIntersection(r4);

            if (u1 != null &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(va, u1[0], polygon) &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(vb, u1[0], polygon) &&
                !clonePoints.contains(u1[0])) {
              insertTripleIntoPolygon(clonePoints, u1);
              System.out.println("inserting: " + u1[0]);
              if(steps != null)
                scene.addPoint(u1[0], Color.GREEN);
            }
            if (u2 != null &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(va, u2[0], polygon) &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(vb, u2[0], polygon) &&
                !clonePoints.contains(u2[0])) {
              insertTripleIntoPolygon(clonePoints, u2);
              System.out.println("inserting: " + u2[0]);
              if(steps != null)
                scene.addPoint(u2[0], Color.GREEN);
            }
            if (u3 != null &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(va, u3[0], polygon) &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(vb, u3[0], polygon) &&
                !clonePoints.contains(u3[0])) {
              insertTripleIntoPolygon(clonePoints, u3);
              System.out.println("inserting: " + u3[0]);
              if(steps != null)
                scene.addPoint(u3[0], Color.GREEN);
            }
            if (u4 != null &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(va, u4[0], polygon) &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(vb, u4[0], polygon) &&
                !clonePoints.contains(u4[0])) {
              insertTripleIntoPolygon(clonePoints, u4);
              System.out.println("inserting: " + u4[0]);
              if(steps != null)
                scene.addPoint(u4[0], Color.GREEN);
            }
            lastVisible = vi;
            
            if (steps != null){
              scene.save();
            }
            
          }
          // case 3+4 viPrev visible to one of va and vb
          else if (prevFromVa || prevFromVb) {
            System.out.println("case 3 4: from va visible, 2 intersections");
            Point vx;
            if(prevFromVa)
              vx = vb;
            else
              vx = va;
            
            Ray r1 = new Ray(vx, lastVisible);
            Ray r2 = new Ray(vx, vi);
            
            if(steps != null){
              scene = steps.newScene();
              scene.addPolygon(polygon, true);
              scene.addLineSegment(vaVb, true);

              if(r1._base != r1._support)
                scene.addRay(r1, Color.YELLOW);
              if(r2._base != r2._support)
                scene.addRay(r2, Color.YELLOW);
            }

            Point[] u1 = polygon.firstIntersection(r1);
            Point[] u2 = polygon.firstIntersection(r2);

            if (u1 != null &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(va, u1[0], polygon) &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(vb, u1[0], polygon) && 
                !clonePoints.contains(u1[0])) {
              insertTripleIntoPolygon(clonePoints, u1);
              lastVisible = new RPAPoint(u1[0]);
              System.out.println("inserting: " + u1[0] + " , also last visible");
              if(steps != null)
                scene.addPoint(u1[0], Color.GREEN);
            }
            if (u2 != null &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(va, u2[0], polygon) &&
                GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(vb, u2[0], polygon) && 
                !clonePoints.contains(u2[0])) {
              insertTripleIntoPolygon(clonePoints, u2);
              lastVisible = new RPAPoint(u2[0]);
              System.out.println("inserting: " + u2[0] + " , also last visible");
              if(steps != null)
                scene.addPoint(u2[0], Color.GREEN);
            }
            
            if (steps != null){
              scene.save();
            }
          }
        }
        if (vi.equals(polygonPoints.get(polygonIter.nextIndex()))) {
          prev = polygonIter.next();
        }
        System.out.println("end of minorLoop1 \n");
      }
      List<Point> visibleRegionPoints = new ArrayList<Point>();
      for (int i = 0; i < clonePoints.size(); i++) {
        RPAPoint current = clonePoints.get(i);
        if(current.state != State.DEL)
          visibleRegionPoints.add(current);
      }
      return new OrderedListPolygon(visibleRegionPoints);
    }
    
    private boolean insertTripleIntoPolygon(List<RPAPoint> clonePoints, Point[] triple){
      if (!(clonePoints.contains(triple[1]) && clonePoints.contains(triple[2])))
          return false;
      else {
        int indx1 = clonePoints.indexOf(triple[1]);
        int indx2 = clonePoints.indexOf(triple[2]);
        List<RPAPoint> sublist;
        if(indx1 < indx2)
          sublist = clonePoints.subList(indx1, indx2+1);
        else
          sublist = clonePoints.subList(indx2, indx1+1);
        for (int i = 0; i < sublist.size()-1; i++) {
          //LineSegment ab = new LineSegment(sublist.get(i), sublist.get(i+1));
          if (new LineSegment(sublist.get(i), sublist.get(i+1)).containsPoint(triple[0])){
            sublist.add(i+1, new RPAPoint(triple[0]));
            return true;
          }
        }
      }
      return false;
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
        
        if(next.equals(prev)){
          System.out.println("trianlge => visible and inside");
          return true;
        }
        double angle1 = innerCuttingAngle(next, p1, prev);
        double angle2 = innerCuttingAngle(next, p1, p2);
        if(angle1 > angle2 || MathUtils.doubleEquals(angle1, angle2)){
          System.out.println("test for cutting angle: " + angle1 + " >= " + angle2 + " => inside");
          return true;
        }
        else {
          System.out.println("test for cutting angle: " + angle1 + " >= " + angle2 + " => outside");
        }
        
      }
      
      return false;
    }
    
    private double innerCuttingAngle(Point p1, Point p2, Point p3){
      Line l1 = new Line(p1, p2);
      Line l2 = new Line(p2, p3);
      double angle = l1.cuttingAngle(l2);
      
      if (MathUtils.checkOrientation(p2, p1, p3) <= -1){
        if (angle < 0)
          return 360.0 + angle;
        if (angle > 0){
          if (MathUtils.doubleEquals(angle, 90.0))
            return 270.0;
          else
            return 180.0 + angle;
        }
      }
      else if(MathUtils.checkOrientation(p2, p1, p3) >= 1){
        if (angle < 0)
          return 180.0 + angle;
        if (angle > 0){
          if (MathUtils.doubleEquals(angle, 90.0))
            return 90.0;
          else
            return angle;
        }
      }
      else{
        if(new Ray(p2, p1).containsPoint(p3))
          return 0.0;
        else
          return 180.0;
      }
      return -1.0;
    }

    @Override
    public void stop() {
      dostop = true;
    }
  }
}
