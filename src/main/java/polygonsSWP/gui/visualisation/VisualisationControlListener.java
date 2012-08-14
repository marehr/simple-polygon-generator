package polygonsSWP.gui.visualisation;

import polygonsSWP.data.Scene;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public interface VisualisationControlListener
{
  /**
   * Event emitted when a new frame should be drawn to the canvas.
   * @param scene 
   */
  public void onNewScene(Scene scene);
}
