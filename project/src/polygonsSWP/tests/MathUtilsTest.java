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
import polygonsSWP.generators.PermuteAndReject;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.TwoOptMoves;
import sun.font.CreatedFontTracker;


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

  /*
   * Return the clockwise status of a curve, clockwise or counterclockwise n
   * vertices making up curve p return 0 for incomputables eg: colinear points
   * CLOCKWISE == 1 COUNTERCLOCKWISE == -1 It is assumed that - the polygon is
   * closed - the last point is not repeated. - the polygon is simple (does not
   * intersect itself or have holes)
   */
  public int ClockWise(List<Point> p) {
    int i, j, k;
    int count = 0;
    double z;
    int n = p.size();

    if (n < 3) return (0);

    for (i = 0; i < n; i++) {
      j = (i + 1) % n;
      k = (i + 2) % n;
      z = (p.get(i).x - p.get(i).x) * (p.get(k).y - p.get(j).y);
      z -= (p.get(j).y - p.get(i).y) * (p.get(k).x - p.get(j).x);
      if (z < 0) count--;
      else if (z > 0) count++;
    }
    if (count > 0) return -1;
    else if (count < 0) return 1;
    else return 0;
  }

  /**
   * Test for triangulatePolygon
   * TODO: needs to be verified with ONLY counter-clockwise ordered polygons.
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
    for (int i = 6; i < 16; ++i) {
      HashMap<String, Object> map = new HashMap<String, Object>();
      map.put("n", i);
      map.put("size", 100);
      PermuteAndReject pAR = new PermuteAndReject();
      Polygon rPoly = pAR.generate(map, null);
      // Reverse Polygon.
      List<Point> tP = new ArrayList<Point>();
      if (ClockWise(tP) == 1) {
        for (Point item : rPoly.getPoints())
          tP.add(0, item);
        result = MathUtils.triangulatePolygon(new OrderedListPolygon(tP));
      } else {
        result = MathUtils.triangulatePolygon(rPoly);
      }
      System.out.println(result.size() + " " + i);
      for (Polygon item : result)
        System.out.println(item.getPoints());
      assert(result.size() >= i-2);
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
    Polygon polygon = new OrderedListPolygon(pPoints);
    // triangulate Polygon, result consists of 3 Polygons
    List<Polygon> triangularization = MathUtils.triangulatePolygon(polygon);
    // use method representative times and store which polygon was picked
    HashMap<Polygon, Integer> amountsPolygonsChosen =
        new HashMap<Polygon, Integer>();
    for (Polygon polygon2 : triangularization) {
      amountsPolygonsChosen.put(polygon2, 0);
    }
    int timesTested = 1000;
    for (int i = 0; i < timesTested; i++) {
      Polygon selectedPolygon =
          MathUtils.selectRandomTriangleBySize(triangularization);
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
          MathUtils.calculateSurfaceAreaOfPolygon(triangularization.get(i)));
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

  @Test
  public void testCreateRandomPointInPolygon() {
    PolygonGenerator generator = new PermuteAndReject();
    HashMap<String, Object> parameter = new HashMap<String, Object>();
    parameter.put("n", 4);
    parameter.put("size", 10);
    Polygon polygon = generator.generate(parameter, null);
    List<Point> polygonPoints = polygon.getPoints();
    // TODO: malte auf list order ansprechen !!!
    java.util.Collections.reverse(polygonPoints);
    assertNotNull(polygonPoints);
    System.out.println("--- test createRandomPointInPolygon ---");
    System.out.println(polygonPoints.toString());
    assertTrue(MathUtils.triangulatePolygon(polygon).size() != 0);
    Point randomPoint = MathUtils.createRandomPointInPolygon(polygon);
    System.out.println(randomPoint);
    assertTrue(MathUtils.checkIfPointIsInPolygon(polygon, randomPoint, true));
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
        MathUtils.getIntersectingPointsWithPolygon(poly, new Point(100, 0),
            new Point(50, 10));
    System.out.println(result);
  }
}
