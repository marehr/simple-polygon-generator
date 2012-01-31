package polygonsSWP.gui.generation;
import polygonsSWP.gui.generation.HistorySceneChooser.HistorySceneMode;

public interface HistorySceneModeListener
{
  /**
   * Event emitted when the histor scene mode was switched.
   * mode can be CREATE_AND_SHOW, CREATE and DISABLE
   * 
   * @param mode the mode switched to
   */
  public void onHistorySceneModeSwitched(HistorySceneMode mode);
}