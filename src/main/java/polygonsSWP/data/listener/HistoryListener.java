package polygonsSWP.data.listener;

import polygonsSWP.data.History;
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
public interface HistoryListener
{
  /**
   * Event emitted when a new Scene was saved
   * @param history 
   * @param scene 
   */
  public void onHistorySave(History history, Scene scene);
}
