package polygonsSWP.data;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


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

}
