package polygonsSWP.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.geometry.Ray;
import polygonsSWP.gui.generation.PolygonPointFrame;
import polygonsSWP.util.GeneratorUtils;


public class GeneratorUtilsTest
{

  @Test
  public void testConvexHullWeirdTriangle() {
    List<Point> points = new LinkedList<Point>(), expectedHull =
        new LinkedList<Point>();
    points.add(new Point(555, 466));
    points.add(new Point(566, 489));
    points.add(new Point(547, 233));
    expectedHull.add(new Point(547, 233));
    expectedHull.add(new Point(555, 466));
    expectedHull.add(new Point(566, 489));

    OrderedListPolygon assignedHull = GeneratorUtils.convexHull(points);
    assertTrue(assignedHull.size() == 3);

    Object[] expecteds = expectedHull.toArray(), actuals =
        assignedHull.getPoints().toArray();
    assertArrayEquals(expecteds, actuals);
  }

  @Test
  public void testConvexHull() {
    System.out.println("\ntestConvexHull");

    List<Point> points = new LinkedList<Point>(), expectedHull =
        new LinkedList<Point>();
    OrderedListPolygon assignedHull;

    Point k;

    k = new Point(1, 3);
    points.add(k);
    expectedHull.add(k);

    k = new Point(2, 2);
    points.add(k);
    expectedHull.add(k);

    k = new Point(4, 1);
    points.add(k);
    expectedHull.add(k);

    points.add(new Point(5, 2));
    points.add(new Point(6, 2));

    k = new Point(8, 1);
    points.add(k);
    expectedHull.add(k);

    k = new Point(10, 2);
    points.add(k);
    expectedHull.add(k);

    k = new Point(10, 4);
    points.add(k);
    expectedHull.add(k);

    points.add(new Point(6, 4));

    k = new Point(3, 5);
    points.add(k);
    expectedHull.add(k);

    // convex hull is always the same, independent of the order of points!
    Collections.shuffle(points);

    assignedHull = GeneratorUtils.convexHull(points);

    Object[] expecteds = expectedHull.toArray(), actuals =
        assignedHull.getPoints().toArray();

    System.out.println("expected: " + expectedHull);
    System.out.println("assigned: " + assignedHull.getPoints());

    assertArrayEquals(expecteds, actuals);
  }

  @Test
  public void failingConvexHull(){
    System.out.println("\nfailingConvexHull");

    List<Point> list = Arrays.asList(
        new Point(0, 12), new Point(3, 6), new Point(11, 0),
        new Point(26, 2), new Point(13, 9)
    );

    Polygon poly = GeneratorUtils.convexHull(list);
    Point[] expected = list.toArray(new Point[]{}),
            actual = poly.getPoints().toArray(new Point[]{});

    System.out.println("expected: " + list);
    System.out.println("actual: " + poly.getPoints());
    assertArrayEquals(expected, actual);
  }

  @Test
  public void isPolygonPointVisibleTest() {
    ArrayList<Point> points = new ArrayList<Point>();
    points.add(new Point(0, 0));
    points.add(new Point(0, 50));
    points.add(new Point(50, 50));
    Polygon polygon = new OrderedListPolygon(points);

    Point point = new Point(40, 30);

    assertTrue(GeneratorUtils.isPolygonVertexVisible(point, points.get(0),
        polygon));
    assertTrue(GeneratorUtils.isPolygonVertexVisible(point, points.get(2),
        polygon));
    assertFalse(GeneratorUtils.isPolygonVertexVisible(point, points.get(1),
        polygon));

    point = new Point(0, 25);

    // Punkt liegt genau auf der Polygonkante (0,0) - (0, 50)
    // Letzter Fall ist falsch, da Punkte auf einer PolygonKante auch als
    // Schnittpunkte zaehlen und es damit genau 2 Schnittpunkte gibt.

    // TODO: den letzten Fall vielleicht erlauben. (in GeneralPosition kann der
    // Fall nicht auftreten)
    assertFalse(GeneratorUtils.isPolygonVertexVisible(point, points.get(0),
        polygon));
    assertFalse(GeneratorUtils.isPolygonVertexVisible(point, points.get(2),
        polygon));
    assertFalse(GeneratorUtils.isPolygonVertexVisible(point, points.get(1),
        polygon));
  }

  @Test
  public void isPolygonVertexVisibleNoBlockingColliniearsTestGeneralPosition() {
    List<Point> points =
        Arrays.asList(new Point(20, 30), new Point(30, 35), new Point(30, 45));
    Polygon polygon = new OrderedListPolygon(points);
    Point point = new Point(0, 0);

    // letzter Testcase schlaegt fehl, obwohl eigentlich sichtbar.
    assertTrue(GeneratorUtils.isPolygonVertexVisible(point, points.get(0),
        polygon));
    assertTrue(GeneratorUtils.isPolygonVertexVisible(point, points.get(1),
        polygon));
    assertFalse(GeneratorUtils.isPolygonVertexVisible(point, points.get(2),
        polygon));
  }
  
  @Test
  public void testIsPointOnPolygonVisible(){
    List<Point> polyognPoints = new ArrayList<Point>();
    polyognPoints.add(new Point(0,0));
    polyognPoints.add(new Point(2,0));
    polyognPoints.add(new Point(2,1));
    polyognPoints.add(new Point(3,0));
    polyognPoints.add(new Point(3,2));
    polyognPoints.add(new Point(0,2));
    Polygon testPolygon = new OrderedListPolygon(polyognPoints);
    assertTrue(GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(new Point(0,0), new Point(0,2), testPolygon));
    assertFalse(GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(new Point(0,0), new Point(3,0), testPolygon));
    
    polyognPoints.clear();
    polyognPoints.add(new Point(372.568,276.651));
    polyognPoints.add(new Point(520.4,469.238));
    polyognPoints.add(new Point(463.044,268.042));
    testPolygon = new OrderedListPolygon(polyognPoints);
    assertTrue(GeneratorUtils.isPolygonVertexVisibleNoBlockingColliniears(new Point(0,0), new Point(0,2), testPolygon));
  }
}
