package polygonsSWP.tests.geometry;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public class PolygonIntersectionTest {

  static List<Point> points = Arrays.asList(
      new Point(3, 2), new Point(6, 2), new Point(7, 4),
      new Point(6, 6), new Point(3, 6), new Point(2, 4)
  );


  @Test
  public void lineSegmentPointsAreOutsideOfThePolygon(){
    Polygon polygon = new OrderedListPolygon(points);

    // hat zwei ueberschneidungen, beide punkte ausserhalb des polygons
    LineSegment line = new LineSegment(new Point(1, 1), new Point(7, 6));
    List<Point[]> intersects = polygon.intersect(line);

    assertNotNull(intersects);
    assertEquals(2, intersects.size());

    // Wir erwarten nach Javadoc hier den Schnittpunkt + die beiden Endpunkte
    // der Kante des Polygons, in Reihenfolge wie im Polygon.
    
    assertEquals(new Point(2.765, 2.471).toString(), intersects.get(0)[0].toString());
    assertEquals(new Point(2, 4), intersects.get(0)[1]);
    assertEquals(new Point(3, 2), intersects.get(0)[2]);

    assertEquals(new Point(6.294, 5.412).toString(), intersects.get(1)[0].toString());
    assertEquals(new Point(7, 4), intersects.get(1)[1]);
    assertEquals(new Point(6, 6), intersects.get(1)[2]);
  }


  @Test
  public void intersectionPointIsVertexOfThePolygon(){
    Polygon polygon = new OrderedListPolygon(points);

    // hat zwei ueberschneidungen, ein punkte ist eine ecke des polygons
    LineSegment line = new LineSegment(new Point(2, 1), new Point(7, 6));
    List<Point[]> intersects = polygon.intersect(line);

    assertNotNull(intersects);
    assertEquals(2, intersects.size());

    // schnittpunkt geht genau durch einen punkt vom polygon
    // dann ist das format [point, null, null]
    assertEquals(new Point(3, 2), intersects.get(0)[0]);
    assertEquals(null, intersects.get(0)[1]);
    assertEquals(null, intersects.get(0)[2]);

    assertEquals(new Point(6.333, 5.333).toString(), intersects.get(1)[0].toString());
    assertEquals(new Point(7, 4), intersects.get(1)[1]);
    assertEquals(new Point(6, 6), intersects.get(1)[2]);



    // der Schnittpunkt ist genau auf einem Punkt des Polygons
    line = new LineSegment(new Point(3, 6), new Point(3, 7));
    intersects = polygon.intersect(line);

    assertNotNull(intersects);
    assertEquals(1, intersects.size());

    // schnittpunkt ist genau ein punkt vom polygon,
    // dann ist das format [point, null, null]
    assertEquals(new Point(3, 6), intersects.get(0)[0]);
    assertEquals(null, intersects.get(0)[1]);
    assertEquals(null, intersects.get(0)[2]);
  }


  @Test
  public void intersectionPointIsOnThePolygon(){
    Polygon polygon = new OrderedListPolygon(points);

    // der Schnittpunkt ist genau auf einer Kante des Polygons
    LineSegment line = new LineSegment(new Point(5, 6), new Point(5, 7));
    List<Point[]> intersects = polygon.intersect(line);

    assertNotNull(intersects);
    assertEquals(1, intersects.size());

    // das ist ein ganz normaler schnittpunkt, darum hat er
    // das format [point, lineseg[0], lineseg[1]]
    assertEquals(new Point(5, 6), intersects.get(0)[0]);
    assertEquals(new Point(6, 6), intersects.get(0)[1]);
    assertEquals(new Point(3, 6), intersects.get(0)[2]);
  }


  @Test
  public void intersectionLineSegment(){
    Polygon polygon = new OrderedListPolygon(points);

    // Es gibt eine Schnittkante
    LineSegment line = new LineSegment(new Point(2, 2), new Point(4, 2));
    List<Point[]> intersects = polygon.intersect(line);

    assertNotNull(intersects);
    assertEquals(1, intersects.size());

    // eine Kante des Polygons und die Strecke liegt teilweise auf einander
    // das format [null, lineseg[0], lineseg[1]]
    assertEquals(null, intersects.get(0)[0]);
    assertEquals(new Point(3, 2), intersects.get(0)[1]);
    assertEquals(new Point(6, 2), intersects.get(0)[2]);

    // es gibt eine schnittkante, aber ein punkt ist eckpunkt
    line = new LineSegment(new Point(3, 2), new Point(4, 2));
    intersects = polygon.intersect(line);

    assertNotNull(intersects);
    assertEquals(1, intersects.size());

    // das format sollte auch [null, lineseg[0], lineseg[1]] sein
    assertEquals(null, intersects.get(0)[0]);
    assertEquals(new Point(3, 2), intersects.get(0)[1]);
    assertEquals(new Point(6, 2), intersects.get(0)[2]);
    
    // es gibt eine Schnittkante und sie ist auf liegt komplett in/auf einer
    // Kante des Polygons
    line = new LineSegment(new Point(5, 2), new Point(4, 2));
    intersects = polygon.intersect(line);

    assertNotNull(intersects);
    assertEquals(1, intersects.size());

    // das format sollte auch [null, lineseg[0], lineseg[1]] sein
    assertEquals(null, intersects.get(0)[0]);
    assertEquals(new Point(3, 2), intersects.get(0)[1]);
    assertEquals(new Point(6, 2), intersects.get(0)[2]);
  }

}
