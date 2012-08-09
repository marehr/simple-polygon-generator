package polygonsSWP.generators.heuristics;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import polygonsSWP.generators.IllegalParameterizationException;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.SteadyGrowthConvexHull2;
import polygonsSWP.geometry.Triangle;
import polygonsSWP.data.History;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.data.Scene;
import polygonsSWP.util.GeneratorUtils;
import polygonsSWP.util.MathUtils;


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

    private final Color OLD_HULL = Color.LIGHT_GRAY;
    private final Color POLYGON_HULL = new Color(0xDCDCDC);
    private final Color VISIBLE_EDGE = Color.MAGENTA;
    private final Color CHOOSEN_VISIBLE_EDGE = new Color(0xE0115F);
    private final Color POINT_IN_HULL = Color.RED;
    private final Color BLACKLISTED_POINTS = Color.CYAN;
    private final Color NEW_EDGE_POINT = Color.GREEN;
    private final Color VALID_HULL = Color.GREEN;

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

    private class BlackList{
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

      public Point getNextPoint(){
        int nextIndex = rand.nextInt(size);
        swap(nextIndex, size - 1);
        swap(points.size() - 1, size - 1);

        size--;
        return points.get(points.size() - 1);
      }

      public List<Point> blacklistedPoints(){
        return points.subList(size , points.size() - 1);
      }

      public void swap(int i, int j){
        points.set(j, points.set(i, points.get(j)));
      }

      public Point remove(){
        Point a = points.remove(points.size() - 1);
        reset();
        return a;
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
      }

      return polygon;
    }

    private Polygon generate0()
      throws InterruptedException {

      SteadyGrowthConvexHull2 hull = initialize(), copy;
      ArrayList<Point> polygon = new ArrayList<Point>(points.size());
      polygon.addAll(hull.getPoints());

      BlackList blacklist = new BlackList(points);

      int rejected = 0;

      while (points.size() > 0) {
        if (doStop) throw new InterruptedException();

        runs++;

        Point randomPoint = blacklist.getNextPoint();
        copy = (SteadyGrowthConvexHull2) hull.clone();

        int insertIndex = hull.addPointReturnAndInsertIndex(randomPoint);

        // sind jetzt irgendwelche punkte in der neuen konvexen huelle?
        // - wenn ja, dann akzeptieren wir den gewaehlten punkt nicht
        // - wenn nein, dann akzeptieren wir den punkt und machen weiter
        Point containsPoint = containsAnyPoint(hull, insertIndex);

        if (containsPoint != null) {

          /**
           * VISUALISATION
           */
          if( steps != null ) {
            Polygon poly = new OrderedListPolygon(polygon);
            newScene(hull, OLD_HULL)
            .addPolygon(copy, POLYGON_HULL)
            .addPolygon(poly, true)
            .addPoint(randomPoint, NEW_EDGE_POINT)
            .addPoint(containsPoint, POINT_IN_HULL)
            .addPoints(blacklist.blacklistedPoints(), BLACKLISTED_POINTS)
            .save();
          }

          rejections++;
          rejected++;

          hull = copy;
          continue;
        }

        // waehlen einen zufaelligen startpunkt aus
        int startIndex = rand.nextInt(polygon.size());
        insertIndex = getIndexOfVisibleEdge(polygon, randomPoint, startIndex);

        /**
         * VISUALISATION
         */
        if( steps != null ) {
          Point pk = polygon.get(MathUtils.modulo(insertIndex-1, polygon.size())),
                pl = polygon.get(insertIndex);

          Polygon poly = new OrderedListPolygon(polygon);
          Scene scene = newScene(hull, OLD_HULL)
          .addPolygon(copy, POLYGON_HULL)
          .addPolygon(poly, true)
          .addLineSegment(new LineSegment(pk, pl), CHOOSEN_VISIBLE_EDGE)
          .addPoint(randomPoint, NEW_EDGE_POINT)
          .addPoints(blacklist.blacklistedPoints(), BLACKLISTED_POINTS);

          int i = insertIndex;
          // zeichne alle waehlbaren kanten
          do{
            i = getIndexOfVisibleEdge(polygon, randomPoint, i);
            if(insertIndex == i) break;

            pk = polygon.get(MathUtils.modulo(i - 1, polygon.size()));
            pl = polygon.get(i);
            scene.addLineSegment(new LineSegment(pk, pl), VISIBLE_EDGE);
          } while(true);

          scene.save();
        }

        blacklist.remove();
        polygon.add(insertIndex, randomPoint);

        maximumRejections = Math.max(rejected, maximumRejections);
        rejected = 0;
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

    private SteadyGrowthConvexHull2 initialize()
      throws InterruptedException {

      SteadyGrowthConvexHull2 hull;
      do {
        if (doStop) throw new InterruptedException();

        Point a = GeneratorUtils.removeRandomPoint(points), b =
            GeneratorUtils.removeRandomPoint(points), c =
            GeneratorUtils.removeRandomPoint(points);

        hull = new SteadyGrowthConvexHull2();
        hull.addPoint(a);
        hull.addPoint(b);
        hull.addPoint(c);

        Point containsPoint = containsAnyPoint(hull, 0);
        if (containsPoint == null) {

          if (steps != null) {
            newScene(hull, VALID_HULL).save();
          }

          break;
        }

        if (steps != null) {
          newScene(hull).addPoint(containsPoint, POINT_IN_HULL).save();
        }

        initializeRejections++;

        points.add(a);
        points.add(b);
        points.add(c);
      }
      while (true);

      return hull;
    }

    private Point containsAnyPoint(SteadyGrowthConvexHull2 hull, int insertIndex) {
      if(insertIndex < 0) return null;
      Triangle triangle = new Triangle(Arrays.asList(
          hull.getPointInRange(insertIndex - 1),
          hull.getPointInRange(insertIndex),
          hull.getPointInRange(insertIndex + 1)
      ));

      for (Point point : points) {
        // NOTE: Wenn ein Punkt genau auf dem Rand der Convexen Huelle
        // liegt, dann wird hier gesagt, dass die Convexe Huelle diesen
        // Punkt nicht beinhaltet, damit diese Funktion null zurueckgibt
        // und dieser Punkt akzeptiert wird, da die Convexe Huelle
        // diesen Punkt einfach verschluckt und sich dadurch nicht aendert.
        if (triangle.containsPoint(point, false)) return point;
      }

      return null;
    }

    @Override
    public void stop() {
      doStop = true;
    }
  }
}
