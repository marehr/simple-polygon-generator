package polygonsSWP.gui.generation;

import polygonsSWP.data.PolygonHistory;
import polygonsSWP.geometry.Polygon;

public interface PolygonGenerationPanelListener
{
  /**
   * Event emitted when the polygon generation was started.
   */
  public void onPolygonGenerationStarted();
  
  /**
   * Event emitted when the polygon generation was cancelled
   * (as requested by user).
   */
  public void onPolygonGenerationCancelled();
  
  /**
   * Event emitted when the polygon generation has finished.
   * 
   * @param newPolygon the newly generated polygon
   * @param history the step-by-step visualisation of the generation.
   * 				May be null.
   */
  public void onPolygonGenerated(Polygon newPolygon, PolygonHistory history);
}
