package polygonsSWP.gui.generation;
import polygonsSWP.gui.generation.HistorySceneChooser.HistorySceneMode;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public interface HistorySceneModeListener
{
  /**
   * Event emitted when the histor scene mode was switched.
   * mode can be CREATE_AND_SHOW, CREATE, LAST_SHOW  or DISABLE
   * 
   * @param mode the mode switched to
   */
  public void onHistorySceneModeSwitched(HistorySceneMode mode);
}