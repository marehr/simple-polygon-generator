package polygonsSWP.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.SteadyGrowthConvexHull;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
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
    List<Point> points = Arrays.asList(
                  new Point(1, 1), new Point(1, 2),
                  new Point(1, 3), new Point(1, 4),
                  new Point(4, 1), new Point(4, 4)
                ),
                expectedHull= new ArrayList<Point>();

    expectedHull.add(new Point(1, 1));
    expectedHull.add(new Point(1, 4));
    expectedHull.add(new Point(4, 1));
    expectedHull.add(new Point(4, 4));

    Collections.shuffle(points);

    for (Point point : points) {
      hull.addPoint(point);
    }
    System.out.println("hull: " + hull.getPoints());

    // edge cases containen nicht den punkt!
    assertFalse("edge case(0)", hull.containsPoint(new Point(1, 1), false));
    assertFalse("edge case(1)", hull.containsPoint(new Point(1, 3), false));
    assertFalse("edge case(2)", hull.containsPoint(new Point(2, 4), false));
    assertFalse("edge case(3)", hull.containsPoint(new Point(4, 2), false));

    // edge cases containen den punkt!
    assertTrue("edge case(0)", hull.containsPoint(new Point(1, 1), true));
    assertTrue("edge case(1)", hull.containsPoint(new Point(1, 3), true));
    assertTrue("edge case(2)", hull.containsPoint(new Point(2, 4), true));
    assertTrue("edge case(3)", hull.containsPoint(new Point(4, 2), true));

    assertTrue("should contain", hull.containsPoint(new Point(2, 2), false));
    assertTrue("should contain", hull.containsPoint(new Point(2, 2), true));

    assertFalse("shouldn't contain", hull.containsPoint(new Point(0, 0), false));
    assertFalse("shouldn't contain", hull.containsPoint(new Point(0, 0), true));

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

  @Test
  public void failingConvexHull(){
    System.out.println("\nfailingConvexHull");

    List<Point> list = Arrays.asList(
        new Point(0, 12), new Point(3, 6), new Point(11, 0),
        new Point(26, 2), new Point(13, 9)
    );

    SteadyGrowthConvexHull hull = new SteadyGrowthConvexHull();
    for(Point a: list){
      hull.addPoint(a);
    }

    Point[] expected = list.toArray(new Point[]{}),
            actual = hull.getPoints().toArray(new Point[]{});

    System.out.println("expected: " + list);
    System.out.println("actual: " + hull.getPoints());
    assertArrayEquals(expected, actual);
  }

  @Test
  public void failingConvexHull1(){
    System.out.println("\nfailingConvexHull1");

    List<Point> list = Arrays.asList(
        new Point(280, 580), new Point(300, 320), new Point(530, 0),
        new Point(520, 180), new Point(420, 280)
    );

    SteadyGrowthConvexHull hull = new SteadyGrowthConvexHull();
    for(Point a: list){
      hull.addPoint(a);
    }

    Point[] expected = list.subList(0, 4).toArray(new Point[]{}),
            actual = hull.getPoints().toArray(new Point[]{});

    System.out.println("expected: " + list.subList(0, 4));
    System.out.println("actual: " + hull.getPoints());
    assertArrayEquals(expected, actual);
  }
}
