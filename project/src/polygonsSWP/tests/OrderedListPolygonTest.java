package polygonsSWP.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import polygonsSWP.data.OrderedListPolygon;
import polygonsSWP.data.Point;

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
  }

}
