package polygonsSWP.gui.generation;

import polygonsSWP.data.History;

public interface ShortestPathGenerationListener
{
  public void onSPStarted(History h);
  public void onSPFinished();
}
