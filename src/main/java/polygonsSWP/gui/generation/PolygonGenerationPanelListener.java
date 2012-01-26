package polygonsSWP.gui.generation;

import polygonsSWP.data.PolygonHistory;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.geometry.Polygon;

public interface PolygonGenerationPanelListener
{
  /**
   * Event emitted when the polygon generation was started.
   * @param stats statistical information about generated polygon
   *        and the generator's run. May be null.
   * @param history the step-by-step visualisation of the generation.
   *        May be null.
   */
  public void onPolygonGenerationStarted(PolygonStatistics stats, PolygonHistory steps);
  
  /**
   * Event emitted when the polygon generation was cancelled
   * (as requested by user).
   */
  public void onPolygonGenerationCancelled();
  
  /**
   * Event emitted when the polygon generation has finished.
   * 
   * @param newPolygon the newly generated polygon
   * @param stats statistical information about generated polygon
   *        and the generator's run. May be null.
   * @param history the step-by-step visualisation of the generation.
   * 				May be null.
   */
  public void onPolygonGenerated(Polygon newPolygon, PolygonStatistics stats, PolygonHistory history);
}
