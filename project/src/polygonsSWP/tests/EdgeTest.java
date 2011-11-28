package polygonsSWP.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.Point;

public class EdgeTest
{

  @Test
  public void testIsIntersecting() {
    Point[] isect = new Point[1];
    
    // Intersecting lines
    LineSegment a = new LineSegment(new Point(0, 0), new Point(10, 10));
    LineSegment b = new LineSegment(new Point(10, 0), new Point(0, 10));
    assertTrue(a.isIntersecting(b, isect));
    
    // Not intersecting lines
    b = new LineSegment(new Point(50, 0), new Point(0, 50));
    assertFalse(a.isIntersecting(b, isect));
    
    // Parallel lines (not intersecting)
    b = new LineSegment(new Point(1, 0), new Point(11, 10));
    assertFalse(a.isIntersecting(b, isect));
    
    // Coincident lines (intersecting!)
    b = new LineSegment(new Point(2, 2), new Point(8, 8));
    assertTrue(a.isIntersecting(b, isect));
    
    // Endpoint of a lying on b (intersecting!)
    a = new LineSegment(new Point(0, 0), new Point(10, 0));
    b = new LineSegment(new Point(5, 0), new Point(2, 10));
    assertTrue(a.isIntersecting(b, isect));
    
    // Sharing a point (intersecting!)
    a = new LineSegment(new Point(0, 0), new Point(10, 0));
    b = new LineSegment(new Point(10, 0), new Point(0, 10));
    assertTrue(a.isIntersecting(b, isect));   
  }

}
