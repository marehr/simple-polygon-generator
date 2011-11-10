package polygonsSWP.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import polygonsSWP.data.Edge;
import polygonsSWP.data.Point;

public class EdgeTest
{

  @Test
  public void testIsIntersecting() {
    // Intersecting lines
    Edge a = new Edge(new Point(0, 0), new Point(10, 10));
    Edge b = new Edge(new Point(10, 0), new Point(0, 10));
    assertTrue(a.isIntersecting(b));
    
    // Not intersecting lines
    b = new Edge(new Point(50, 0), new Point(0, 50));
    assertFalse(a.isIntersecting(b));
    
    // Parallel lines (not intersecting)
    b = new Edge(new Point(1, 0), new Point(11, 10));
    assertFalse(a.isIntersecting(b));
    
    // Coincident lines (intersecting!)
    b = new Edge(new Point(2, 2), new Point(8, 8));
    assertTrue(a.isIntersecting(b));
    
    a = new Edge(new Point(0, 0), new Point(10, 0));
    b = new Edge(new Point(10, 0), new Point(0, 10));
    assertFalse(a.isIntersecting(b));
  }

}
