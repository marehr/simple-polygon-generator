package polygonsSWP.data;

import java.util.Iterator;
import java.util.List;


/**
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
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
