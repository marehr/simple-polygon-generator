package polygonsSWP.tests.geometry;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Triangle;
import polygonsSWP.util.GeneratorUtils;


public class OrderedListPolygonTest
{
  @Test
  public void testGetSurfaceArea() {
    OrderedListPolygon p = new OrderedListPolygon();
    p.addPoint(new Point(1, 1));
    p.addPoint(new Point(4, 1));
    p.addPoint(new Point(4, 4));
    assertTrue(4.5d == p.getSurfaceArea());
  }
  
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

  @Test
  public void testTriangulate() {
    OrderedListPolygon poly = new OrderedListPolygon();

    poly.addPoint(new Point(7, 1));
    poly.addPoint(new Point(0, 0));
    poly.addPoint(new Point(10, 0));
    poly.addPoint(new Point(15, 6));
    
    List<Triangle> l = poly.triangulate();
    
    assertTrue(l.size() == 2);
    
    double area = l.get(0).getSurfaceArea() + l.get(1).getSurfaceArea();
    assertTrue(area == poly.getSurfaceArea());
  }
}
