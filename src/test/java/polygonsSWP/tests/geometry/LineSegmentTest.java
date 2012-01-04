package polygonsSWP.tests.geometry;

import static org.junit.Assert.*;

import org.junit.Test;

import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.Point;


public class LineSegmentTest
{

  @Test
  public void testIsIntersecting() {
    Point[] isect = new Point[1];

    // Intersecting lines segments
    LineSegment a = new LineSegment(new Point(0, 0), new Point(10, 10));
    LineSegment b = new LineSegment(new Point(10, 0), new Point(0, 10));
    Point[] result = a.intersect(b);
    assertTrue(result != null);
    assertTrue(result.length == 1);
    assertTrue(result[0].getClass() == Point.class);

    // Not intersecting lines segments
    b = new LineSegment(new Point(50, 0), new Point(0, 50));
    assertTrue(a.intersect(b) == null);

    // Parallel lines segments (not intersecting)
    b = new LineSegment(new Point(1, 0), new Point(11, 10));
    assertTrue(a.intersect(b) == null);

    // Coincident lines segments (intersecting!)
    b = new LineSegment(new Point(2, 2), new Point(8, 8));
    result = a.intersect(b);
    assertTrue(result != null);
    assertTrue(result.length == 0);

    // Endpoint of a lying on b (intersecting!)
    a = new LineSegment(new Point(0, 0), new Point(10, 0));
    b = new LineSegment(new Point(5, 0), new Point(2, 10));
    result = a.intersect(b);
    assertTrue(result != null);
    assertTrue(result.length == 1);
    assertTrue(result[0].getClass() == Point.class);

    // Sharing a point (intersecting!)
    a = new LineSegment(new Point(0, 0), new Point(10, 0));
    b = new LineSegment(new Point(10, 0), new Point(0, 10));
    result = a.intersect(b);
    assertTrue(result != null);
    assertTrue(result.length == 1);
    assertTrue(result[0].getClass() == Point.class);
  }

}
