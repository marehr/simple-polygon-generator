package polygonsSWP.tests.geometry;

import static org.junit.Assert.*;

import org.junit.Test;

import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Ray;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public class RayTest
{
  @Test
  public void containsPoint(){
    Ray r = new Ray(new Point(5, 5), new Point(20, 20));

    // eckpunkte
    assertEquals(true, r.containsPoint(new Point(5, 5)));
    assertEquals(true, r.containsPoint(new Point(20, 20)));

    // punkte die auf dem strahl liegen
    assertEquals(true, r.containsPoint(new Point(10, 10)));
    assertEquals(true, r.containsPoint(new Point(15, 15)));
    assertEquals(true, r.containsPoint(new Point(30, 30)));

    // punkte die NICHT auf dem strahl liegen
    assertEquals(false, r.containsPoint(new Point(0, 0)));
    assertEquals(false, r.containsPoint(new Point(-5, -5)));

    // punkte die neben der geraden des strahls liegen
    assertEquals(false, r.containsPoint(new Point(15, 14)));
    assertEquals(false, r.containsPoint(new Point(15, 16)));

    /**
     * horizontal Ray
     */
    {
      Ray hRay = new Ray(new Point(0, 20), new Point(20, 20));

      // eckpunkte
      assertEquals(true, hRay.containsPoint(new Point(0, 20)));
      assertEquals(true, hRay.containsPoint(new Point(20, 20)));

      // punkte die auf dem strahl liegen
      assertEquals(true, hRay.containsPoint(new Point(13, 20)));
      assertEquals(true, hRay.containsPoint(new Point(25, 20)));

      // punkte die NICHT auf dem strahl liegen
      assertEquals(false, hRay.containsPoint(new Point(-5, 20)));

      // punkte die neben der geraden des strahls liegen
      assertEquals(false, hRay.containsPoint(new Point(-5, 19)));
      assertEquals(false, hRay.containsPoint(new Point(25, 21)));
    }

    {
      Ray hRay = new Ray(new Point(0, 0), new Point(20, 0));

      // eckpunkte
      assertEquals(true, hRay.containsPoint(new Point(0, 0)));
      assertEquals(true, hRay.containsPoint(new Point(20, 0)));

      // punkte die auf dem strahl liegen
      assertEquals(true, hRay.containsPoint(new Point(13, 0)));
      assertEquals(true, hRay.containsPoint(new Point(25, 0)));

      // punkte die NICHT auf dem strahl liegen
      assertEquals(false, hRay.containsPoint(new Point(-5, 0)));

      // punkte die neben der geraden des strahls liegen
      assertEquals(false, hRay.containsPoint(new Point(-5, 19)));
      assertEquals(false, hRay.containsPoint(new Point(25, 21)));
    }

    /**
     * vertical Ray
     */
    {
      Ray vRay = new Ray(new Point(15, 5), new Point(15, 25));

      // eckpunkte
      assertEquals(true, vRay.containsPoint(new Point(15, 5)));
      assertEquals(true, vRay.containsPoint(new Point(15, 25)));

      // punkte die auf dem strahl liegen
      assertEquals(true, vRay.containsPoint(new Point(15, 20)));
      assertEquals(true, vRay.containsPoint(new Point(15, 30)));

      // punkte die NICHT auf dem strahl liegen
      assertEquals(false, vRay.containsPoint(new Point(15, -5)));

      // punkte die neben der geraden des strahls liegen
      assertEquals(false, vRay.containsPoint(new Point(16, 20)));
      assertEquals(false, vRay.containsPoint(new Point(16, -5)));
      assertEquals(false, vRay.containsPoint(new Point(16, 30)));
    }

    {
      Ray vRay = new Ray(new Point(0, 5), new Point(0, 25));

      // eckpunkte
      assertEquals(true, vRay.containsPoint(new Point(0, 5)));
      assertEquals(true, vRay.containsPoint(new Point(0, 25)));

      // punkte die auf dem strahl liegen
      assertEquals(true, vRay.containsPoint(new Point(0, 20)));
      assertEquals(true, vRay.containsPoint(new Point(0, 30)));

      // punkte die NICHT auf dem strahl liegen
      assertEquals(false, vRay.containsPoint(new Point(0, -5)));

      // punkte die neben der geraden des strahls liegen
      assertEquals(false, vRay.containsPoint(new Point(16, 20)));
      assertEquals(false, vRay.containsPoint(new Point(16, -5)));
      assertEquals(false, vRay.containsPoint(new Point(16, 30)));
    }
  }
}
