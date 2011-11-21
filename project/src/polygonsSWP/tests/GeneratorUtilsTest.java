package polygonsSWP.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import polygonsSWP.data.OrderedListPolygon;
import polygonsSWP.data.Point;
import polygonsSWP.data.Polygon;
import polygonsSWP.util.GeneratorUtils;
import polygonsSWP.util.MathUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class GeneratorUtilsTest
{

  @Test
  public void testConvexHull() {
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

    expectedHull.add(new Point(1, 3));

    // convex hull is always the same, independent of the order of points!
    Collections.shuffle(points);

    assignedHull = GeneratorUtils.convexHull(points);

    Object[] expecteds = expectedHull.toArray(), actuals =
        assignedHull.getPoints().toArray();

    System.out.println("expected: ");
    for (Object a : expecteds) {
      System.out.println((Point) a);
    }

    System.out.println("assigned: ");
    for (Object a : actuals) {
      System.out.println((Point) a);
    }

    assertArrayEquals(expecteds, actuals);
  }
}
