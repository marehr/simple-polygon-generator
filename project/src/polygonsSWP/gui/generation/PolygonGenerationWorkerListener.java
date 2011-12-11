package polygonsSWP.gui.generation;

import polygonsSWP.geometry.Polygon;

interface PolygonGenerationWorkerListener
{
  public void onFinished(Polygon polygon);
}
