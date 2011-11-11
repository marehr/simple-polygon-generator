package polygonsSWP.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import polygonsSWP.data.Point;
import polygonsSWP.util.MathUtils;


;

/**
 * Test environment for MathUtils.
 * 
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 */
public class MathUtilsTest
{
  /**
   * Test for CheckOrientation
   */
  @Test
  public void testCheckOrientation() {
    // Point is on the segment
    Point begin = new Point(0, 0);
    Point end = new Point(10, 0);
    Point toCheck = new Point(5, 0);
    assertEquals(MathUtils.checkOrientation(begin, end, toCheck), 0);
    // Point is on the left side
    toCheck.y = 1;
    assertEquals(MathUtils.checkOrientation(begin, end, toCheck), 1);
    // Point is on the right side
    toCheck.y = -1;
    assertEquals(MathUtils.checkOrientation(begin, end, toCheck), -1);
  }
}
