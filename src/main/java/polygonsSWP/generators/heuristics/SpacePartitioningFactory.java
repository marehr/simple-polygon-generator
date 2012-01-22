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
import polygonsSWP.geometry.Line;
import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Ray;
import polygonsSWP.data.PolygonHistory;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.data.Scene;
import polygonsSWP.util.GeneratorUtils;
import polygonsSWP.util.MathUtils;


public class SpacePartitioningFactory
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
    return "SpacePartitioning";
  }

  @Override
  public PolygonGenerator createInstance(Map<Parameters, Object> params,
      PolygonStatistics stats, PolygonHistory steps)
    throws IllegalParameterizationException {

    List<Point> points = GeneratorUtils.createOrUsePoints(params);

    return new SpacePartitioning(points, steps, stats);
  }


  private static class SpacePartitioning
    implements PolygonGenerator
  {

    // TODO: set size properly
    private int size = 600;
    private Random rand = new Random();

    private List<Point> points;
    private final PolygonHistory steps;
    private boolean doStop = false;
    private PolygonStatistics stats = null;

    /**
     * colors
     */
    private Color LEFT_POINTS = Color.RED;
    private Color RIGHT_POINTS = Color.MAGENTA;
    private Color MIDDLE_POINTS = Color.ORANGE;
    private Color MIDDLE_LINE = Color.ORANGE;
    private Color FIRST_LAST_EDGE = new Color(0x007426);

    private Color LEFT_POLYGON = Color.GRAY;
    private Color RIGHT_POLYGON = Color.LIGHT_GRAY;

    SpacePartitioning(List<Point> points, PolygonHistory steps,
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
      doStop = false;

      if (steps != null) {
        steps.clear();
      }

      Polygon p = null;
      try {
        p = generate0();
      }
      catch (RuntimeException e) {
        e.printStackTrace();
      }
      catch (InterruptedException e) {}

      if (steps != null) {
        newScene(p).save();
      }

      return doStop == true ? null : p;
    }

    private Polygon generate0()
      throws InterruptedException {
      // System.out.println("<------------------------- NEW GENERATE ------------------------->");
      Scene scene = null;

      Point first = GeneratorUtils.removeRandomPoint(points),
             last = GeneratorUtils.removeRandomPoint(points);
      // System.out.println("first: " + first + ", last: "+ last);

      List<Point> left = new ArrayList<Point>(points.size()), right =
          new ArrayList<Point>(points.size());

      partionateIn(left, right, points, first, last);

      if( steps != null ) {
        newScene(null)
        .addPoint(first, true)
        .addPoint(last, true)
        .addLine(new Line(first, last), true)
        .addPoints(left, LEFT_POINTS)
        .addPoints(right, RIGHT_POINTS)
        .save();
      }

      OrderedListPolygon leftPolygon, rightPolygon;

      if( steps != null) {
        scene = newScene(null).addLine(new Line(first, last), true);
      }

      leftPolygon = spacePartitioning(left, first, last, scene);

      if( steps != null) {
        scene = newScene(leftPolygon).addLine(new Line(first, last), true);
      }

      rightPolygon = spacePartitioning(right, last, first, scene);

      if( steps != null ) {
        newScene(null)
        .addPoint(first, true)
        .addPoint(last, true)
        .addLine(new Line(first, last), true)
        .addPolygon(leftPolygon, LEFT_POLYGON)
        .addPolygon(rightPolygon, RIGHT_POLYGON)
        .addPoints(left, LEFT_POINTS)
        .addPoints(right, RIGHT_POINTS)
        .save();
      }

      // System.out.println("\n\n");
      // System.out.println("result in generate");
      // System.out.println("first: " + first);
      // System.out.println("last: "+ last);
      // System.out.println("left: " + leftPolygon.getPoints());
      // System.out.println("right: " + rightPolygon.getPoints());
      OrderedListPolygon merge = merge(leftPolygon, rightPolygon);
      // System.out.println("polygon: " + merge.getPoints());

      if (!merge.isSimple()) {
        String out =
            GeneratorUtils.isInGeneralPosition(merge.getPoints())
                ? "true"
                : "false";
        throw new RuntimeException("general position: " + out + "\n" +
            "generated Polygon is not simple: " + merge.getPoints());
      }
      // System.err.println(merge.isSimple()? "simple" : "not simple");

      return merge;
    }

    private void removeDuplicates(Polygon left, Polygon right) {
      List<Point> leftPoints = left.getPoints(), rightPoints =
          right.getPoints();

      assert leftPoints.size() > 0 && rightPoints.size() > 0;

      // on borders can be duplicated elements, we must remove them
      if (rightPoints.get(0).equals(leftPoints.get(leftPoints.size() - 1))) {
        rightPoints.remove(0);
      }

      assert leftPoints.size() > 0 && rightPoints.size() > 0;

      if (leftPoints.get(0).equals(rightPoints.get(rightPoints.size() - 1))) {
        leftPoints.remove(0);
      }
    }

    private/* String */void partionateIn(List<Point> left, List<Point> right,
        List<Point> points, Point first, Point last)
      throws InterruptedException {

      if (doStop) throw new InterruptedException();

      // String output = "";
      for (Point point : points) {

        int orients = MathUtils.checkOrientation(first, last, point);
        // output += "orients: [" + first + "," + last + "," + point +
        // "]" +
        // (orients < 0 ? "LEFT" : (orients == 0 ? "ONSEGMENT" :
        // "RIGHT")) +
        // "\n";
        if (orients < 0) { // on left-side
          left.add(point);
        }
        else {
          right.add(point);
        }

      }

      // return /*output*/ null;
    }

    private OrderedListPolygon merge(OrderedListPolygon left,
        OrderedListPolygon right) {
      removeDuplicates(left, right);

      left.getPoints().addAll(right.getPoints());
      return left;
    }

    private OrderedListPolygon spacePartitioning(List<Point> points,
        Point first, Point last, Scene leftScene)
      throws InterruptedException {
      Scene oldScene = leftScene;

      if (doStop) throw new InterruptedException();

      OrderedListPolygon polygon = null;

      // base size == 0
      if (points.size() == 0) {
        ArrayList<Point> list = new ArrayList<Point>();
        list.add(first);
        list.add(last);

        // System.out.println("\n\n---size == 0---\npoints: " + points);
        // System.out.println("first: " + first);
        // System.out.println("last: "+ last);
        // System.out.println("draw segment: " + first + " to " + last);
        // System.out.println("------");
        polygon = new OrderedListPolygon(list);

        return polygon;
      }

      // base size == 1
      if (points.size() == 1) {
        ArrayList<Point> list = new ArrayList<Point>();
        list.add(first);
        list.add(points.get(0));
        list.add(last);

        // System.out.println("\n\n---size == 1---\npoints: " + points);
        // System.out.println("first: " + first);
        // System.out.println("last: "+ last);
        // System.out.println("draw segment: " + first + " to " +
        // points.get(0)
        // +
        // " to " + last);
        // System.out.println("------");
        polygon = new OrderedListPolygon(list);

        return polygon;
      }

      Point endMiddle = GeneratorUtils.removeRandomPoint(points),
            startMiddle = new LineSegment(first, last)
              .getPointOnLineSegment(rand.nextDouble());

      List<Point> left = new ArrayList<Point>(points.size()), right =
          new ArrayList<Point>(points.size());

      // String output =
      partionateIn(left, right, points, startMiddle, endMiddle);

      boolean onLeftSide =
          MathUtils.checkOrientation(first, last, endMiddle) == -1;

      OrderedListPolygon leftPolygon, rightPolygon;

      if( steps != null ) {
        newScene(null)
        .mergeScene(oldScene)
        .addPoint(first, true)
        .addPoint(last, true)
        .addLineSegment(new LineSegment(first, last), FIRST_LAST_EDGE)
        .addPoint(startMiddle, MIDDLE_POINTS)
        .addPoint(endMiddle, MIDDLE_POINTS)
        .addLine(new Line(startMiddle, endMiddle), MIDDLE_LINE)
        .addPoints(left, LEFT_POINTS)
        .addPoints(right, RIGHT_POINTS)
        .save();

        leftScene = newScene(null)
        .addLine(new Line(startMiddle, endMiddle), true)
        .mergeScene(leftScene);
      }

      // compute left side of polygon
      leftPolygon = spacePartitioning(onLeftSide ? left : right, first, endMiddle, leftScene);

      if( steps != null ) {
        newScene(leftPolygon, LEFT_POLYGON)
        .mergeScene(leftScene)
        .addPoint(first, true)
        .addPoint(last, true)
        .addLineSegment(new LineSegment(first, last), FIRST_LAST_EDGE)
        .addPoint(startMiddle, MIDDLE_POINTS)
        .addPoint(endMiddle, MIDDLE_POINTS)
        .addLine(new Line(startMiddle, endMiddle), MIDDLE_LINE)
        .addPoints(left, LEFT_POINTS)
        .addPoints(right, RIGHT_POINTS)
        .save();

        leftScene = newScene(leftPolygon)
        .mergeScene(leftScene);
      }

      rightPolygon = spacePartitioning(onLeftSide ? right : left, endMiddle, last, leftScene);

      if( steps != null ) {
        newScene(null)
        .mergeScene(leftScene)
        .addPoint(first, true)
        .addPoint(last, true)
        .addLineSegment(new LineSegment(first, last), FIRST_LAST_EDGE)
        .addPoint(startMiddle, MIDDLE_POINTS)
        .addPoint(endMiddle, MIDDLE_POINTS)
        .addLine(new Line(startMiddle, endMiddle), MIDDLE_LINE)
        .addPolygon(rightPolygon, RIGHT_POLYGON)
        .addPoints(left, LEFT_POINTS)
        .addPoints(right, RIGHT_POINTS)
        .save();

        points.add(endMiddle);
      }

      // System.out.println("\n\n---general---\npoints: " + points +
      // ", ordered: "
      // + (onLeftSide ? "LEFT" : "RIGHT"));
      // System.out.println("first: " + first);
      // System.out.println("last: "+ last);
      // System.out.println("middle: " + middle);
      // System.out.println(output);

      // System.out.println("left: " + leftPolygon.getPoints());
      // System.out.println("right: " + rightPolygon.getPoints());

      OrderedListPolygon merge = merge(leftPolygon, rightPolygon);

      if( steps != null ) {
        newScene(null)
        .mergeScene(oldScene)
        .addPoint(first, true)
        .addPoint(last, true)
        .addLineSegment(new LineSegment(first, last), FIRST_LAST_EDGE)
        .addPoint(startMiddle, MIDDLE_POINTS)
        .addPoint(endMiddle, MIDDLE_POINTS)
        .addLine(new Line(startMiddle, endMiddle), MIDDLE_LINE)
        .addPolygon(merge, true)
        .addPoints(left, LEFT_POINTS)
        .addPoints(right, RIGHT_POINTS)
        .save();
      }

      // System.out.println("merge: " + merge.getPoints());
      // System.out.println("------");

      return merge;
    }

    @Override
    public void stop() {
      doStop = true;
    }
  }
}
