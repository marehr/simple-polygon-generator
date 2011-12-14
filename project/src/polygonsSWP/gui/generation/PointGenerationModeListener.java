package polygonsSWP.gui.generation;

import java.util.List;

import polygonsSWP.geometry.Point;

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
