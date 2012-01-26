package polygonsSWP.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import polygonsSWP.data.listener.HistoryListener;


/**
 * THis object contains a ordered List of scenes and can be used to iterate over
 * them and display them.
 * 
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 */

public class PolygonHistory implements History
{
  private List<Scene> sceneList = Collections.synchronizedList(new ArrayList<Scene>());
  private HistoryListener listener = null;

  @Override
  public Scene newScene() {
    return new HistoryScene(this);
  }

  @Override
  public void setHistoryListener(HistoryListener listener) {
    this.listener = listener;
  }

  @Override
  public void addScene(Scene newScene) {
    sceneList.add(newScene);

    if(listener != null)
      listener.onHistorySave(this, newScene);
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
