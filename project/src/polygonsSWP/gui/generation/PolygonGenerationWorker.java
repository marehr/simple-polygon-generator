package polygonsSWP.gui.generation;

import java.util.Map;

import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGenerator.Parameters;
import polygonsSWP.geometry.Polygon;

class PolygonGenerationWorker
  implements Runnable
{
  private final PolygonGenerator pg;
  private final Map<Parameters, Object> params;
  private final PolygonGenerationWorkerListener cb;
  
  PolygonGenerationWorker(PolygonGenerator generator, 
       Map<Parameters, Object> parameters, 
       PolygonGenerationWorkerListener callback) {
    pg = generator;
    params = parameters;
    cb = callback;
  }

  @Override
  public void run() {
    Polygon p = pg.generate(params, null);
    cb.onFinished(p);
  }

  public void stop() {
    // TODO implement
    //    pg.stop();
  }
}
