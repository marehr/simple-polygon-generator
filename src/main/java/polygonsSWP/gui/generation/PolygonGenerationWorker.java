package polygonsSWP.gui.generation;

import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.geometry.Polygon;


class PolygonGenerationWorker
  implements Runnable
{
  private final PolygonGenerator pg;
  private final PolygonGenerationWorkerListener cb;

  PolygonGenerationWorker(PolygonGenerator generator,
      PolygonGenerationWorkerListener callback) {
    pg = generator;
    cb = callback;
  }

  @Override
  public void run() {
    try{
      Polygon p = pg.generate();
      if (p == null) cb.onCancelled();
      else cb.onFinished(p);
    } catch(Exception e){
      e.printStackTrace();
      cb.onCancelled();
    }
  }

  public void stop() {
    pg.stop();
  }
}
