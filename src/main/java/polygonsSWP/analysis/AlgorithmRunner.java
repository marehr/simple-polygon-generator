package polygonsSWP.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
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
 * @@author Kadir
 */
public class AlgorithmRunner
{
  public static boolean run(int runs, int threads, String databasepath, String output, PolygonGeneratorFactory factory, Map<Parameters, Object> params) {
    // Create database if necessary:
    DatabaseWriter dbw = null;
    if(databasepath != null) {
      dbw = new DatabaseWriter(databasepath);
    }
    
    // Check if file already exists:
    if(output != null && new File(output).exists()) {
      System.err.println("Specified output file already exists.");
      return false;
    }
    
    ExecutorService es = Executors.newFixedThreadPool(threads);
    try {
      
      for (int i = 0; i < runs; i++) {
        
        // If the polygons should be saved, create filename.
        String fn = null;
        if(output != null) {
          // TODO fixed digit
          fn = output + "-" + i;
        }
        
        es.execute(new PolygonGeneratorWorker(dbw, fn, factory, params));
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
    DatabaseWriter dbw;
    String output;
    

    public PolygonGeneratorWorker(DatabaseWriter dbw, String output, PolygonGeneratorFactory factory,
        Map<Parameters, Object> params) throws IllegalParameterizationException {
      this.dbw = dbw;
      this.output = output;
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
      if(dbw != null)
        dbw.writeToDatabase(statistics);
      
      if(output != null) {
        BufferedWriter bf;
        try {
          bf = new BufferedWriter(new OutputStreamWriter(
              new FileOutputStream(output)));
          bf.write(polygon.toString());
          bf.close();
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }

  }

}
