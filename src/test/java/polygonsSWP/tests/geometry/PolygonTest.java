package polygonsSWP.tests.geometry;

import static org.junit.Assert.*;

import org.junit.Test;

import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;

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
    
    Point testp = new Point(19, 54);
    assertTrue(poly.containsPoint(testp, true));
  }

}
