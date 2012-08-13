package polygonsSWP.tests.geometry;

import static org.junit.Assert.*;

import org.junit.Test;

import polygonsSWP.geometry.Line;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Ray;
import polygonsSWP.util.MathUtils;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
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
  
  @Test
  public void testCuttingAngle(){
    // l1 horizontal: m1 = 0
    Line l1 = new Line(new Point(2, 2), new Point(3, 2)); 
    
    // l2 horizontal: m2 = 0, l2 parallel to l1 
    Line l2 = new Line(new Point(3, 3), new Point(4, 3));
    
    // l3 vertical, orthogonal to l1, l2
    Line l3 = new Line(new Point(1, 1), new Point(1, 3));
    
    // gradient l4: m4 = 1/3
    Line l4 = new Line(new Point(1, 2), new Point(4, 3));
    
    // gradient l5: m5 = 3
    Line l5 = new Line(new Point(2, 3), new Point(3, 6));
    
    // gradient l6: m6 = -1/3
    Line l6 = new Line(new Point(2, 5), new Point(-1, 6));
    
    // gradient l7: m7 = -3
    Line l7 = new Line(new Point(4, 2), new Point(3, 5));
    
    // parallel
    assertEquals(0.0, l1.cuttingAngle(l2), MathUtils.EPSILON);
    // orthogonal
    assertEquals(90.0, l2.cuttingAngle(l3), MathUtils.EPSILON);
    // one vertical but not orthogonal to other
    assertEquals(-71.565, l3.cuttingAngle(l4), 0.05);
    assertEquals(71.565, l4.cuttingAngle(l3), 0.05);
    // one horizontal, cutting angles with l4-l7
    // need angles !!!
    assertEquals(18.4349, l2.cuttingAngle(l4), 0.05);
    assertEquals(71.5651, l2.cuttingAngle(l5), 0.05);
    assertEquals(-18.4349, l2.cuttingAngle(l6), 0.05);
    assertEquals(-71.5651, l2.cuttingAngle(l7), 0.05);
    assertEquals(-18.4349, l4.cuttingAngle(l2), 0.05);
    assertEquals(-71.5651, l5.cuttingAngle(l2), 0.05);
    assertEquals(18.4349, l6.cuttingAngle(l2), 0.05);
    assertEquals(71.5651, l7.cuttingAngle(l2), 0.05);
    // two orthogonal lines, none horizontal
    assertEquals(90.0, l5.cuttingAngle(l6), 0.05);
    assertEquals(90.0, l6.cuttingAngle(l5), 0.05);
    // two arbitrary lines
    assertEquals(36.8699, l5.cuttingAngle(l7), 0.05);
    assertEquals(-36.8699, l7.cuttingAngle(l5), 0.05);
  }
}
