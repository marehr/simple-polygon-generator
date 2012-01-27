package polygonsSWP.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import polygonsSWP.data.listener.HistoryListener;


/**
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 */

public class History
{
  private List<Scene> sceneList = Collections.synchronizedList(new ArrayList<Scene>());
  private HistoryListener listener = null;

  public Scene newScene() {
    return new HistoryScene(this);
  }

  public void setHistoryListener(HistoryListener listener) {
    this.listener = listener;
  }

  public void addScene(Scene newScene) {
    sceneList.add(newScene);

    if(listener != null)
      listener.onHistorySave(this, newScene);
  }

  public List<Scene> getScenes() {
    return sceneList;
  }

  public Iterator<Scene> getSceneIterator() {
    return sceneList.iterator();
  }

  public void clear() {
    sceneList.clear();
  }

}
