package polygonsSWP.generators.heuristics;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import polygonsSWP.generators.IllegalParameterizationException;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.SteadyGrowthConvexHull;
import polygonsSWP.geometry.Triangle;
import polygonsSWP.data.History;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.data.Scene;
import polygonsSWP.util.GeneratorUtils;
import polygonsSWP.util.MathUtils;
import polygonsSWP.util.Random;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public class SteadyGrowthFactory
  implements PolygonGeneratorFactory
{

  @Override
  public boolean acceptsUserSuppliedPoints() {
    return true;
  }

  @Override
  public List<Parameters> getAdditionalParameters() {
    return new LinkedList<Parameters>();
  }

  @Override
  public String toString() {
    return "SteadyGrowth";
  }

  @Override
  public PolygonGenerator createInstance(Map<Parameters, Object> params,
      PolygonStatistics stats, History steps)
    throws IllegalParameterizationException {

    List<Point> points = GeneratorUtils.createOrUsePoints(params);
    return new SteadyGrowth(points, steps, stats);
  }


  private static class SteadyGrowth
    implements PolygonGenerator
  {

    private ArrayList<Point> points;
    final private History steps;
    private boolean doStop = false;
    private PolygonStatistics stats = null;
    private Random rand = new Random();

    private int initializeRejections = 0;
    private int maximumRejections = 0;
    private int rejections = 0;
    private int runs = 0;
//    private long lookupTime = 0;
//    private int lookupTimes = 0;
//    private long visibleEdgeTime = 0;
//    private int visibleEdgeTimes = 0;

    private final Color OLD_HULL = Color.LIGHT_GRAY;
    private final Color POLYGON_HULL = new Color(0xDCDCDC);
//    private final Color VISIBLE_EDGE = Color.MAGENTA;
    private final Color CHOOSEN_VISIBLE_EDGE = new Color(0xE0115F);
    private final Color POINT_IN_HULL = Color.RED;
    private final Color BLACKLISTED_POINTS = Color.CYAN;
    private final Color NEW_EDGE_POINT = Color.GREEN;
//    private final Color VALID_HULL = Color.GREEN;

    public SteadyGrowth(List<Point> points, History steps,
        PolygonStatistics stats) {
      this.points = new ArrayList<Point>(points);
      this.steps = steps;
      this.stats = stats;
    }

    private Scene newScene(Polygon polygon){
      return newScene(polygon, null);
    }

    private Scene newScene(Polygon polygon, Color color){
      Scene scene = steps.newScene();
      if(polygon == null) return scene;
      if(color == null) return scene.addPolygon(polygon, true);

      return scene.addPolygon(polygon, color);
    }

    private class BlackList implements Iterable<Point>{
      public int size;
      public ArrayList<Point> points;

      private Random rand = new Random();

      public BlackList(ArrayList<Point> points){
        this.points = points;
        reset();
      }

      public void reset(){
        size = points.size();
      }

      public Point nextRandomAndRemove(){
        nextRandom();
        return remove();
      }

      public Point nextRandom(){
        int nextIndex = rand.nextInt(size);

        swap(nextIndex, size - 1);
        swap(points.size() - 1, size - 1);
        size--;

        return points.get(points.size() - 1);
      }

      private void blacklist(int index) {
        swap(index, size - 1);
        swap(points.size() - 2, size - 1);
        size--;
      }

      public List<Point> avaiblePoints(){
        if(size <= 0) return null;

        return points.subList(0 , size);
      }

      public List<Point> blacklistedPoints(){
        if(size >= points.size()) return null;

        return points.subList(size , points.size());
      }

      public void swap(int i, int j){
        points.set(j, points.set(i, points.get(j)));
      }

      public Point remove(){
        Point a = points.remove(points.size() - 1);
        return a;
      }

      @Override
      public Iterator<Point> iterator() {
        return new Iterator<Point>() {
          public int curr = -1;

          @Override
          public boolean hasNext() {
            return size > 0 && curr < size - 1;
          }

          @Override
          public Point next() {
            if(!hasNext()) throw new NoSuchElementException();
            Point a = points.get(++curr);
            return a;
          }

          @Override
          public void remove() {
            blacklist(curr--);
          }
        };
      }

    }

    @Override
    public Polygon generate() {

      if (steps != null) {
        steps.clear();
      }

      Polygon polygon = null;
      try {
        polygon = generate0();
      }
      catch (InterruptedException e) {}
      catch (RuntimeException e) {
        e.printStackTrace();
      }

      if (steps != null) {
        newScene(polygon).save();
      }

      if(stats != null){
        stats.iterations = runs;
        stats.rejections = rejections;
        stats.maximumRejections = maximumRejections;
        stats.initializeRejections = initializeRejections;

//        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
//        System.out.println("iterations: " + runs);
//        System.out.println("rejections: " + rejections);
//        System.out.println("maximumRejections: " + maximumRejections);
//        System.out.println("initializeRejections: " + initializeRejections);
//        System.out.println("seq search: " + SteadyGrowthConvexHull.seqSearches);
//        System.out.println("lookup time: " + lookupTime);
//        System.out.println("lookups: " + lookupTimes);
//        System.out.println("ratio: " + (lookupTime / lookupTimes));
//        System.out.println("visible edge time: " + visibleEdgeTime);
//        System.out.println("visible edges: " + visibleEdgeTimes);
//        System.out.println("ratio: " + (visibleEdgeTime / visibleEdgeTimes));

        SteadyGrowthConvexHull.seqSearches = 0;
      }

      return polygon;
    }

    private Object[] getNextPointAndHull(
        List<Point> polygon, SteadyGrowthConvexHull hull, BlackList blacklist) {

      SteadyGrowthConvexHull copy = (SteadyGrowthConvexHull) hull.clone();

      int rejected = 0;
      Point randomPoint = null;

      while(blacklist.size > 0){
        hull = copy;
        copy = (SteadyGrowthConvexHull) hull.clone();

        randomPoint = blacklist.nextRandom();

        int insertIndex = hull.addPointReturnAndInsertIndex(randomPoint);
        Triangle triangle = constructTriangle(hull, insertIndex);
        blacklistPoints(triangle, blacklist);

        /**
         * VISUALISATION
         */
        if( steps != null ) {
          Polygon poly = new OrderedListPolygon(polygon);
          newScene(hull, OLD_HULL)
          .addPolygon(copy, POLYGON_HULL)
          .addPolygon(poly, true)
          //.addPoints(blacklist.blacklistedPoints(), BLACKLISTED_POINTS)
          .addPoints(blacklist.avaiblePoints(), POINT_IN_HULL)
          .addPoint(randomPoint, NEW_EDGE_POINT)
          .save();
        }

        rejections++;
        rejected++;
      }
      blacklist.remove();
      blacklist.reset();

      maximumRejections = Math.max(rejected, maximumRejections);

      return new Object[]{hull, randomPoint};
    }

    private Polygon generate0()
      throws InterruptedException {

      BlackList blacklist = new BlackList(points);

      SteadyGrowthConvexHull hull = initialize(blacklist);

      ArrayList<Point> polygon = new ArrayList<Point>(points.size());
      polygon.addAll(hull.getPoints());

      while (points.size() > 0) {
        if (doStop) throw new InterruptedException();

        runs++;

//        long start = System.nanoTime();
        Object[] rets = getNextPointAndHull(polygon, hull, blacklist);
//        lookupTime = System.nanoTime() - start;
//        lookupTimes++;

        hull = (SteadyGrowthConvexHull) rets[0];
        Point randomPoint = (Point)rets[1];

        // waehlen einen zufaelligen startpunkt aus
//        start = System.nanoTime();
        int startIndex = rand.nextInt(polygon.size());
        int insertIndex = getIndexOfVisibleEdge(polygon, randomPoint, startIndex);
//        visibleEdgeTime = System.nanoTime() - start;
//        visibleEdgeTimes++;

        /**
         * VISUALISATION
         */
        if( steps != null ) {
          Point pk = polygon.get(MathUtils.modulo(insertIndex-1, polygon.size())),
                pl = polygon.get(insertIndex);

          Polygon poly = new OrderedListPolygon(polygon);
          Scene scene = newScene(hull, POLYGON_HULL)
          .addPolygon(poly, true)
          .addLineSegment(new LineSegment(pk, pl), CHOOSEN_VISIBLE_EDGE)
          .addPoint(randomPoint, NEW_EDGE_POINT)
          .addPoints(blacklist.blacklistedPoints(), BLACKLISTED_POINTS);

//          int i = insertIndex;
          // zeichne alle waehlbaren kanten
//          do{
//            i = getIndexOfVisibleEdge(polygon, randomPoint, i);
//            if(insertIndex == i) break;
//
//            pk = polygon.get(MathUtils.modulo(i - 1, polygon.size()));
//            pl = polygon.get(i);
//            scene.addLineSegment(new LineSegment(pk, pl), VISIBLE_EDGE);
//          } while(true);

          scene.save();
        }

        polygon.add(insertIndex, randomPoint);
      }

      return new OrderedListPolygon(polygon);
    }

    private int getIndexOfVisibleEdge(ArrayList<Point> points, Point a, int start) {
      OrderedListPolygon polygon = new OrderedListPolygon(points);

      Point base;
      boolean lastVisible = false, visible = false;

      int size = points.size();
      for (int i = start % size, k = 0; k <= size; i = (i+1) % size, ++k) {
        lastVisible = visible;

        base = points.get(i);
        visible = GeneratorUtils.isPolygonVertexVisible(base, a, polygon);

        if (!lastVisible || !visible) continue;
        return i;
      }

      // Nach dem Paper von Held gibt es immer eine Kante, die sichtbar ist
      throw new RuntimeException("steady-growth: should not happen");
    }

    private SteadyGrowthConvexHull initialize(BlackList blacklist)
      throws InterruptedException {

      SteadyGrowthConvexHull hull = new SteadyGrowthConvexHull();

      // select randomly the first two points
      Point a = blacklist.nextRandomAndRemove(),
            b = blacklist.nextRandomAndRemove();

      hull.addPoint(a);
      hull.addPoint(b);

      Object[] rets = getNextPointAndHull(hull.getPoints(), hull, blacklist);

      blacklist.reset();

      initializeRejections = rejections;

      return (SteadyGrowthConvexHull) rets[0];
    }

    private void blacklistPoints(Polygon triangle, BlackList blackList) {
      if(triangle == null) return;

      Iterator<Point> iter = blackList.iterator();

      while (iter.hasNext()) {
        Point point = iter.next();

        if(triangle.containsPoint(point, true)) continue;

        iter.remove();
      }
    }

    private Triangle constructTriangle(Polygon hull, int insertIndex){
      if(insertIndex < 0) return null;

      return new Triangle(
          hull.getPointInRange(insertIndex - 1),
          hull.getPointInRange(insertIndex),
          hull.getPointInRange(insertIndex + 1)
      );
    }

    @Override
    public void stop() {
      doStop = true;
    }
  }
}
