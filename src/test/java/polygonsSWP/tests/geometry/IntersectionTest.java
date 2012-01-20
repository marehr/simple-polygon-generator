package polygonsSWP.tests.geometry;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.Point;

public class IntersectionTest
{
  // TODO TODO TODO TODO
  @Test
  public void testEndpoint() {
    Point a = new Point(0, 0);
    Point b = new Point(0, 10);
    Point c = new Point(0, 20);
    Point d = new Point(0, 30);
    LineSegment l1 = new LineSegment(a, b);
    LineSegment l2 = new LineSegment(c, d);
    
    Point[] isects = l1.intersect(l2);
    assertTrue(isects == null);
  }
}
