package polygonsSWP.tests;

import org.junit.Test;

import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.util.SeidelTrapezoidation;

public class SeidelTrapezoidationTest
{
  @Test
  public void testGenerateTrapezoidation() {   
    // Test horizontal edges.
    OrderedListPolygon poly = new OrderedListPolygon();
    poly.addPoint(new Point(20, 20));   
    poly.addPoint(new Point(50, 50));
    poly.addPoint(new Point(70, 80));
    poly.addPoint(new Point(30, 100));
    poly.addPoint(new Point(-20, 100));
    System.out.println(SeidelTrapezoidation.generateTrapezoidation(poly));
  }

}
