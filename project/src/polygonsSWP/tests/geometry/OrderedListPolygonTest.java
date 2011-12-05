package polygonsSWP.tests.geometry;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import polygonsSWP.generators.PermuteAndReject;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.TwoOptMoves;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.geometry.Triangle;


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

  @Test
  public void testTriangulate() {
    Map<PolygonGenerator.Parameters, Object> params =
        new HashMap<PolygonGenerator.Parameters, Object>();
    for (int i = 3; i < 1000; i++) {
      params.put(PolygonGenerator.Parameters.n, i);
      params.put(PolygonGenerator.Parameters.size, 10000);
      PolygonGenerator gen = new PermuteAndReject();
      OrderedListPolygon poly = (OrderedListPolygon) gen.generate(params, null);
      for (Triangle item : poly.triangulate())
        System.out.println(item);
      assertTrue(poly.triangulate().size()>=poly.size()/2);
    }
  }
}
