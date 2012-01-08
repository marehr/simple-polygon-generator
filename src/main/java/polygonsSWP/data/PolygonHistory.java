package polygonsSWP.data;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * THis object contains a ordered List of scenes and can be used to iterate over
 * them and display them.
 * 
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 */

public class PolygonHistory
  implements History
{
  private LinkedList<Scene> sceneList;

  public PolygonHistory() {
    sceneList = new LinkedList<Scene>();
  }

  @Override
  public Scene newScene() {
    return new HistoryScene(this);
  }

  @Override
  public void addScene(Scene newScene) {
    sceneList.add(newScene);
  }

  @Override
  public List<Scene> getScenes() {
    return sceneList;
  }

  @Override
  public Iterator<Scene> getSceneIterator() {
    return sceneList.iterator();
  }

  @Override
  public void clear() {
    sceneList.clear();
  }

}
