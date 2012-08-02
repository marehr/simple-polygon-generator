package polygonsSWP.generators.rpa;

import java.awt.Color;
import java.beans.Visibility;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import polygonsSWP.util.Random;

import polygonsSWP.data.History;
import polygonsSWP.data.HistoryScene;
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
import polygonsSWP.gui.generation.PolygonGenerationPanel;
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
    
    if(n<3){throw new IllegalParameterizationException("n must be greater or equal 3");}
    
    Integer size = (Integer) params.get(Parameters.size);
    if (size == null)
      throw new IllegalParameterizationException(
          "Size of bounding box not set.", Parameters.size);

    return new RandomPolygonAlgorithm(n, size, steps, stats);
  }

  private static void debug(Object str){
    int c = PolygonGenerationPanel.counter;
    if(c != 0) return;
    System.out.println(str);
  }

  private static class RandomPolygonAlgorithm
    implements PolygonGenerator
  {
    private boolean dostop = false;
    private int _n;
    private int _size;
    private final History steps;
    private final PolygonStatistics statistics;

    RandomPolygonAlgorithm(int n, int size, History steps,
        PolygonStatistics statistics) {
      this._n = n;
      this._size = size;
      this.steps = steps;
      this.statistics = statistics;
    }

    @Override
    public Polygon generate() {

      Random random = Random.create();
      
      // create bounding box
      OrderedListPolygon boundingBox = new OrderedListPolygon();
      boundingBox.addPoint(new Point(0,0));
      boundingBox.addPoint(new Point(_size,0));
      boundingBox.addPoint(new Point(_size,_size));
      boundingBox.addPoint(new Point(0,_size));

      // 1. generate 3 rand points -> polygon P
      OrderedListPolygon polygon =
          new OrderedListPolygon(
              GeneratorUtils.createRandomSetOfPointsInSquare(3, _size, true));
      // reverse point list if ordered clockwise
      if (polygon.isClockwise() >= 0) {
        Collections.reverse(polygon.getPoints());
      }

      List<Point> polygonPoints = polygon.getPoints();

      if (steps != null) {
        Scene scene = steps.newScene();
        scene.addPolygon(boundingBox, false);
        scene.addPolygon(polygon, true);
        scene.save();
      }

      // 2. n-3 times:
      for (int i = 0; i < _n - 3; i++) {
        debug("main loop:" + i);
        // test if algorithm should be canceled
        if (dostop) break;
        // 2.a select random line segment VaVb
        // (assumed that there will be less than 2^31-1 points)

        // 2.b determine visible region to VaVb -> P'
        //first determine visible region inside polygon
        //then the outer part

        // index von vb
        int indexVb = random.nextInt(polygon.size());
        int indexVa = (indexVb+1) % polygon.size();
        Point vb = polygon.getPoint(indexVb);
        Point va = polygon.getPoint(indexVa);

        // TODO: make va first point in visibleRegionInside and vb last point
        Polygon visibleRegionInside =
            visiblePolygonRegionFromLineSegment(polygon, boundingBox, indexVb, null);

        Polygon outerPolygon = outerPolygon(polygon, boundingBox, indexVb);

        // index von vb
        int index = outerPolygon.size() - 1;

        Scene mergeInScene = null;
        if(steps != null) {
          mergeInScene = steps.newScene();
          mergeInScene.addPolygon(polygon, true);
        }

        Polygon visibleRegionOutside =
          visiblePolygonRegionFromLineSegment(outerPolygon, boundingBox, index, mergeInScene);

        // TODO after: make va first point in visibleRegionInside and vb last point
        // -> remove search for vb in mergeInnerAndOuterRegion
        Polygon mergedPolygon = mergeInnerAndOuterRegion(visibleRegionInside, visibleRegionOutside, vb);

        assert(visibleRegionInside.isClockwise() <= 0);
        assert(visibleRegionOutside.isClockwise() <= 0);
        assert(outerPolygon.isClockwise() <= 0);

        debug("visible region: " + visibleRegionInside.getPoints() + "\n");

        if (steps != null) {
          Scene scene2 = steps.newScene();
          scene2.addPolygon(boundingBox, false);
          scene2.addPolygon(polygon, true);
          scene2.addPolygon(visibleRegionInside, Color.GREEN);
          scene2.addPolygon(outerPolygon, HistoryScene.POLYCOLOR.darker());
          scene2.addPolygon(visibleRegionOutside, Color.GREEN.darker());
          scene2.addLineSegment(new LineSegment(va, vb), true);
          scene2.save();

          Scene scene3 = steps.newScene();
          scene3.addPolygon(boundingBox, false);
          scene3.addPolygon(polygon, true);
          scene3.addPolygon(mergedPolygon, Color.GREEN);
          scene3.save();
        }

        // 2.c randomly select point Vc in P'

        Scene scene2 = null;
        if (steps != null) {
          scene2 = steps.newScene();
          scene2.addPolygon(boundingBox, false);
          scene2.addPolygon(polygon, true);
        }

        Triangle selectedTriangle;
        if (mergedPolygon.size() > 3) {
          List<Triangle> triangles =
              ((OrderedListPolygon) mergedPolygon).triangulate();
          debug("Triangulation: ");
          for (Triangle triangle : triangles) {
            debug(triangle.getPoints());
            if (steps != null) {
              scene2.addPolygon(triangle, Color.LIGHT_GRAY);
            }
          }
          selectedTriangle = Triangle.selectRandomTriangleBySize(triangles);
          debug("selectedTriangle: " + selectedTriangle);
          if (steps != null) {
            scene2.addPolygon(
                new OrderedListPolygon(selectedTriangle.getPoints()),
                Color.GRAY);
          }
        }
        else {
          selectedTriangle = new Triangle(mergedPolygon.getPoints());
          debug("SelectedRegion is whole polygon.");
          if (steps != null) {
            scene2.addPolygon(
                new OrderedListPolygon(selectedTriangle.getPoints()),
                Color.LIGHT_GRAY);
          }
        }
        Point randomPoint = selectedTriangle.createRandomPoint();
        debug("random point: " + randomPoint);
        if (scene2 != null) {
          scene2.addPoint(randomPoint, true);
          scene2.save();
        }

        // 2.d add line segments VaVc and VcVb (delete line segment VaVb)
        polygonPoints.add((indexVb + 1) % polygonPoints.size(), randomPoint);
        debug("new polygon" + polygon.getPoints());
        debug("-----------------\n");

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
     * polygon determined by the Points va and vb and returns a polygon
     * representing this region. It is assumed, that the points in polygon are
     * ordered counterclockwise. In this order, Vb is left from Va
     * (vvvvvVbVavvvv)
     * 
     * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
     * @param polygon
     * @param mergeInScene 
     * @param p1
     * @param p2
     * @return
     */
    private Polygon visiblePolygonRegionFromLineSegment(Polygon polygon, Polygon boundingBox,
        int randomIndex, Scene mergeInScene) {

      CircularList<RPAPoint> polyPoints = new CircularList<RPAPoint>();
      for (Point point : polygon.getPoints()) {
        polyPoints.add(new RPAPoint(point));
      }

      @SuppressWarnings("unchecked")
      CircularList<RPAPoint> clonePoints = (CircularList<RPAPoint>) polyPoints.clone();

      RPAPoint vb = polyPoints.get(randomIndex);
      RPAPoint va = polyPoints.get((randomIndex + 1) % polyPoints.size());
      
      debug("va, vb: " + va + vb + "\n");

      va.visVa = true;
      va.visVb = true;
      va.visVaIns = true;
      va.visVbIns = true;
      va.state = State.BOTH;
      vb.visVa = true;
      vb.visVb = true;
      vb.visVaIns = true;
      vb.visVbIns = true;
      vb.state = State.BOTH;

      /*
       * b. intersect Line VaVb with clone, take first intersection on each side
       * of line, if existent, insert them into clone
       */

      LineSegment vaVb = new LineSegment(va, vb);

      Scene baseScene = null, scene = null;
      if (steps != null) {
        Color fill = HistoryScene.POLYCOLOR;

        baseScene = steps.newScene();
        baseScene.mergeScene(mergeInScene);
        baseScene.addPolygon(polygon, mergeInScene == null ? fill : fill.darker());
        baseScene.addPolygon(boundingBox, false);
        baseScene.addLineSegment(vaVb, true);

        scene = steps.newScene();
        scene.mergeScene(baseScene);
      }

      Ray rayVaVb = new Ray(va, vb);
      Ray rayVbVa = new Ray(vb, va);
      Point[] isec1 = polygon.firstIntersection(rayVaVb);
      Point[] isec2 = polygon.firstIntersection(rayVbVa);
      debug("extending vavb");
      // if vx/vy exists and is no point of polygon
      if (isec1 != null && !clonePoints.contains(isec1[0])) {
        insertTripleIntoPolygon(clonePoints, isec1);
        debug("inserting " + isec1[0]);
        if (scene != null) {
          scene.addPoint(isec1[0], true);
        }
      }
      if (isec2 != null && !clonePoints.contains(isec2[0])) {
        insertTripleIntoPolygon(clonePoints, isec2);
        debug("inserting " + isec2[0]);
        if (scene != null) {
          scene.addPoint(isec2[0], true);
        }
      }
      debug("clone points after extension: " + clonePoints);
      if (scene != null) {
        scene.save();
      }

      /*
       * c. beginning with Va.next determine vertices(running variable vi)
       * visible from both Va and Vb. maintain point last visible from both Va
       * and Vb.
       */

      RPAPoint lastVisible = clonePoints.get(clonePoints.indexOf(va));
      ListIterator<RPAPoint> cloneIter =
          clonePoints.listIterator(clonePoints.indexOf(va));
      ListIterator<RPAPoint> polygonIter =
          polyPoints.listIterator(polyPoints.indexOf(va));
      RPAPoint prev = lastVisible;
      
      int k = 0;

      while (!clonePoints.get(cloneIter.nextIndex()).equals(va)) {
        


        // get new vi
        RPAPoint vi;
        do {
          vi = cloneIter.next();
        }
        while (vi.state == State.DEL || vi.state == State.OUT);

        debug("minorLoop " + (k++) + ", visit all vertices");
        debug("clonePoints: " + clonePoints);
        debug("vb: " + vb + ", va: " + va + ", vi: " + vi);

        // visibility of vi form va and vb
        
        vi = checkVisibility(polygon, vi, va, vb);
            

        // test visibility of previous element of polygon (not clone).

        debug("---------------------");
        debug("prev: " + prev);

        debug("fromVa: " + vi.visVaIns + ", fromVb: " + vi.visVbIns +
            ", prevFromVa: " + prev.visVaIns + ", prevFromVb: " + prev.visVbIns);

        if (steps != null) {
          scene = steps.newScene();
          scene.mergeScene(baseScene);

          if (vi.visVaIns && vi.visVbIns) scene.addPoint(vi, Color.GREEN);
          else if (vi.visVaIns && !vi.visVbIns) scene.addPoint(vi, Color.ORANGE);
          else if (!vi.visVaIns && vi.visVbIns) scene.addPoint(vi, Color.PINK);
          else scene.addPoint(vi, Color.RED);

          if (prev.visVaIns && prev.visVbIns) scene.addPoint(prev, Color.GREEN);
          else if (prev.visVaIns && !prev.visVbIns) scene.addPoint(prev, Color.ORANGE);
          else if (!prev.visVaIns && prev.visVbIns) scene.addPoint(prev, Color.PINK);
          else scene.addPoint(prev, Color.RED);
          scene.save();
        }
        
        if (vi.state == State.IN || vi.state == State.BOTH) {
          debug("visible from va and vb");
          // case 1 viPrev visible from va and vb
          if (prev.state == State.IN || prev.state == State.BOTH) {
            debug("case 1: set last visible");
            lastVisible = vi;
          }
          // case 2 viPrev not visible from va and vb
          else if (!prev.visVaIns && !prev.visVbIns) {
            debug("case 2: viPev not visible form va and vb, 4 intersections, keep 2");
            Ray r1 = new Ray(va, lastVisible);
            Ray r2 = new Ray(vb, lastVisible);
            Ray r3 = new Ray(va, vi);
            Ray r4 = new Ray(vb, vi);

            if (steps != null) {
              scene = steps.newScene();
              scene.mergeScene(baseScene);

              if (r1._base != r1._support) scene.addRay(r1, Color.YELLOW);
              if (r2._base != r2._support) scene.addRay(r2, Color.YELLOW);
              if (r3._base != r3._support) scene.addRay(r3, Color.YELLOW);
              if (r4._base != r4._support) scene.addRay(r4, Color.YELLOW);
            }

            Point[] u1 = polygon.firstIntersection(r1);
            Point[] u2 = polygon.firstIntersection(r2);
            Point[] u3 = polygon.firstIntersection(r3);
            Point[] u4 = polygon.firstIntersection(r4);

            if (u1 != null &&
                isVertexVisibleFromInside(polygon,
                    va, u1[0]) &&
                isVertexVisibleFromInside(polygon,
                    vb, u1[0]) && !clonePoints.contains(u1[0])) {
              insertTripleIntoPolygon(clonePoints, u1);
              debug("inserting: " + u1[0]);
              if (steps != null) scene.addPoint(u1[0], Color.GREEN);
            }
            if (u2 != null &&
                isVertexVisibleFromInside(polygon,
                    va, u2[0]) &&
                isVertexVisibleFromInside(polygon,
                    vb, u2[0]) && !clonePoints.contains(u2[0])) {
              insertTripleIntoPolygon(clonePoints, u2);
              debug("inserting: " + u2[0]);
              if (steps != null) scene.addPoint(u2[0], Color.GREEN);
            }
            if (u3 != null &&
                isVertexVisibleFromInside(polygon,
                    va, u3[0]) &&
                isVertexVisibleFromInside(polygon,
                    vb, u3[0]) && !clonePoints.contains(u3[0])) {
              insertTripleIntoPolygon(clonePoints, u3);
              debug("inserting: " + u3[0]);
              if (steps != null) scene.addPoint(u3[0], Color.GREEN);
            }
            if (u4 != null &&
                isVertexVisibleFromInside(polygon,
                    va, u4[0]) &&
                isVertexVisibleFromInside(polygon,
                    vb, u4[0]) && !clonePoints.contains(u4[0])) {
              insertTripleIntoPolygon(clonePoints, u4);
              debug("inserting: " + u4[0]);
              if (steps != null) scene.addPoint(u4[0], Color.GREEN);
            }
            lastVisible = vi;

            if (steps != null) {
              scene.save();
            }

          }
          // case 3+4 viPrev visible to one of va and vb
          else if (prev.visVaIns || prev.visVbIns) {
            debug("case 3,4: viPrev from va/vb visible, 2 intersections");
            Point vx;
            if (prev.visVaIns) vx = vb;
            else vx = va;

            Ray r1 = new Ray(vx, lastVisible);
            Ray r2 = new Ray(vx, vi);

            if (steps != null) {
              scene = steps.newScene();
              scene.mergeScene(baseScene);

              if (r1._base != r1._support) scene.addRay(r1, Color.YELLOW);
              if (r2._base != r2._support) scene.addRay(r2, Color.YELLOW);
            }

            Point[] u1 = polygon.firstIntersection(r1);
            Point[] u2 = polygon.firstIntersection(r2);
            
            if (u1 != null)
              debug("intersection 2 rays u1[0]: " + u1[0] + ", visible from va: " + isVertexVisibleFromInside(polygon, va, u1[0]) + ", visible from vb: " + isVertexVisibleFromInside(polygon, vb, u1[0]) + ", not in clonePoints: " + !clonePoints.contains(u1[0]));
            if (u2!=null)
              debug("intersection 2 rays u2[0]: " + u2[0]);

            debug("gobba1");
            if (u1 != null &&
                isVertexVisibleFromInside(polygon, va, u1[0]) &&
                isVertexVisibleFromInside(polygon, vb, u1[0]) && 
                !clonePoints.contains(u1[0])) {
              debug("gobba2");
              insertTripleIntoPolygon(clonePoints, u1);
              lastVisible = new RPAPoint(u1[0]);
              debug(clonePoints.indexOf(lastVisible));
              debug("state of inserted point" + lastVisible.state);
              debug("inserting: " + u1[0] + " , also last visible");
              if (steps != null) scene.addPoint(u1[0], Color.GREEN);
            }
            if (u2 != null &&
                isVertexVisibleFromInside(polygon,
                    va, u2[0]) &&
                isVertexVisibleFromInside(polygon,
                    vb, u2[0]) && !clonePoints.contains(u2[0])) {
              insertTripleIntoPolygon(clonePoints, u2);
              lastVisible = new RPAPoint(u2[0]);
              debug("inserting: " + u2[0] + " , also last visible");
              if (steps != null) scene.addPoint(u2[0], Color.GREEN);
            }

            if (steps != null) {
              scene.save();
            }
          }
        }
        // synchronize previous
        if (vi.equals(polyPoints.get(polygonIter.nextIndex()))) {
          prev = polygonIter.next();
        }
        debug("end of minorLoop1 \n");
      }
      List<Point> visibleRegionPoints = new ArrayList<Point>();
      for (int i = 0; i < clonePoints.size(); i++) {
        RPAPoint current = clonePoints.get(i);
        if (current.state == State.IN || current.state == State.BOTH || current.state == State.NEW) visibleRegionPoints.add(current);
      }
      return new OrderedListPolygon(visibleRegionPoints);
    }
    
    
    /**
     * Inserts point triple[0] between triple[1] and triple[2]. If there are
     * already points between triple[1] and triple[2], triple[0] is inserted
     * according to its position on the line segment triple[1]triple[2].
     * 
     * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
     * @param clonePoints List<RPAPoints> representing the current clone RPA
     *          works on.
     * @param triple
     * @return true if inserting triple[0] was successful, false otherwise 
     */
    private boolean insertTripleIntoPolygon(List<RPAPoint> clonePoints,
        Point[] triple) {
      if (!(clonePoints.contains(triple[1]) || !clonePoints.contains(triple[2]))) return false;
      else {
        
        // find out which index is the lowest
        int index1 = -1;
        int index2 = -1;
        
        int tempnIdex1 = clonePoints.indexOf(triple[1]);
        int tempnIdex2 = clonePoints.indexOf(triple[2]);
        
        if (tempnIdex1 < tempnIdex2) {
          index1 = tempnIdex1;
          index2 = tempnIdex2;
        } else {
          index1 = tempnIdex2;
          index2 = tempnIdex1;
        }
        
        // calculate number of points between triple[1] and triple[2]
        // first cc-wise, second c-wise
        // take the shorter path
        
        int size = clonePoints.size();
        
        if ((size - index1) - (size - index2) <= index1 + (size - 4)) {
          // path from triple[1] to triple[2] cc-wise shorter
          RPAPoint curr = clonePoints.get(index1);
          ListIterator<RPAPoint> iter = clonePoints.listIterator(index1);
          RPAPoint next = iter.next();
          while(curr != clonePoints.get(index2)){
            if(new LineSegment(curr, next).containsPoint(triple[0])){
              clonePoints.add(clonePoints.indexOf(next), new RPAPoint(triple[0]));
              return true;
            }
            curr = next;
            next = iter.next();
          }
        } else {
          // path from triple[1] to triple[2] c-wise shorter
          RPAPoint curr = clonePoints.get(index1);
          ListIterator<RPAPoint> iter = clonePoints.listIterator(index1);
          RPAPoint next = iter.previous();
          while(curr != clonePoints.get(index2)){
            if(new LineSegment(curr, next).containsPoint(triple[0])){
              clonePoints.add(clonePoints.indexOf(curr), new RPAPoint(triple[0]));
              return true;
            }
            curr = next;
            next = iter.previous();
          }
        }
      }
      return false;
    }
    
    
    private RPAPoint checkVisibility(Polygon polygon, RPAPoint vi, RPAPoint va, RPAPoint vb){
      if(vi.state != State.NEW)
        return vi;
      
    	vi.visVa = GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(polygon, va, vi);
    	vi.visVb = GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(polygon, vb, vi);
    	
    	if (vi.visVa)
        vi.visVaIns = isVertexVisibleFromInside(polygon, va, vi);
    	if (vi.visVb)
    	  vi.visVbIns = isVertexVisibleFromInside(polygon, vb, vi);
    	
    	vi.setState();
    	
    	return vi;    	
    }

    private Polygon mergeInnerAndOuterRegion(Polygon inner, Polygon outer, Point vb) {
      // TODO: remove duplicated boundary points by va and vb
      // TODO: make it really work
      int indexVb = inner.getPoints().indexOf(vb);
      int indexVa = (indexVb + 1) % inner.size();

//      Point vb = inner.getPoint(indexVb);
//      Point va = inner.getPoint(indexVa);

//      debug("----------------");
//      debug("mergeInnerAndOuter!!");
//      debug("indexVb: " + indexVb);
//      debug("indexVa: " + indexVa);
//      debug("vb: " + vb);
//      debug("va: " + va);
//      debug("inner: " + inner.getPoints());
//      debug("outer: " + outer.getPoints());

      // TODO: remove copying
      ArrayList<Point> innerPoints = new ArrayList<Point>(inner.getPoints()),
                       outerPoints = new ArrayList<Point>(outer.getPoints());

      // NOTICE: outers first point is vb and last point is va
      outerPoints.remove(0);
      outerPoints.remove(outerPoints.size() - 1);

      innerPoints.addAll(indexVa, outerPoints);
      debug("poly: " + innerPoints);
      return new OrderedListPolygon(innerPoints);
    }

    /**
     * WICHTIG: erster Punkt vom Polygon ist va und letzter Punkt vom ist vb
     */
    private Polygon outerPolygon(Polygon polygon, Polygon boundingBox, int randomIndex){
      List<Point> polyPoints = polygon.getPoints();

      Point vb = polyPoints.get(randomIndex);
      Point va = polyPoints.get((randomIndex + 1) % polyPoints.size());

      List<Point> bounds = new LinkedList<Point>(boundingBox.getPoints());

      Ray rayVaVb = new Ray(va, vb);
      Ray rayVbVa = new Ray(vb, va);

      List<Point> left = collectVerticesUntilLastIntersection(polygon, rayVbVa);

      Collections.reverse(polygon.getPoints());

      List<Point> right = collectVerticesUntilLastIntersection(polygon, rayVaVb);
      Collections.reverse(polyPoints);

      debug("right: "+ right + "\nleft: " + left);

      Point[] isecLeft  = boundingBox.firstIntersection(rayVbVa),
              isecRight = boundingBox.firstIntersection(rayVaVb);

      // add intersection points
      left.add(isecLeft[0]);
      right.add(isecRight[0]);

      // remove checked boundaries
      bounds.remove(isecLeft[1]);
      bounds.remove(isecLeft[2]);
      bounds.remove(isecRight[1]);
      bounds.remove(isecRight[2]);

      // adding points of boundary box to new polygon
      Point leftPoint = null;
      Point rightPoint = null;

      if (MathUtils.checkOrientation(va, vb, isecLeft[1]) > 0){
        leftPoint = isecLeft[1];
      } else {
        leftPoint = isecLeft[2];
      }

      if (MathUtils.checkOrientation(va, vb, isecRight[1]) > 0){
        rightPoint = isecRight[1];
      } else {
        rightPoint = isecRight[2];
      }

      left.add(leftPoint);

      if(!leftPoint.equals(rightPoint))
        right.add(rightPoint);

      // if one boundary is still unchecked, check it now
      if(bounds.size() > 0 && MathUtils.checkOrientation(va, vb, bounds.get(0)) > 0) {
        left.add(bounds.get(0));
      }

      Collections.reverse(left);

      debug("right: "+ right + "\nleft: " + left);
      right.addAll(left);
      debug("outer Polygon: " + right);

      return new OrderedListPolygon(right);
    }

    private List<Point> collectVerticesUntilLastIntersection(Polygon polygon, Ray ray) {
      List<Point> points = polygon.getPoints();
      int size = points.size();

      ArrayList<Point> list = new ArrayList<Point>(size);
      list.add(ray._support);

      Point[] lastIsec = polygon.lastIntersection(ray);

      // TODO: REMOVE COMMENT ist doch wrong :D
      // wenn der ray keine Polygon Ecke trifft,
      // dann treffen wir die BoundingBox als naechstes.
      // Um Kolinearitaet im OuterPolygon zu vermeiden, fuegen
      // wir hier NICHT ray._support hinzu, so dass der
      // BoundingBox Schnittpunkt, dass neue va bzw. vb wird.
      if(lastIsec == null){
        return list;
      }

      int index = points.indexOf(ray._support);

      while(true) {
        index = (index + 1) % size;
        Point curr = points.get(index);

        list.add(curr);

        if(lastIsec[1] == curr){
          list.add(lastIsec[2]);
          return list;
        }

        if(lastIsec[2] == curr){
          list.add(lastIsec[1]);
          return list;
        }
      }
    }
    

    /**
     * Determines if one vertex of a polygon can 'see' another point on that
     * polygon. Line segments on the line of sight block sight, excluded end
     * points and collinear line segments. The line of sight must only cross the
     * inside of the polygon for p2 to be visible from p1.
     * 
     * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
     * @param polygon
     * @param p1 Is p2 'visible' from here?
     * @param p2 Can p1 'see' this point?
     * @return
     */
    private boolean isVertexVisibleFromInside(Polygon polygon, Point p1,
        Point p2) {
      List<Point> points = polygon.getPoints();
      debug("----------------");
      debug("visibility from inside: ");
      debug("from " + p1 + " to " + p2);

      // Test for intersections with polygon.
      boolean visible =
          GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(polygon, p1,
              p2);
      debug("test for sight: " + visible);

      if (visible) {
        Point next = points.get((points.indexOf(p1) + 1) % points.size());
        Point prev =
            points.get((points.size() + points.indexOf(p1) - 1) % points.size());

        if (next.equals(prev)) {
          debug("trianlge => visible and inside");
          return true;
        }
        double angle1 = innerCuttingAngle(next, p1, prev);
        double angle2 = innerCuttingAngle(next, p1, p2);
        if (MathUtils.doubleCompare(angle1, angle2) >= 0) {
          debug("test for cutting angle: " + angle1 + " >= " +
              angle2 + " => inside");
          return true;
        }
        else {
          debug("test for cutting angle: " + angle1 + " < " +
              angle2 + " => outside");
        }

      }

      return false;
    }

    /**
     * Calculates angle between to lines. The line l1 is represented by p1 and
     * p2 and seen as orientated form p2 to p1. line l2 is represented by p2 and
     * p3 and oriented form p2 to p3. The inner cutting angle is calculated by
     * starting form the left side of l1 and spinning left until reaching the
     * right side of l2.
     * 
     * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
     * @param p1 2nd point of l1
     * @param p2 1st point of l1 and l2
     * @param p3 2nd point of l2
     * @return cutting angle between l1 and l2 in degree, 0 <= angle < 360
     */
    private double innerCuttingAngle(Point p1, Point p2, Point p3) {
      Line l1 = new Line(p1, p2);
      Line l2 = new Line(p2, p3);
      double angle = l1.cuttingAngle(l2);

      if (MathUtils.checkOrientation(p2, p1, p3) <= -1) {
        if (angle < 0) return 360.0 + angle;
        if (angle > 0) {
          if (MathUtils.doubleEquals(angle, 90.0)) return 270.0;
          else return 180.0 + angle;
        }
      }
      else if (MathUtils.checkOrientation(p2, p1, p3) >= 1) {
        if (angle < 0) return 180.0 + angle;
        if (angle > 0) {
          if (MathUtils.doubleEquals(angle, 90.0)) return 90.0;
          else return angle;
        }
      }
      else {
        if (new Ray(p2, p1).containsPoint(p3)) return 0.0;
        else return 180.0;
      }
      return -1.0;
    }

    @Override
    public void stop() {
      dostop = true;
    }
  }
}
