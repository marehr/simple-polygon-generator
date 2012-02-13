package polygonsSWP.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import polygonsSWP.analysis.Option.DynamicParameter;
import polygonsSWP.analysis.Option.StaticParameter;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.generators.IllegalParameterizationException;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.generators.PolygonGeneratorFactory.Parameters;
import polygonsSWP.generators.heuristics.IncrementalConstructionAndBacktrackingFactory;
import polygonsSWP.generators.heuristics.SpacePartitioningFactory;
import polygonsSWP.generators.heuristics.SteadyGrowthFactory;
import polygonsSWP.generators.heuristics.TwoOptMovesFactory;
import polygonsSWP.generators.heuristics.VelocityVirmaniFactory;
import polygonsSWP.generators.other.ConvexHullGeneratorFactory;
import polygonsSWP.generators.other.PermuteAndRejectFactory;
import polygonsSWP.generators.other.SweepLineTestFactory;
import polygonsSWP.generators.rpa.RandomPolygonAlgorithmFactory;
import polygonsSWP.geometry.Polygon;


/**
 * TODO: Test latest functions
 * @author Kadir
 */
public class AlgorithmRunner
{
  private static int cores = 2;

  static PolygonGeneratorFactory[] facs = { new SweepLineTestFactory(),
      new SpacePartitioningFactory(), new PermuteAndRejectFactory(),
      new TwoOptMovesFactory(), new RandomPolygonAlgorithmFactory(),
      new IncrementalConstructionAndBacktrackingFactory(),
      new ConvexHullGeneratorFactory(), new VelocityVirmaniFactory(),
      new SteadyGrowthFactory() };

  private static HashMap<Character, Parameters> optionMap =
  new HashMap<Character, PolygonGeneratorFactory.Parameters>() {
    private static final long serialVersionUID = 1L;
  
    {
      put('n', Parameters.n);
      put('r', Parameters.radius);
      put('s', Parameters.size);
      put('i', Parameters.runs);
      put('v', Parameters.velocity);
    }
  
  };

  private static OptionCombinator optionCombinator;

  private static int chosenAlgorithm;
  private static int multiplicator;
  private static DatabaseWriter database;
  
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    
    if(args.length >= 5)
    {
      String input = "";
      String databaseFile = "database.db";
      try
      {
        databaseFile = args[0];
        cores = Integer.valueOf(args[1]);
        chosenAlgorithm = Integer.valueOf(args[2]);
        multiplicator = Integer.valueOf(args[3]);
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
      
      for(int i=3; i < args.length; i++)
        input += args[i]+" ";
      
      optionCombinator = readLineAndOptions(input);
      if(optionCombinator != null)
      {
        database = new DatabaseWriter(databaseFile);
        execute();
        database.close();
      }
      else
        introAndExit();
    }
    else
      introAndExit();
  }

  
  private static void introAndExit() {
    System.out.println("----------- Algorithm -----------");
    for (int i = 0; i < facs.length; i++)
      System.out.println(i + ". " + facs[i].toString());
    System.out.println();
    System.out.println("----------- Parameters -----------");
    System.out.println("(n) Number of Points      [ALL]");
    System.out.println("(s) Size of Boundaries    [ALL]");
    System.out.println("(i) Number of iterations  [Virmani]");
    System.out.println("(r) Radius                [Virmani]");
    System.out.println("(v) Velocity              [Virmani]");
    System.out.println("-------------- Use --------------");
    System.out.println("AlgorithmRunner databaseFile numberOfCores algorithm multiplicator parameter1 parameter2 [...]");
    System.out.println("Example 1: \n" +
    		               "SpacePartitioning with number of points (n) ranging from 10 to 26 step 2 and fixed Bounding Box of 400 and running in 4 Threads and for" +
    		               "every Configuration 5 runs:");
    System.out.println("AlgorithmRunner 4 2 n;10;26;2 s;400 m;12");
    System.exit(0);
  }

  private static OptionCombinator readLineAndOptions(String input) {

    String[] params = input.split(" ");
    OptionCombinator oc = new OptionCombinator();
    for(int i = 0; i < params.length; i++)
    {
      String[] param = params[i].split(";");
      if(param.length == 4 || param.length == 2)
      {
        if(optionMap.containsKey(param[0].charAt(0)))//
        {
          Parameters p = optionMap.get(param[0].charAt(0));
          if(param.length == 2)//StaticParameter
          {
            int value = 0;
            try{value = Integer.valueOf(param[1]);}
            catch(NumberFormatException ex){System.out.println("Illegal parameters");return null;};
            oc.add(new StaticParameter(p,value));
          }
          else
          {
            int min = Integer.valueOf(param[1]);
            int max = Integer.valueOf(param[2]);
            int steps = Integer.valueOf(param[3]);
            oc.add(new DynamicParameter(p,min,max,steps));
          }
        } 
      }
      else
        return null;//paramlength not matched
    }
        
    return oc;
  }

  
  static void execute()
  {
    ExecutorService es = Executors.newFixedThreadPool(cores * 2);
    
    Map<Parameters, Object> params;
    while((params = optionCombinator.next()) != null)
      for(int i = 0; i < multiplicator; i++)
        es.execute(new PolygonGeneratorWorker(facs[chosenAlgorithm], params));
      
    
    es.shutdown();
    
    while(!es.isTerminated()) {
      try {
        es.awaitTermination(3600, TimeUnit.SECONDS);
      }
      catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    
    System.out.println("Execution finished!");
  }
  
  
  public static class PolygonGeneratorWorker implements Runnable
  {
    PolygonGenerator polygonGenerator;
    PolygonStatistics statistics;
    public PolygonGeneratorWorker(PolygonGeneratorFactory factory, Map<Parameters, Object> params)
    {
      statistics = new PolygonStatistics();
      statistics.used_algorithm = factory.toString();
      statistics.number_of_points = (Integer) params.get(Parameters.n);
      PolygonGenerator pg = null;
      try {
        pg = factory.createInstance(params, statistics, null);
      }
      catch (IllegalParameterizationException e) {e.printStackTrace();return;}
      
      this.polygonGenerator = pg;
    }
    
    @Override
    public void run() {
      long start = System.nanoTime();
      Polygon polygon = polygonGenerator.generate();
      long end = System.nanoTime();
      if(polygon == null)
        throw new RuntimeException("PolygonGeneratorWorker/run: polygon is null!");//maybe it can be null ?!? Warn me
      statistics.time_for_creating_polygon = end - start;
      statistics.timestamp = start;
      statistics.circumference = polygon.getCircumference();
      statistics.surface_area = polygon.getSurfaceArea();
      database.writeToDatabase(statistics);
    }
    
  }
  
  

  

}
