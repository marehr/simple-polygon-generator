package polygonsSWP.generators.rpa;

import java.awt.Color;
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
import polygonsSWP.util.GeneratorUtils;
import polygonsSWP.util.MathUtils;

/**
 * Random Polygon Algorithm
 *
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 */
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

    if (n < 3) { throw new IllegalParameterizationException(
        "n must be greater or equal 3"); }

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
      boundingBox.addPoint(new Point(0, 0));
      boundingBox.addPoint(new Point(_size, 0));
      boundingBox.addPoint(new Point(_size, _size));
      boundingBox.addPoint(new Point(0, _size));

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

        // test if algorithm should be canceled
        if (dostop) break;

        // 2.a select random line segment VaVb
        // (assumed that there will be less than 2^31-1 points)

        // 2.b determine visible region to VaVb -> P'
        // first determine visible region inside polygon
        // then the outer part

        // index of vb
        int indexVb = random.nextInt(polygon.size());
        int indexVa = (indexVb + 1) % polygon.size();

        Polygon visibleRegionInside =
            visiblePolygonRegionFromLineSegment(polygon, boundingBox, indexVb,
                null);

        Polygon outerPolygon =
            generateOuterPolygon(polygon, boundingBox, indexVb);

        Scene mergeInScene = null;
        if (steps != null) {
          mergeInScene = steps.newScene();
          mergeInScene.addPolygon(polygon, true);
        }

        // index of vb in outer polygon
        int indexVbOuter = outerPolygon.size() - 1;

        Polygon visibleRegionOutside =
            visiblePolygonRegionFromLineSegment(outerPolygon, boundingBox,
                indexVbOuter, mergeInScene);

        Polygon mergedPolygon = mergeInnerAndOuterRegion(visibleRegionInside, visibleRegionOutside);

        assert (visibleRegionInside.isClockwise() <= 0);
        assert (visibleRegionOutside.isClockwise() <= 0);
        assert (outerPolygon.isClockwise() <= 0);

        if (steps != null) {
          Point vb = polygon.getPoint(indexVb);
          Point va = polygon.getPoint(indexVa);

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

          if (steps != null) {
            for (Triangle triangle : triangles) {
              scene2.addPolygon(triangle, Color.LIGHT_GRAY);
            }
          }

          selectedTriangle = Triangle.selectRandomTriangleBySize(triangles);

          if (steps != null) {
            scene2.addPolygon(
                new OrderedListPolygon(selectedTriangle.getPoints()),
                Color.GRAY);
          }
        }
        else {
          selectedTriangle = new Triangle(mergedPolygon.getPoints());
          if (steps != null) {
            scene2.addPolygon(
                new OrderedListPolygon(selectedTriangle.getPoints()),
                Color.LIGHT_GRAY);
          }
        }

        Point randomPoint = selectedTriangle.createRandomPoint();
        if (scene2 != null) {
          scene2.addPoint(randomPoint, true);
          scene2.save();
        }

        // 2.d add line segments VaVc and VcVb (delete line segment VaVb)
        polygonPoints.add((indexVb + 1) % polygonPoints.size(), randomPoint);

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
    private Polygon visiblePolygonRegionFromLineSegment(Polygon polygon,
        Polygon boundingBox, int indexVb, Scene mergeInScene) {

      CircularList<RPAPoint> polyPoints = new CircularList<RPAPoint>();

      for (int i = indexVb + 1, end = i + polygon.size(); i < end; ++i){
        polyPoints.add(new RPAPoint(polygon.getPointInRange(i)));
      }

      @SuppressWarnings("unchecked")
      CircularList<RPAPoint> clonePoints =
          (CircularList<RPAPoint>) polyPoints.clone();

      RPAPoint vb = polyPoints.get(polyPoints.size() - 1);
      RPAPoint va = polyPoints.get(0);

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
        baseScene.addPolygon(polygon,
            mergeInScene == null ? fill : fill.darker());
        baseScene.addPolygon(boundingBox, false);
        baseScene.addLineSegment(vaVb, true);

        scene = steps.newScene();
        scene.mergeScene(baseScene);
      }

      Ray rayVaVb = new Ray(va, vb);
      Ray rayVbVa = new Ray(vb, va);
      Point[] isec1 = polygon.firstIntersection(rayVaVb);
      Point[] isec2 = polygon.firstIntersection(rayVbVa);

      // if vx/vy exists and is no point of polygon
      if (isec1 != null && !clonePoints.contains(isec1[0])) {
        insertIntersectionIntoPolgon(clonePoints, isec1);
        if (scene != null) {
          scene.addPoint(isec1[0], true);
        }
      }

      if (isec2 != null && !clonePoints.contains(isec2[0])) {
        insertIntersectionIntoPolgon(clonePoints, isec2);

        if (scene != null) {
          scene.addPoint(isec2[0], true);
        }
      }

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

        // visibility of vi form va and vb

        vi = checkVisibility(polygon, vi, va, vb);

        // test visibility of previous element of polygon (not clone).

        if (steps != null) {
          scene = steps.newScene();
          scene.mergeScene(baseScene);

          if (vi.visVaIns && vi.visVbIns) scene.addPoint(vi, Color.GREEN);
          else if (vi.visVaIns && !vi.visVbIns) scene.addPoint(vi, Color.ORANGE);
          else if (!vi.visVaIns && vi.visVbIns) scene.addPoint(vi, Color.PINK);
          else scene.addPoint(vi, Color.RED);

          if (prev.visVaIns && prev.visVbIns) scene.addPoint(prev, Color.GREEN);
          else if (prev.visVaIns && !prev.visVbIns) scene.addPoint(prev,
              Color.ORANGE);
          else if (!prev.visVaIns && prev.visVbIns) scene.addPoint(prev,
              Color.PINK);
          else scene.addPoint(prev, Color.RED);
          scene.save();
        }

        if (vi.state == State.IN || vi.state == State.BOTH) {
          // case 1 viPrev visible from va and vb
          if (prev.state == State.IN || prev.state == State.BOTH) {
            lastVisible = vi;
          }
          // case 2 viPrev not visible from va and vb
          else if (!prev.visVaIns && !prev.visVbIns) {
            Ray r1 = new Ray(va, lastVisible);
            Ray r2 = new Ray(vb, lastVisible);
            Ray r3 = new Ray(va, vi);
            Ray r4 = new Ray(vb, vi);

            if (steps != null) {
              scene = steps.newScene();
              scene.mergeScene(baseScene);

              if (!r1._base.equals(r1._support))
                scene.addRay(r1, Color.YELLOW);
              if (!r2._base.equals(r2._support))
                scene.addRay(r2, Color.YELLOW);
              if (!r3._base.equals(r3._support))
                scene.addRay(r3, Color.YELLOW);
              if (!r4._base.equals(r4._support))
                scene.addRay(r4, Color.YELLOW);
            }
            RPAPoint p1 =
                shootRayAndInsertIntersection(polygon, clonePoints, r1, va, vb);
            RPAPoint p2 =
                shootRayAndInsertIntersection(polygon, clonePoints, r2, va, vb);
            RPAPoint p3 =
                shootRayAndInsertIntersection(polygon, clonePoints, r3, va, vb);
            RPAPoint p4 =
                shootRayAndInsertIntersection(polygon, clonePoints, r4, va, vb);

            if (p1 != null) {
              lastVisible = p1;
              if (steps != null) scene.addPoint(p1, Color.GREEN);
            }

            if (p2 != null) {
              lastVisible = p2;
              if (steps != null) scene.addPoint(p2, Color.GREEN);
            }

            if (p3 != null) {
              lastVisible = p3;
              if (steps != null) scene.addPoint(p3, Color.GREEN);
            }

            if (p4 != null) {
              lastVisible = p4;
              if (steps != null) scene.addPoint(p4, Color.GREEN);
            }

            lastVisible = vi;

            if (steps != null) {
              scene.save();
            }

          }
          // case 3+4 viPrev visible to one of va and vb
          else if (prev.visVaIns || prev.visVbIns) {

            RPAPoint vx;
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

            RPAPoint p1 =
                shootRayAndInsertIntersection(polygon, clonePoints, r1, va, vb);
            RPAPoint p2 =
                shootRayAndInsertIntersection(polygon, clonePoints, r2, va, vb);

            if (p1 != null) {
              lastVisible = p1;
              if (steps != null) scene.addPoint(p1, Color.GREEN);
            }

            if (p2 != null) {
              lastVisible = p2;
              if (steps != null) scene.addPoint(p2, Color.GREEN);
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
      }

      List<Point> visibleRegionPoints = new ArrayList<Point>();
      for (int i = 0; i < clonePoints.size(); i++) {
        RPAPoint current = clonePoints.get(i);
        if (current.state == State.IN || current.state == State.BOTH ||
            current.state == State.NEW) visibleRegionPoints.add(current);
      }

      return new OrderedListPolygon(visibleRegionPoints);
    }

    /**
     * Intersects given ray and polygon. If there exists an intersection not
     * already in the polygon, test for visibility from va and vb. If the
     * intersection is visible for Va and Vb through the inside of the polygon,
     * insert the intersection. Sort out Intersections with rays from outside
     * polygon.
     * 
     * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
     * @param polygon Original polygon before current iteration.
     * @param clonePoints List currently worked on.
     * @param ray
     * @param va Second point of currently selected edge.
     * @param vb First point of currently selected edge.
     * @return {@link RPAPoint} if inserted new Point into clonePoints, else
     *         null.
     */
    private RPAPoint shootRayAndInsertIntersection(Polygon polygon,
        List<RPAPoint> clonePoints, Ray ray, RPAPoint va, RPAPoint vb) {

      Point[] isec = polygon.firstIntersection(ray);

      if (isec == null) {
        return null;
      }

      if (!isVertexVisibleFromInside(polygon, va, isec[0])) {
        return null;
      }

      if (!isVertexVisibleFromInside(polygon, vb, isec[0])) {
        return null;
      }

      if (clonePoints.contains(isec[0])) return null;

      if (!clonePoints.contains(isec[1]) || !clonePoints.contains(isec[2])) { return null; }

      int supportIndex = clonePoints.indexOf(new RPAPoint(ray._support));
      int index1 = clonePoints.indexOf(new RPAPoint(isec[1]));
      int index2 = clonePoints.indexOf(new RPAPoint(isec[2]));

      // sort out intersections with ray from outside of polygon.
      if ((supportIndex - index1) % clonePoints.size() < (supportIndex - index2) %
          clonePoints.size()) {

        if (MathUtils.checkOrientation(isec[1], isec[2], ray._support) > -1)
        return null;
      }
      else {

        if (MathUtils.checkOrientation(isec[2], isec[1], ray._support) > -1) {
          return null;
        }
      }

      return insertIntersectionIntoPolgon(clonePoints, isec);
    }

    /**
     * Inserts point triple[0] between triple[1] and triple[2]. If there are
     * already points between triple[1] and triple[2], triple[0] is inserted
     * according to its position on the line segment triple[1]triple[2].
     * 
     * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
     * @param clonePoints List<RPAPoints> representing the current clone RPA
     *          works on.
     * @param isec
     * @return true if inserting triple[0] was successful, false otherwise
     */
    private RPAPoint insertIntersectionIntoPolgon(List<RPAPoint> clonePoints,
        Point[] isec) {
      if (!(clonePoints.contains(isec[1]) || !clonePoints.contains(isec[2])))
        return null;

      // find out which index is the lowest
      int index1 = -1;
      int index2 = -1;

      int tempnIdex1 = clonePoints.indexOf(isec[1]);
      int tempnIdex2 = clonePoints.indexOf(isec[2]);

      if (tempnIdex1 < tempnIdex2) {
        index1 = tempnIdex1;
        index2 = tempnIdex2;
      }
      else {
        index1 = tempnIdex2;
        index2 = tempnIdex1;
      }

      // calculate number of points between triple[1] and triple[2]
      // first cc-wise, second c-wise
      // take the shorter path

      int size = clonePoints.size();

      if ((size - index1) - (size - index2) <= index1 + (size - index2)) {
        // path from triple[1] to triple[2] cc-wise shorter
        RPAPoint curr = clonePoints.get(index1);
        ListIterator<RPAPoint> iter = clonePoints.listIterator(index1);
        RPAPoint next = iter.next();
        while (curr != clonePoints.get(index2)) {
          if (new LineSegment(curr, next).containsPoint(isec[0])) {
            RPAPoint newPoint = new RPAPoint(isec[0]);
            clonePoints.add(clonePoints.indexOf(next), newPoint);
            return newPoint;
          }
          curr = next;
          next = iter.next();
        }
      }
      else {
        // path from triple[1] to triple[2] c-wise shorter
        RPAPoint curr = clonePoints.get(index1);
        ListIterator<RPAPoint> iter = clonePoints.listIterator(index1);
        RPAPoint next = iter.previous();
        while (curr != clonePoints.get(index2)) {
          if (new LineSegment(curr, next).containsPoint(isec[0])) {
            RPAPoint newPoint = new RPAPoint(isec[0]);
            clonePoints.add(clonePoints.indexOf(curr), newPoint);
            return newPoint;
          }
          curr = next;
          next = iter.previous();
        }
      }
      return null;
    }

    /**
     * Checks if RPAPoint is simple visible and visible from inside from
     * currently selected edge. Sets related attribute and state of RPAPoint.
     * 
     * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
     * @param polygon
     * @param vi
     * @param va Second point of currently selected edge.
     * @param vb First point of currently selected edge.
     * @return vi with set visibility attributes and state.
     */
    private RPAPoint checkVisibility(Polygon polygon, RPAPoint vi, RPAPoint va,
        RPAPoint vb) {
      if (vi.state != State.NEW) return vi;

      vi.visVa =
          GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(polygon,
              va, vi);
      vi.visVb =
          GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(polygon,
              vb, vi);

      if (vi.visVa) vi.visVaIns = isVertexVisibleFromInside(polygon, va, vi);
      if (vi.visVb) vi.visVbIns = isVertexVisibleFromInside(polygon, vb, vi);

      vi.setState();

      return vi;
    }

    /**
     * Merges the inner and outer visible region to one visible region
     *
     * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
     * @param {@link Polygon} inner
     * @param {@link Polygon} outer
     * @return {@link Polygon} 
     */
    private Polygon mergeInnerAndOuterRegion(Polygon inner, Polygon outer) {
      List<Point> innerPoints = inner.getPoints(),
                  outerPoints = outer.getPoints();

      // NOTICE: outers first point is vb and last point is va
      outerPoints.remove(0);
      outerPoints.remove(outerPoints.size() - 1);

      innerPoints.addAll(outerPoints);
      return new OrderedListPolygon(innerPoints);
    }

    /**
     * Returns the outer region by intersecting the polygon with the line
     * va to vb and the bounding box
     *
     * NOTICE: the first point of the generated polygon is va and the last is vb
     *
     * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
     * @param {@link Polygon} polygon
     * @param {@link Polygon} boundingBox
     * @param int indexVb
     * @return {@link Polygon} the outer polygon
     */
    private Polygon generateOuterPolygon(Polygon polygon, Polygon boundingBox,
        int indexVb) {
      List<Point> polyPoints = polygon.getPoints();

      Point vb = polyPoints.get(indexVb);
      Point va = polyPoints.get((indexVb + 1) % polyPoints.size());

      List<Point> bounds = new LinkedList<Point>(boundingBox.getPoints());

      Ray rayVaVb = new Ray(va, vb);
      Ray rayVbVa = new Ray(vb, va);

      Point[] isecPolygonLeft = polygon.lastIntersection(rayVbVa);
      Point[] isecPolygonRight = polygon.lastIntersection(rayVaVb);

      Point[] isecBoundaryLeft = boundingBox.firstIntersection(rayVbVa), isecBoundaryRight =
          boundingBox.firstIntersection(rayVaVb);

      List<Point> left =
          collectVerticesUntilLastIntersection(polygon, va, isecPolygonLeft,
              isecPolygonRight, isecBoundaryLeft, isecBoundaryRight);
      Collections.reverse(polyPoints);

      List<Point> right =
          collectVerticesUntilLastIntersection(polygon, vb, isecPolygonRight,
              isecPolygonLeft, isecBoundaryRight, isecBoundaryLeft);
      Collections.reverse(polyPoints);

      int isLeft = 1;
      if (left.get(left.size() - 1) == isecBoundaryRight[0]) {
        isLeft = -1;
      }

      // remove checked boundaries
      bounds.remove(isecBoundaryLeft[1]);
      bounds.remove(isecBoundaryLeft[2]);
      bounds.remove(isecBoundaryRight[1]);
      bounds.remove(isecBoundaryRight[2]);

      // adding points of boundary box to new polygon
      Point leftPoint = null;
      Point rightPoint = null;

      if (isLeft * MathUtils.checkOrientation(va, vb, isecBoundaryLeft[1]) > 0) {
        leftPoint = isecBoundaryLeft[1];
      }
      else {
        leftPoint = isecBoundaryLeft[2];
      }

      if (isLeft * MathUtils.checkOrientation(va, vb, isecBoundaryRight[1]) > 0) {
        rightPoint = isecBoundaryRight[1];
      }
      else {
        rightPoint = isecBoundaryRight[2];
      }

      left.add(isLeft == 1 ? leftPoint : rightPoint);

      if (!leftPoint.equals(rightPoint))
        right.add(isLeft == 1 ? rightPoint : leftPoint);

      // if one boundary is still unchecked, check it now
      if (bounds.size() > 0 &&
          isLeft * MathUtils.checkOrientation(va, vb, bounds.get(0)) > 0) {
        left.add(bounds.get(0));
      }

      Collections.reverse(left);

      right.addAll(left);

      return new OrderedListPolygon(right);
    }

    /**
     * Helper method for generateOuterPolygon
     *
     * This method goes from a given startPoint in CCW order and collects all 
     * edges of the polygon until it finds one of intersection points on the
     * boundary
     *
     * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
     * @param polygon
     * @param startPoint
     * @param isecPolygonLeft
     * @param isecPolygonRight
     * @param isecBoundaryLeft
     * @param isecBoundaryRight
     * @return {@link List}
     */
    private List<Point> collectVerticesUntilLastIntersection(Polygon polygon,
        Point startPoint, Point[] isecPolygonLeft, Point[] isecPolygonRight,
        Point[] isecBoundaryLeft, Point[] isecBoundaryRight) {

      List<Point> points = polygon.getPoints();
      int size = points.size();

      ArrayList<Point> list = new ArrayList<Point>(size);
      list.add(startPoint);

      if (isecPolygonLeft == null) {
        list.add(isecBoundaryLeft[0]);
        return list;
      }

      int index = points.indexOf(startPoint);

      while (true) {
        index = (index + 1) % size;
        Point curr = points.get(index);

        list.add(curr);

        if (isecPolygonLeft[1] == curr) {
          list.add(isecPolygonLeft[0]);
          list.add(isecBoundaryLeft[0]);
          return list;
        }

        if (isecPolygonLeft[2] == curr) {
          list.add(isecPolygonLeft[0]);
          list.add(isecBoundaryLeft[0]);
          return list;
        }

        if (isecPolygonRight == null) continue;

        if (isecPolygonRight[1] == curr) {
          list.add(isecPolygonRight[0]);
          list.add(isecBoundaryRight[0]);
          return list;
        }

        if (isecPolygonRight[2] == curr) {
          list.add(isecPolygonRight[0]);
          list.add(isecBoundaryRight[0]);
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

      // Test for intersections with polygon.
      boolean visible =
          GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(polygon,
              p1, p2);

      if (visible) {
        Point next = points.get((points.indexOf(p1) + 1) % points.size());
        Point prev =
            points.get((points.size() + points.indexOf(p1) - 1) % points.size());

        if (next.equals(prev)) {
          return true;
        }

        double angle1 = innerCuttingAngle(next, p1, prev);
        double angle2 = innerCuttingAngle(next, p1, p2);

        if (MathUtils.doubleCompare(angle1, angle2) >= 0) {
          return true;
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
