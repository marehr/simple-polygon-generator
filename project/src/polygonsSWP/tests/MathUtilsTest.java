package polygonsSWP.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import polygonsSWP.data.OrderedListPolygon;
import polygonsSWP.data.Point;
import polygonsSWP.data.Polygon;
import polygonsSWP.util.MathUtils;


;

/**
 * Test environment for MathUtils.
 * 
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 */
public class MathUtilsTest
{
  /**
   * Test for CheckOrientation
   */
  @Test
  public void testCheckOrientation() {
    // Point is on the segment
    Point begin = new Point(0, 0);
    Point end = new Point(10, 0);
    Point toCheck = new Point(5, 0);
    assertEquals(MathUtils.checkOrientation(begin, end, toCheck), 0);
    // Point is on the left side
    toCheck.y = 1;
    assertEquals(MathUtils.checkOrientation(begin, end, toCheck), 1);
    // Point is on the right side
    toCheck.y = -1;
    assertEquals(MathUtils.checkOrientation(begin, end, toCheck), -1);
  }

  /**
   * Just test for triangle TODO: further testing
   */
  @Test
  public void testCheckIfPointIsInPolygon() {
    // Create Triangle
    List<Point> list = new ArrayList<Point>();
    list.add(new Point(0, 0));
    list.add(new Point(10, 0));
    list.add(new Point(0, 10));
    Polygon poly = new OrderedListPolygon(list);
    // Point is on poly
    assertFalse(MathUtils.checkIfPointIsInPolygon(poly, new Point(0, 0), false));
    // Point is in poly
    assertTrue(MathUtils.checkIfPointIsInPolygon(poly, new Point(1, 1), true));
    // Point is out of poly
    assertFalse(MathUtils.checkIfPointIsInPolygon(poly, new Point(50, 50), true));
  }

  /**
   * Test for triangulatePolygon
   */
  @Test
  public void testTriangulatePolygon() {
    // Create a triangle
    List<Point> pPoints = new ArrayList<Point>();
    pPoints.add(new Point(0, 2));
    pPoints.add(new Point(0, 0));
    pPoints.add(new Point(2, 0));
    OrderedListPolygon triangle = new OrderedListPolygon(pPoints);
    List<Polygon> result = MathUtils.triangulatePolygon(triangle);
    Polygon poly = result.get(0);
    assertEquals(result.size(), 1);
    assertTrue(poly.equals(triangle));
    triangle.addPoint(new Point(2, 2));
    result = MathUtils.triangulatePolygon(triangle);
    assertEquals(2, result.size());
    triangle.addPoint(new Point(1, 3));
    result = MathUtils.triangulatePolygon(triangle);
    assertEquals(3, result.size());
    triangle.deletePoint(new Point(1,3));
    triangle.addPoint(new Point(1, 1));
    result = MathUtils.triangulatePolygon(triangle);
    assertEquals(3, result.size());
  }
}
