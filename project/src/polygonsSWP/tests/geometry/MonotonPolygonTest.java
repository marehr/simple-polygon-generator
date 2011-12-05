package polygonsSWP.tests.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.MonotonPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Triangle;

public class MonotonPolygonTest
{
  private MonotonPolygon testPoly;
  @Before
  public void setup() {
    List<LineSegment> tmpList = new ArrayList<LineSegment>();
    tmpList.add(new LineSegment(new Point(0,0), new Point(10,-10)));
    tmpList.add(new LineSegment(new Point(10,-10), new Point(20,0)));
    tmpList.add(new LineSegment(new Point(20,0), new Point(10,10)));
    tmpList.add(new LineSegment(new Point(10,10), new Point(0,0)));
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
  
  @Test
  public void triangulationTest() {

  }
}
