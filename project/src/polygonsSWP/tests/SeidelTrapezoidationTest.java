package polygonsSWP.tests;

import java.util.List;

import org.junit.Test;

import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
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
    
    // Test outer inclusive polygons ('U' shaped) + merging.
    poly = new OrderedListPolygon();
    poly.addPoint(new Point(0, 100));
    poly.addPoint(new Point(10, 20));
    poly.addPoint(new Point(80, 10));
    poly.addPoint(new Point(90, 110));
    poly.addPoint(new Point(50, 30));
    List<Polygon> trapezoids = SeidelTrapezoidation.generateTrapezoidation(poly);
    System.out.println(trapezoids.size());
    System.out.println(trapezoids);
  }

}
