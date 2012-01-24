package polygonsSWP.tests.geometry;

import static org.junit.Assert.*;

import org.junit.Test;

import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.Point;


public class LineSegmentTest
{

  @Test
  public void testIsIntersecting() {
    // Intersecting lines segments
    LineSegment a = new LineSegment(new Point(0, 0), new Point(10, 10));
    LineSegment b = new LineSegment(new Point(10, 0), new Point(0, 10));
    Point[] result = a.intersect(b);
    assertTrue(result != null);
    assertTrue(result.length == 1);
    assertTrue(result[0].getClass() == Point.class);

    // Not intersecting lines segments
    b = new LineSegment(new Point(50, 0), new Point(0, 50));
    assertTrue(a.intersect(b) == null);

    // Parallel lines segments (not intersecting)
    b = new LineSegment(new Point(1, 0), new Point(11, 10));
    assertTrue(a.intersect(b) == null);

    // Coincident lines segments (intersecting!)
    b = new LineSegment(new Point(2, 2), new Point(8, 8));
    result = a.intersect(b);
    assertTrue(result != null);
    assertTrue(result.length == 0);

    // Endpoint of a lying on b (intersecting!)
    a = new LineSegment(new Point(0, 0), new Point(10, 0));
    b = new LineSegment(new Point(5, 0), new Point(2, 10));
    result = a.intersect(b);
    assertTrue(result != null);
    assertTrue(result.length == 1);
    assertTrue(result[0].getClass() == Point.class);

    // Sharing a point (intersecting!)
    a = new LineSegment(new Point(0, 0), new Point(10, 0));
    b = new LineSegment(new Point(10, 0), new Point(0, 10));
    result = a.intersect(b);
    assertTrue(result != null);
    assertTrue(result.length == 1);
    assertTrue(result[0].getClass() == Point.class);
  }

  @Test
  public void containsPoint() {
    LineSegment seg, seg2;

    seg = new LineSegment(new Point(5, 0), new Point(10, 0));

    // die ecken sollten "enthalten"
    assertTrue(seg.containsPoint(new Point(5, 0)));
    assertTrue(seg.containsPoint(new Point(10, 0)));

    // die mitte sollte als drauf erkannt werden
    assertTrue(seg.containsPoint(new Point(7.5, 0)));

    // die sind links bzw. rechts von der Strecke
    assertFalse(seg.containsPoint(new Point(0, 0)));
    assertFalse(seg.containsPoint(new Point(15, 0)));

    // die sind alle parrallel zu der Strecke
    assertFalse(seg.containsPoint(new Point(0, 15)));
    assertFalse(seg.containsPoint(new Point(5, 15)));
    assertFalse(seg.containsPoint(new Point(7.5, 15)));
    assertFalse(seg.containsPoint(new Point(10, 15)));
    assertFalse(seg.containsPoint(new Point(15, 15)));



    seg = new LineSegment(new Point(1, 1), new Point(5, 7));

    // die ecken solten "enthalten"
    assertTrue(seg.containsPoint(new Point(1, 1)));
    assertTrue(seg.containsPoint(new Point(5, 7)));

    // die mitte sollte als drauf erkannt werden
    assertTrue(seg.containsPoint(new Point(3, 4)));

    // die sind links bzw. rechts von der Strecke
    assertFalse(seg.containsPoint(new Point(-1, -2)));
    assertFalse(seg.containsPoint(new Point(7, 10)));

    // irgendwo liegend
    assertFalse(seg.containsPoint(new Point(0, 0)));
    assertFalse(seg.containsPoint(new Point(10, 15)));


    // lassen ein Paar Strecken schneiden und schauen, ob diese Schnittpunkte
    // auf der Strecke liegt
    for(int i = 0; i < 7; ++i){
      seg2 = new LineSegment(new Point(i - 1, i + 1), new Point(i + 1, i + 1));
      Point[] isec = seg.intersect(seg2);

      assertEquals(1, isec.length);
      assertTrue(seg.containsPoint(isec[0]));
    }
  }

}
