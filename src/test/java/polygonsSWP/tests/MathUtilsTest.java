package polygonsSWP.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.geometry.Triangle;
import polygonsSWP.util.MathUtils;


/**
 * Test environment for MathUtils.
 * 
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
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
    
    // Point is on the line
    toCheck = new Point(15, 0);
    assertEquals(MathUtils.checkOrientation(begin, end, toCheck), 0);
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
    assertFalse(poly.containsPoint(new Point(0, 0), false));
    // Point is in poly
    assertTrue(poly.containsPoint(new Point(1, 1), true));
    // Point is out of poly
    assertFalse(poly.containsPoint(new Point(50, 50), true));
  }

  /**
   * Test for triangulatePolygon
   * TODO: needs to be verified with ONLY counter-clockwise ordered polygons.
   */
  @Test
  public void testTriangulatePolygon() {
    System.out.println("--- test testTriangulatePolygon: ---");
    
    // Create a triangle
    List<Point> pPoints = new ArrayList<Point>();
    pPoints.add(new Point(0, 2));
    pPoints.add(new Point(0, 0));
    pPoints.add(new Point(2, 0));
    OrderedListPolygon triangle = new OrderedListPolygon(pPoints);

    List<Triangle> result = triangle.triangulate();
    Triangle poly = result.get(0);
    assertEquals(result.size(), 1);
    assertTrue(poly.equals(triangle));

    pPoints.add(new Point(2, 2));
    triangle = new OrderedListPolygon(pPoints);
    result = triangle.triangulate();
    assertEquals(2, result.size());

    pPoints.add(new Point(1, 3));
    triangle = new OrderedListPolygon(pPoints);
    result = triangle.triangulate();
    assertEquals(3, result.size());

    pPoints.remove(new Point(1, 3));
    pPoints.add(new Point(1, 1));
    triangle = new OrderedListPolygon(pPoints);
    result = triangle.triangulate();
    assertEquals(3, result.size());
    
    // Test with random polygons:
    /*
    for (int i = 6; i < 16; ++i) {
      HashMap<Parameters, Object> map = new HashMap<Parameters, Object>();
      map.put(Parameters.n, i);
      map.put(Parameters.size, 100);
      PermuteAndReject pAR = new PermuteAndReject();
      // We know P&R returns an OrderedListPolygon in counterclockwise orientation.
      OrderedListPolygon rPoly = (OrderedListPolygon) pAR.generate(map, null);
      result = rPoly.triangulate();
      System.out.println(result.size() + " " + i);
      for (Polygon item : result)
        System.out.println(item.getPoints());
      assert(result.size() >= i-2);
    }*/
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
    double result = polygon.getSurfaceArea();
    assertEquals(1.5, result, 0.0001);
    // Create a Polygon
    pPoints = new ArrayList<Point>();
    pPoints.add(new Point(0, 0));
    pPoints.add(new Point(2, 0));
    pPoints.add(new Point(1, 3));
    pPoints.add(new Point(0, 3));
    polygon = new OrderedListPolygon(pPoints);
    result = polygon.getSurfaceArea();
    assertEquals(4.5, result, 0.0001);
  }

  /**
   * test for selcetRandomPolygonBySize TODO: work around the problem with the
   * random seed while testing
   */
  @Test
  public void testSelectRandomTriangleBySize() {
    // Create a Polygon
    List<Point> pPoints = new ArrayList<Point>();
    pPoints = new ArrayList<Point>();
    pPoints.add(new Point(0, 0));
    pPoints.add(new Point(2, 0));
    pPoints.add(new Point(2, 2));
    pPoints.add(new Point(1, 10));
    pPoints.add(new Point(0, 10));
    OrderedListPolygon polygon = new OrderedListPolygon(pPoints);
    // triangulate Polygon, result consists of 3 Polygons
    List<Triangle> triangularization = polygon.triangulate();
    // use method representative times and store which polygon was picked
    HashMap<Polygon, Integer> amountsPolygonsChosen =
        new HashMap<Polygon, Integer>();
    for (Polygon polygon2 : triangularization) {
      amountsPolygonsChosen.put(polygon2, 0);
    }
    int timesTested = 1000;
    for (int i = 0; i < timesTested; i++) {
      Polygon selectedPolygon =
        Triangle.selectRandomTriangleBySize(triangularization);
      assertTrue(triangularization.contains(selectedPolygon));
      amountsPolygonsChosen.put(selectedPolygon,
          amountsPolygonsChosen.get(selectedPolygon) + 1);
    }
    // print out statistics for selectRandomPolygon
    System.out.println("--- test selectRandomPolygonBySize: ---");
    System.out.println("size of Triangularization: " + triangularization.size());
    System.out.println("times tested: " + timesTested);
    for (int i = 0; i < triangularization.size(); i++) {
      System.out.println("surface area of triangle " + i + " :" +
          triangularization.get(i).getSurfaceArea());
    }
    int polygonsReturned = 0;
    for (Polygon polygon2 : triangularization) {
      int returnedThisPolygon = amountsPolygonsChosen.get(polygon2);
      System.out.println(polygon2 + " :" + returnedThisPolygon);
      polygonsReturned += returnedThisPolygon;
    }
    System.out.println("returned polygons: " + polygonsReturned);
    System.out.println("\n");
  }
  
  // TODO: Was testet dieser Test?
  /*
  @Test
  public void testCreateRandomPointInPolygon() {
    PolygonGenerator generator = new PermuteAndReject();
    HashMap<Parameters, Object> parameter = new HashMap<Parameters, Object>();
    parameter.put(Parameters.n, 4);
    parameter.put(Parameters.size, 10);
    // We know P&R returns an OrderedListPolygon
    OrderedListPolygon polygon = (OrderedListPolygon) generator.generate(parameter, null);
    List<Point> polygonPoints = polygon.getPoints();
    // TODO: malte auf list order ansprechen !!!
    java.util.Collections.reverse(polygonPoints);
    assertNotNull(polygonPoints);
    System.out.println("--- test createRandomPointInPolygon ---");
    System.out.println(polygonPoints.toString());
    assertTrue(polygon.triangulate().size() != 0);
    Point randomPoint = polygon.createRandomPoint();
    System.out.println(randomPoint);
    assertTrue(polygon.containsPoint(randomPoint, true));
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
    List<Point[]> result =
        MathUtils.getIntersectionsPolygonLine(poly, new Point(100, 0),
            new Point(50, 10));
    System.out.println(result);
  }*/
}
