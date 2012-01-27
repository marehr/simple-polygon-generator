package polygonsSWP.tests.geometry;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import polygonsSWP.geometry.MonotonPolygon;
import polygonsSWP.geometry.Point;

public class MonotonPolygonTest
{
  private MonotonPolygon testPoly;
  @Before
  public void setup() {
    List<Point> tmpList = new ArrayList<Point>();
    tmpList.add(new Point(0,0));
    tmpList.add(new Point(10,-10));
    tmpList.add(new Point(20,0));
    tmpList.add(new Point(10,10));
    testPoly = new MonotonPolygon(tmpList);
  }
  
  @After
  public void tearDown() {
    testPoly = null;
  }
  
  @Test
  public void getSortedPointsTest() {
    List<Point> tmpList = testPoly.getSortedPoints();
    Point first = tmpList.get(0);
    for(int i = 1; i<tmpList.size(); ++i) {
      assertTrue(first.compareTo(tmpList.get(i))==-1);
      first = tmpList.get(i);      
    }
  }
  
  @Test
  public void areNeighboursTest() {
    assertTrue(testPoly.areNeighbours(new Point(0,0), new Point(10,-10)));
    assertTrue(testPoly.areNeighbours(new Point(10,-10), new Point(0,0)));
    assertFalse(testPoly.areNeighbours(new Point(0,0), new Point(0,0)));
    assertFalse(testPoly.areNeighbours(new Point(0,0), new Point(20,0)));
  }
  
  /**
   * Is tested further through OrderedListPolygonTriangulateTest
   */
  @Test
  public void triangulationTest() {
    testPoly.triangulate();
    System.out.println(testPoly.getTriangles());
  }
}
