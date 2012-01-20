package polygonsSWP.generators.heuristics;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import polygonsSWP.generators.IllegalParameterizationException;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.data.PolygonHistory;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.util.GeneratorUtils;
import polygonsSWP.util.MathUtils;
import polygonsSWP.util.SteadyGrowthConvexHull;


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
    private PolygonHistory steps;
    private boolean doStop = false;
    private PolygonStatistics stats = null;

    private int initializeRejections = 0;
    private int maximumRejections = 0;
    private int rejections = 0;
    private int runs = 0;

    public SteadyGrowth(List<Point> points, PolygonHistory steps,
        PolygonStatistics stats) {
      this.points = points;
      // this.points = new ArrayList<Point>(Arrays.asList(
      // new Point(0, 0), new Point(10, 0),
      // new Point(20, 30), new Point(40, 30),
      // new Point(30, 35), new Point(30, 55),
      // new Point(40, 50), new Point(50, 50),
      // new Point(60, 10), new Point(70, 20)));
      // boolean b = GeneratorUtils.isInGeneralPosition(this.points);
      // System.out.println(b ? "general pos" : "not in general pos");
      this.steps = steps;
      this.stats = stats;
    }

    @Override
    public Polygon generate() {
      int called = 0;

      synchronized (SteadyGrowthFactory.class) {
        called = calls++;
      }

      System.out.println(called + ".: started generation");
      System.out.println("points: " + points);

      Polygon polygon = null;
      try {
        polygon = generate0();
      }
      catch (InterruptedException e) {}
      catch (RuntimeException e) {
        e.printStackTrace();
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
        // System.out.println("insertIndex: " + insertIndex);
        polygon.add(insertIndex, a);
      }

      return new OrderedListPolygon(polygon);
    }

    private int getIndexOfVisibleEdge(ArrayList<Point> points, Point a) {
      OrderedListPolygon polygon = new OrderedListPolygon(points);

      Point /* first, last, */b;
      boolean lastVisible = false, visible = false;

      for (int i = 0, size = points.size(); i <= size; i++) {
        lastVisible = visible;

        b = points.get(i % size);
        visible = GeneratorUtils.isPolygonPointVisible(b, a, polygon);
        // System.out.println(b + " -> " + a + "; visible: " + visible +
        // "; lastVisible: " + lastVisible);

        if (!lastVisible || !visible) continue;
        return i;
      }

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
          break;
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
        if (hull.containsPoint(point)) return point;
      }

      return null;
    }

    @Override
    public void stop() {
      doStop = true;
    }
  }
}
