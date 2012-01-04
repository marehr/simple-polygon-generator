package polygonsSWP.tests.geometry;

import static org.junit.Assert.*;

import org.junit.Test;

import polygonsSWP.geometry.Line;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Ray;

public class LineTest
{

  @Test
  public void testIntersectRay() {
    // line is the x-axis
    Line l = new Line(new Point(0, 0), new Point(10, 0));
    
    // ray starting above the x-axis and going up
    Ray r = new Ray(new Point(2, 1), new Point(2, 10));
    
    assertTrue(l.intersect(r) == null);
  }

  @Test
  public void testIntersectLine() {
    Line l = new Line(new Point(0, 0), new Point(10, 10));
    
    Line l2 = new Line(new Point(10, 0), new Point(0, 10));
    
    Point exp = new Point(5, 5);
    Point ret = l.intersect(l2)[0];

    assertTrue(exp.equals(ret));
  }

}
