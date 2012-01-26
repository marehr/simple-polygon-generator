package polygonsSWP.data.listener;

import polygonsSWP.data.History;
import polygonsSWP.data.Scene;

public interface HistoryListener
{

  /**
   * Event emitted when a new Scene was saved
   * @param history 
   * @param scene 
   */
  public void onHistorySave(History history, Scene scene);
}
