package polygonsSWP.tests.geometry;

import static org.junit.Assert.*;

import org.junit.Test;

import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.util.MathUtils;

public class PolygonTest
{
  @Test
  public void testContainsPoint() {
    OrderedListPolygon poly = new OrderedListPolygon();
    poly.addPoint(new Point(0, 100));
    poly.addPoint(new Point(10, 20));
    poly.addPoint(new Point(80, 10));
    poly.addPoint(new Point(90, 110));
    poly.addPoint(new Point(50, 30));

    assertTrue(poly.containsPoint(new Point(19, 54), true));

    assertFalse(poly.containsPoint(new Point(10, 20), false));
    assertTrue(poly.containsPoint(new Point(10, 20), true));
  }

  @Test
  public void testContainsPointFailed() {
    OrderedListPolygon poly = new OrderedListPolygon();
    poly.addPoint(new Point(20, 20));
    poly.addPoint(new Point(40, 40));
    poly.addPoint(new Point(60, 20));
    poly.addPoint(new Point(70, 40));
    poly.addPoint(new Point(70, 70));
    poly.addPoint(new Point(50, 80));
    poly.addPoint(new Point(50, 60));
    poly.addPoint(new Point(40, 60));
    poly.addPoint(new Point(20, 80));
    poly.addPoint(new Point(10, 50));

    /*
     * alle diese punkte liegen in dem polygon, die
     * meisten in irgendwelchen ecken
     */

    // dieser Punkt liegt auf der Geraden (20, 80) -> (40, 60)
    assertTrue(poly.containsPoint(new Point(50, 50), false));
    assertTrue(poly.containsPoint(new Point(50, 50), true));

    assertTrue(poly.containsPoint(new Point(20, 70), false));
    assertTrue(poly.containsPoint(new Point(20, 70), true));

    assertTrue(poly.containsPoint(new Point(60, 30), false));
    assertTrue(poly.containsPoint(new Point(60, 30), true));

    assertTrue(poly.containsPoint(new Point(20, 30), false));
    assertTrue(poly.containsPoint(new Point(20, 30), true));

    assertTrue(poly.containsPoint(new Point(60, 70), false));
    assertTrue(poly.containsPoint(new Point(60, 70), true));

    /*
     * diese punkte haben horizontal drei schnittpunkte
     */
    assertTrue(poly.containsPoint(new Point(20, 40), false));
    assertTrue(poly.containsPoint(new Point(20, 40), true));

    assertTrue(poly.containsPoint(new Point(60, 40), false));
    assertTrue(poly.containsPoint(new Point(60, 40), true));

    assertTrue(poly.containsPoint(new Point(30, 60), false));
    assertTrue(poly.containsPoint(new Point(30, 60), true));

    /*
     * alle diese punkte liegen ausserhalb des polygons
     */
    assertFalse(poly.containsPoint(new Point(40, 70), false));
    assertFalse(poly.containsPoint(new Point(40, 70), true));

    // dieser Punkt liegt auf der geraden (60, 20) -> (40, 40)
    // (failed before fix, with onLine = true)
    assertFalse(poly.containsPoint(new Point(10, 70), false));
    assertFalse(poly.containsPoint(new Point(10, 70), true));

    assertFalse(poly.containsPoint(new Point(80, 50), false));
    assertFalse(poly.containsPoint(new Point(80, 50), true));

    // dieser Punkt liegt auf der geraden (50, 70) -> (50, 40)
    // (failed before fix, with onLine = true)
    assertFalse(poly.containsPoint(new Point(70, 20), false));
    assertFalse(poly.containsPoint(new Point(70, 20), true));

    assertFalse(poly.containsPoint(new Point(40, 30), false));
    assertFalse(poly.containsPoint(new Point(40, 30), true));

    assertFalse(poly.containsPoint(new Point(10, 30), false));
    assertFalse(poly.containsPoint(new Point(10, 30), true));

    /*
     * diese punkte haben horizontal drei schnittpunkte
     */
    assertFalse(poly.containsPoint(new Point(00, 40), false));
    assertFalse(poly.containsPoint(new Point(00, 40), true));

    assertFalse(poly.containsPoint(new Point(10, 80), false));
    assertFalse(poly.containsPoint(new Point(10, 80), true));

    /*
     * Eckpunkte und Punkte die auf einer Kante des Polygons liegen
     */

    // auf der Ecke (40, 40) - (60, 20)
    assertFalse(poly.containsPoint(new Point(50, 30), false));
    assertTrue(poly.containsPoint(new Point(50, 30), true));

    // auf der Ecke (50, 80) - (70, 70)
    for(double i = 0; i <= 10; i+=0.25){
      LineSegment seg0 = new LineSegment(new Point(50, 80), new Point(70, 70)),
                  seg1 = new LineSegment(new Point(50, 70 + i), new Point(70, 70 + i));
      Point isec = seg0.intersect(seg1)[0];

      // der Schnittpunkt muss auf der Ecke des Polygons liegen
      assertEquals(0, MathUtils.checkOrientation(seg0._a, seg0._b, isec));

      assertFalse(poly.containsPoint(isec, false));
      assertTrue(poly.containsPoint(isec, true));
    }
  }

  @Test
  public void testIsClockWise() {
    OrderedListPolygon poly = new OrderedListPolygon();
    poly.addPoint(new Point(20, 20));
    poly.addPoint(new Point(40, 40));
    poly.addPoint(new Point(60, 20));
    poly.addPoint(new Point(70, 40));
    poly.addPoint(new Point(70, 70));
    poly.addPoint(new Point(50, 80));
    poly.addPoint(new Point(50, 60));
    poly.addPoint(new Point(40, 60));
    poly.addPoint(new Point(20, 80));
    poly.addPoint(new Point(10, 50));

    // is counter clockwise
    assertEquals(-1, poly.isClockwise());

    poly.reverse();
    // is counter clockwise
    assertEquals(1, poly.isClockwise());
  }
}
