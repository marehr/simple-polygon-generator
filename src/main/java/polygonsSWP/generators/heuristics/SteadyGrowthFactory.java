package polygonsSWP.generators.heuristics;

import java.awt.Color;
import java.util.ArrayList;
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
import polygonsSWP.geometry.SteadyGrowthConvexHull;
import polygonsSWP.data.PolygonHistory;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.data.Scene;
import polygonsSWP.util.GeneratorUtils;


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
      PolygonStatistics stats, PolygonHistory steps)
    throws IllegalParameterizationException {

    List<Point> points = GeneratorUtils.createOrUsePoints(params);
    return new SteadyGrowth(points, steps, stats);
  }


  private static class SteadyGrowth
    implements PolygonGenerator
  {

    private static int calls = 0;

    private List<Point> points;
    final private PolygonHistory steps;
    private boolean doStop = false;
    private PolygonStatistics stats = null;

    private int initializeRejections = 0;
    private int maximumRejections = 0;
    private int rejections = 0;
    private int runs = 0;

    // TODO: get from parameters
    private int size = 600;

    private final Color OLD_HULL = Color.LIGHT_GRAY;
    private final Color POLYGON_HULL = new Color(0xDCDCDC);
    private final Color VISIBLE_EDGE = new Color(0xE0115F);
    private final Color POINT_IN_HULL = Color.RED;
    private final Color NEW_EDGE_POINT = Color.GREEN;
    private final Color VALID_HULL = Color.GREEN;

    public SteadyGrowth(List<Point> points, PolygonHistory steps,
        PolygonStatistics stats) {
      this.points = points;
      this.steps = steps;
      this.stats = stats;
    }

    private Scene newScene(Polygon polygon){
      return newScene(polygon, null);
    }

    private Scene newScene(Polygon polygon, Color color){
      Scene scene = steps.newScene().setBoundingBox(size, size);
      if(polygon == null) return scene;
      if(color == null) return scene.addPolygon(polygon, true);

      return scene.addPolygon(polygon, color);
    }

    @Override
    public Polygon generate() {
      int called = 0;

      synchronized (SteadyGrowthFactory.class) {
        called = calls++;
      }

      System.out.println(called + ".: started generation");
      System.out.println("points: " + points);

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
        newScene(polygon).safe();
      }

      System.out.println(called + ".: rejections: ");
      System.out.println(called + ".: \tintialize = " + initializeRejections);
      System.out.println(called + ".: \ttotal = " + rejections);
      System.out.println(called + ".: \tmaximum = " + maximumRejections);
      System.out.println(called + ".: while repeated: " + runs);
      System.out.println(called + ".: finished generation");
      System.out.println(called + ".: polygon: " + polygon.getPoints());
      System.out.println();

      return polygon;
    }

    private Polygon generate0()
      throws InterruptedException {

      SteadyGrowthConvexHull hull = initialize(), copy;
      ArrayList<Point> polygon = new ArrayList<Point>(points.size());
      polygon.addAll(hull.getPoints());

      // System.out.println("points: " + points);
      // System.out.println("current hull: " + hull.getPoints());

      Random rand = new Random();

      int rejected = 0;

      while (points.size() > 0) {
        if (doStop) throw new InterruptedException();

        runs++;

        int index = rand.nextInt(points.size());

        Point a = points.get(index);
        copy = (SteadyGrowthConvexHull) hull.clone();

        // System.out.println("\n\n");
        // System.out.println("points: " + points);
        // System.out.println("index: " + index);
        // System.out.println("polygon: " + polygon);
        // System.out.println("add " + a + " to current hull: " +
        // hull.getPoints());
        hull.addPoint(a);
        // System.out.println("current hull: " + hull.getPoints());

        // sind jetzt irgendwelche punkte in der neuen konvexen huelle?
        // - wenn ja, dann akzeptieren wir den gewaehlten punkt nicht
        // - wenn nein, dann akzeptieren wir den punkt und machen weiter
        Point containsPoint = containsAnyPoint(hull);
        if (containsPoint != null) {

          if( steps != null ) {
            Polygon poly = new OrderedListPolygon(polygon);
            newScene(hull, OLD_HULL)
            .addPolygon(copy, POLYGON_HULL)
            .addPolygon(poly, true)
            .addPoint(a, NEW_EDGE_POINT)
            .addPoint(containsPoint, POINT_IN_HULL).safe();
          }

          rejections++;
          rejected++;

          // System.out.println("reject: " + a + "\n\n");
          hull = copy;
          continue;
        }

        maximumRejections = Math.max(rejected, maximumRejections);
        rejected = 0;
        // System.out.println("accept: " + a);

        points.remove(index);

        int insertIndex = getIndexOfVisibleEdge(polygon, a);

        if( steps != null ) {
          Point pk = polygon.get(insertIndex - 1),
                pl = polygon.get(insertIndex % polygon.size());

          Polygon poly = new OrderedListPolygon(polygon);
          newScene(hull, OLD_HULL)
          .addPolygon(copy, POLYGON_HULL)
          .addPolygon(poly, true)
          .addLineSegment(new LineSegment(pk, pl), VISIBLE_EDGE)
          .addPoint(a, NEW_EDGE_POINT).safe();
        }
        // System.out.println("insertIndex: " + insertIndex);
        polygon.add(insertIndex, a);
      }

      return new OrderedListPolygon(polygon);
    }

    private int getIndexOfVisibleEdge(ArrayList<Point> points, Point a) {
      OrderedListPolygon polygon = new OrderedListPolygon(points);

      Point base;
      boolean lastVisible = false, visible = false;

      for (int i = 0, size = points.size(); i <= size; i++) {
        lastVisible = visible;

        base = points.get(i % size);
        visible = GeneratorUtils.isPolygonPointVisible(base, a, polygon);
        // System.out.println(b + " -> " + a + "; visible: " + visible +
        // "; lastVisible: " + lastVisible);

        if (!lastVisible || !visible) continue;
        return i;
      }

      // Nach dem Paper von Held gibt es immer eine Kante, die sichtbar ist
      throw new RuntimeException("steady-growth: should not happen");
    }

    private SteadyGrowthConvexHull initialize()
      throws InterruptedException {

      SteadyGrowthConvexHull hull;
      do {
        if (doStop) throw new InterruptedException();

        Point a = GeneratorUtils.removeRandomPoint(points), b =
            GeneratorUtils.removeRandomPoint(points), c =
            GeneratorUtils.removeRandomPoint(points);

        hull = new SteadyGrowthConvexHull();
        hull.addPoint(a);
        hull.addPoint(b);
        hull.addPoint(c);

        Point containsPoint = containsAnyPoint(hull);
        if (containsPoint == null) {

          if (steps != null) {
            newScene(hull, VALID_HULL).safe();
          }

          break;
        }

        if (steps != null) {
          newScene(hull).addPoint(containsPoint, POINT_IN_HULL).safe();
        }

        initializeRejections++;

        // System.out.println("reject hull: " + hull.getPoints());
        // System.out.println();

        points.add(a);
        points.add(b);
        points.add(c);
      }
      while (true);

      return hull;
    }

    private Point containsAnyPoint(SteadyGrowthConvexHull hull) {
      for (Point point : points) {
        // NOTE: Wenn ein Punkt genau auf dem Rand der Convexen Huelle
        // liegt, dann wird hier gesagt, dass die Convexe Huelle diesen
        // Punkt nicht beinhaltet, damit diese Funktion null zurueckgibt
        // und dieser Punkt akzeptiert wird, da die Convexe Huelle
        // diesen Punkt einfach verschluckt und sich dadurch nicht aendert.
        if (hull.containsPoint(point, false)) return point;
      }

      return null;
    }

    @Override
    public void stop() {
      doStop = true;
    }
  }
}
