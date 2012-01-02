package polygonsSWP.data;

import java.util.Iterator;
import java.util.List;

public interface History {

  public Scene newScene();
  
  public void addScene(Scene newScene);
  
  public List<Scene> getScenes();
  
  public Iterator<Scene> getSceneIterator();
  
}
