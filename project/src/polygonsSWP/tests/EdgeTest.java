package polygonsSWP.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.Point;

public class EdgeTest
{

  @Test
  public void testIsIntersecting() {
    // Intersecting lines
    LineSegment a = new LineSegment(new Point(0, 0), new Point(10, 10));
    LineSegment b = new LineSegment(new Point(10, 0), new Point(0, 10));
    assertTrue(a.isIntersecting(b));
    
    // Not intersecting lines
    b = new LineSegment(new Point(50, 0), new Point(0, 50));
    assertFalse(a.isIntersecting(b));
    
    // Parallel lines (not intersecting)
    b = new LineSegment(new Point(1, 0), new Point(11, 10));
    assertFalse(a.isIntersecting(b));
    
    // Coincident lines (intersecting!)
    b = new LineSegment(new Point(2, 2), new Point(8, 8));
    assertTrue(a.isIntersecting(b));
    
    a = new LineSegment(new Point(0, 0), new Point(10, 0));
    b = new LineSegment(new Point(10, 0), new Point(0, 10));
    assertFalse(a.isIntersecting(b));
  }

}
