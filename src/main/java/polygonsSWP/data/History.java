package polygonsSWP.data;

import java.util.Iterator;
import java.util.List;

import polygonsSWP.data.listener.HistoryListener;


/**
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 */

public interface History
{

  /**
   * This method creates a new scene and returns it.
   * 
   * @return the new scene
   */
  public Scene newScene();

  /**
   * TODO: describe me
   * @param listener
   */
  public void setHistoryListener(HistoryListener listener);

  /**
   * This adds the scene to the history object.
   * 
   * @param newScene the new scene
   */
  public void addScene(Scene newScene);

  /**
   * Returns a ordered list of all scenes
   * 
   * @return ordered list of scenes
   */
  public List<Scene> getScenes();

  /**
   * Returns an iterator for all the scenes in the history object
   * 
   * @return
   */
  public Iterator<Scene> getSceneIterator();

  /**
   * Clear out every scene data so the history can be reused.
   */
  public void clear();
}
