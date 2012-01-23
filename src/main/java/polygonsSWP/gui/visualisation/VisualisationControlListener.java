package polygonsSWP.gui.visualisation;

import polygonsSWP.data.Scene;

public interface VisualisationControlListener
{

  /**
   * Event emitted when a new frame should be drawn to the canvas.
   * @param scene 
   */
  public void onNewScene(Scene scene);
}
