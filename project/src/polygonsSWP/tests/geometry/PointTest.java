package polygonsSWP.tests.geometry;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import polygonsSWP.geometry.Point;

public class PointTest
{
  @Test
  public void compareToTest() {
    Point first = new Point(0,0);
    assertEquals(first.compareTo(new Point(0,0)), 0);
    assertEquals(first.compareTo(new Point(1,0)), -1);
    assertEquals(first.compareTo(new Point(1,1)), -1);
    assertEquals(first.compareTo(new Point(0,-1)), 1);
    assertEquals(first.compareTo(new Point(-1,0)), 1);
  }
}
