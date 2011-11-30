package polygonsSWP.tests.geometry;

import static org.junit.Assert.*;

import org.junit.Test;

import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;


public class OrderedListPolygonTest
{
  @Test
  public void testIsSimple() {
    // Simple polygon
    OrderedListPolygon p = new OrderedListPolygon();
    p.addPoint(new Point(0, 0));
    p.addPoint(new Point(10, 0));
    p.addPoint(new Point(0, 10));
    assertTrue(p.isSimple());

    // Complex polygon
    p.addPoint(new Point(10, 10));
    assertFalse(p.isSimple());
    
    // point a of edge ab lying on edge cd
    p = new OrderedListPolygon();
    p.addPoint(new Point(0, 0));
    p.addPoint(new Point(10, 0));
    p.addPoint(new Point(10, 10));
    p.addPoint(new Point(5, 0));
    p.addPoint(new Point(0, 10));
    assertFalse(p.isSimple());
    
    // edge ab lying on edge cd
    p = new OrderedListPolygon();
    p.addPoint(new Point(0, 0));
    p.addPoint(new Point(10, 0));
    p.addPoint(new Point(10, 10));
    p.addPoint(new Point(7, 0));
    p.addPoint(new Point(3, 0));
    p.addPoint(new Point(0, 10));
    assertFalse(p.isSimple());
  }

  @Test
  public void testEquals() {
    OrderedListPolygon p = new OrderedListPolygon();
    p.addPoint(new Point(0, 0));
    p.addPoint(new Point(10, 0));
    p.addPoint(new Point(0, 10));

    OrderedListPolygon p1 = new OrderedListPolygon();
    p1.addPoint(new Point(0, 0));
    p1.addPoint(new Point(10, 0));
    p1.addPoint(new Point(0, 10));

    assertTrue(p.equals(p1));
    p = new OrderedListPolygon();
    p.addPoint(new Point(10, 0));
    p.addPoint(new Point(0, 10));
    p.addPoint(new Point(0, 0));

    p1 = new OrderedListPolygon();
    p1.addPoint(new Point(0, 0));
    p1.addPoint(new Point(10, 0));
    p1.addPoint(new Point(0, 10));

    assertTrue(p.equals(p1));
    p = new OrderedListPolygon();
    p.addPoint(new Point(0, 10));
    p.addPoint(new Point(0, 0));
    p.addPoint(new Point(10, 0));

    p1 = new OrderedListPolygon();
    p1.addPoint(new Point(0, 0));
    p1.addPoint(new Point(10, 0));
    p1.addPoint(new Point(0, 10));

    assertTrue(p.equals(p1));
    p = new OrderedListPolygon();
    p.addPoint(new Point(1, 10));
    p.addPoint(new Point(0, 0));
    p.addPoint(new Point(10, 0));

    p1 = new OrderedListPolygon();
    p1.addPoint(new Point(0, 0));
    p1.addPoint(new Point(10, 0));
    p1.addPoint(new Point(0, 10));
    assertFalse(p.equals(p1));
  }
}
