package polygonsSWP.gui.generation;

import java.util.List;

import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;

public interface PolygonGenerationPanelListener
{
  /**
   * Event emitted when the polygon generation was started.
   */
  public void onPolygonGenerationStarted();
  
  /**
   * Event emitted when the polygon generation has finished.
   * 
   * @param newPolygon the newly generated polygon
   */
  public void onPolygonGenerated(Polygon newPolygon);
  
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
