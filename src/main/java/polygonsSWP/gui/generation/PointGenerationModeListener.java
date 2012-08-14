package polygonsSWP.gui.generation;

import java.util.List;

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
public interface PointGenerationModeListener
{
  /**
   * Event emitted when the point generation mode was switched.
   * Modes are randomPoints == true, i.e. points are to be generated
   * randomly at runtime, randomPoints == false, which means
   * points are selected by user.
   * 
   * @param randomPoints true, if random points, false, if user-selected.
   * @param points reference to list of user-selected points or
   *        null in case of random points.
   */
  public void onPointGenerationModeSwitched(boolean randomPoints, List<Point> points);
}
