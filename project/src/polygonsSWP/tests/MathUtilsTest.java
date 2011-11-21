package polygonsSWP.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import polygonsSWP.data.OrderedListPolygon;
import polygonsSWP.data.Point;
import polygonsSWP.data.Polygon;
import polygonsSWP.generators.PermuteAndReject;
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
    triangle.deletePoint(new Point(1, 3));
    triangle.addPoint(new Point(1, 1));
    result = MathUtils.triangulatePolygon(triangle);
    assertEquals(3, result.size());
    // Test with random polygons:
    for(int i = 6; i < 15; ++i) {
      HashMap<String,Object> map = new HashMap<String,Object>();
      map.put("n", i);
      map.put("size", 100);
      PermuteAndReject pAR = new PermuteAndReject();
      Polygon rPoly = pAR.generate(map, null);
      result = MathUtils.triangulatePolygon(rPoly);
      System.out.println(result.size() + " " + i);
    }
  }

  /**
   * Test for calculateSurfaceAreaOfPolygon
   */
  @Test
  public void testCalculateSurfaceAreaOfPolygon() {
    // Create triangle 1
    List<Point> pPoints = new ArrayList<Point>();
    // Create triangle
    pPoints = new ArrayList<Point>();
    pPoints.add(new Point(0, 1));
    pPoints.add(new Point(0, 0));
    pPoints.add(new Point(3, 0));
    Polygon polygon = new OrderedListPolygon(pPoints);
    double result = MathUtils.calculateSurfaceAreaOfPolygon(polygon);
    assertEquals(1.5, result, 0.0001);
    // Create a Polygon
    pPoints = new ArrayList<Point>();
    pPoints.add(new Point(0, 0));
    pPoints.add(new Point(2, 0));
    pPoints.add(new Point(1, 3));
    pPoints.add(new Point(0, 3));
    polygon = new OrderedListPolygon(pPoints);
    result = MathUtils.calculateSurfaceAreaOfPolygon(polygon);
    assertEquals(4.5, result, 0.0001);
  }
  /**
   * test for selcetRandomPolygonBySize
   * TODO: work around the problem with the random seed while testing
   */
  //@Test
  public void testSelectRandomPolygonBySize() {
    // Create a Polygon
    List<Point> pPoints = new ArrayList<Point>();
    pPoints = new ArrayList<Point>();
    pPoints.add(new Point(0, 0));
    pPoints.add(new Point(2, 0));
    pPoints.add(new Point(2, 2));
    pPoints.add(new Point(1, 10));
    pPoints.add(new Point(0, 10));
    Polygon polygon = new OrderedListPolygon(pPoints);
    // triangulate Polygon, result consists of 3 Polygons
    List<Polygon> triangularization = MathUtils.triangulatePolygon(polygon);
    // use method representative times and store which polygon was picked
    HashMap<Polygon, Integer> amountsPolygonsChosen = new HashMap<Polygon, Integer>();
    for (Polygon polygon2 : triangularization) {
      amountsPolygonsChosen.put(polygon2, 0);
    }
    for (int i = 0; i < 10000000; i++) {
      Polygon selectedPolygon = MathUtils.selectRandomPolygonBySize(triangularization);
      assertTrue(triangularization.contains(selectedPolygon));
      amountsPolygonsChosen.put(selectedPolygon, amountsPolygonsChosen.get(selectedPolygon) +1);
    }
    // print out statistics for selectRandomPolygon
    System.out.println("--- test selectRandomPolygonBySize: ---");
    System.out.println("size of Triangularization: " + triangularization.size());
    for (int i = 0; i < triangularization.size(); i++) {
      System.out.println("surface area of triangle " + i + " :" +
          MathUtils.calculateSurfaceAreaOfPolygon(triangularization.get(i)));
    }
    for (Polygon polygon2 : triangularization) {
      System.out.println(polygon2 + " :" + amountsPolygonsChosen.get(polygon2));
    }    
  }
  
  @Test
  public void testIntersectWithPolygon() {
    // Create a triangle
    List<Point> pPoints = new ArrayList<Point>();
    pPoints.add(new Point(0, 100));
    pPoints.add(new Point(0, 0));
    pPoints.add(new Point(50, 10));
    pPoints.add(new Point(100, 0));
    pPoints.add(new Point(100, 100));
    OrderedListPolygon poly = new OrderedListPolygon(pPoints);
    List<Point> result = MathUtils.getIntersectingPointsWithPolygon(poly, new Point(100,0), new Point(50,10));
    System.out.println(result);
  }
}
