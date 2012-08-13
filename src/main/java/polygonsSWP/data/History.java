package polygonsSWP.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import polygonsSWP.data.listener.HistoryListener;


/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public class History
{
  private List<Scene> sceneList = Collections.synchronizedList(new ArrayList<Scene>());
  HistoryListener listener = null;
  int boundingBox;

  public History(int boundingBox){
    this.boundingBox = boundingBox;
  }

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
