package polygonsSWP.gui.generation;

import polygonsSWP.data.History;

public interface ShortestPathGenerationListener
{
  public void onSPfinished(History h);
  public void onSPCancelled();
}
