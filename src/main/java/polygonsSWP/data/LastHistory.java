package polygonsSWP.data;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import polygonsSWP.data.listener.HistoryListener;


/**
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 */

public class LastHistory extends History
{
  private volatile Scene lastScene = null;
  private HistoryListener listener = null;

  public LastHistory(int boundingBox) {
    super(boundingBox);
  }

  public Scene newScene() {
    return new HistoryScene(this);
  }

  public void setHistoryListener(HistoryListener listener) {
    this.listener = listener;
  }

  public void addScene(Scene newScene) {
    lastScene = newScene;

    if(listener != null)
      listener.onHistorySave(this, lastScene);
  }

  public List<Scene> getScenes() {
    if(lastScene == null) return Arrays.asList();
    return Arrays.asList(lastScene);
  }

  public Iterator<Scene> getSceneIterator() {
    return getScenes().iterator();
  }

  public void clear() {
    lastScene = null;
  }

}
