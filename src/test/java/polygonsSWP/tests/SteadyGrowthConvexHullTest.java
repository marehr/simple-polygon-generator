package polygonsSWP.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import polygonsSWP.geometry.Point;
import polygonsSWP.util.SteadyGrowthConvexHull;

/**
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 */
public class SteadyGrowthConvexHullTest {
  @Test
  public void testBinSearch(){
    ArrayList<Point> points = new ArrayList<Point>();

    int pos = Collections.binarySearch(points, new Point(1,1));
    assertEquals(-1, pos);

    points.add(new Point(1,1));

    pos = Collections.binarySearch(points, new Point(1,1));
    assertEquals(0, pos);

    pos = Collections.binarySearch(points, new Point(0,0));
    assertEquals(0, Math.abs(pos) - 1);

    pos = Collections.binarySearch(points, new Point(3,0));
    assertEquals(1, Math.abs(pos) - 1);

    points.add(new Point(3,0));

    pos = Collections.binarySearch(points, new Point(3,0));
    assertEquals(1, pos);

    pos = Collections.binarySearch(points, new Point(2,0));
    assertEquals(1, Math.abs(pos) - 1);

    pos = Collections.binarySearch(points, new Point(4,0));
    assertEquals(2, Math.abs(pos) - 1);
  }

  @Test
  public void testContainsPoint(){
    System.out.println("testContainsPoint - start");

    SteadyGrowthConvexHull hull  = new SteadyGrowthConvexHull();
    ArrayList<Point> points = new ArrayList<Point>(),
                expectedHull= new ArrayList<Point>();

    points.add(new Point(1, 1));
    expectedHull.add(new Point(1, 1));
    points.add(new Point(1, 2));
    points.add(new Point(1, 4));
    expectedHull.add(new Point(1, 4));
    points.add(new Point(4, 1));
    expectedHull.add(new Point(4, 1));
    points.add(new Point(4, 4));
    expectedHull.add(new Point(4, 4));

    Collections.shuffle(points);

    for (Point point : points) {
      hull.addPoint(point);
    }
    System.out.println("hull: " + hull.getPoints());

    // edge cases containen nicht den punkt!
    assertFalse("edge case(0)", hull.containsPoint(new Point(1, 1)));
    assertFalse("edge case(1)", hull.containsPoint(new Point(1, 3)));
    assertFalse("edge case(2)", hull.containsPoint(new Point(2, 4)));
    assertFalse("edge case(3)", hull.containsPoint(new Point(4, 2)));

    assertTrue("should contain", hull.containsPoint(new Point(2, 2)));
    assertFalse("shouldn't contain", hull.containsPoint(new Point(0, 0)));

    System.out.println("testContainsPoint - end\n\n");
  }

  @Test
  public void notInGeneralPosition(){
    System.out.println("notInGeneralPosition - start");

    SteadyGrowthConvexHull assignedHull  = new SteadyGrowthConvexHull();
    ArrayList<Point> points = new ArrayList<Point>(),
                expectedHull= new ArrayList<Point>();

    points.add(new Point(1, 1));
    expectedHull.add(new Point(1, 1));
    points.add(new Point(4, 1));
    expectedHull.add(new Point(4, 1));
    points.add(new Point(4, 4));
    expectedHull.add(new Point(4, 4));
    points.add(new Point(1, 4));
    expectedHull.add(new Point(1, 4));

    points.add(new Point(1, 2));
    points.add(new Point(2, 1));
    points.add(new Point(2, 4));
    points.add(new Point(4, 2));


    Collections.shuffle(points);

    for (Point point : points) {
      assignedHull.addPoint(point);
    }
    System.out.println("hull: " + assignedHull.getPoints());

    Object[] expecteds = expectedHull.toArray(),
    actuals = assignedHull.getPoints().toArray();

    System.out.println("expected: " + expectedHull);
    System.out.println("assigned: " + assignedHull.getPoints());

    assertArrayEquals(expecteds, actuals);
  }

  @Test
  public void testConvexHull() {
    System.out.println("testConvexHull - start");
    SteadyGrowthConvexHull assignedHull  = new SteadyGrowthConvexHull();
    List<Point> points = new LinkedList<Point>(), expectedHull = new LinkedList<Point>();

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

    for (Point point : points) {
//      System.out.println(":: add point " + point + " ::\n");
      assignedHull.addPoint(point);
    }

    Object[] expecteds = expectedHull.toArray(), actuals = assignedHull
        .getPoints().toArray();

    System.out.println("expected: " + expectedHull);
    System.out.println("assigned: " + assignedHull.getPoints());

    assertArrayEquals(expecteds, actuals);
    System.out.println("testConvexHull - end\n\n");
  }
}
