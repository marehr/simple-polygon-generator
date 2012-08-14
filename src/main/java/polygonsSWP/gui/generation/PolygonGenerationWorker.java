package polygonsSWP.gui.generation;

import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.geometry.Polygon;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
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
