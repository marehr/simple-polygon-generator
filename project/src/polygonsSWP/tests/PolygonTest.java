package polygonsSWP.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import polygonsSWP.data.Point;
import polygonsSWP.data.Polygon;

public class PolygonTest
{
  @Test
  public void testIsSimple() {
    // Simple polygon
    Polygon p = new Polygon();
    p.addPoint(new Point(0, 0));
    p.addPoint(new Point(10, 0));
    p.addPoint(new Point(0, 10));
    assertTrue(p.isSimple());
    
    // Complex polygon
    p.addPoint(new Point(10, 10));
    assertFalse(p.isSimple());
  }

}
