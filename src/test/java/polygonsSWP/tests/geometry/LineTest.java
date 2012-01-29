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

  @Test
  public void containsPoint(){
    Line l = new Line(new Point(5, 5), new Point(20, 20));

    // eckpunkte
    assertEquals(true, l.containsPoint(new Point(5, 5)));
    assertEquals(true, l.containsPoint(new Point(20, 20)));

    // punkte die auf der geraden liegen
    assertEquals(true, l.containsPoint(new Point(10, 10)));
    assertEquals(true, l.containsPoint(new Point(15, 15)));
    assertEquals(true, l.containsPoint(new Point(0, 0)));
    assertEquals(true, l.containsPoint(new Point(30, 30)));
    assertEquals(true, l.containsPoint(new Point(-5, -5)));

    // punkte die NICHT auf der geraden liegen
    assertEquals(false, l.containsPoint(new Point(15, 14)));
    assertEquals(false, l.containsPoint(new Point(15, 16)));

    /**
     * horizontal line
     */
    {
      Line hLine = new Line(new Point(0, 20), new Point(20, 20));

      // eckpunkte
      assertEquals(true, hLine.containsPoint(new Point(0, 20)));
      assertEquals(true, hLine.containsPoint(new Point(20, 20)));

      // punkte die auf der geraden liegen
      assertEquals(true, hLine.containsPoint(new Point(13, 20)));
      assertEquals(true, hLine.containsPoint(new Point(-5, 20)));
      assertEquals(true, hLine.containsPoint(new Point(25, 20)));

      // punkte die NICHT auf der geraden liegen
      assertEquals(false, hLine.containsPoint(new Point(-5, 19)));
      assertEquals(false, hLine.containsPoint(new Point(25, 21)));
    }

    {
      Line hLine = new Line(new Point(0, 0), new Point(20, 0));

      // eckpunkte
      assertEquals(true, hLine.containsPoint(new Point(0, 0)));
      assertEquals(true, hLine.containsPoint(new Point(20, 0)));

      // punkte die auf der geraden liegen
      assertEquals(true, hLine.containsPoint(new Point(13, 0)));
      assertEquals(true, hLine.containsPoint(new Point(-5, 0)));
      assertEquals(true, hLine.containsPoint(new Point(25, 0)));

      // punkte die NICHT auf der geraden liegen
      assertEquals(false, hLine.containsPoint(new Point(-5, 19)));
      assertEquals(false, hLine.containsPoint(new Point(25, 21)));
    }

    /**
     * vertical line
     */
    {
      Line vLine = new Line(new Point(15, 5), new Point(15, 25));

      // eckpunkte
      assertEquals(true, vLine.containsPoint(new Point(15, 5)));
      assertEquals(true, vLine.containsPoint(new Point(15, 25)));

      // punkte die auf der geraden liegen
      assertEquals(true, vLine.containsPoint(new Point(15, 20)));
      assertEquals(true, vLine.containsPoint(new Point(15, -5)));
      assertEquals(true, vLine.containsPoint(new Point(15, 30)));


      assertEquals(false, vLine.containsPoint(new Point(16, 20)));
      assertEquals(false, vLine.containsPoint(new Point(16, -5)));
      assertEquals(false, vLine.containsPoint(new Point(16, 30)));
    }

    {
      Line vLine = new Line(new Point(0, 5), new Point(0, 25));

      // eckpunkte
      assertEquals(true, vLine.containsPoint(new Point(0, 5)));
      assertEquals(true, vLine.containsPoint(new Point(0, 25)));

      // punkte die auf der geraden liegen
      assertEquals(true, vLine.containsPoint(new Point(0, 20)));
      assertEquals(true, vLine.containsPoint(new Point(0, -5)));
      assertEquals(true, vLine.containsPoint(new Point(0, 30)));


      assertEquals(false, vLine.containsPoint(new Point(16, 20)));
      assertEquals(false, vLine.containsPoint(new Point(16, -5)));
      assertEquals(false, vLine.containsPoint(new Point(16, 30)));
      }
  }
}
