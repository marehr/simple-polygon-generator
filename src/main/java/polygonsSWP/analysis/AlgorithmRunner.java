package polygonsSWP.analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

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
 * TODO: Change Architecture, so the casts are successfull
 * @author Kadir
 */
public class AlgorithmRunner
{
  static PolygonGeneratorFactory[] facs = { new SweepLineTestFactory(),
      new SpacePartitioningFactory(), new PermuteAndRejectFactory(),
      new TwoOptMovesFactory(), new RandomPolygonAlgorithmFactory(),
      new IncrementalConstructionAndBacktrackingFactory(),
      new ConvexHullGeneratorFactory(), new VelocityVirmaniFactory(),
      new SteadyGrowthFactory() };

  private static int cores = 2;

  private static int chosenAlgorithm;
  private static InputStreamReader converter = new InputStreamReader(System.in);
  private static BufferedReader console = new BufferedReader(converter);
  private static String input;
  
  private static Thread[] threads = new Thread[cores];//Make cores dynamic
  private static PolygonStatistics[] stats = new PolygonStatistics[cores];
  private static OptionCombinator optionCombinator;
  
  private static void intro() {
    System.out.println("------------------------------");

    for (int i = 0; i < facs.length; i++)
      System.out.println(i + ". " + facs[i].toString());

    System.out.println("------------------------------");

  }

  private static int readInt() {
    int res = 0;
    boolean run = true;
    do {
      try {
        input = console.readLine();
        res = Integer.valueOf(input);
      }
      catch (NumberFormatException ex) {
        System.out.println("Not an Integer!");
      }
      catch (IOException e) {
        e.printStackTrace();
        return -1;
      }
      run = false;
    }
    while (run);
    return res;
  }

  private static int readAlgorithm(String message, int max, String error) {
    int res = 0;
    boolean run = true;
    System.out.println(message);
    do {
      res = readInt();
      if (res < 0 || res > max) System.out.println(error);
      else run = false;
    }
    while (run);

    return res;
  }

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

  static void displayAlgorithmOptions() {
    System.out.println("------------------------------------");
    System.out.println("To input a variable n from min=1 to max=10 in steps=3 (n={1,4,7,10}) type: \"n;1;10;3\"");
    System.out.println("To input a static n (n={2}) type: \"n;2\"");
    System.out.println("Options: ");
    System.out.println("Number of Points (n)");
    System.out.println("Size of Boundaries (s)");
    if (facs[chosenAlgorithm].getAdditionalParameters().contains(
        Parameters.runs)) System.out.println("Number of iterations (i)");
    if (facs[chosenAlgorithm].getAdditionalParameters().contains(
        Parameters.radius)) System.out.println("Radius (r)");
    if (facs[chosenAlgorithm].getAdditionalParameters().contains(
        Parameters.velocity)) System.out.println("Velocity (v)");
    System.out.println("--------------------------------------");
  }
  
  static String readLine()
  {
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    String line = "";
    try {
      line = bufferedReader.readLine();
    }
    catch (IOException e) {
      e.printStackTrace();
      return null;
    }
    return line;
  }
  
  private static OptionCombinator readLineAndOptions() {
    String input = readLine();
    System.out.println("input read: "+ input);//TODO: Remove this line (Debug)
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
            double value = Double.valueOf(param[1]);
            oc.add(new StaticParameter(p,value));
          }
          else
          {
            double min = Double.valueOf(param[1]);
            double max = Double.valueOf(param[2]);
            double steps = Double.valueOf(param[3]);
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
    Callback callback = new Callback();
    PolygonGenerator generator = null;
    for(int i = 0; i < threads.length; i++)
    {
      stats[i] = new PolygonStatistics();
      try {
        generator = facs[chosenAlgorithm].createInstance(optionCombinator.next(), stats[i], null);
      } catch (IllegalParameterizationException e) {e.printStackTrace();}
      threads[i] = new Thread(new PolygonGeneratorWorker(generator, callback, i));
      threads[i].start();
    }
  }
  
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    if (args.length != 1)
      System.out.println("Ungültige anzahl von Parametern");

    intro();
    chosenAlgorithm =
        readAlgorithm("Wähle Algorithmus", facs.length - 1,
            "Zahl nicht gültig. Tippe Zahl für Algorithmus ein.");
    displayAlgorithmOptions();
    optionCombinator =  readLineAndOptions();
    if(optionCombinator != null)
      execute();
  }
  
  public static class PolygonGeneratorWorker implements Runnable
  {
    int coreNumber;
    PolygonGenerator polygonGenerator;
    Callback callback;
    public PolygonGeneratorWorker(PolygonGenerator polygonGenerator, Callback callback, int coreNumber)
    {
      this.polygonGenerator = polygonGenerator;
      this.callback = callback;
      this.coreNumber = coreNumber;
    }
    
    @Override
    public void run() {
      Polygon polygon = polygonGenerator.generate();
      if(polygon != null)
        callback.onFinished(polygon);
      
      throw new RuntimeException("PolygonGeneratorWorker/run: polygon is null!");//TODO maybe it can be null ?!? Warn me
    }
    
  }
  
  
  public static class Callback
  {
    void onFinished(Polygon polygon)//TODO make synchronized
    {
      System.out.print("Callback: ");
      System.out.println(polygon.toString());
      //TODO Fill the Thread with new Parameters
      //TODO Save into Database or call function to do the job
    }
  }
  

}
