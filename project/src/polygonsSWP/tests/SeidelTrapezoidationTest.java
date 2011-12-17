package polygonsSWP.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.util.SeidelTrapezoidation;

public class SeidelTrapezoidationTest
{

  @Test
  public void testGenerateTrapezoidation() {
    OrderedListPolygon poly = new OrderedListPolygon();
    poly.addPoint(new Point(2, 2));   
    poly.addPoint(new Point(5, 5));
    poly.addPoint(new Point(3, 10));
    System.out.println(SeidelTrapezoidation.generateTrapezoidation(poly));
  }

}
