package polygonsSWP.tests.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Triangle;


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
      assertTrue(testTriangle.containsPoint(point,
          true));
    }
  }

  @Test
  public void selectRandomTriangleBySizeTest() {
    // TODO: Do Implement!
    System.out.println("NOT IMPLEMENTED YET!");
  }
}