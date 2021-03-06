package polygonsSWP.analysis;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.generators.IllegalParameterizationException;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.generators.PolygonGeneratorFactory.Parameters;
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
public class AlgorithmRunner
{
  public static boolean run(int runs, int threads, PolygonLog out, PolygonGeneratorFactory factory, Map<Parameters, Object> params) {
    
    ExecutorService es = Executors.newFixedThreadPool(threads);
    try {
      
      for (int i = 0; i < runs; i++) {        
        es.execute(new PolygonGeneratorWorker(out, factory, params));
      }
        
    
    } catch (IllegalParameterizationException ipe) {
      System.err.println("Illegal parameterization: " + ipe.getMessage());
      ipe.printStackTrace();
      return false;
    }
    
    es.shutdown();

    while (!es.isTerminated()) {
      try {
        es.awaitTermination(3600, TimeUnit.SECONDS);
      }
      catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    return true;
  }


  public static class PolygonGeneratorWorker
    implements Runnable
  {
    PolygonGenerator polygonGenerator;
    PolygonStatistics statistics;
    PolygonLog out;
    

    public PolygonGeneratorWorker(PolygonLog out, PolygonGeneratorFactory factory,
        Map<Parameters, Object> params) throws IllegalParameterizationException {
      this.out = out;
      statistics = new PolygonStatistics();
      statistics.used_algorithm = factory.toString();
      statistics.number_of_points = (Integer) params.get(Parameters.n);
      polygonGenerator = factory.createInstance(params, statistics, null);
    }

    @Override
    public void run() {
      long start = System.nanoTime();
      Polygon polygon = polygonGenerator.generate();
      long end = System.nanoTime();
      if (polygon == null)
        throw new RuntimeException(
            "PolygonGeneratorWorker/run: polygon is null!");
      
      statistics.time_for_creating_polygon = end - start;
      statistics.timestamp = start;
      statistics.circumference = polygon.getCircumference();
      statistics.surface_area = polygon.getSurfaceArea();
      
      //write here Statistics and polygon
      out.writeOut(polygon, statistics);
    }

  }

}
