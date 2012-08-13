package polygonsSWP.tests.geometry;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import polygonsSWP.geometry.Point;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public class PointTest
{
  @Test
  public void compareToTest() {
    Point first = new Point(0,0);
    assertEquals(first.compareTo(new Point(0,0)), 0);
    assertEquals(first.compareTo(new Point(1,0)), -1);
    assertEquals(first.compareTo(new Point(1,1)), -1);
    assertEquals(first.compareTo(new Point(0,-1)), 1);
    assertEquals(first.compareTo(new Point(-1,0)), 1);

    assertEquals(first.compareTo(new Point(-15,2)), 1);
  }

  @Test
  public void compareToByYTest() {
    Point first = new Point(0,0);
    assertEquals(first.compareToByY(new Point(0,0)), 0);
    assertEquals(first.compareToByY(new Point(1,0)), -1);
    assertEquals(first.compareToByY(new Point(1,1)), -1);
    assertEquals(first.compareToByY(new Point(0,-1)), 1);
    assertEquals(first.compareToByY(new Point(-1,0)), 1);

    assertEquals(first.compareToByY(new Point(-15,2)), -1);
  }
}
