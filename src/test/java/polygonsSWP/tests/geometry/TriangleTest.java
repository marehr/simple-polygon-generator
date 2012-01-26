package polygonsSWP.tests.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Triangle;
import polygonsSWP.util.GeneratorUtils;


public class TriangleTest
{
  private Triangle testTriangle;

  @Before
  public void setup() {
    testTriangle =
        new Triangle(new Point(0, 0), new Point(10, 0), new Point(10, 10));
  }

  @After
  public void tearDown() {
    testTriangle = null;
  }

  @Test
  public void formsTriangleTest() {
    // Real Triangle:
    assertTrue(Triangle.formsTriangle(new LineSegment(new Point(0, 0),
        new Point(10, 0)),
        new LineSegment(new Point(10, 10), new Point(10, 0)), new LineSegment(
            new Point(0, 0), new Point(10, 10))));
    // False Triangle
    assertFalse(Triangle.formsTriangle(new LineSegment(new Point(0, 0),
        new Point(10, 0)),
        new LineSegment(new Point(10, 11), new Point(10, 0)), new LineSegment(
            new Point(0, 0), new Point(10, 10))));
    // Identical Lines
    assertFalse(Triangle.formsTriangle(new LineSegment(new Point(0, 0),
        new Point(10, 0)), new LineSegment(new Point(10, 0), new Point(0, 0)),
        new LineSegment(new Point(10, 10), new Point(10, 10))));

    // Identical Points
    // assertFalse(Triangle.formsTriangle(new LineSegment(new Point(0, 0), new
    // Point(0, 0)),
    // new LineSegment(new Point(0, 0), new Point(0, 0)), new LineSegment(
    // new Point(0, 0), new Point(0, 0))));
    /**
     * Necessary assertion is in LineSegment
     */
  }

  @Test
  public void EqualsTest() {
    // Same Triangle:
    Triangle sameTriangle =
        new Triangle(new Point(0, 0), new Point(10, 0), new Point(10, 10));
    // One moved:
    Triangle oneMovedTriangle =
        new Triangle(new Point(10, 0), new Point(10, 10), new Point(0, 0));
    // Two moved:
    Triangle twoMovedTriangle =
        new Triangle(new Point(10, 10), new Point(0, 0), new Point(10, 0));
    // Not similar:
    Triangle noSimilarTriangle =
        new Triangle(new Point(10, 0), new Point(20, 20), new Point(0, 0));
    assertTrue(sameTriangle.equals(testTriangle));
    assertTrue(oneMovedTriangle.equals(testTriangle));
    assertTrue(twoMovedTriangle.equals(testTriangle));
    assertFalse(noSimilarTriangle.equals(testTriangle));
  }

  @Test
  public void containsPointTest2(){
    Triangle triangle = new Triangle(new Point(0, 1), new Point(4, 4), new Point(6, 0));

    /**
     * contains
     */
    assertEquals(true, triangle.containsPoint(new Point(3, 3), false));
    assertEquals(true, triangle.containsPoint(new Point(3, 3), true));

    assertEquals(true, triangle.containsPoint(new Point(5, 1), false));
    assertEquals(true, triangle.containsPoint(new Point(5, 1), true));

    /**
     * does not contains
     */
    assertEquals(false, triangle.containsPoint(new Point(1, 2), false));
    assertEquals(false, triangle.containsPoint(new Point(1, 2), true));

    assertEquals(false, triangle.containsPoint(new Point(3, 0), false));
    assertEquals(false, triangle.containsPoint(new Point(3, 0), true));

    assertEquals(false, triangle.containsPoint(new Point(5, 3), false));
    assertEquals(false, triangle.containsPoint(new Point(5, 3), true));

    assertEquals(false, triangle.containsPoint(new Point(8, 7), false));
    assertEquals(false, triangle.containsPoint(new Point(8, 7), true));

    /**
     * online
     */

    assertEquals(false, triangle.containsPoint(new Point(0, 1), false));
    assertEquals(true, triangle.containsPoint(new Point(0, 1), true));

    assertEquals(false, triangle.containsPoint(new Point(4, 4), false));
    assertEquals(true, triangle.containsPoint(new Point(4, 4), true));

    assertEquals(false, triangle.containsPoint(new Point(6, 0), false));
    assertEquals(true, triangle.containsPoint(new Point(6, 0), true));

    assertEquals(false, triangle.containsPoint(new Point(5, 2), false));
    assertEquals(true, triangle.containsPoint(new Point(5, 2), true));

    LineSegment seg1 = new LineSegment(new Point(0, 1), new Point(6, 0)),
                seg2 = new LineSegment(new Point(2, 0), new Point(2, 1));
    Point[] isec = seg1.intersect(seg2);

    assertEquals(false, triangle.containsPoint(isec[0], false));
    assertEquals(true, triangle.containsPoint(isec[0], true));

    Triangle triangle2 = new Triangle(new Point(308, 434), new Point(145, 386), new Point(102, 516));
    assertEquals(false, triangle2.containsPoint(new Point(490, 400), false));
    assertEquals(false, triangle2.containsPoint(new Point(490, 400), true));
  }

  @Test
  public void getSurfaceAreaTest() {
    assertEquals(50, testTriangle.getSurfaceArea(), 0);
  }

  @Test
  public void containsPointTest() {
    // If Points are on line is active:
    assertTrue(testTriangle.containsPoint(new Point(0, 0), true));
    assertTrue(testTriangle.containsPoint(new Point(9, 1), true));
    assertFalse(testTriangle.containsPoint(new Point(20, 20), true));
    // If Points are on line is inactive:
    assertFalse(testTriangle.containsPoint(new Point(0, 0), false));
    assertTrue(testTriangle.containsPoint(new Point(9, 1), false));
    assertFalse(testTriangle.containsPoint(new Point(20, 20), false));
  }

  @Test
  public void createRandomPointTest() {
    for (int i = 1; i < 10000; ++i){
      Point point = testTriangle.createRandomPoint();
      //System.out.println("triangle: " + point);
      assertTrue(testTriangle.containsPoint(point, true));
    }
  }

  @Test
  public void createRandomPointTest2() {
    Triangle triangle = new Triangle(new Point(0, 1), new Point(4, 4), new Point(6, 0));
    for (int i = 1; i < 10000; ++i){
      Point point = triangle.createRandomPoint();
      assertTrue(triangle.containsPoint(point, true));
    }

    triangle = new Triangle(new Point(269.808,137.508), new Point(521.891,145.39), new Point(116.261,541.005));
    for (int i = 1; i < 10000; ++i){
      Point point = triangle.createRandomPoint();
      assertTrue(triangle.containsPoint(point, true));
    }

    List<Point> points = GeneratorUtils.createRandomSetOfPointsInSquare(3, 100, false);
    triangle = new Triangle(points);

    for (int i = 1; i < 10000; ++i){
      Point point = triangle.createRandomPoint();
      assertTrue(triangle.containsPoint(point, true));
    }
  }

  @Test
  public void selectRandomTriangleBySizeTest() {
    // TODO: Do Implement!
    System.out.println("NOT IMPLEMENTED YET!");
  }
}