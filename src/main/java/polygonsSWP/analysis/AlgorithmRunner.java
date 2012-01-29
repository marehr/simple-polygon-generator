package polygonsSWP.analysis;

import java.util.HashMap;
import java.util.Scanner;

import polygonsSWP.analysis.Option.StaticParameter;
import polygonsSWP.analysis.Option.DynamicParameter;
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

/**
 * 
 * @author Kadir
 *
 */
public class AlgorithmRunner
{
  static PolygonGeneratorFactory[] facs = {
    new SweepLineTestFactory(),
    new SpacePartitioningFactory(),
    new PermuteAndRejectFactory(),
    new TwoOptMovesFactory(),
    new RandomPolygonAlgorithmFactory(),
    new IncrementalConstructionAndBacktrackingFactory(),
    new ConvexHullGeneratorFactory(),
    new VelocityVirmaniFactory(),
    new SteadyGrowthFactory()
  };
  
  
  static //Something like a Constructor for static classes
  {
    
  }
  
  private int cores = 2;
  
  private static int chosenAlgorithm;
  private static Scanner scanner = new Scanner(System.in);
  private static String input;
  
  private static void intro()
  {
    System.out.println("------------------------------");
    
    for(int i = 0; i < facs.length; i++)
      System.out.println(i+". " + facs[i].toString());
   
    System.out.println("------------------------------");
    
  }
  
  private static double readDouble()
  {
    double res = 0;
    boolean run = true;
    do
    {
      input = scanner.nextLine();
      try{
        res = Double.valueOf(input);
      }catch(NumberFormatException ex)
      {
       System.out.println("Not a Double!"); 
      }
      run = false;
    }while(run);
    return res;
  }  
  
  private static int readInt()
  {
    int res = 0;
    boolean run = true;
    do
    {
      input = scanner.nextLine();
      try{
        res = Integer.valueOf(input);
      }catch(NumberFormatException ex)
      {
        System.out.println("Not an Integer!");
      }
      run = false;
    }while(run);
    return res;
  }
  
  private static int displayOption(String message, int max, String error)
  {
    int res = 0;
    boolean run = true;
    System.out.println(message);
    do
    {
      res = readInt();
      if(res < 0 || res > max)
        System.out.println(error);
      else
        run = false;
    }while(run);
    
    
    return res;
  }

  
  static void displayAlgorithmOptions()
  {
    System.out.println("--------------------------------------");
    System.out.println("Options: ");
    System.out.println("Number of Points (n)");
    System.out.println("Size of Boundaries (s)");
    if(facs[chosenAlgorithm].getAdditionalParameters().contains(Parameters.runs))
      System.out.println("Number of iterations (i)");
    if(facs[chosenAlgorithm].getAdditionalParameters().contains(Parameters.radius))
      System.out.println("Radius (r)");
    if(facs[chosenAlgorithm].getAdditionalParameters().contains(Parameters.velocity))
      System.out.println("Velocity (v)");
    System.out.println("Option Number of Points (n)");
    System.out.println("--------------------------------------");
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    /*
    intro();
    chosenAlgorithm = displayOption("Wähle Algorithmus", facs.length-1, "Zahl nicht gültig. Tippe Zahl für Algorithmus ein.");
    displayAlgorithmOptions();
    */
    
    //Zum testen
    OptionCombination comb = new OptionCombination();
    comb.add(new StaticParameter(Parameters.size, 500));
    comb.add(new StaticParameter(Parameters.radius, 100));
    comb.add(new DynamicParameter(Parameters.n,10,16,2));
    comb.add(new DynamicParameter(Parameters.runs,50,62,3));
    comb.add(new DynamicParameter(Parameters.velocity,50,68,6));
    
    int i = 0;
    HashMap<Parameters, Number> d;
    while( (d = comb.next()) != null)
    {
      i++;
      System.out.println(d.toString());
    }
      
    System.out.println("i== "+ i);
    
  }


}
